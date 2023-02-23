package dtos;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User information type that the frontend consumes and produces.
 * 
 * @author Wanderley Drumond
 *
 */
@XmlRootElement
@NoArgsConstructor
@Getter
@Setter
public class UserDTO implements Serializable {
	/**
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String name, username, password, token;
	private Role role;
}