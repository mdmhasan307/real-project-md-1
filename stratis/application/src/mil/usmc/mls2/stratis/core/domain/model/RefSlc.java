package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefSlc {
    private Integer refSlcId;
    private String shelfLifeCode;
    private Integer span;
    private Integer epxendLifeInA;
    private Integer expendLifeInB;
    private Integer expendLifeInC;
    private Integer repairLifeInA;
    private Integer spanDays;
    private Integer minVSpan;
    private Integer minDSpan;
}
