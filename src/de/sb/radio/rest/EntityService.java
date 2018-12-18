package de.sb.radio.rest;

import static de.sb.radio.rest.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.sb.radio.persistence.Album;
import de.sb.radio.persistence.BaseEntity;
import de.sb.radio.persistence.Document;
import de.sb.radio.persistence.HashTools;
import de.sb.radio.persistence.Person;
import de.sb.radio.persistence.Person.Group;
import de.sb.radio.persistence.Track;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

/**
 * JAX-RS based REST service implementation for polymorphic entity resources,
 * defining the following path and method combinations:
 * <ul>
 * <li>GET entities/{id}: Returns the entity matching the given identity.</li>
 * <li>DELETE entities/{id}: Deletes the entity matching the given
 * identity.</li>
 * </ul>
 */
@Path("")
@Copyright(year = 2018, holders = "Sascha Baumeister")
public class EntityService {
	static private final Set<String> EMPTY_WORD_SINGLETON = Collections.singleton("");

	static private final String CRITERIA_QUERY_JPQL_PERSON = "select p.identity from Person as p where "
			+ "(:lowerCreationTimestamp is null or p.creationTimestamp >= :lowerCreationTimestamp) and "
			+ "(:upperCreationTimestamp is null or p.creationTimestamp <= :upperCreationTimestamp) and "
			+ "(:surname is null or p.surname = :surname) and "
			+ "(:forename is null or p.forename = :forename) and"
			+ "(:lastTransmissionTimestamp is null or p.lastTransmissionTimestamp = :lastTransmissionTimestamp) and"
			+ "(:lastTransmissionAddress is null or p.lastTransmissionAddress = :lastTransmissionAddress) and "
			+ "(:email is null or p.email = :email)";

	static private final String CRITERIA_QUERY_JPQL_ALBUM = "select a.identity from Album as a where "
			+ "(:lowerCreationTimestamp is null or a.creationTimestamp >= :lowerCreationTimestamp) and "
			+ "(:upperCreationTimestamp is null or a.creationTimestamp <= :upperCreationTimestamp) and "
			+ "(:title is null or a.title = :title) and "
			+ "(:releaseYear is null or a.releaseYear >= :releaseYear) and "
			+ "(:releaseYear is null or a.releaseYear <= :releaseYear) and "
			+ "(:trackCount is null or a.trackCount >= :trackCount) and "
			+ "(:trackCount is null or a.trackCount <= :trackCount)";
	
	static private final String CRITERIA_QUERY_JPQL_TRACK = "select t.identity from Track as t where "
			+ "(:lowerCreationTimestamp is null or t.creationTimestamp >= :lowerCreationTimestamp) and "
			+ "(:upperCreationTimestamp is null or t.creationTimestamp <= :upperCreationTimestamp) and "
			+ "(:name is null or t.name = :name) and " 
			+ "(:ignoreArtists = true or t.artist in :artists) and "
			+ "(:ignoreGenres = true or t.genre in :genres) and "
			+ "(:lowerOrdinal is null or t.ordinal >= :lowerOrdinal) and "
			+ "(:upperOrdinal is null or t.ordinal <= :upperOrdinal)";

	static private final String CRITERIA_QUERY_JPQL_GENRES = "select distinct t.genre from Track as t";
	static private final String CRITERIA_QUERY_JPQL_ARTISTS = "select distinct t.artist from Track as t";

	static private final String CRITERIA_QUERY_JPQL_DOCUMENT = "select d.identity from Document as d where d.contentHash = :contentHash";

	/**
	 * Returns the entity with the given identity.
	 * 
	 * @param entityIdentity the entity identity
	 * @return the matching entity (HTTP 200)
	 * @throws ClientErrorException  (HTTP 404) if the given entity cannot be found
	 * @throws PersistenceException  (HTTP 500) if there is a problem with the
	 *                               persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated
	 *                               with the current thread is not open
	 */
	@GET
	@Path("entities/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public BaseEntity queryEntity(@PathParam("id") @Positive final long entityIdentity) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final BaseEntity entity = radioManager.find(BaseEntity.class, entityIdentity);
		if (entity == null)
			throw new ClientErrorException(Status.NOT_FOUND);

		return entity;
	}

