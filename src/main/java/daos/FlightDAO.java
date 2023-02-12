package daos;

import javax.ejb.Stateless;

import entities.Flight;

@Stateless
public class FlightDAO extends GenericDAO<Flight> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FlightDAO() {
		super(Flight.class);
	}

}