package mappers;

import java.sql.Timestamp;

import javax.ejb.Stateless;

import dtos.FlightDTO;
import entities.Flight;

/**
 * Class responsible by transform <code>Flight</code> data that transits between backend and frontend.
 * 
 * @author Wanderley Drumond
 *
 */
@Stateless
public class FlightMapper {
	/**
	 * Changes a <code>Flight</code> DTO object into a <code>Flight</code> Entity object.
	 * 
	 * @param flightDTO the object that will be transformed into Entity object
	 * @return the Entity resultant object
	 */
	public Flight toEntity(FlightDTO flightDTO) {
		Flight flight = new Flight();
		
		flight.setDestination(flightDTO.getDestination());
		flight.setTotalSeats(flightDTO.getTotalSeats());
		flight.setDepartTime(Timestamp.valueOf(flightDTO.getDepartTime()));
		
		return flight;
	}
	
	/**
	 * Changes a <code>Flight</code> Entity object into a <code>Flight</code> DTO object.
	 * 
	 * @param flight the object that will be transformed into DTO object
	 * @return the DTO resultant object
	 */
	public FlightDTO toDTO(Flight flight) {
		FlightDTO flightDTO = new FlightDTO();
		
		flightDTO.setId(flight.getId());
		flightDTO.setCode(flight.getCode());
		flightDTO.setDestination(flight.getDestination());
		flightDTO.setTotalSeats(flight.getTotalSeats());
		flightDTO.setDepartTime(flight.getDepartTime().toString());
		
		return flightDTO;
	}
}