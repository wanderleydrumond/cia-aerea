package daos;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import entities.User;
import enums.Role;

/**
 * Class that makes the database communication layer role in relation with of
 * the user table.
 * 
 * @author Wanderley Drumond
 *
 */
@Stateless
public class UserDAO extends GenericDAO<User> {
	/**
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;

	public UserDAO() {
		super(User.class);
	}

	/**
	 * Finds a user in database with the given token.
	 * 
	 * @param token 	  the token to use as the search key
	 * @param tokenColumn the column in the database that contains the token value
	 * @return
	 * 	<ul>If:
	 * 		<li>the user was found, <strong>the user with the given token</strong></li>
	 * 		<li>no user was found, <strong>null</strong></li>
	 * 	</ul> 
	 */
	public Optional<User> findByToken(String token, String tokenColumn) {
		try {
			final CriteriaQuery<User> CRITERIA_QUERY;
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CRITERIA_QUERY = criteriaBuilder.createQuery(User.class);
			Root<User> userTable = CRITERIA_QUERY.from(User.class);

			CRITERIA_QUERY.select(userTable).where(criteriaBuilder.equal(userTable.get(tokenColumn), token));

			return entityManager.createQuery(CRITERIA_QUERY).getResultList().stream().findFirst();
		} catch (Exception exception) {
			System.err.println("Catch findByToken() in UserDAO");
			exception.printStackTrace();

			return null;
		}
	}

	public List<User> findAllByRole(Role role) {
		try {
			final CriteriaQuery<User> CRITERIA_QUERY;
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CRITERIA_QUERY = criteriaBuilder.createQuery(User.class);
			Root<User> userTable = CRITERIA_QUERY.from(User.class);
			
			CRITERIA_QUERY.select(userTable).where(criteriaBuilder.equal(userTable.get("role"), role));
			
			return entityManager.createQuery(CRITERIA_QUERY).getResultList();
		} catch (Exception exception) {
			System.err.println("Catch findAllByToken() in UserDAO");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Finds a user which belongs the given username and password.
	 * 
	 * @param username the username of the user
	 * @param password the password of the user
	 * @return
	 * 	<ul>If:
	 * 		<li><strong>Successful</strong>, the user object</li>
	 * 		<li><strong>Unsuccessful</strong>, null</li>
	 * 	</ul> 
	 */
	public User signIn(String username, String password) {
		try {
			final CriteriaQuery<User> CRITERIA_QUERY;
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CRITERIA_QUERY = criteriaBuilder.createQuery(User.class);
			Root<User> userTable = CRITERIA_QUERY.from(User.class);

			CRITERIA_QUERY.select(userTable).where(criteriaBuilder
					.and(criteriaBuilder.equal(userTable.get("username"), username),
						 criteriaBuilder.equal(userTable.get("password"), password)));
			
			return entityManager.createQuery(CRITERIA_QUERY).getSingleResult();
		} catch (Exception exception) {
			System.err.println("Catch signIn() in UserDAO");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Updates the table that contains the given token, deleting it.
	 * 
	 * @param token to be found/deleted
	 * @return the amount of rows updated
	 */
	public Integer signOut(String token) {
		try {
			final CriteriaUpdate<User> CRITERIA_UPDATE;
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CRITERIA_UPDATE = criteriaBuilder.createCriteriaUpdate(User.class);
			Root<User> userTable = CRITERIA_UPDATE.from(User.class);
			
			CRITERIA_UPDATE.set("token", null);
			CRITERIA_UPDATE.where(criteriaBuilder.equal(userTable.get("token"), token));
			
			return entityManager.createQuery(CRITERIA_UPDATE).executeUpdate();
		} catch (Exception exception) {
			System.err.println("Catch signOut() in UserDAO");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Checks in database if exists the same username as that have been given.
	 * 
	 * @param username to be found in the database
	 * @return
	 * 		  <ul>
	 * 			If:
	 * 			<li>Is found, <strong>TRUE</strong></li>
	 * 			<li>Is not found, gets <code>NoResultException, then </code><strong>FALSE</strong></li>
	 * 			<li>Something goes wrong, <strong>NULL</strong></li>
	 * 		  </ul>
	 */
	public Boolean exists(String username) {
		try {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

			CriteriaQuery<Boolean> query = criteriaBuilder.createQuery(Boolean.class);
			query.from(User.class);
			query.select(criteriaBuilder.literal(true));

			Subquery<User> subquery = query.subquery(User.class);
			Root<User> subRootEntity = subquery.from(User.class);
			subquery.select(subRootEntity);

			Predicate predicate = criteriaBuilder.equal(subRootEntity.get("username"), username);
			subquery.where(predicate);
			query.where(criteriaBuilder.exists(subquery));

			TypedQuery<Boolean> typedQuery = entityManager.createQuery(query);
			
			return typedQuery.getSingleResult();
		} catch (NoResultException noResultException) { // Esse catch é esperado quando nenhum resultado é encontrado.
			System.out.println("Catch NoResultException in exists() in UserDAO.");
			
			return false;
		} catch (Exception exception) {
			System.err.println("Catch exists() in UserDAO");
			exception.printStackTrace();
			
			return null;
		} 
		
	}
}