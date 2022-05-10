package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QUserEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.UserEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class UserEntityRepositoryCustomImpl implements UserEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<UserEntity> findByEdipi(String edipi) {

    val qUser = QUserEntity.userEntity;
    val query = new JPAQuery<>(entityManager).select(qUser).from(qUser).where(qUser.cacNumber.eq(edipi));

    return Optional.ofNullable(query.fetchFirst());
  }
}
