package mil.usmc.mls2.stratis.core.processor.gcss.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component("FeedProcessedListener")
@RequiredArgsConstructor
public class FeedProcessListener {

  private final GlobalConstants globalConstants;

  @Async
  @EventListener
  public void handle(FeedProcessedEvent event) {
    //when the feed processed event gets triggered we need to put something into the users session to notify them the feed is complete.

    val session = event.getSession();
    if (session == null) {
      log.debug("Session invalidated (user logged out/timed out.  No Feed Processed Notification will be sent");
      return;
    }
    val user = (UserInfo) session.getAttribute(globalConstants.getUserbeanSession());

    //in case the session still exists, but there is no user do nothing.
    if (user == null) return;

    String resultString = String.format("The %s feed process has completed:  %s", event.getFeedType(), event.getFeedResults());
    try {
      //This is in here in the rare instance that a Feed processes in less then 1 second.  The Imports screen has a 1 second delay before the screen updates
      //to let the user know they can go on their way and will be notified.  However that ADF updateUI manages to somehow wipe the message from session this
      //generates.  So block that from happening, we wait an extra second here before putting it into session.
      TimeUnit.SECONDS.sleep(1);
    }
    catch (Exception e) {
      //no-op
    }
    log.debug("User {} notified of Feed Processed Event: {}", user.getUsername(), resultString);
    event.getSession().setAttribute(FeedEvent.SESSION_IDENTIFIER, resultString);
  }
}
