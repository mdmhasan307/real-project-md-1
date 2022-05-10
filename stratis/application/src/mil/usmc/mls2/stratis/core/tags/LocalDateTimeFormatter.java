package mil.usmc.mls2.stratis.core.tags;

import lombok.Setter;
import lombok.val;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.time.LocalDate;

@Setter
public class LocalDateTimeFormatter extends TagSupport {

  private LocalDate date;

  @Override
  public int doStartTag() throws JspException {
    if (date == null) return SKIP_BODY;
    try {
      JspWriter out = pageContext.getOut();
      val dateService = ContextUtils.getBean(DateService.class);
      val dateToDisplay = dateService.formatDate(date, DateConstants.SITE_DATE_FORMATTER_PATTERN);
      out.println(dateToDisplay);
    }
    catch (IOException e) {
      throw new JspTagException(e.getClass() + " : " + e.getMessage());
    }

    return SKIP_BODY;
  }
}
