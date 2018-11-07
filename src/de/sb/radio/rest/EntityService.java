package de.sb.radio.rest;

import static de.sb.radio.rest.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import java.net.URI;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.constraints.Positive;
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
import javax.ws.rs.core.UriBuilder;

import de.sb.radio.persistence.Album;
import de.sb.radio.persistence.BaseEntity;
import de.sb.radio.persistence.Document;
import de.sb.radio.persistence.Person;
import de.sb.radio.persistence.Person.Group;
import de.sb.radio.persistence.Track;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestJpaLifecycleProvider;


/**
 * JAX-RS based REST service implementation for polymorphic entity resources, defining the following path and method combinations:
 * <ul>
 * <li>GET entities/{id}: Returns the entity matching the given identity.</li>
 * <li>DELETE entities/{id}: Deletes the entity matching the given identity.</li>
 * </ul>
 */
//TODO: remove comment!
@Path("")
@Copyright(year = 2018, holders = "Sascha Baumeister")
public class EntityService {
	
	static private final String CRITERIA_QUERY_JPQL_PERSON = "select p from Person as p where"
			+ "(:lastname is null or p.lastname = :lastname) and "
			+ "(:forename is null or p.forename = :forename) and "
			+ "(:email is null or p.email = :email)";
	
	static private final String CRITERIA_QUERY_JPQL_ALBUM = "select a from Album as a where"
			+ "(:title is null or a.title = :title) and "
			+ "(:releaseYear is null or a.releaseYear >= :releaseYear) and "
			+ "(:releaseYear is null or a.releaseYear <= :releaseYear) and "
			+ "(:trackCount is null or a.trackCount >= :trackCount) and "
			+ "(:trackCount is null or a.trackCount <= :trackCount)";
	
	static private final String CRITERIA_QUERY_JPQL_TRACK = "select t from Track as t where"
			+ "(:name is null or t.name = :name) and "
			+ "(:artist is null or t.artist = :artist) and "
			+ "(:genre is null or t.genre = :genre) and "
			+ "(:ordinal is null or t.ordinal >= :ordinal) and "
			+ "(:ordinal is null or t.ordinal <= :ordinal)";
	
	/**
	 * Returns the entity with the given identity.
	 * @param entityIdentity the entity identity
	 * @return the matching entity (HTTP 200)
	 * @throws ClientErrorException (HTTP 404) if the given entity cannot be found
	 * @throws PersistenceException (HTTP 500) if there is a problem with the persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated with the current thread is not open
	 */
	@GET
	@Path("entities/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public BaseEntity queryEntity (
		@PathParam("id") @Positive final long entityIdentity
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final BaseEntity entity = radioManager.find(BaseEntity.class, entityIdentity);
		if (entity == null) throw new ClientErrorException(Status.NOT_FOUND);

		return entity;
	}


	/**
	 * Deletes the entity matching the given identity, or does nothing if no such entity exists.
	 * @param requesterIdentity the authenticated requester identity
	 * @param entityIdentity the entity identity
	 * @return void (HTTP 204)
	 * @throws ClientErrorException (HTTP 403) if the given requester is not an administrator
	 * @throws ClientErrorException (HTTP 404) if the given entity cannot be found
	 * @throws ClientErrorException (HTTP 409) if there is a database constraint violation (like conflicting locks)
	 * @throws PersistenceException (HTTP 500) if there is a problem with the persistence layer
	 * @throws IllegalStateException (HTTP 500) if the entity manager associated with the current thread is not open
	 */
	@DELETE
	@Path("entities/{id}")
	public void deleteEntity (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long entityIdentity
	) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person requester = radioManager.find(Person.class, requesterIdentity);
		if (requester == null || requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);

		final BaseEntity entity = radioManager.find(BaseEntity.class, entityIdentity);
		if (entity == null) throw new ClientErrorException(Status.NOT_FOUND);
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
	
	@GET
	@Path("/people")
	@Produces(MediaType.APPLICATION_JSON)
	public Person[] getPeople(
			@QueryParam("resultOffset") int resultOffset,
			@QueryParam("resultLimit") int resultLimit,
			@QueryParam("email") final String email,
			@QueryParam("forename") final String forename,
			@QueryParam("lastname") final String lastname
			) {
		// TODO: sorted by family name, given name, email
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		
		final TypedQuery<Person> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_PERSON, Person.class);
		query.setParameter("lastname", lastname);
		query.setParameter("forename", forename);
		query.setParameter("email", email);
		query.setMaxResults(resultLimit);
		query.setFirstResult(resultOffset);
		
		// funktioniert es so? ist es nicht ein array mit der Groesse 0?
//		final Person[] people = query.getResultList().toArray(new Person[0]);
		// das waere mein Vorschlag
		final Person[] people = (Person[]) query.getResultList().toArray();
		
