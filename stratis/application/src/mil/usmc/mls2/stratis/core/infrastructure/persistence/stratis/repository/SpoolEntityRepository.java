package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SpoolEntity;
import org.springframework.data.jpa.repository.Query;

public interface SpoolEntityRepository extends EntityRepository<SpoolEntity, Integer>, SpoolEntityRepositoryCustom {

  @Query(value = "select SPOOL_BATCH_NUM_SEQ.nextval from dual", nativeQuery = true)
  Integer getSpoolBatchNumber();
}