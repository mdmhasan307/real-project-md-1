package mil.stratis.view.reports;

import java.util.ArrayList;
import java.util.List;

public class StandardReport {

  private String name;
  private List<StandardReport> reports;

  public StandardReport() {
    super();
  }

  public StandardReport(String name) {
    this.name = name; // To Store Header Values (Storing Group Name)
    reports = new ArrayList<>(); // To Store reports in Report Group
  }

  public void setReports(List<StandardReport> reportsInGroups) {
    this.reports = reportsInGroups;
  }

  public List<StandardReport> getReports() {
    return reports;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addReports(StandardReport report) {
    reports.add(report);
  }
}

