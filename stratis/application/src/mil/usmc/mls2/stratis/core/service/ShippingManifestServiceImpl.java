package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifest;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.ShippingManifestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingManifestServiceImpl implements ShippingManifestService {

    private final ShippingManifestRepository shippingManifestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingManifest> search(ShippingManifestSearchCriteria criteria) {
        return shippingManifestRepository.search(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(ShippingManifestSearchCriteria criteria) {
        return shippingManifestRepository.count(criteria);
    }
}
