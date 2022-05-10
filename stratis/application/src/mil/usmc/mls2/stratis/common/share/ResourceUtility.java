package mil.usmc.mls2.stratis.common.share;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unused"})
public class ResourceUtility {

  private final ResourceLoader resourceLoader;

  public Resource loadResource(String resourceName) {
    log.info("attempting to load resource '{}'", resourceName);

    Resource resource;

    if (resourceName.startsWith("classpath:")) {
      resource = resourceLoader.getResource(resourceName);
      if (resource.exists()) {
        log.info("found using classpath (as supplied), returning resource for '{}'", resourceName);
        return resource;
      }
    }
    else if (resourceName.startsWith("file:")) {
      resource = resourceLoader.getResource(resourceName);
      if (resource.exists()) {
        log.info("found using file (as supplied), returning resource for '{}'", resourceName);
        return resource;
      }
    }
    else {
      // try from classpath
      resource = resourceLoader.getResource("classpath:" + resourceName);
      if (resource.exists()) {
        log.info("found using classpath (implied), returning resource for '{}'", resourceName);
        return resource;
      }
      // try from file
      resource = resourceLoader.getResource("file:" + resourceName);
      if (resource.exists()) {
        log.info("found using file (implied), returning resource for '{}'", resourceName);
        return resource;
      }

      // try as-is
      resource = resourceLoader.getResource(resourceName);
      if (resource.exists()) {
        log.info("found as-is, returning resource for '{}'", resourceName);
        return resource;
      }
    }

    log.warn("failed to find resource '{}'...returning.", resourceName);
    return resource;
  }
}

