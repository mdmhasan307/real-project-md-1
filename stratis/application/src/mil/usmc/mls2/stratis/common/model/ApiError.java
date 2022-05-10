package mil.usmc.mls2.stratis.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unused")
public class ApiError {

  RestStatus status;
  String message;
  ApiErrorDetail[] errors;

  @JsonIgnore
  Exception cause;

  public static ApiError badRequest(String message) {
    return new ApiError(RestStatus.BAD_REQUEST, message, null, null);
  }

  public static ApiError badRequest(String message, ApiErrorDetail[] errors) {
    return new ApiError(RestStatus.BAD_REQUEST, message, errors, null);
  }

  public static ApiError forbidden(String message) {
    return new ApiError(RestStatus.FORBIDDEN, message, null, null);
  }

  public static ApiError notFound(String message) {
    return new ApiError(RestStatus.NOT_FOUND, message, null, null);
  }

  public static ApiError serverError(String message) {
    return new ApiError(RestStatus.SERVER_ERROR, message, null, null);
  }

  public static ApiError serverError(String message, Exception cause) {
    return new ApiError(RestStatus.SERVER_ERROR, message, null, cause);
  }
}
