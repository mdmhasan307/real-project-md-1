package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class WacEntityRepositoryCustomImpl implements WacEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;
}