		return people;
	}
	
	@POST
	@Path("/people")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public long updatePerson (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@HeaderParam("Set-Password") final String password,
			final Person template) {
		
			final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
			final Person requester = radioManager.find(Person.class, requesterIdentity);
			if (requester.getGroup() != Person.Group.ADMIN 
					&& requester.getIdentity() != template.getIdentity()){
				// requester is not admin and not self
				throw new ClientErrorException(Status.FORBIDDEN);
			}
			Person person;
			if (template.getIdentity() == 0) {
				// create new person
				person = new Person(template.getAvatar());

			} else {
				person = radioManager.find(Person.class, template.getIdentity());
			}
			person.setEmail(template.getEmail());
			person.setForename(template.getForename());
			// make sure non-administrators don’t set their Group to ADMIN
			if(person.getGroup() != Person.Group.ADMIN && template.getGroup() == Person.Group.ADMIN) {
				throw new ClientErrorException(Status.FORBIDDEN);
			} else {
				person.setGroup(template.getGroup());					
			}
			person.setLastname(template.getLastname());
			person.setPasswordHash(password);
			
			return person.getIdentity();
	}
	
	@GET
	@Path("/people/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPerson (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long personIdentity) { 
		// Check if person id is zero
		long queryPerson = personIdentity;
		if (personIdentity == 0) {
			queryPerson = requesterIdentity;
		}
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person person = radioManager.find(Person.class, queryPerson);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		return person;
	}
	
	@GET
	@Path("/albums")
	@Produces(MediaType.APPLICATION_JSON)
	public Album[] getAlbums(
			@QueryParam("resultOffset") int resultOffset,
			@QueryParam("resultLimit") int resultLimit,
			@QueryParam("title") String title,
			@QueryParam("releaseYear") short releaseYear,
			@QueryParam("trackCount") byte trackCount
			) {
		
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		
		final TypedQuery<Album> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_ALBUM, Album.class);
		query.setParameter("title", title);
		query.setParameter("releaseYear", releaseYear);
		query.setParameter("trackCount", trackCount);
		query.setFirstResult(resultOffset);
		query.setMaxResults(resultLimit);
		
		// see getPeople
//		final Person[] people = query.getResultList().toArray(new Person[0]);
		final Album[] albums = (Album[]) query.getResultList().toArray();
		
		return albums;
	}

	@POST
	@Path("/albums")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public long updateAlbum(
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@QueryParam("coverReference") final long coverReference,
		final Album template) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Person requester = radioManager.find(Person.class, requesterIdentity);
		if (requester.getGroup() != Person.Group.ADMIN){
			throw new ClientErrorException(Status.FORBIDDEN);
		}
		Album album;
		if(template.getIdentity() == 0) {
			album = new Album(template.getCover());	
		} else {
			album = radioManager.find(Album.class, template.getIdentity());
		}
		album.setTitle(template.getTitle());
		album.setReleaseYear(template.getReleaseYear());		
		return album.getIdentity();
	}
	
	@GET
	@Path("/tracks")
	@Produces(MediaType.APPLICATION_JSON)
	public Track[] getTracks(
			@QueryParam("name") String name,
			@QueryParam("artist") String artist,
			@QueryParam("genre") String genre,
			@QueryParam("ordinal") byte ordinal
			) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		
		final TypedQuery<Track> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_TRACK, Track.class);
		query.setParameter("name", name);
		query.setParameter("artist", artist);
		query.setParameter("genre", genre);
		query.setParameter("ordinal", ordinal);
		// see getPeople
//		final Track[] tracks = query.getResultList().toArray(new Track[0]);
		final Track[] tracks = (Track[]) query.getResultList().toArray();
		
		return tracks;
	}

//	@POST
//	@Path("/tracks")
//	@Produces(MediaType.TEXT_PLAIN)
//	@Consumes(MediaType.APPLICATION_JSON)	

//	@GET
//	@Path("tracks/genres")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Track[] getGenres(
//			@QueryParam("genre") String genre) {
//		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
//		final TypedQuery<Track> query = radioManager.createQuery(CRITERIA_QUERY_JPQL_TRACK_GENRE, Track.class);
//		query.setParameter("genre", genre);
//		final Track[] genres = query.getResultList().toArray(new Track[0]);
		
//		return genres;
//	}
	
	@GET
	@Path("/documents/{id}")
	public Response getDocument (
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long documentIdentity) {
		final EntityManager radioManager = RestJpaLifecycleProvider.entityManager("radio");
		final Document document = radioManager.find(Document.class, documentIdentity);
		if (document == null) throw new ClientErrorException(Status.NOT_FOUND);
		
		return Response.ok(document).build(); // TODO check this !?
	}

//	@POST
//	@Path("documents")
//	@Consumes(MediaType.TEXT_PLAIN)
}