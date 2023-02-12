package daos;

import javax.ejb.Stateless;

import entities.Ticket;

@Stateless
public class TicketDAO extends GenericDAO<Ticket> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TicketDAO() {
		super(Ticket.class);
	}

}