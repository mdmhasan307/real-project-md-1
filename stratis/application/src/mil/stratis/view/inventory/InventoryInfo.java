package mil.stratis.view.inventory;

import lombok.NoArgsConstructor;
import lombok.val;
import mil.stratis.model.services.InventoryModuleImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;

@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class InventoryInfo extends MdssBackingBean {

  // listener for each time the page is loaded

  @Override
  public void onPageLoad() {

    int temp3;
    String temp4;

    InventoryModuleImpl service = getInventoryAMService();

    if (!this.isPostback()) {
      //Get the user Id from the userbean
      temp4 = (JSFUtils.getManagedBeanValue("userbean.userId")).toString();
      temp3 = Integer.parseInt(temp4);

      //Call the application module to filter the EquipTableView based on
      //the workstation_id
      service.setUserId(temp3);

      //Call the function in the application module to
      //filter the InvPendingView based on the wac
      val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
      val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);

      service.buildQueueRSForInventoryItem(
          Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()),
          Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()),
          false, sortOrder);
      //Retrieve the Divider index for the bin from the
      //application module

      //Call the function in the application module to
      //filter the LocPendingView based on the wac
      service.buildQueueRSForLocSurvey(
          Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()),
          Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()),
          false, sortOrder);
    }
  }
}
