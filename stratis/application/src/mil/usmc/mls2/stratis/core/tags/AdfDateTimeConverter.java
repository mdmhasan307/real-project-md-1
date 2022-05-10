package mil.usmc.mls2.stratis.core.tags;

import lombok.Setter;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;

@Setter
public class AdfDateTimeConverter extends DateTimeConverter {

  private String pattern;
  private String fromPattern;
  private boolean shiftZone;

  public AdfDateTimeConverter(String pattern, String fromPattern, boolean shift) {
    this.pattern = pattern;
    this.fromPattern = fromPattern;
    this.shiftZone = shift;
  }

  public String getAsString(FacesContext context, UIComponent component, Object value) {
    val dateService = ContextUtils.getBean(DateService.class);
    if (value == null) {
      return null;
    }
    else {
      val valueStr = value.toString();
      //The screen shows the formatted date shifted to display timezone if required.  Default is true
      //right now only things that should NOT shift are Date only fields such as Expiration Date and Manufacture Date
      val dateToDisplay = dateService.formatForAdfDisplay(valueStr, fromPattern, pattern, shiftZone, false);

      //Tooltip displays the formatted date directly from the database
      val tooltipDate = dateService.formatForAdfDisplay(valueStr, fromPattern, pattern, false, true);
      return "<span title='" + tooltipDate + "'>" + dateToDisplay + "</span>";
    }
  }

  public Object getAsObject(FacesContext context, UIComponent component, String value) {
    throw new StratisRuntimeException("Unsupported Function in AdfDateTimeConverter");
  }
}
