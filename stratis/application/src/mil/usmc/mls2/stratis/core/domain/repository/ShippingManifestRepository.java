package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.ShippingManifest;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestSearchCriteria;

import java.util.List;

public interface ShippingManifestRepository {
    List<ShippingManifest> search(ShippingManifestSearchCriteria criteria);

    Long count(ShippingManifestSearchCriteria criteria);
}
