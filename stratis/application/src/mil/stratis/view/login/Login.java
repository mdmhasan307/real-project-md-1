package mil.stratis.view.login;

import lombok.NoArgsConstructor;
import mil.stratis.view.BackingHandler;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Login extends BackingHandler {

  private transient RichInputText usernameinput;
  private transient RichSelectOneChoice userworkstationselect;
  private boolean loginfailed = false;
  private String navigation = "";

  List EquipLoginSelectedValues = new ArrayList();
  List EquipLoginAllValues = new ArrayList();
  
  public void setEquipLoginSelectedValues(List equipLoginSelectedValues) {
    this.EquipLoginSelectedValues = equipLoginSelectedValues;
  }

  public List getEquipLoginSelectedValues() {
    return EquipLoginSelectedValues;
  }

  public void setEquipLoginAllValues(List equipLoginAllValues) {
    this.EquipLoginAllValues = equipLoginAllValues;
  }

  public void setNavigation(String navigation) {
    this.navigation = navigation;
  }

  public String getNavigation() {
    return navigation;
  }

  public void setLoginfailed(boolean loginfailed) {
    this.loginfailed = loginfailed;
  }

  public boolean isLoginfailed() {
    return loginfailed;
  }

  public void setUsernameinput(RichInputText usernameinput) {
    this.usernameinput = usernameinput;
  }

  public RichInputText getUsernameinput() {
    return usernameinput;
  }

  public void setUserworkstationselect(RichSelectOneChoice userworkstationselect) {
    this.userworkstationselect = userworkstationselect;
  }

  public RichSelectOneChoice getUserworkstationselect() {
    return userworkstationselect;
  }

  public List getEquipLoginAllValues() {
    return EquipLoginAllValues;
  }
}
