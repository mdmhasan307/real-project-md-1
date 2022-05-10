package mil.usmc.mls2.stratis.core.utility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

public class FacesUtils {

  public static Config createConfig(ServletContext servletContext) throws ServletException {
    FacesContextFactory facesContextFactory;
    Lifecycle lifecycle;
    try {
      facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory("javax.faces.context.FacesContextFactory");
    }
    catch (FacesException var7) {
      throw new UnavailableException("severe.webapp.facesservlet.init_failed");
    }

    try {
      val lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory("javax.faces.lifecycle.LifecycleFactory");
      val lifecycleId = ObjectUtils.firstNonNull(servletContext.getInitParameter("javax.faces.LIFECYCLE_ID"), "DEFAULT");
      lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
    }
    catch (FacesException var6) {
      Throwable rootCause = var6.getCause();
      if (rootCause == null) throw var6;
      throw new ServletException(var6.getMessage(), rootCause);
    }

    return Config.of(facesContextFactory, lifecycle);
  }

  @Getter
  @RequiredArgsConstructor(staticName = "of")
  public static class Config {

    private final FacesContextFactory facesContextFactory;
    private final Lifecycle lifecycle;
  }
}
