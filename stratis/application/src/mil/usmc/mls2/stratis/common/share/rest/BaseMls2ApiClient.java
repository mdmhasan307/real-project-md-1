package mil.usmc.mls2.stratis.common.share.rest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.share.ResourceUtility;
import mil.usmc.mls2.stratis.common.share.hmac.HmacUtility;
import mil.usmc.mls2.stratis.core.utility.JsonUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
@Getter(value = AccessLevel.PROTECTED)
@Accessors(fluent = true)
@SuppressWarnings("Duplicates")
public abstract class BaseMls2ApiClient {

  private HmacUtility hmacUtility = null;
  private RestTemplate restTemplate = null;

  protected void configure(RestClientSettings settings, HmacUtility hmacUtility, ResourceUtility resourceUtility) throws Exception {
    this.restTemplate = RestTemplateBuilder.buildRestTemplate(settings, resourceUtility);
    this.hmacUtility = hmacUtility;
  }

  protected abstract String baseEndpointAddress();

  protected abstract String baseUri();

  @SuppressWarnings("SameParameterValue")
  protected <T> T get(String service, Class<T> clazz, Map<String, Object> params) {
    val headers = new HttpHeaders();
    val method = HttpMethod.GET;
    configureHeaders(service, headers, method);

    val requestEntity = new HttpEntity<>(headers);
    val builder = UriComponentsBuilder.fromHttpUrl(toRestUrl(service));
    params.forEach(builder::queryParam);

    val url = builder.toUriString();
    log.debug("calling url: {}", url);
    val response = restTemplate.exchange(url, method, requestEntity, clazz);

    checkStatus(response, service);
    return response.getBody();
  }

  protected String getJson(String service, Map<String, Object> params) {
    val headers = new HttpHeaders();
    val method = HttpMethod.GET;
    configureHeaders(service, headers, method);

    val requestEntity = new HttpEntity<>(headers);
    val builder = UriComponentsBuilder.fromHttpUrl(toRestUrl(service));
    params.forEach(builder::queryParam);

    val url = builder.toUriString();
    log.debug("calling url: {}", url);

    val response = restTemplate.exchange(url, method, requestEntity, Object.class);
    checkStatus(response, service);
    return JsonUtils.toJson(response.getBody());
  }

  protected String postJson(String service, Object payload) {
    val headers = new HttpHeaders();
    val method = HttpMethod.POST;
    configureHeaders(service, headers, method);

    val requestEntity = new HttpEntity<>(JsonUtils.toJson(payload), headers);
    val builder = UriComponentsBuilder.fromHttpUrl(toRestUrl(service));

    val response = restTemplate.exchange(builder.toUriString(), method, requestEntity, Object.class);

    checkStatus(response, service);
    return JsonUtils.toJson(response.getBody());
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public <T> ClientGenericResponse<Collection<T>> convertToClientGenericCollectionResponse(Class<T> clazz, String json) {
    val clientGenericResponse = (ClientGenericResponse<Collection<Map<String, String>>>) JsonUtils.toObject(json, ClientGenericResponse.class);
    Collection<T> records = new ArrayList<>();
    for (Map<String, String> record : clientGenericResponse.getData()) {
      records.add(JsonUtils.toObject(record, clazz, true));
    }

    return ClientGenericResponse.of(clientGenericResponse.isSuccess(), clientGenericResponse.getError(), records);
  }

  private String toRestUrl(String endpoint) {
    return format("%s/%s/%s", baseEndpointAddress(), baseUri(), endpoint);
  }

  private void configureHeaders(String service, HttpHeaders headers, HttpMethod method) {
    hmacUtility.configureHeaders(baseEndpointAddress(), service, headers, method);
  }

  private <T> void checkStatus(ResponseEntity<T> response, String service) {
    if (HttpStatus.OK != response.getStatusCode())
      throw new IllegalStateException(format("failed on service '%s' with response code: %s", service, response.getStatusCode()));
  }
}
