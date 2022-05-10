package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.ReceiptMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.NiinInfoEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ReceiptEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReceiptRepositoryImpl implements ReceiptRepository {

  private final ReceiptEntityRepository entityRepository;
  private final NiinInfoEntityRepository niinInfoEntityRepository;
  private final ReceiptMapper mapper;

  @Override
  public List<Receipt> search(ReceiptSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Optional<Receipt> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Long count(ReceiptSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public void save(Receipt receipt) {
    val entity = entityRepository.findById(receipt.getRcn()).orElseGet(ReceiptEntity::new);
    apply(entity, receipt);
    entityRepository.saveAndFlush(entity);
  }

  private void apply(ReceiptEntity entity, Receipt receipt) {
    entity.setRcn(receipt.getRcn());
    entity.setFrustrateCode(receipt.getFrustrateCode());
    entity.setFrustrateLocation(receipt.getFrustrateLocation());
    entity.setQuantityStowed(receipt.getQuantityStowed());
    entity.setQuantityReleased(receipt.getQuantityReleased());
    entity.setQuantityMeasured(receipt.getQuantityMeasured());
    entity.setQuantityInvoiced(receipt.getQuantityInvoiced());
    entity.setQuantityInduced(receipt.getQuantityInduced());
    if (receipt.getNiinInfo() != null) {
      val niin = niinInfoEntityRepository.findById(receipt.getNiinInfo().getNiinId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate niin with id '%s'", receipt.getNiinInfo().getNiinId())));
      entity.setNiinInfo(niin);
    }
    entity.setContactNumber(receipt.getContactNumber());
    entity.setFundCode(receipt.getFundCode());
    entity.setSignalCode(receipt.getSignalCode());
    entity.setDocumentNumber(receipt.getDocumentNumber());
    entity.setDocumentId(receipt.getDocumentID());
    entity.setStatus(receipt.getStatus());
    entity.setCreatedBy(receipt.getCreatedBy());
    entity.setCreatedDate(receipt.getCreatedDate());
    entity.setModifiedBy(receipt.getModifiedBy());
    entity.setModifiedDate(receipt.getModifiedDate());
    entity.setConversionFlag(receipt.getConversionFlag());
    entity.setRoutingId(receipt.getRoutingId());
    entity.setWorkStation(receipt.getWorkStation());
    entity.setRdd(receipt.getRdd());
    entity.setSupplementaryAddress(receipt.getSupplementaryAddress());
    entity.setConsignee(receipt.getConsignee());
    entity.setDodDistCode(receipt.getDodDistCode());
    entity.setRpd(receipt.getRpd());
    entity.setPartialShipmentIndicator(receipt.getPartialShipmentIndicator());
    entity.setTracabilityNumber(receipt.getTracabilityNumber());
    entity.setClassCommoditNumber(receipt.getClassCommoditNumber());
    entity.setShipedFrom(receipt.getShipedFrom());
    entity.setCc(receipt.getCc());
    entity.setProjectCode(receipt.getProjectCode());
    entity.setPc(receipt.getPc());
    entity.setCognizanceCode(receipt.getCognizanceCode());
    entity.setMechNonMechFlag(receipt.getMechNonMechFlag());
    entity.setRation(receipt.getRation());
    entity.setSuffix(receipt.getSuffix());
    entity.setShelfLifeCode(receipt.getShelfLifeCode());
    entity.setWeight(receipt.getWeight());
    entity.setLength(receipt.getLength());
    entity.setHeight(receipt.getHeight());
    entity.setWidth(receipt.getWidth());
    entity.setUi(receipt.getUi());
    entity.setPrice(receipt.getPrice());
    entity.setFsc(receipt.getFsc());
    entity.setPartNumber(receipt.getPartNumber());
    entity.setSerialNumber(receipt.getSerialNumber());
    entity.setQuantityBackordered(receipt.getQuantityBackordered());
    entity.setRi(receipt.getRi());
    entity.setCube(receipt.getCube());
    entity.setNiin(receipt.getNiin());
    entity.setPartialShipment(receipt.getPartialShipment());
    entity.setQtyStowloss(receipt.getQtyStowloss());
    entity.setOverageFlag(receipt.getOverageFlag());
    entity.setShortageFlag(receipt.getShortageFlag());
    entity.setSecurityMarkClass(receipt.getSecurityMarkClass());
  }
}
