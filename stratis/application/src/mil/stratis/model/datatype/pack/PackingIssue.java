package mil.stratis.model.datatype.pack;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PackingIssue {

  private String status = "";
  private String pin = "";
  private String suffixCode = "";
  private String scn = "";
  private String aac = "";
  private String documentNumber = "";
  private String qtyPicked = "";

  public String toString() {
    return pin +
        " " +
        scn +
        " " +
        status;
  }
}
