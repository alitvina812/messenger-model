package de.sb.radio.persistence;



import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
//import java.util.List;
//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.ParameterExpression;
//import javax.persistence.criteria.Path;
//import javax.persistence.criteria.Root;
//import javax.ws.rs.ClientErrorException;
//import javax.ws.rs.core.Response.Status;

import de.sb.toolbox.net.RestJpaLifecycleProvider;


public class SanityCheck {
	static public void main (final String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("radio");
		EntityManager em = emf.createEntityManager();
		em.find(BaseEntity.class, 1L);
	}
}

//public class SanityCheck {

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("radio");
//		EntityManager em = emf.createEntityManager();
//		
//		Person ines = em.find(Person.class, 2L);
//		System.out.println(ines);
//	}

//	static public void main (final String[] args) {
//			EntityManagerFactory emf = Persistence.createEntityManagerFactory("radio");
//			EntityManager em = emf.createEntityManager();
//			
////			CriteriaBuilder cb = em.getCriteriaBuilder();
////			CriteriaQuery<Person[]> criteria = cb.createQuery(Person[].class);
////			Root<Person> root = criteria.from(Person.class);
////			
////			String filterCriteria = "guest";
////			//define search attributes
////			javax.persistence.criteria.Path<String> email = root.get("email");		
////			javax.persistence.criteria.Path<String> forename = root.get("forename");
////			javax.persistence.criteria.Path<String> lastname = root.get("lastname");
////			//define search conditions
////			criteria.multiselect(root).where(cb.or(
////					cb.like(forename, filterCriteria),
////					cb.like(lastname, filterCriteria),
////					cb.like(email, filterCriteria)));
////			
////			// run the query
////			TypedQuery<Person[]> query = em.createQuery(criteria);
////			System.out.println("Result:");
////			System.out.println(query.getResultList());
//			
//			
//			// test getPerson()
////			long personId = 0, personIdentity = 2;
////			long queryPerson = personId;
////			if (personId == 0) {
////				queryPerson = personIdentity;
////			}
////			final EntityManager radioManager = em;// RestJpaLifecycleProvider.entityManager("radio");
////			final Person person = radioManager.find(Person.class, queryPerson);
////			if (person == null) throw new ClientErrorException(Status.NOT_FOUND);
////			System.out.println("Result of getPerson():");
////			System.out.println(person);
//			
//			//get album
//			String title = "test";
//			short releaseYear = 1990;
//			byte trackCount = 1;
//			
//			List<Album[]> albums; 
//			CriteriaBuilder cb = em.getCriteriaBuilder();
//			CriteriaQuery<Album[]> criteriaAlbum = cb.createQuery(Album[].class);
//			Root<Album> rootAlbum = criteriaAlbum.from(Album.class);
//			
//			//define search attributes
//			javax.persistence.criteria.Path<String> titleCriteria = rootAlbum.get("title");		
//			javax.persistence.criteria.Path<Integer> releaseYearCriteria = rootAlbum.get("releaseYear");
//			javax.persistence.criteria.Path<Integer> trackCountCriteria = rootAlbum.get("trackCount");
//			//define search conditions
//			criteriaAlbum.multiselect(rootAlbum).where(cb.or(
//					cb.like(titleCriteria, title),
//					cb.equal(releaseYearCriteria, releaseYear),
//					cb.equal(trackCountCriteria, trackCount)));
//			
//			// run the query
//			TypedQuery<Album[]> queryAlbum = em.createQuery(criteriaAlbum);
//			//result
//			albums = queryAlbum.getResultList();
//			System.out.println(albums);
//	}
//	
//	
//}
