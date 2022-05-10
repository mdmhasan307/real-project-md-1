package mil.usmc.mls2.stratis.common.share.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import mil.usmc.mls2.stratis.common.model.ApiError;
import mil.usmc.mls2.stratis.common.model.ApiResponse;
import mil.usmc.mls2.stratis.common.share.json.JsonFieldAccessors;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@SuppressWarnings("unused")
@JsonFieldAccessors
public class ClientGenericResponse<T> implements ApiResponse<T> {

  private final boolean success;
  private final ApiError error;
  private final T data;

  public static <T> ClientGenericResponse<T> success(@NonNull T data) {
    return ClientGenericResponse.of(true, null, data);
  }

  public static <T> ClientGenericResponse<T> failure(ApiError error) {
    return ClientGenericResponse.of(false, error, null);
  }

  public static <T> ClientGenericResponse<T> of(boolean success, ApiError error, T data) {return new ClientGenericResponse<T>(success, error, data);}
}
