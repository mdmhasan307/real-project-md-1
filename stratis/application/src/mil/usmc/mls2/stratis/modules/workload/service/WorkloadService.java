package mil.usmc.mls2.stratis.modules.workload.service;

import mil.usmc.mls2.stratis.core.domain.model.Picking;
import mil.usmc.mls2.stratis.modules.workload.domain.model.NewCartonParams;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingConsolidationInfo;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingStationResult;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingStationType;

import java.math.BigDecimal;
import java.util.function.Function;

public interface WorkloadService {

  PackingStationResult getPackingStation(Integer pid, int userId, Integer qtyPicked, int mcpxPref);

  PackingStationResult getPackingStation(Picking pick, int userId, Integer qtyPicked, int mcpxPref);

  PackingStationResult getPackingStation(Picking pick, int userId, Integer qtyPicked, int mcpxPref, Function<Integer, PackingConsolidationInfo> lastProcessedPackageProvider);

  PackingConsolidationInfo openNewCarton(NewCartonParams newCartonParams, Integer customerId, BigDecimal
      weight, BigDecimal cube, Integer pickQty, int userId, PackingStationType packingStationType, Integer issueQty, String priorityGroup);
}
