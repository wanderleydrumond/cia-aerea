package entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Entity
@Table(name = "flights")
@Data
public class Flight implements Serializable {
	/**
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@NotBlank
	private String code;
	@NotBlank
	private String destination;
	private Timestamp departTime;
	@NotNull
	private Integer totalSeats;
	@OneToMany(mappedBy = "flightDetails")
	private List<Ticket> tickets;
}