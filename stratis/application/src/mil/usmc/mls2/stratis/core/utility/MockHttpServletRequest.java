package mil.usmc.mls2.stratis.core.utility;

import lombok.val;

import javax.servlet.http.HttpServletRequest;

public class MockHttpServletRequest extends org.springframework.mock.web.MockHttpServletRequest {

  public static MockHttpServletRequest of(HttpServletRequest input) {
    val request = new MockHttpServletRequest();
    return request;
  }
}
