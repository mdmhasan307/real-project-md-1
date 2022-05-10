package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Shipping;
import mil.usmc.mls2.stratis.core.domain.model.ShippingSearchCriteria;

import java.util.List;

public interface ShippingService {
    List<Shipping> search(ShippingSearchCriteria criteria);

    Long count(ShippingSearchCriteria criteria);
}
