package mil.usmc.mls2.stratis.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RestStatus {

  OK(HttpStatus.OK),
  BAD_REQUEST(HttpStatus.BAD_REQUEST),
  FORBIDDEN(HttpStatus.FORBIDDEN),
  NOT_FOUND(HttpStatus.BAD_REQUEST), // by design treating as a BAD_REQUEST not a NOT_FOUND
  SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus httpStatus;
}