package mil.usmc.mls2.stratis.common.domain.exception;

import java.util.List;

/**
 * Marker interface to identify middleware generated exceptions
 */
public interface StratisException {

  List<String> getUserMessages();

  List<String> getSystemMessages();
}
