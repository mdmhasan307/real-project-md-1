package mil.stratis.view.reports;

import java.io.Serializable;

public class TransactionDateLOV implements Serializable {

  private String transactionDate;

  public TransactionDateLOV() {
    super();
  }

  public void setTransactionDate(String transactionDate) {
    this.transactionDate = transactionDate;
  }

  public String getTransactionDate() {
    return transactionDate;
  }
}
