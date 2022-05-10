package mil.usmc.mls2.stratis.core.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.ReceiptService;
import mil.usmc.mls2.stratis.core.service.RefDasfService;
import mil.usmc.mls2.stratis.core.service.StowService;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptProcessorImpl implements ReceiptProcessor {

  private final ReceiptService receiptService;
  private final StowService stowService;
  private final ReceiptSuffixProcessor receiptSuffixProcessor;
  private final StowProcessor stowProcessor;
  private final RefDasfService refDasfService;
  private final DateService dateService;


  @Override
  public Receipt copyAndUpdateToD6AReceipt(Receipt d6tReceipt, Location location) {

    //get dasf qty left
    val dasf = refDasfService.search(RefDasfSearchCriteria.builder().documentNumber(d6tReceipt.getDocumentNumber()).build());
    val dasfQty = dasf.stream().findFirst().map(RefDasf::getQuantityDue).orElse(0);
    //update current receipt ro dasf qty left and save
    val induced = d6tReceipt.getQuantityInduced();
    val left = induced - dasfQty;
    d6tReceipt.setQuantitiesForD6Copy(dasfQty.intValue());
    receiptService.save(d6tReceipt);
    //copy receipt and set remaining
    Receipt d6aReceipt = d6tReceipt.toBuilder()
        .suffix(receiptSuffixProcessor.generateSuffix(d6tReceipt.getDocumentNumber()))
        .quantityInduced(left)
        .quantityInvoiced(left)
        .rcn(-1) //blank out the rcn to save a new receipt
        .build();
    receiptService.save(d6aReceipt);
    //return D6A receipt
    val receipts = receiptService.search(ReceiptSearchCriteria.fromReceipt(d6aReceipt));
    val newReceipt = receipts.stream().findFirst().orElse(null);
    if (newReceipt == null)
      return null;

    //copy receipts stows
    //if not serial niin
    if (!"Y".equalsIgnoreCase(d6tReceipt.getNiinInfo().getSerialControlFlag())) {
      //copy d6t stow and updated quantity
      val stows = stowService.search(StowSearchCriteria.builder().receipt(d6tReceipt).build());
      val d6tStow = stows.stream().findFirst().orElse(null);
      d6tStow.setQtyToBeStowed(d6tReceipt.getQuantityInduced());
      stowService.update(d6tStow);
      val expDate = d6tStow.getExpirationDate() != null ? dateService.formatDate(d6tStow.getExpirationDate(), DateConstants.DEFAULT_INPUT_DATE_FORMAT) : "";
      val domDate = d6tStow.getDateOfManufacture() != null ? dateService.formatDate(d6tStow.getDateOfManufacture(), DateConstants.DEFAULT_INPUT_DATE_FORMAT) : "";
      stowProcessor.createStowForReceipt(newReceipt, left, d6tStow.getCreatedBy(), expDate, domDate, null);
      val addedStows = stowService.search(StowSearchCriteria.builder().receipt(newReceipt).build());
      val d6aStow = addedStows.stream().findFirst().orElse(null);
      d6aStow.finalizeStowForReceiptCreation(location, d6tStow.getCreatedBy());
      stowService.update(d6aStow);
    }
    else {
      //else
      //split stows between receipts
      val stows = stowService.search(StowSearchCriteria.builder().receipt(d6tReceipt).build());
      for (int s = 0; s < stows.size(); s++) {
        if (s >= dasfQty.intValue()) {
          //reassign this stow to the new receipt
          stows.get(s).reassginReceipt(newReceipt);
          stowService.update(stows.get(s));
        }
      }
    }
    return newReceipt;
  }

}
