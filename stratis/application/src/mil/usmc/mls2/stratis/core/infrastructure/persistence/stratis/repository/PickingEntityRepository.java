package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickingEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface PickingEntityRepository extends EntityRepository<PickingEntity, Integer>, PickingEntityRepositoryCustom {

  void deleteByScn(String scn);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(value = "delete from picking_hist where scn=:scn AND created_date > :createdDate",
      nativeQuery = true)
  void deleteHistoryByScnAndDate(@Param("scn") String scn, @Param("createdDate") OffsetDateTime createdDate);

  @Query(value = "select sum(QTY_PICKED) from picking_hist where scn = :scn and status = :status and created_date > :createdDate",
      nativeQuery = true)
  Optional<Long> findCountOfPickingHistory(@Param("scn") String scn, @Param("status") String status, @Param("createdDate") OffsetDateTime createdDate);
}
