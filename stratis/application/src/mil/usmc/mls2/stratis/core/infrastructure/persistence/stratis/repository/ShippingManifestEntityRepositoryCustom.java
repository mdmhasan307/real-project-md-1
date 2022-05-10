package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingManifestEntity;

import java.util.List;

public interface ShippingManifestEntityRepositoryCustom {

  List<ShippingManifestEntity> search(ShippingManifestSearchCriteria criteria);

  Long count(ShippingManifestSearchCriteria criteria);
}
