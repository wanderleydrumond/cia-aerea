package daos;

import javax.ejb.Stateless;

import entities.User;

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