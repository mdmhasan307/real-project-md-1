package mil.stratis.view.login;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class LoginController extends Login {
  
  public List getEquipLoginAllValues() {
    // clear the list and then fill it with all the equipment
    EquipLoginAllValues.clear();
    try {
      // make the lists to be filled
      List id = new ArrayList(), name = new ArrayList();

      // fill the rows from the database
      getLoginModule().returnEquipRows(id, name);

      // add the rows we got back
      for (int i = 0; i < id.size(); i++) {
        EquipLoginAllValues.add(new SelectItem(id.get(i),
            name.get(i).toString()));
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      EquipLoginAllValues.add(new SelectItem(1, "WorkStation Error"));
    }
    return EquipLoginAllValues;
  }
}
