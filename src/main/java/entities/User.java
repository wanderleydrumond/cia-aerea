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

import enums.Role;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User implements Serializable {
	/**
	 * 
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
	@OneToMany(mappedBy = "passenger")
	private List<Ticket> tickets;
}