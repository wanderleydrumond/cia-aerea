package daos;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import entities.Flight;

/**
 * Class that makes the database communication layer role in relation with of the flights table.
 * 
 * @author Wanderley Drumond
 *
 */
@Stateless
public class FlightDAO extends GenericDAO<Flight> {

	/**
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;

	public FlightDAO() {
		super(Flight.class);
	}
	
	/**
	 * Finds the biggest/newest id saved in database.
	 * 
	 * @return the biggest/newest id found
	 */
	public Integer findNewestId() {
		try {
			final CriteriaQuery<Integer> CRITERIA_QUERY;
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CRITERIA_QUERY = criteriaBuilder.createQuery(Integer.class);
			Root<Flight> userTable = CRITERIA_QUERY.from(Flight.class);
			
			Expression<Integer> expression = userTable.get("id");
			CRITERIA_QUERY.select(criteriaBuilder.max(expression));
			
			Integer newestId = entityManager.createQuery(CRITERIA_QUERY).getSingleResult();
			
			return newestId == null ? 0 : newestId;
		} catch (Exception exception) {
			System.err.println("Catch Exception findNewestId() in FlightDAO");
			exception.printStackTrace();
			return null;
		}
	}
}