package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerial;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerialSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefGabfSerialMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefGabfSerialEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefGabfSerialRepositoryImpl implements RefGabfSerialRepository {

  private static final RefGabfSerialMapper REF_GABF_SERIAL_MAPPER = RefGabfSerialMapper.INSTANCE;
  private final RefGabfSerialEntityRepository entityRepository;

  @Override
  public Set<RefGabfSerial> search(RefGabfSerialSearchCriteria criteria) {
    val results = entityRepository.search(criteria);
    return REF_GABF_SERIAL_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void save(RefGabfSerial refGabfSerial) {
    val result = entityRepository.save(REF_GABF_SERIAL_MAPPER.modelToEntity(refGabfSerial));
    REF_GABF_SERIAL_MAPPER.entityToModel(result, refGabfSerial);
  }

  @Override
  public void deleteAll() {
    entityRepository.deleteAll();
  }
}
