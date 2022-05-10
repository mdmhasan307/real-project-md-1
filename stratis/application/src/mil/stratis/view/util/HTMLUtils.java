package mil.stratis.view.util;

import lombok.NoArgsConstructor;

import java.util.Locale;

@NoArgsConstructor
public class HTMLUtils {

  private String currentParagraphStyle = "";
  private String currentParagraphClass = "";
  private String currentSpanStyle = "";
  private String currentSpanClass = "";
  private String currentSpanHeaderClass = "";
  private String currentSpanValueClass = "";
  private String currentSpanHeaderStyle = "";
  private String currentSpanValueStyle = "";
  
  /**
   * Method to add hison report style sheet.
   */
  public String addStyles() {
    StringBuilder buf = new StringBuilder();

    buf.append("\n<style type=\"text/css\">\n");

    buf.append("p.TxtNormal, li.TxtNormal, div.TxtNormal ");
    buf.append("{ \n");
    buf.append("margin-top:6.0pt; \n");
    buf.append("margin-right:0in; \n");
    buf.append("margin-bottom:6.0pt; \n");
    buf.append("margin-left:0in; \n");
    buf.append("tab-stops:.5in; \n");
    buf.append("font-size:10.0pt; \n");
    buf.append("font-family:\"Arial\",\"sans-serif\"; \n");
    buf.append("} \n");

    buf.append(".paddedto8 { \n");
    buf.append("margin-top:6.0pt; \n");
    buf.append("margin-right:0in; \n");
    buf.append("margin-left:2in; \n");
    buf.append("width:100px; \n");
    buf.append("} ");

    buf.append(".paddedto20 { \n");
    buf.append("margin-top:6.0pt; \n");
    buf.append("margin-right:0in; \n");
    buf.append("margin-left:0in; \n");
    buf.append("width:400px; \n");
    buf.append("} \n");

    buf.append(".HdrTxt { \n");
    buf.append("font-family:\"Tahoma\",\"sans-serif\"; \n");
    buf.append("color:black; \n");
    buf.append("margin-left: .5em; \n");
    buf.append("} \n");

    buf.append(".HdrTxt2 { \n");
    buf.append("font-family:\"Tahoma\",\"sans-serif\"; \n");
    buf.append("color:black; \n");
    buf.append("margin-left: 1.5em; \n");
    buf.append("} \n");

    buf.append(".HdrTxt3 { \n");
    buf.append("font-family:\"Tahoma\",\"sans-serif\"; \n");
    buf.append("color:black; \n");
    buf.append("margin-left:   3em; \n");
    buf.append("} \n");

    buf.append(".ValTxt { \n");
    buf.append("font-family:\"Tahoma\",\"sans-serif\"; \n");
    buf.append("color:black; \n");
    buf.append("margin-left: .2em; \n");
    buf.append("} \n");

    buf.append("</style>\n");
    return buf.toString();
  }

  /**
   * Method to add a blank 4pt header line.
   */
  public String addBlankLine() {
    return "<p style='margin:0in;margin-bottom:4pt'></p>\n";
  }

  /**
   * Method to start a paragph with style.
   */
  public String startStyledParagraph() {
    StringBuilder buf = new StringBuilder("<p");
    if (currentParagraphClass.length() > 0) {
      buf.append(' ').append(currentParagraphClass);
    }
    if (currentParagraphStyle.length() > 0) {
      buf.append(' ').append(currentParagraphStyle);
    }
    buf.append(">\n");
    return buf.toString();
  }

  public String endParagraph() {
    return "\n</p>\n";
  }

  public String addStyledText(String text, String classString, String styleString) {
    String currentSpanClassTemp = currentSpanClass;
    setCurrentSpanClass(classString);
    StringBuilder buf = new StringBuilder();
    buf.append(addStyledText(text, styleString));
    setCurrentSpanClass(currentSpanClassTemp);
    return buf.toString();
  }

  public String addStyledText(String text, String spanStyle) {
    String currentSpanStyleTemp = currentSpanStyle;
    StringBuilder buf = new StringBuilder("<span");
    if (currentSpanClass.length() > 0) {
      buf.append(' ').append(currentSpanClass);
    }
    setCurrentSpanStyle(spanStyle);
    if (currentSpanStyle.length() > 0) {
      buf.append(' ').append(currentSpanStyle);
    }
    buf.append('>').append(text).append("</span>");
    setCurrentSpanStyle(currentSpanStyleTemp);
    return buf.toString();
  }

  public String addStyledHeaderText(String text) {
    StringBuilder buf = new StringBuilder("<span");

    if (currentSpanHeaderClass.length() > 0) {
      buf.append(' ').append(currentSpanHeaderClass);
    }
    if (currentSpanValueStyle.length() > 0) {
      buf.append(' ').append(currentSpanHeaderStyle);
    }
    buf.append('>').append(text).append("</span>");

    return buf.toString();
  }

  public String addStyledValueText(String text) {
    StringBuilder buf = new StringBuilder("<span");

    if (currentSpanValueClass.length() > 0) {
      buf.append(' ').append(currentSpanValueClass);
    }
    if (currentSpanValueStyle.length() > 0) {
      buf.append(' ').append(currentSpanValueStyle);
    }
    buf.append('>').append(text).append("</span>");

    return buf.toString();
  }

  /**
   * Use this version to override the spanClass of the styledValuePair.
   */
  public String addStyledValuePair(String header, String value, String spanHeaderClass) {
    StringBuilder buf = new StringBuilder();
    String currentSpanClassTemp = currentSpanHeaderClass;
    setCurrentSpanHeaderClass(spanHeaderClass);
    // setCurrentSpanClass( spanClass );
    buf.append(addStyledValuePair(header, value));
    setCurrentSpanHeaderClass(currentSpanClassTemp);
    return buf.toString();
  }

