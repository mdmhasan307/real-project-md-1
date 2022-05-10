package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.ShippingSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingEntity;

import java.util.List;

public interface ShippingEntityRepositoryCustom {

  List<ShippingEntity> search(ShippingSearchCriteria criteria);

  Long count(ShippingSearchCriteria criteria);
}
