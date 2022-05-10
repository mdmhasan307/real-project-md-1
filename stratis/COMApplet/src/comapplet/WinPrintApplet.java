package comapplet;

import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.JApplet;

public class WinPrintApplet extends JApplet {

  private String urlToPrint = "";
  private String noCopies = "";
  private String orientation = "";
  private boolean ready = false;
  private boolean debug = true;

  public WinPrintApplet() {
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(null);
  }

  public void init() {
    try {
      //* initialize the content pane
      jbInit();

      ready = true;
    }
    catch (Exception e) {
      System.out.println("initialize exception");
      e.printStackTrace();
      ready = false;
    }
  }

  private boolean isNotEmpty(String str) {
    if (str == null)
      return false;
    if (str.trim().equals(""))
      return false;
    return true;
  }

  public void start() {
    //* initialize the parameters
    this.debug = getParameter("debug").equalsIgnoreCase("true");
    this.urlToPrint = getParameter("url");
    this.noCopies = getParameter("noCopies");
    this.orientation = getParameter("orientation");
    if (this.orientation == null)
      this.orientation = "";

    if (ready && isNotEmpty(urlToPrint)) {
      try {
        if (debug) System.out.println("window printing ... " + urlToPrint);

        //* added to support multiple prints in same applet context
        StringTokenizer st = new StringTokenizer(urlToPrint, " ");
        String url = "";
        if (st != null) {
          while (st.hasMoreTokens()) {
            url = st.nextToken();
            if (url != null) {
              this.getAppletContext().showDocument(new URL(url), "_blank");
            }
          }
        }
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
