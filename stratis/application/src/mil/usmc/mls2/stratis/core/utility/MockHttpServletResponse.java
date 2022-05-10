package mil.usmc.mls2.stratis.core.utility;

import lombok.val;

import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse extends org.springframework.mock.web.MockHttpServletResponse {

  public static MockHttpServletResponse of(HttpServletResponse input) {
    val response = new MockHttpServletResponse();
    return response;
  }
}
