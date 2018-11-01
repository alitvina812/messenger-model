package de.sb.radio.rest;

import static de.sb.radio.rest.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.validation.constraints.Positive;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import de.sb.radio.persistence.BaseEntity;
import de.sb.radio.persistence.Person;
import de.sb.radio.persistence.Person.Group;
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
// @Path("")
@Copyright(year = 2018, holders = "Sascha Baumeister")
public class EntityServiceSkeleton {

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
}