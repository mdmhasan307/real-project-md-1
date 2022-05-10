package mil.usmc.mls2.stratis.core.infrastructure.dao;

import mil.stratis.model.datatype.user.UserRights;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class UserDao {

  @PersistenceContext
  private EntityManager entityManager;

  /*
  Replacement for pkg_user_mgmt.f_get_group_privs_4_user
   */
  public long getGroupPrivsForUser(int userId, int userTypeId) {

    AtomicLong userRights = new AtomicLong();

    Query q = entityManager.createNativeQuery("SELECT ug.group_id, gl.group_name, gp.priv_id, pl.priv_name " +
        "FROM USER_GROUPS ug, GROUP_LU gl, GROUP_PRIVS gp, " +
        "PRIV_LU pl, ACCT_TYPE_PRIVS ap, USER_ACCT_TYPES uat " +
        "WHERE ug.user_id = :userId " +
        "AND ug.acct_type_id = :userTypeId " +
        "AND ug.group_id = gl.group_id " +
        "AND ug.group_id = gp.group_id " +
        "AND gp.priv_id = pl.priv_id " +
        "AND ap.acct_type_id = ug.acct_type_id " +
        "AND pl.priv_id = ap.priv_id " +
        "AND uat.acct_type_id = ug.acct_type_id " +
        "AND ug.user_id = uat.user_id");

    q.setParameter("userId", userId);
    q.setParameter("userTypeId", userTypeId);
    List<Object[]> results = q.getResultList();

    results.forEach(x -> {
      userRights.updateAndGet(v -> v | UserRights.userRightLookUp((String) x[3]));
    });

    return userRights.get();
  }
}