	/**
	 * Deletes the entity matching the given identity, or does nothing if no such
	 * entity exists.
	 * 
	 * @param requesterIdentity the authenticated requester identity
	 * @param entityIdentity    the entity identity
	 * @return void (HTTP 204)
	 * @throws ClientErrorException  (HTTP 403) if the given requester is not an
	 *                               administrator
	 * @throws ClientErrorException  (HTTP 404) if the given entity cannot be found
	 * @throws ClientErrorException  (HTTP 409) if there is a database constraint
	 *                               violation (like conflicting locks)
	 * @throws PersistenceException  (HTTP 500) if there is a problem with the
	 *                               persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated
	 *                               with the current thread is not open
	 */
	@DELETE
	@Path("entities/{id}")
	public void deleteEntity(@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long entityIdentity) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person requester = radioManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Group.ADMIN)
			throw new ClientErrorException(FORBIDDEN);

		final BaseEntity entity = radioManager.find(BaseEntity.class, entityIdentity);
		if (entity == null)
			throw new ClientErrorException(Status.NOT_FOUND);
		radioManager.remove(entity);

		try {
			radioManager.getTransaction().commit();
		} catch (final RollbackException exception) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			radioManager.getTransaction().begin();
		}

		radioManager.getEntityManagerFactory().getCache().evict(BaseEntity.class, entityIdentity);
	}

	/**
	 * Returns a list of all persons
	 * 
	 * @param resultOffset
	 * @param resultLimit
	 * @param email email address of person
	 * @param forename forename of person
	 * @param surname surname 
	 * @return list of all persons (HTTP 200)
	 */
	@GET
	@Path("/people")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Person> queryPeople(
			@QueryParam("resultOffset") @PositiveOrZero int resultOffset,
			@QueryParam("resultLimit")  @PositiveOrZero int resultLimit,
			@QueryParam("lowerCreationTimestamp") @PositiveOrZero final long lowerCreationTimestamp,
			@QueryParam("upperCreationTimestamp") @PositiveOrZero final long upperCreationTimestamp,
			@QueryParam("email") @Email final String email,
			@QueryParam("forename") final String forename, 
			@QueryParam("surname") final String surname,
			@QueryParam("lastTransmissionTimestamp") final Byte lastTransmissionTimestamp, 
			@QueryParam("lastTransmissionAddress") final String lastTransmissionAddress
			
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");

		final TypedQuery<Long> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_PERSON, Long.class);
		if (resultLimit > 0) query.setMaxResults(resultLimit);
		if (resultOffset > 0) query.setFirstResult(resultOffset);
		query
			.setParameter("surname", surname)
			.setParameter("forename", forename)
			.setParameter("lastTransmissionTimestamp", lastTransmissionTimestamp)
			.setParameter("lastTransmissionAddress", lastTransmissionAddress)
			.setParameter("email", email)
			.setParameter("lowerCreationTimestamp", lowerCreationTimestamp)
			.setParameter("upperCreationTimestamp", upperCreationTimestamp);

		final List<Long> peopleReferences = query.getResultList();
		final List<Person> people = new ArrayList<>();
		for (final long reference : peopleReferences) {
			final Person person = radioManager.find(Person.class, reference);
			if (person != null) people.add(person);
		}
		people.sort(Comparator.comparing(Person::getSurname).thenComparing(Person::getForename).thenComparing(Person::getEmail));
		return people;
	}

	@POST
	@Path("/people")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public long modifyPerson(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@HeaderParam("Set-Password") final String password,
			@QueryParam("avatarReference") @Positive final Long avatarReference, 
			@NotNull @Valid final Person template
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person requester = radioManager.find(Person.class, requesterIdentity);
		if (requester == null || (requester.getGroup() != Person.Group.ADMIN && requester.getIdentity() != template.getIdentity())) 
			// requester is not admin and not self
			throw new ClientErrorException(Status.FORBIDDEN);

		final boolean insert = template.getIdentity() == 0;
		
		final Person person;
		
		if (insert) {
			final Document avatar = radioManager.find(Document.class, avatarReference==null ? 1L : avatarReference);
			if (avatar==null) throw new ClientErrorException(Status.NOT_FOUND);
			person = new Person(avatar);
		} else {
			person = radioManager.find(Person.class, template.getIdentity());
			if (person==null) throw new ClientErrorException(Status.NOT_FOUND);
			if (avatarReference != null) {
				final Document avatar = radioManager.find(Document.class, avatarReference);
				person.setAvatar(avatar);
			} 
		}
		
		if (person.getGroup() != Person.Group.ADMIN && template.getGroup() == Person.Group.ADMIN) {
			throw new ClientErrorException(Status.FORBIDDEN);
		} 
		
		// make sure non-administrators don't set their Group to ADMIN		
		person.setEmail(template.getEmail());
		person.setForename(template.getForename());
		person.setGroup(template.getGroup());
		person.setSurname(template.getSurname());
		if (password != null && !password.isEmpty()) {
			person.setPasswordHash(HashTools.sha256HashCode(password));
		}
		
		if(insert) {
			radioManager.persist(person);
		} else {
			radioManager.flush();
		}

		try {
			radioManager.getTransaction().commit();	
		} catch (PersistenceException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			radioManager.getTransaction().begin();
		}
	
		return person.getIdentity();
	}

	@GET
	@Path("/people/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Person queryPerson(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @PositiveOrZero final long personIdentity
	) {
		// Check if person id is zero
		long identity = personIdentity == 0 ? requesterIdentity : personIdentity;
		
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person person = radioManager.find(Person.class, identity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);
		return person;
	}

	@GET
	@Path("/albums")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Album> queryAlbums(
			@QueryParam("resultOffset") @PositiveOrZero int resultOffset, 
			@QueryParam("resultLimit") @PositiveOrZero int resultLimit,
			@QueryParam("lowerCreationTimestamp") @PositiveOrZero final long lowerCreationTimestamp,
			@QueryParam("upperCreationTimestamp") @PositiveOrZero final long upperCreationTimestamp,
			@QueryParam("title") String title, 
			@QueryParam("releaseYear") @PositiveOrZero Short releaseYear,
			@QueryParam("trackCount") @PositiveOrZero Byte trackCount
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final TypedQuery<Long> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_ALBUM, Long.class);
		if (resultOffset > 0) query.setFirstResult(resultOffset);
		if (resultLimit > 0) query.setMaxResults(resultLimit);
		query.setParameter("title", title);
		query.setParameter("releaseYear", releaseYear);
		query.setParameter("trackCount", trackCount);
		query.setParameter("lowerCreationTimestamp", lowerCreationTimestamp);
		query.setParameter("upperCreationTimestamp", upperCreationTimestamp);
		final List<Long> albumsReferences = query.getResultList();
		
		final List<Album> albums = new ArrayList<>();
		for (final long reference : albumsReferences) {
			final Album album = radioManager.find(Album.class, reference);
			if (album != null) albums.add(album);
		}

		albums.sort(Comparator.comparing(Album::getTitle).thenComparing(Album::getIdentity));
		return albums;
	}

	@POST
	@Path("/albums")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public long modifyAlbum(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@QueryParam("coverReference") @PositiveOrZero final Long coverReference, 
			@NotNull  final Album template
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person requester = radioManager.find(Person.class, requesterIdentity);
		if (requester == null || (requester.getGroup() != Person.Group.ADMIN)) {
			throw new ClientErrorException(Status.FORBIDDEN);
		}
		
		final boolean insert = template.getIdentity() == 0;
		final Document cover = radioManager.find(Document.class, coverReference==null ? 1L : coverReference);
		final Album album;
		if (insert) {
			if (cover == null) throw new ClientErrorException(Status.NOT_FOUND);
			album = new Album(cover);
		} else {
			album = radioManager.find(Album.class, template.getIdentity());
			if(album == null) throw new ClientErrorException(Status.NOT_FOUND);
			if(coverReference != null && cover == null) throw new ClientErrorException(Status.NOT_FOUND);
			album.setCover(cover);
		}
		
		album.setTitle(template.getTitle());
		album.setReleaseYear(template.getReleaseYear());
		album.setTrackCount(template.getTrackCount());
		
		if(insert) {
			radioManager.persist(album);
		} else {
			radioManager.flush();
		}
		
		try {
			radioManager.getTransaction().commit();
		} catch (PersistenceException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			radioManager.getTransaction().begin();
		}

		return album.getIdentity();
	}

	@GET
	@Path("/tracks")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Track> queryTracks(
			@QueryParam("resultOffset") @PositiveOrZero int resultOffset, 
			@QueryParam("resultLimit") @PositiveOrZero int resultLimit,
			@QueryParam("lowerCreationTimestamp") @PositiveOrZero final long lowerCreationTimestamp,
			@QueryParam("upperCreationTimestamp") @PositiveOrZero final long upperCreationTimestamp,
			@QueryParam("name") String name, 
			@QueryParam("artist") Set<String> artists, 
			@QueryParam("genre") Set<String> genres,
			@QueryParam("lowerOrdinal") @PositiveOrZero Byte lowerOrdinal,
			@QueryParam("upperOrdinal") @PositiveOrZero Byte upperOrdinal
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final TypedQuery<Long> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_TRACK, Long.class);
		if (resultLimit > 0) query.setMaxResults(resultLimit);
		if (resultOffset > 0) query.setFirstResult(resultOffset);
		query.setParameter("name", name);
		query.setParameter("lowerCreationTimestamp", lowerCreationTimestamp);
		query.setParameter("upperCreationTimestamp", upperCreationTimestamp);
		query.setParameter("lowerOrdinal", lowerOrdinal);
		query.setParameter("upperOrdinal", upperOrdinal);
		query.setParameter("ignoreArtists", artists.isEmpty());
		query.setParameter("artists", artists.isEmpty() ? EMPTY_WORD_SINGLETON : artists);
		query.setParameter("ignoreGenres", genres.isEmpty());
		query.setParameter("genres", genres.isEmpty() ? EMPTY_WORD_SINGLETON : genres);

		final List<Long> tracksReferences = query.getResultList();
		final List<Track> tracks = new ArrayList<>();
		for (final long reference : tracksReferences) {
			final Track track = radioManager.find(Track.class, reference);
			if (track != null) tracks.add(track);
		}
		
		tracks.sort(Comparator.comparing(Track::getName).thenComparing(Track::getArtist).thenComparing(Track::getIdentity));
		return tracks;

	}

	@POST
	@Path("/tracks")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public long modifyTrack(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@QueryParam("recordingReference") final Long recordingReference,
			@QueryParam("albumReference") final Long albumReference,
			final Track template
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person requester = radioManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final boolean insert = template.getIdentity() == 0;
		Track track;
		final Document recording = recordingReference==null ? null : radioManager.find(Document.class, recordingReference);
		final Album album = albumReference==null ? null : radioManager.find(Album.class, albumReference);


		if (insert) {
			if (requester == null || (requester.getGroup() != Person.Group.ADMIN)) throw new ClientErrorException(Status.FORBIDDEN);
			if (recording == null || album == null) throw new ClientErrorException(Status.NOT_FOUND);
			track = new Track(album, requester, recording);
		} else {
			track = radioManager.find(Track.class, template.getIdentity());
			if (track == null) throw new ClientErrorException(Status.NOT_FOUND);
			if (track.getOwner().getIdentity() != requester.getIdentity()) throw new ClientErrorException(Status.FORBIDDEN);
			if (albumReference != null && album == null) throw new ClientErrorException(Status.NOT_FOUND);
			if (recordingReference != null && recording == null) throw new ClientErrorException(Status.NOT_FOUND);
			track.setAlbum(album);
			track.setRecording(recording);
		}
		track.setName(template.getName());
		track.setArtist(template.getArtist());
		track.setGenre(template.getGenre());
		track.setOrdinal(template.getOrdinal());
		
		if(insert) {
			radioManager.persist(track);
		} else {
			radioManager.flush();
		}
		
		try {
			radioManager.getTransaction().commit();
		} catch (PersistenceException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			radioManager.getTransaction().begin();
		}
		
		if (album != null) radioManager.getEntityManagerFactory().getCache().evict(Album.class, album.getIdentity());
		if (insert) radioManager.getEntityManagerFactory().getCache().evict(Person.class, requester.getIdentity());
		return track.getIdentity();
	}

	@GET
	@Path("tracks/artists")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<String> queryArtists(
			@QueryParam("resultOffset") @PositiveOrZero int resultOffset, 
			@QueryParam("resultLimit") @PositiveOrZero int resultLimit
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final TypedQuery<String> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_ARTISTS, String.class);
		if (resultLimit > 0) query.setMaxResults(resultLimit);
		if (resultOffset > 0) query.setFirstResult(resultOffset);
		final List<String> artists = query.getResultList();
		artists.sort(Comparator.naturalOrder());

		return artists;
	}

	@GET
	@Path("tracks/genres")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<String> queryGenres(
			@QueryParam("resultOffset") @PositiveOrZero int resultOffset, 
			@QueryParam("resultLimit") @PositiveOrZero int resultLimit
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final TypedQuery<String> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_GENRES, String.class);
		if (resultLimit > 0) query.setMaxResults(resultLimit);
		if (resultOffset > 0) query.setFirstResult(resultOffset);
		final List<String> genres = query.getResultList();
		genres.sort(Comparator.naturalOrder());

		return genres;
	}
	
	@GET
	@Path("/documents/{id}")
	@Produces(MediaType.WILDCARD)
	public Response queryDocument(
			@PathParam("id") @Positive final long documentIdentity
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Document document = radioManager.find(Document.class, documentIdentity);
		if (document == null) throw new ClientErrorException(Status.NOT_FOUND);
		return Response.ok(document.getContent(), document.getContentType()).build(); 
	}

	@POST
	@Path("documents")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public long modifyDocument (
			@NotNull byte[] content,
			@HeaderParam ("Content-type") String contentType
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		byte[] contentHash = HashTools.sha256HashCode(content);
		final TypedQuery<Long> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_DOCUMENT, Long.class);
		query.setParameter("contentHash", contentHash);
		List<Long> queryResult = query.getResultList();
		
		final boolean insert = queryResult.isEmpty();
		final Document document;
		if (insert) {
			document = new Document();
			document.setContent(content);
		} else {
			long identity = queryResult.get(0);
			document = radioManager.find(Document.class, identity);
			if (document == null) throw new ClientErrorException(Status.NOT_FOUND);
		}
		document.setContentType(contentType);
		
		if(insert) {
			radioManager.persist(document);
		} else {
			radioManager.flush();
		}
		
		try {
			radioManager.getTransaction().commit();
		} catch (PersistenceException e) {
			throw new ClientErrorException(Status.CONFLICT);
		} finally {
			radioManager.getTransaction().begin();
		}
		return document.getIdentity();
	}
	// JUNIT: Filter methode aufruf und post document methode mit einem zufallsinhalt
	// JAX-RS client API
	// GET something filter query, POST Document
}