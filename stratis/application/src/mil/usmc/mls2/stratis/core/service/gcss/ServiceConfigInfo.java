package mil.usmc.mls2.stratis.core.service.gcss;

import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.apache.cxf.endpoint.Client;
import org.springframework.context.annotation.Profile;

import java.util.Map;

/**
 * General Service Configuration.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Profile(Profiles.LEGACY)
public class ServiceConfigInfo<F> {

  private Client client;
  private Map<String, Object> context;
  private String feedEndPoint;
  private F feedInterface;

  private Integer connectionTimeoutInMilliseconds;
  private Integer receiveTimeoutInMilliseconds;

  public ServiceConfigInfo(R12GcssFeedConstants constants) {
    this.connectionTimeoutInMilliseconds = constants.getConnectionTimeoutInMilliseconds();
    this.receiveTimeoutInMilliseconds = constants.getReceiveTimeoutInMilliseconds();
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  public String getFeedEndPoint() {
    return feedEndPoint;
  }

  public void setFeedEndPoint(String feedEndPoint) {
    this.feedEndPoint = feedEndPoint;
  }

  public F getFeedInterface() {
    return feedInterface;
  }

  public void setFeedInterface(F feedInterface) {
    this.feedInterface = feedInterface;
  }

  public Integer getConnectionTimeoutInMilliseconds() {
    return connectionTimeoutInMilliseconds;
  }

  public void setConnectionTimeoutInMilliseconds(Integer connectionTimeoutInMilliseconds) {
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
  }

  public Integer getReceiveTimeoutInMilliseconds() {
    return receiveTimeoutInMilliseconds;
  }

  public void setReceiveTimeoutInMilliseconds(Integer receiveTimeoutInMilliseconds) {
    this.receiveTimeoutInMilliseconds = receiveTimeoutInMilliseconds;
  }
}
