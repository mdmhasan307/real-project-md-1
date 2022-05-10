package mil.usmc.mls2.stratis.common.model;

public interface ApiResponse<T> {

  boolean isSuccess();

  ApiError getError();

  T getData();
}