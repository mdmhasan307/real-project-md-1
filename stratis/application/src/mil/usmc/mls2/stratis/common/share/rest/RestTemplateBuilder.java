package mil.usmc.mls2.stratis.common.share.rest;

import lombok.val;
import mil.usmc.mls2.stratis.common.share.ResourceUtility;
import org.apache.http.Header;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;

public class RestTemplateBuilder {

  public static RestTemplate buildRestTemplate(RestClientSettings settings, ResourceUtility resourceUtility) throws Exception {
    val sslContextBuilder = SSLContexts.custom();
    if (settings.keyMaterialEnabled()) {
      val keyStore = keyStore(settings, resourceUtility);
      // determine the certificate to use for the request
      sslContextBuilder.loadKeyMaterial(keyStore, settings.keyStorePass().toCharArray(), (var1, var2) -> settings.keyStoreAlias());
    }
    if (settings.trustMaterialTrustAll()) {
      sslContextBuilder.loadTrustMaterial(null, (var1, var2) -> true);
    }
    else {
      val trustStore = trustStore(settings, resourceUtility);
      sslContextBuilder.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy());
    }

    val sslContext = sslContextBuilder.build();
    val sslCsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    val headers = new ArrayList<Header>();
    val httpClient = HttpClients
        .custom()
        .setSSLSocketFactory(sslCsf)
        .setDefaultHeaders(headers)
        .build();

    val requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(settings.connectTimeoutSeconds() * 1000);
    requestFactory.setReadTimeout(settings.readTimeoutSeconds() * 1000);
    return new RestTemplate(requestFactory);
  }

  private static KeyStore keyStore(RestClientSettings settings, ResourceUtility resourceUtility) throws Exception {
    val keystore = KeyStore.getInstance(settings.keyStoreType());
    try (val inputStream = resourceUtility.loadResource(settings.keyStoreFile()).getInputStream()) {
      keystore.load(inputStream, settings.keyStorePass().toCharArray());
      return keystore;
    }
  }

  private static KeyStore trustStore(RestClientSettings settings, ResourceUtility resourceUtility) throws Exception {
    val truststore = KeyStore.getInstance(settings.trustStoreType());
    try (val inputStream = resourceUtility.loadResource(settings.trustStoreFile()).getInputStream()) {
      truststore.load(inputStream, settings.trustStorePass().toCharArray());
      return truststore;
    }
  }
}
