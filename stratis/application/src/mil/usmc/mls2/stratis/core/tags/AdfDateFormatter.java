package mil.usmc.mls2.stratis.core.tags;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import org.apache.myfaces.trinidad.bean.FacesBean;
import org.apache.myfaces.trinidad.component.UIXValue;
import org.apache.myfaces.trinidad.webapp.UIXComponentELTag;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.servlet.jsp.JspException;

@Slf4j
@Setter
@NoArgsConstructor
public class AdfDateFormatter extends UIXComponentELTag {

  private ValueExpression value;
  private String pattern = DateConstants.SITE_DATE_FORMATTER_PATTERN;
  //Default fromPattern is the standard ADF Row Date Return Pattern, but can be overridden when another pattern is known.
  private String fromPattern = DateConstants.ADF_ROW_DATE_RETURN_PATTERN;
  private boolean shiftZone = true;

  @Override
  public String getComponentType() {
    return "oracle.adf.RichOutputText";
  }

  @Override
  public String getRendererType() {
    return "oracle.adf.rich.Text";
  }

  public final void setValue(ValueExpression value) {
    this.value = value;
  }

  public final void setPattern(String pattern) {
    if (pattern != null)
      this.pattern = pattern;
  }

  public final void setFromPattern(String pattern) {
    if (pattern != null)
      this.fromPattern = pattern;
  }

  public final void setShiftZone(String shift) {
    if (shift != null) {
      shiftZone = Boolean.parseBoolean(shift);
    }
  }

  @Override
  protected void setProperties(FacesBean bean) {
    super.setProperties(bean);
    this.setProperty(bean, UIXValue.VALUE_KEY, value);

    FacesContext fctx = FacesContext.getCurrentInstance();
    Application application = fctx.getApplication();
    ExpressionFactory expressionFactory = application.getExpressionFactory();
    ELContext context = fctx.getELContext();
    ValueExpression createValueExpression =
        expressionFactory.createValueExpression(context, "false", Boolean.class);

    this.setProperty(bean, RichOutputText.ESCAPE_KEY, createValueExpression);

    bean.setProperty(UIXValue.CONVERTER_KEY, createConverter());
  }

  @Override
  public void release() {
    super.release();
    this.value = null;
  }

  @Override
  protected UIComponent createComponent(FacesContext context, String newId) throws JspException {
    val component = super.createComponent(context, newId);
    val converter = createConverter();
    ((ValueHolder) component).setConverter(converter);
    return component;
  }

  private Converter createConverter() {
    return new AdfDateTimeConverter(pattern, fromPattern, shiftZone);
  }
}