  public String addStyledValuePair(String header, String value) {
    StringBuilder buf = new StringBuilder();
    buf.append(addStyledHeaderText(header));
    buf.append(addStyledValueText(value));
    return buf.toString();
  }

  /**
   * Setter for the class paragraph style.
   * The user can provide a whole string or just the attributes.
   */
  public void setCurrentParagraphStyle(String currentParagraphStyle) {
    StringBuilder buf = new StringBuilder("");
    if (currentParagraphStyle != null) {
      if (currentParagraphStyle.toLowerCase(Locale.US).startsWith("style=")) {
        buf.append(currentParagraphStyle);
      }
      else {
        buf.append("style='").append(currentParagraphStyle).append("'");
      }
    }
    this.currentParagraphStyle = buf.toString();
  }

  public String getCurrentParagraphStyle() {
    return currentParagraphStyle;
  }

  /**
   * Setter for the class for a paragraph.
   * The user can provide a whole string or just the attributes.
   */
  public void setCurrentParagraphClass(String currentParagraphClass) {
    StringBuilder buf = new StringBuilder("");
    if (currentParagraphClass != null) {
      if (currentParagraphClass.toLowerCase(Locale.US).startsWith("class=")) {
        buf.append(currentParagraphClass);
      }
      else {
        buf.append("class=\"").append(currentParagraphClass).append('"');
      }
    }
    this.currentParagraphClass = buf.toString();
  }

  public String getCurrentParagraphClass() {
    return currentParagraphClass;
  }

  /**
   * Setter for the span style.
   * The user can provide a whole string or just the attributes.
   */
  public void setCurrentSpanStyle(String currentSpanStyle) {
    StringBuilder buf = new StringBuilder("");
    if (currentSpanStyle != null) {
      if (currentSpanStyle.toLowerCase(Locale.US).startsWith("style=")) {
        buf.append(currentSpanStyle);
      }
      else {
        buf.append("style='").append(currentSpanStyle).append("'");
      }
    }
    this.currentSpanStyle = buf.toString();
  }

  public String getCurrentSpanStyle() {
    return currentSpanStyle;
  }

  /**
   * Setter for the span style.
   * The user can provide a whole string or just the attributes.
   */
  public void setCurrentSpanClass(String currentSpanClass) {
    StringBuilder buf = new StringBuilder("");
    if (currentSpanClass != null) {
      if (currentSpanClass.toLowerCase(Locale.US).startsWith("class=")) {
        buf.append(currentSpanClass);
      }
      else {
        buf.append("class=\"").append(currentSpanClass).append('"');
      }
    }
    this.currentSpanClass = buf.toString();
  }

  public String getCurrentSpanClass() {
    return currentSpanClass;
  }

  public void setCurrrentSpanHeaderClass(String currentSpanHeaderClass) {
    StringBuilder buf = new StringBuilder("");
    if (currentSpanHeaderClass != null) {
      if (currentSpanHeaderClass.toLowerCase(Locale.US).startsWith("class=")) {
        buf.append(currentSpanHeaderClass);
      }
      else {
        buf.append("class=\"").append(currentSpanHeaderClass).append('"');
      }
    }
    this.currentSpanHeaderClass = buf.toString();
  }

  public void setCurrentSpanHeaderClass(String currentSpanHeaderClass) {
    StringBuilder buf = new StringBuilder("");
    if (currentSpanHeaderClass != null) {
      if (currentSpanHeaderClass.toLowerCase(Locale.US).startsWith("class=")) {
        buf.append(currentSpanHeaderClass);
      }
      else {
        buf.append("class=\"").append(currentSpanHeaderClass).append('"');
      }
    }
    this.currentSpanHeaderClass = buf.toString();
  }

  public String getCurrentSpanHeaderClass() {
    return currentSpanHeaderClass;
  }

  public void setCurrentSpanValueClass(String currentSpanValueClass) {
    StringBuilder buf = new StringBuilder("");
    if (currentSpanValueClass != null) {
      if (currentSpanValueClass.toLowerCase(Locale.US).startsWith("class=")) {
        buf.append(currentSpanValueClass);
      }
      else {
        buf.append("class=\"").append(currentSpanValueClass).append('"');
      }
    }
    this.currentSpanValueClass = buf.toString();
  }

  public String getCurrentSpanValueClass() {
    return currentSpanValueClass;
  }

  public void setCurrentSpanHeaderStyle(String currentSpanHeaderStyle) {
    StringBuilder buf = new StringBuilder("");
    if (currentSpanHeaderStyle != null) {
      if (currentSpanHeaderStyle.toLowerCase(Locale.US).startsWith("style=")) {
        buf.append(currentSpanHeaderStyle);
      }
      else {
        buf.append("style='").append(currentSpanHeaderStyle).append("'");
      }
    }
    this.currentSpanHeaderStyle = buf.toString();
  }

  public String getCurrentSpanHeaderStyle() {
    return currentSpanHeaderStyle;
  }

  public void setCurrentSpanValueStyle(String style) {
    StringBuilder buf = new StringBuilder("");
    if (style != null) {
      if (style.toLowerCase(Locale.US).startsWith("style=")) {}
    }
    else {
      buf.append("style='").append(style).append("'");
    }
    currentSpanValueStyle = buf.toString();
  }

  public String getCurrentSpanValueStyle() {
    return currentSpanValueStyle;
  }
}
