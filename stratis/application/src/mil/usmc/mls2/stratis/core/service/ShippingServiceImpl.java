package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.Shipping;
import mil.usmc.mls2.stratis.core.domain.model.ShippingSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.ShippingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingRepository shippingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Shipping> search(ShippingSearchCriteria criteria) {
        return shippingRepository.search(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(ShippingSearchCriteria criteria) {
        return shippingRepository.count(criteria);
    }
}
