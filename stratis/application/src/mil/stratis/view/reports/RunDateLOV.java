package mil.stratis.view.reports;

import java.io.Serializable;

public class RunDateLOV implements Serializable {

  private String runDate;

  public RunDateLOV() {
    super();
  }

  public void setRunDate(String runDate) {
    this.runDate = runDate;
  }

  public String getRunDate() {
    return runDate;
  }
}
