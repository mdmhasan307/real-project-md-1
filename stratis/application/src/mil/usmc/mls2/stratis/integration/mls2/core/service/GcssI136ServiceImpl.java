package mil.usmc.mls2.stratis.integration.mls2.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.usmc.mls2.integration.migs.gcss.r001.outbound.model.ItemMasterData;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.domain.repository.NiinInfoRepository;
import mil.usmc.mls2.stratis.core.domain.repository.RefSlcRepository;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.modules.mhif.application.model.ItemMasterProcessResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcssI136ServiceImpl implements GcssI136Service {

  private static final String SUCCESS_STATUS = "S";
  private final NiinInfoRepository niinInfoRepository;
  private final RefSlcRepository refSlcRepository;
  private final DefaultTransactionExecutor transactionExecutor;

  @Override
  @Transactional(readOnly = true)
  public ItemMasterProcessResult processItemMasterData(Set<ItemMasterData> data, boolean alwaysProcess) {
    val niinsSkipped = new AtomicInteger();
    val niinsErrored = new AtomicInteger();
    val niinsProcessed = new AtomicInteger();
    val messages = new ArrayList<String>();

    data.forEach(itemMasterRecord -> {
      if (alwaysProcess || SUCCESS_STATUS.equals(itemMasterRecord.getEstatus())) {
        //Extensions can be null if no Shelf life code is provided.  Even though the database has a
        //default value of 0 in the shelfLifeExtension column.
        Integer shelfLifeExtension = null;
        if (itemMasterRecord.getShelfLifeCode() != null) {
          int slc = Util.cleanInt(itemMasterRecord.getShelfLifeCode());
          if (slc > 0 && slc < 8) {
            shelfLifeExtension = 1;
          }
        }
        val unitPrice = itemMasterRecord.getUnitPrice() != null ? itemMasterRecord.getUnitPrice().intValue() : 0;
        val nomenclature = Util.cleanString(itemMasterRecord.getNomenclature()).replace('\'', ' ');  //* remove apostrophes from NOMENCLATURE - breaks print SID label

        NiinInfo niinInfo;
        val now = OffsetDateTime.now();
        val niinInfoToUpdate = niinInfoRepository.findByNiin(itemMasterRecord.getNiin());
        val refslc = refSlcRepository.findByShelfLifeCode(itemMasterRecord.getShelfLifeCode()).orElse(null);
        if (niinInfoToUpdate.isPresent()) {
          niinInfo = niinInfoToUpdate.get();

          niinInfo.updateNiinInfoFromGcss(itemMasterRecord.getFsc(), nomenclature, unitPrice,
              itemMasterRecord.getSerialControlFlag(), itemMasterRecord.getLotControlFlag(),
              itemMasterRecord.getUoi(), itemMasterRecord.getPrecMIC(), itemMasterRecord.getControlledInvItemCode(),
              shelfLifeExtension, itemMasterRecord.getDemilC(), refslc);
          log.debug("NIIN Previously found: {}", niinInfo.getNiin());
        }
        else {
          niinInfo = NiinInfo.builder()
              .fsc(itemMasterRecord.getFsc())
              .niin(itemMasterRecord.getNiin())
              .cube(new BigDecimal(1)) //defaults to 1 in the db via a stored procedure
              .inventoryThreshold("N") //defaults to N in the db via a stored procedure
              .nomenclature(nomenclature)
              .price(unitPrice)
              .ui(itemMasterRecord.getUoi())
              .serialControlFlag(itemMasterRecord.getSerialControlFlag())
              .lotControlFlag(itemMasterRecord.getLotControlFlag())
              .activityDate(now)
              .lastMhifUpdateDate(now)
              .smic(itemMasterRecord.getPrecMIC())
              .scc(itemMasterRecord.getControlledInvItemCode()) // CIIC, formerly SEC, in GCSS Physical security code replacement
              .shelfLifeExtension(shelfLifeExtension)
              .demilCode(itemMasterRecord.getDemilC())
              .shelfLifeCode(refslc)
              .build();
          log.debug("new Niin: {}", niinInfo.getNiin());
        }

        //run the save niin info in its own transaction.  This is to allow each niin and its triggers to not rely on the other niins inserts/updates.
        val exceptionOccurred = new AtomicBoolean(false);
        transactionExecutor.execute("save NIIN Info",
            () -> niinInfoRepository.save(niinInfo),
            e -> {
              exceptionOccurred.set(true);
              niinsErrored.incrementAndGet();
              messages.add(String.format("NIIN processing Failed for NIIN %s", itemMasterRecord.getNiin()));
              log.warn("unable to save NIIN info {}", itemMasterRecord.getNiin());
            });
        if (!exceptionOccurred.get()) niinsProcessed.incrementAndGet();
      }
      else {
        niinsSkipped.incrementAndGet();
        messages.add(String.format("NIIN processing skipped for NIIN %s", itemMasterRecord.getNiin()));
        log.info("Niin not processed into stratis NIIN: {}, EStatus: {}", itemMasterRecord.getNiin(), itemMasterRecord.getEstatus());
      }
    });
    return ItemMasterProcessResult.builder()
        .totalNiins(data.size())
        .niinsProcessed(niinsProcessed.get())
        .niinsErrored(niinsErrored.get())
        .niinsSkipped(niinsSkipped.get())
        .messages(messages)
        .build();
  }
}
