package mil.stratis.model.view.gcssmc.common;

import lombok.NoArgsConstructor;
import oracle.jbo.common.JboResourceBundle;

@NoArgsConstructor //JboResourceBundle need default no args constructor
public class DocIdListVORowImplMsgBundle extends JboResourceBundle {

  static final Object[][] sMessageStrings =
      {{"Value_LABEL", "DOC-ID"},
          {"Descriptiion_LABEL", "Description"}};

  /**
   * @return an array of key-value pairs.
   */
  public Object[][] getContents() {
    return super.getMergedArray(sMessageStrings, super.getContents());
  }
}
