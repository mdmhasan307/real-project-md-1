package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.ShippingManifest;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestSearchCriteria;

import java.util.List;

public interface ShippingManifestService {
    List<ShippingManifest> search(ShippingManifestSearchCriteria criteria);

    Long count(ShippingManifestSearchCriteria criteria);
}
