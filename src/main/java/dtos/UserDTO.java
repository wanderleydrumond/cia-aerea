package dtos;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement
@NoArgsConstructor
@Getter
@Setter
public class UserDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String name, username, password, token;
	private Role role;
}