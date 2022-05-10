package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHist;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHistSearchCriteria;

import java.util.List;

public interface ShippingManifestHistRepository {
    List<ShippingManifestHist> search(ShippingManifestHistSearchCriteria criteria);

    Long count(ShippingManifestHistSearchCriteria criteria);
}
