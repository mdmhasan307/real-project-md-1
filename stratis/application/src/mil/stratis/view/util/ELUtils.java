package mil.stratis.view.util;

import javax.el.ValueExpression;

import javax.faces.context.FacesContext;

import oracle.adf.model.BindingContext;

/**
 * This is a utility class
 */

public final class ELUtils {

    /**
     * Private Constructor
     */
    private ELUtils () {

    }

    /**
     *
     * @param booleanExpr
     * @return Boolean value of true or false
     */
    public static boolean test (final String booleanExpr) {
        return Boolean.TRUE.equals(get(booleanExpr));
    }

    /**
     *
     * @param expr
     * @return Object value
     * 
     */
    public static Object get (final String expr) {
        final FacesContext context = FacesContext.getCurrentInstance();
        
        final ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), expr, BindingContext.class);
        return ve.getValue(context.getELContext());
    }

    /**
     *
     * @param expr
     * @param value
     * 
     */
    public static void set (final String expr, final String value) {
        Object valToSet = value;
        if (isELExpr(value)) {
            valToSet = get(value);
        }
        set(expr, valToSet);
    }

    /**
     *
     * @param expr
     * @param value
     * 
     */
    public static void set (final String expr, final Object value) {
        final FacesContext context = FacesContext.getCurrentInstance();
        final ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), expr, BindingContext.class);
        ve.setValue(context.getELContext(), value);
    }

    /**
     *
     * @param obj
     * @return true or false
     */
    private static boolean isELExpr (final Object obj) {
        if (obj instanceof String) {
            String str = (String)obj;
            str = str.trim();
            return str.startsWith("#{") && str.endsWith("}");
        }
        return false;
    }

}
