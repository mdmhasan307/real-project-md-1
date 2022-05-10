package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHist;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHistSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.ShippingManifestHistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingManifestHistServiceImpl implements ShippingManifestHistService {

    private final ShippingManifestHistRepository shippingManifestHistRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingManifestHist> search(ShippingManifestHistSearchCriteria criteria) {
        return shippingManifestHistRepository.search(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(ShippingManifestHistSearchCriteria criteria) {
        return shippingManifestHistRepository.count(criteria);
    }
}
