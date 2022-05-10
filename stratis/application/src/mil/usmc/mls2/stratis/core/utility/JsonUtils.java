package mil.usmc.mls2.stratis.core.utility;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtils {

  private static final String ADDITIONAL_INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";
  private static final String BREAK = "<br />";

  private static ObjectMapper staticObjectMapper; // do not use directly (call createObjectMapper() instead to create a copy)

  private final ObjectMapper globalObjectMapper; // do not use directly (call createObjectMapper() instead to create a copy)

  @PostConstruct
  private void postConstruct() {
    staticObjectMapper = globalObjectMapper;
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TypeReferences {

    @SuppressWarnings("unused")
    public static <K, V, T extends Map<K, V>> TypeReference<T> map(Class<K> keyClass, Class<V> valueClass) {
      return new TypeReference<T>() {
      };
    }

    @SuppressWarnings("unused")
    public static <E, T extends List<E>> TypeReference<T> list(Class<E> entryClass) {
      return new TypeReference<T>() {
      };
    }

    @SuppressWarnings("unused")
    public static <E, T extends Set<E>> TypeReference<T> set(Class<E> entryClass) {
      return new TypeReference<T>() {
      };
    }
  }

  public static String toJson(Object data) {
    try {
      val buffer = new StringWriter();
      val objectMapper = createObjectMapper();
      objectMapper.writeValue(new JsonFactory().createGenerator(buffer), data);
      return buffer.toString();
    }
    catch (IOException e) {
      // convert to a runtime exception
      throw new IllegalArgumentException(e);
    }
  }

  public static <T> T toObject(String jsonData, Class<T> c) throws IOException {
    return toObject(jsonData, c, true);
  }

  public static <T> T toObject(String jsonData, Class<T> c, boolean failOnUnknownProperties) throws IOException {
    if (StringUtils.isBlank(jsonData)) {
      throw new RuntimeException("Unable to convert empty JSON Data String.");
    }

    if (c == null) {
      throw new RuntimeException("Unable to convert JSON Data with null reference Class.");
    }

    val objectMapper = createObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
    return objectMapper.readValue(jsonData, c);
  }

  public static <T> T toObject(String jsonData, TypeReference<T> tr) throws IOException {
    return toObject(jsonData, tr, true);
  }

  public static <T> T toObject(String jsonData, TypeReference<T> tr, boolean failOnUnknownProperties) throws IOException {

    Map<DeserializationFeature, Boolean> features = new HashMap<>();
    features.put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
    return toObject(jsonData, tr, features);
  }

  public static <T> T toObject(String jsonData, TypeReference<T> tr, Map<DeserializationFeature, Boolean> features) throws IOException {

    if (StringUtils.isBlank(jsonData)) {
      throw new IOException("Unable to convert empty JSON Data String.");
    }

    if (tr == null) {
      throw new IOException("Unable to convert JSON Data with null reference TypeReference.");
    }

    val objectMapper = createObjectMapper();
    if (features != null) {
      for (Map.Entry<DeserializationFeature, Boolean> entry : features.entrySet()) {
        objectMapper.configure(entry.getKey(), entry.getValue());
      }
    }

    return objectMapper.readValue(jsonData, tr);
  }

  public static <T> T toObject(@NonNull Map<String, String> jsonData, @NonNull Class<T> clazz, boolean failOnUnknownProperties) throws IOException {
    val features = new HashMap<DeserializationFeature, Boolean>();
    features.put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
    return toObject(jsonData, clazz, features);
  }

  public static <T> T toObject(@NonNull Map<String, String> jsonData, Class<T> clazz, Map<DeserializationFeature, Boolean> features) throws IOException {
    val objectMapper = createObjectMapper();
    if (features != null) {
      for (Map.Entry<DeserializationFeature, Boolean> entry : features.entrySet()) {
        objectMapper.configure(entry.getKey(), entry.getValue());
      }
    }

    // convert map to json;
    return objectMapper.readValue(objectMapper.writeValueAsString(jsonData), clazz);
  }

  public static String formatJsonToHtml(String jsonString) {
    if (StringUtils.isBlank(jsonString)) {
      return StringUtils.EMPTY;
    }

    final StringBuilder formatted = new StringBuilder(jsonString.length() + 1000);

    int indentLevel = 0;
    boolean inQuotedText = false;
    for (int i = 0; i < jsonString.length(); i++) {
      final char token = jsonString.charAt(i);

      switch (token) {
        case '"':
          inQuotedText = !inQuotedText;
          formatted.append(token);
          break;
        case '[':
          if ((1 + i < jsonString.length()) && jsonString.charAt(1 + i) == ']') {
            formatted.append(token); // do not break
            continue;
          }
          // otherwise, fall-through to next case...
        case '{':
          formatted.append(BREAK).append(indent(indentLevel)).append(token).append(BREAK).append(indent(++indentLevel));
          break;
        case ']':
          if (jsonString.charAt(i - 1) == '[') {
            formatted.append(token); // do not break
            continue;
          }
          // fall-through to next case...
        case '}':
          if ((1 + i < jsonString.length()) && jsonString.charAt(1 + i) == ',') {
            formatted.append(BREAK).append(indent(--indentLevel)).append(token);
            continue;
          }
          formatted.append(BREAK).append(indent(--indentLevel)).append(token).append(BREAK).append(indent(indentLevel));
          break;
        case ',':
          if (!inQuotedText) {
            formatted.append(token).append(BREAK).append(indent(indentLevel));
          }
          else {
            formatted.append(token);
          }
          break;
        case ':':
          if (!inQuotedText) {
            formatted.append(token).append("&nbsp;");
          }
          else {
            formatted.append(token);
          }
          break;
        case '\t':
          // fall-through to next case...
        case '\n':
          // fall-through to next case...
        case ' ':
          if (inQuotedText) {
            formatted.append(token);
          }
          break;
        default:
          formatted.append(token);
      }
    }

    return formatted.toString();
  }

  private static String indent(int level) {
    if (level < 1) {
      return StringUtils.EMPTY;
    }

    final StringBuilder indent = new StringBuilder(24 * level);
    for (int i = 0; i < level; i++) {
      indent.append(ADDITIONAL_INDENT);
    }

    return indent.toString();
  }

  public static ObjectMapper createObjectMapper() {
    return staticObjectMapper.copy();
  }
}
