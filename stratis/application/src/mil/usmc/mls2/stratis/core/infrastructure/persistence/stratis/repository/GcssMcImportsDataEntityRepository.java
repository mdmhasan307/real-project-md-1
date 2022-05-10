package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.GcssMcImportsDataEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GcssMcImportsDataEntityRepository extends EntityRepository<GcssMcImportsDataEntity, Integer>, GcssMcImportsDataEntityRepositoryCustom {

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(value = "update gcssmc_imports_data set status='IGNORE', modified_date=sysdate where gcssmc_imports_data_id < :gcssmcImportId and interface_name = :interfaceName and status='READY'",
      nativeQuery = true)
  void updateIgnoreAllPreviousDataByInterface(@Param("gcssmcImportId") Integer gcssmcImportId, @Param("interfaceName") String interfaceName);
}