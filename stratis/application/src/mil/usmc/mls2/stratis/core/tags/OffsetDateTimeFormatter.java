package mil.usmc.mls2.stratis.core.tags;

import lombok.Setter;
import lombok.val;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.time.OffsetDateTime;

@Setter
public class OffsetDateTimeFormatter extends TagSupport {

  private OffsetDateTime date;
  private boolean showTime = false;

  @Override
  public int doStartTag() throws JspException {
    if (date == null) return SKIP_BODY;
    try {
      JspWriter out = pageContext.getOut();
      val dateService = ContextUtils.getBean(DateService.class);
      val dateToDisplay = dateService.shiftAndFormatDateForTag(date, showTime);
      val tooltipDate = dateService.formatDateForTag(date, showTime);
      out.println("<span title='" + tooltipDate + "'>" + dateToDisplay + "</span>");
    }
    catch (IOException e) {
      throw new JspTagException(e.getClass() + " : " + e.getMessage());
    }

    return SKIP_BODY;
  }
}
