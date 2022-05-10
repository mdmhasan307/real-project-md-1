package mil.stratis.view.reports;

import lombok.Data;

import java.io.Serializable;

@Data
public class ImportDateLOV implements Serializable {

  private String importDate;

  public ImportDateLOV() {
    super();
  }
}
