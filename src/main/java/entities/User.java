package entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import enums.Role;
import lombok.Data;

/**
 * User information type that the backend consumes and produces.
 * 
 * @author Wanderley Drumond
 *
 */
@Entity
@Table(name = "users")
@Data
public class User implements Serializable {
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
	private String name;
	@NotBlank
	private String username;
	@NotBlank
	private String password;
	private String token;
	@Enumerated(EnumType.STRING)
	private Role role;
	private Boolean isDeleted;
	@JsonIgnore
	@OneToMany(mappedBy = "passenger")
	private List<Ticket> tickets;
}