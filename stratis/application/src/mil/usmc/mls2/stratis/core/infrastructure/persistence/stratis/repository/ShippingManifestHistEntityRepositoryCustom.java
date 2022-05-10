package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHistSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingManifestHistEntity;

import java.util.List;

public interface ShippingManifestHistEntityRepositoryCustom {

  List<ShippingManifestHistEntity> search(ShippingManifestHistSearchCriteria criteria);

  Long count(ShippingManifestHistSearchCriteria criteria);
}
