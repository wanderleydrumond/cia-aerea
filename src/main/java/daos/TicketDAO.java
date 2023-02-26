package daos;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import entities.Flight;
import entities.Ticket;
import entities.User;

@Stateless
public class TicketDAO extends GenericDAO<Ticket> {

	/**
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialisation to verify that the sender and receiver of a serialised object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;

	public TicketDAO() {
		super(Ticket.class);
	}
	
	public Long countOccupiedSeatsByFlightId(Integer idFlight) {
		try {
			final CriteriaQuery<Long> CRITERIA_COUNT;
			final CriteriaQuery<Flight> CRITERIA_QUERY;
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			
			CRITERIA_COUNT = criteriaBuilder.createQuery(Long.class);
			CRITERIA_QUERY = criteriaBuilder.createQuery(Flight.class);
			
			// Se estou contando algo o sentido é SEMPRE One -> Many
			Root<Flight> flightTable = CRITERIA_COUNT.from(CRITERIA_QUERY.getResultType());
			Join<Flight, Ticket> ticketTable = flightTable.join("tickets");
			
			// Contando quantos tickets existem no voo que tem este id
			CRITERIA_COUNT.select(criteriaBuilder.count(ticketTable)).where(
					criteriaBuilder.and(
							criteriaBuilder.equal(flightTable.get("id"), idFlight),
							criteriaBuilder.equal(ticketTable.get("isCanceled"), false)));
			Long occupiedSeats = entityManager.createQuery(CRITERIA_COUNT).getSingleResult();
			
			return occupiedSeats;
		} catch (Exception exception) {
			System.err.println("Catch Exception countOccupiedSeatsByFlightId() in TicketDAO");
			System.err.println(exception.getClass().getName());
			exception.printStackTrace();
			
			return null;
		}
	}

	public List<Ticket> findTicketsByUserId(int userId) {
		try {
			final CriteriaQuery<Ticket> CRITERIA_QUERY;
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CRITERIA_QUERY = criteriaBuilder.createQuery(Ticket.class);
			Root<Ticket> ticketTable = CRITERIA_QUERY.from(Ticket.class);
			Join<Ticket, User> userTable = ticketTable.join("passenger");
			
			CRITERIA_QUERY.select(ticketTable).where(criteriaBuilder.equal(userTable.get("id"), userId));
			
			return entityManager.createQuery(CRITERIA_QUERY).getResultList();
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " findTicketByUserId() in TicketDAO");
			exception.printStackTrace();
			
			return null;
		}
	}

}