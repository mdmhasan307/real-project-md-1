package mil.stratis.view.util;

import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.jbo.client.Configuration;
import oracle.jbo.common.ampool.EnvInfoProvider;

import java.util.Hashtable;

public class DynamicEnvInfoProvider implements EnvInfoProvider {

  public DynamicEnvInfoProvider() {
    super();
  }

  public Object getInfo(String infoType, Object env) {
    AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);

    String jamthis = adfDbCtxLookupUtils.getDbCtxLookupPath();
    ((Hashtable) env).put(Configuration.JDBC_DS_NAME, jamthis);
    return null;
  }

  @Override
  public void modifyInitialContext(Object o) {
    //noop
  }

  @Override
  public int getNumOfRetries() {
    return 0;
  }
}