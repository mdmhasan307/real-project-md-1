package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Controller
@RequestMapping(value = MappingConstants.DOWNLOAD_ZPL)
@RequiredArgsConstructor
public class DownloadZplController {

  private final GlobalConstants globalConstants;

  @GetMapping
  public void show(HttpServletRequest request, HttpServletResponse response) {

    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return;
      }

      UserInfo user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      final String zpl = user.getPrintcomstring();
      final String filename = "label.txt";

      // get your file as InputStream
      InputStream is = new ByteArrayInputStream(zpl.getBytes());

      // copy it to response’s OutputStream
      IOUtils.copy(is, response.getOutputStream());
      String filenameStr = "attachment;" + "filename = " + filename;
      response.setContentType("text/plain; charset=utf-8");
      response.addHeader("Content - Disposition", "attachment; filename = " + filenameStr);
      response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.addHeader("Pragma", "no-cache");
      response.addHeader("Expires", "0");
      response.flushBuffer();
    }
    catch (IOException e) {
      log.error("Error writing file to output stream", e);
    }
  }
}