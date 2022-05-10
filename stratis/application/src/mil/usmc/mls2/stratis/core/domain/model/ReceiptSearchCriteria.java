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
public class ReceiptSearchCriteria extends BaseSearchCriteria {
    private String documentNumber;
    private String suffix;
    private Boolean checkSuffixNull;
    private String status;
    private String documentId;
    private Boolean documentIdMatch;

    public static ReceiptSearchCriteria fromReceipt(Receipt receipt) {
        return new ReceiptSearchCriteria(
            receipt.getDocumentNumber(),
            receipt.getSuffix(),
            null == receipt.getSuffix(),
            receipt.getStatus(),
            receipt.getDocumentID(),
            null != receipt.getDocumentID());
    }
}
