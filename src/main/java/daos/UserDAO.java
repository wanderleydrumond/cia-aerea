package daos;

import javax.ejb.Stateless;

import entities.User;

/**
 * Class that makes the database communication layer role in relation with of the user table.
 * 
 * @author Wanderley Drumond
 *
 */
@Stateless
public class UserDAO extends GenericDAO<User> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserDAO() {
		super(User.class);
	}

}