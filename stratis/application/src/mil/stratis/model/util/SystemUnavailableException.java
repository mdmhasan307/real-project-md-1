package mil.stratis.model.util;

public class SystemUnavailableException extends Exception {
  private int x;
  
  public SystemUnavailableException() {}
  public SystemUnavailableException(String msg) { super(msg); }
  public SystemUnavailableException(String msg, int x) {
    super(msg);
    this.x = x;
  }

  public int val() { return x; }
  public String getMessage() {
    return "Detail Message: "+ x + " "+ super.getMessage();
  }
}
