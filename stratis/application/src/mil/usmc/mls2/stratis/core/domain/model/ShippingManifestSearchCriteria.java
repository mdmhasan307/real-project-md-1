package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class ShippingManifestSearchCriteria extends BaseSearchCriteria {
    private Integer equipmentNumber;
    private boolean manifestDateNull;
    private String floorLocation;
}
