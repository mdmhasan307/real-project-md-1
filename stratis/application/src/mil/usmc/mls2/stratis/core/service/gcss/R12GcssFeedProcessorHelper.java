package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.FeedsException;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.util.BlindTrustManager;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.FIStaxInInterceptor;
import org.apache.cxf.interceptor.FIStaxOutInterceptor;
import org.apache.cxf.transport.common.gzip.GZIPInInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.BindingProvider;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This component is used to load up the keystores to access GCSS directly.  Meaning its only required
 * in LEGACY mode.  Integration mode MIGS will be the one communicating with GCSS.
 */

@Slf4j
@RequiredArgsConstructor
@Component
@Profile(Profiles.LEGACY)
public class R12GcssFeedProcessorHelper {

  private static final String GZIP_ENCODING = "gzip";
  private static final String SIG_CRYPTO_PROPERTIES_REF = "sigCryptoProperties";
  private static final String ENC_CRYPTO_PROPERTIES_REF = "encCryptoProperties";

  private final R12GcssFeedConstants gcssFeedConstants;
  private final R12GcssClientWsPasswordCallback gcssClientWsPasswordCallback;
  private KeyManager[] keyManagers;
  private TrustManager[] trustManagers;

  @PostConstruct
  private void postConstruct() {
    if (gcssFeedConstants.isPreConfigureSSL()) {
      configureSSL();
    }
    else {
      log.info("pre-configuration of GCSS SSL settings is disabled. reason: preConfigureSSL:{}]", gcssFeedConstants.isPreConfigureSSL());
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  void configureWebServiceRequest(ServiceConfigInfo configInfo) {
    val context = configInfo.getContext();
    val endPointTarget = configInfo.getFeedEndPoint();
    val client = configInfo.getClient();
    val finalEndPoint = gcssFeedConstants.getEndpointBaseUrl() + gcssFeedConstants.getEndpointUrlPath() + endPointTarget;

    log.trace("Feed Retrieval from finalEndPoint='{}'.", finalEndPoint);

    configureContext(context, finalEndPoint);
    configureHttpConduit(client, configInfo);
    configureInterceptors(client);
  }

  @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
  private void configureSSL() {
    log.debug("configuring GCSS SSL settings...");
    configureKeyManagers();
    configureTrustManagers();
  }

  @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
  private void configureKeyManagers() {
    log.info("configuring key managers using '{}'...", gcssFeedConstants.getKeyStoreFile());
    try (val fis = new FileInputStream(new File(gcssFeedConstants.getKeyStoreFile()))) {
      val keyStore = KeyStore.getInstance("JKS");
      val keyStorePassCharArr = gcssFeedConstants.getKeyStorePass().toCharArray();
      keyStore.load(fis, keyStorePassCharArr);

      val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, keyStorePassCharArr);
      keyManagers = keyManagerFactory.getKeyManagers();
    }
    catch (Exception e) {
      log.error("failed to configure ssl settings using keystore file '{}'", gcssFeedConstants.getKeyStoreFile(), e);
      throw new FeedsException("Failed to configure http conduit", e);
    }
  }

  @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
  private void configureTrustManagers() {
    log.info("configuring trust managers using '{}'...", gcssFeedConstants.getTrustStoreFile());
    try (val fis = new FileInputStream(new File(gcssFeedConstants.getTrustStoreFile()))) {
      if (gcssFeedConstants.isSslChecksEnabled()) {
        val trustStore = KeyStore.getInstance("JKS");
        val trustStorePassCharArr = gcssFeedConstants.getTrustStorePass().toCharArray();
        trustStore.load(fis, trustStorePassCharArr);

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        trustManagers = trustManagerFactory.getTrustManagers();
      }
      else {
        trustManagers = new TrustManager[]{new BlindTrustManager()};
      }
    }
    catch (Exception e) {
      log.error("failed to configure ssl settings using truststore file '{}'", gcssFeedConstants.getTrustStoreFile(), e);
      throw new FeedsException("Failed to configure http conduit", e);
    }
  }

  private void configureHttpConduit(Client client, ServiceConfigInfo<?> config) {
    val httpConduit = (HTTPConduit) client.getConduit();
    configureHttpPolicy(httpConduit, config);
    configureSslChecks(httpConduit);
    configureProxy(httpConduit);
    configureWss4j(config);
  }

  private void configureHttpPolicy(HTTPConduit httpConduit, ServiceConfigInfo<?> configInfo) {
    val connectionTimeoutInMilliseconds = ObjectUtils.firstNonNull(configInfo.getConnectionTimeoutInMilliseconds(), gcssFeedConstants.getConnectionTimeoutInMilliseconds());
    val receiveTimeoutInMilliseconds = ObjectUtils.firstNonNull(configInfo.getReceiveTimeoutInMilliseconds(), gcssFeedConstants.getReceiveTimeoutInMilliseconds());
    val compressionEnabled = gcssFeedConstants.isCompressionEnabled();
    val httpClientPolicy = new HTTPClientPolicy();
    httpClientPolicy.setAllowChunking(false);
    httpClientPolicy.setConnectionTimeout(connectionTimeoutInMilliseconds);
    httpClientPolicy.setReceiveTimeout(receiveTimeoutInMilliseconds);

    if (compressionEnabled) {
      httpClientPolicy.setAcceptEncoding(GZIP_ENCODING);
    }

    // assign policy to conduit
    httpConduit.setClient(httpClientPolicy);
  }

  private void configureSslChecks(HTTPConduit httpConduit) {
    TLSClientParameters tlsClientParameters = httpConduit.getTlsClientParameters();
    if (tlsClientParameters == null) {
      tlsClientParameters = new TLSClientParameters();
      httpConduit.setTlsClientParameters(tlsClientParameters);
    }

    tlsClientParameters.setDisableCNCheck(true);
    tlsClientParameters.setUseHttpsURLConnectionDefaultHostnameVerifier(false);

    val requiresSslConfiguration = !gcssFeedConstants.isPreConfigureSSL();
    if (requiresSslConfiguration) { configureSSL(); }

    tlsClientParameters.setKeyManagers(keyManagers);
    tlsClientParameters.setTrustManagers(trustManagers);
  }

  private void configureWss4j(ServiceConfigInfo<?> config) {
    configureWss4jOut(config.getClient().getEndpoint());
  }

  private void configureWss4jOut(Endpoint cxfEndpoint) {
    val props = new HashMap<String, Object>();

    props.put(SIG_CRYPTO_PROPERTIES_REF, sigCryptoProperties());
    props.put(WSHandlerConstants.SIG_PROP_REF_ID, SIG_CRYPTO_PROPERTIES_REF);
    props.put(WSHandlerConstants.SIGNATURE_USER, gcssFeedConstants.getSignatureAlias());
    props.put(WSHandlerConstants.SIGNATURE_PARTS, gcssFeedConstants.getSignatureParts());
    props.put(WSHandlerConstants.SIG_KEY_ID, gcssFeedConstants.getSignatureKeyId());
    props.put(WSHandlerConstants.TTL_TIMESTAMP, gcssFeedConstants.getTtl());
    props.put(WSHandlerConstants.TTL_FUTURE_TIMESTAMP, gcssFeedConstants.getFutureTtl());
    props.put(WSHandlerConstants.PW_CALLBACK_REF, gcssClientWsPasswordCallback);

    if (gcssFeedConstants.isEncryptionEnabled()) {
      props.put(WSHandlerConstants.ACTION, String.format("%s %s %s", WSHandlerConstants.TIMESTAMP, WSHandlerConstants.SIGNATURE, WSHandlerConstants.ENCRYPT));
      props.put(ENC_CRYPTO_PROPERTIES_REF, encCryptoProperties());
      props.put(WSHandlerConstants.ENC_PROP_REF_ID, ENC_CRYPTO_PROPERTIES_REF);
      props.put(WSHandlerConstants.ENCRYPTION_USER, gcssFeedConstants.getEncryptionAlias());
      props.put(WSHandlerConstants.ENCRYPTION_PARTS, gcssFeedConstants.getEncryptionParts());
      props.put(WSHandlerConstants.ENC_KEY_ID, gcssFeedConstants.getEncryptionKeyId());

      if (StringUtils.isNotBlank(gcssFeedConstants.getEncryptionSymAlgorithm())) {
        props.put(WSHandlerConstants.ENC_SYM_ALGO, gcssFeedConstants.getEncryptionSymAlgorithm());
      }
    }
    else {
      props.put(WSHandlerConstants.ACTION, String.format("%s %s", WSHandlerConstants.TIMESTAMP, WSHandlerConstants.SIGNATURE));
    }

    cxfEndpoint.getOutInterceptors().add(new WSS4JOutInterceptor(props));
  }

  private Properties sigCryptoProperties() {
    val keyStoreFile = gcssFeedConstants.getKeyStoreFile();
    val keyStorePass = gcssFeedConstants.getKeyStorePass();
    val keyStoreType = gcssFeedConstants.getKeyStoreType();
    val keyStoreAlias = gcssFeedConstants.getSignatureAlias();
    val trustStoreFile = gcssFeedConstants.getTrustStoreFile();
    val trustStorePass = gcssFeedConstants.getTrustStorePass();
    val trustStoreType = gcssFeedConstants.getTrustStoreType();
    val cryptoProvider = gcssFeedConstants.getCryptoProvider();

    val props = new Properties();
    props.put("org.apache.wss4j.crypto.provider", cryptoProvider);
    props.put("org.apache.wss4j.crypto.merlin.keystore.file", keyStoreFile);
    props.put("org.apache.wss4j.crypto.merlin.keystore.password", keyStorePass);
    props.put("org.apache.wss4j.crypto.merlin.keystore.type", keyStoreType);
    props.put("org.apache.wss4j.crypto.merlin.keystore.alias", keyStoreAlias);

    if (StringUtils.isNotBlank(trustStoreFile)) {
      props.put("org.apache.wss4j.crypto.merlin.truststore.file", trustStoreFile);
      props.put("org.apache.wss4j.crypto.merlin.truststore.password", trustStorePass);
      props.put("org.apache.wss4j.crypto.merlin.truststore.type", trustStoreType);
    }

    if (log.isDebugEnabled()) {
      safeDebugCryptoProperties(props);
    }

    return props;
  }

  private Properties encCryptoProperties() {
    val keyStoreFile = gcssFeedConstants.getKeyStoreFile();
    val keyStorePass = gcssFeedConstants.getKeyStorePass();
    val keyStoreType = gcssFeedConstants.getKeyStoreType();
    val keyStoreAlias = gcssFeedConstants.getEncryptionAlias();
    val cryptoProvider = gcssFeedConstants.getCryptoProvider();

    val props = new Properties();
    props.put("org.apache.wss4j.crypto.provider", cryptoProvider);
    props.put("org.apache.wss4j.crypto.merlin.keystore.file", keyStoreFile);
    props.put("org.apache.wss4j.crypto.merlin.keystore.password", keyStorePass);
    props.put("org.apache.wss4j.crypto.merlin.keystore.type", keyStoreType);
    props.put("org.apache.wss4j.crypto.merlin.keystore.alias", keyStoreAlias);

    return props;
  }

  private void configureProxy(HTTPConduit httpConduit) {
    val proxyEnabled = gcssFeedConstants.isProxyEnabled();
    if (proxyEnabled) {
      log.debug("proxy configuration: configuring proxy server...");

      val proxyServerAddress = gcssFeedConstants.getProxyServerAddress();
      val proxyServerType = StringUtils.equalsIgnoreCase("socks", gcssFeedConstants.getProxyServerType()) ? ProxyServerType.SOCKS : ProxyServerType.HTTP;
      val proxyServerPort = gcssFeedConstants.getProxyServerPort();
      val proxyServerUserName = gcssFeedConstants.getProxyServerUserName();
      val proxyServerPassword = gcssFeedConstants.getProxyServerPassword();
      val httpClientPolicy = httpConduit.getClient();

      if (proxyServerAddress != null && proxyServerPort != 0) {
        log.debug("using proxy: addr:{}, port: {}, type: {}", proxyServerAddress, proxyServerPort, proxyServerType);
        httpClientPolicy.setProxyServer(proxyServerAddress);
        httpClientPolicy.setProxyServerPort(proxyServerPort);
        httpClientPolicy.setProxyServerType(proxyServerType);

        if (StringUtils.isNotBlank(proxyServerUserName) && StringUtils.isNotBlank(proxyServerPassword)) {
          log.debug("applying proxy username and password...");
          val authorizationPolicy = new ProxyAuthorizationPolicy();
          authorizationPolicy.setUserName(proxyServerUserName);
          authorizationPolicy.setPassword(proxyServerPassword);
          httpConduit.setProxyAuthorization(authorizationPolicy);
        }
      }
      else {
        log.warn("proxy server configuration aborted - no address and/or port provided.");
      }
    }
    else {
      log.debug("proxy configuration: proxy disabled.");
    }
  }

  private void configureContext(Map<String, Object> context, String endPointAddress) {
    context.put("set-jaxb-validation-event-handler", "false");
    context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointAddress);
  }

  @SuppressWarnings("deprecation")
  private void configureInterceptors(Client client) {
    val compressionEnabled = gcssFeedConstants.isCompressionEnabled();
    val fastInfoSetEnabled = gcssFeedConstants.isFastInfoSetEnabled();
    val cxfEndpoint = client.getEndpoint();

    if (compressionEnabled) {
      cxfEndpoint.getInInterceptors().add(new GZIPInInterceptor());
    }

    if (fastInfoSetEnabled) {
      cxfEndpoint.getInInterceptors().add(new FIStaxInInterceptor());
      cxfEndpoint.getOutInterceptors().add(new FIStaxOutInterceptor());
    }
  }

  private void safeDebugCryptoProperties(Properties cryptoProperties) {
    val cleansedProperties = new Properties();
    cleansedProperties.putAll(cryptoProperties);
    cleansedProperties.remove("org.apache.wss4j.crypto.merlin.keystore.password");
    cleansedProperties.remove("org.apache.wss4j.crypto.merlin.truststore.password");
    log.debug("Setting the SOAP Message crypto properties to the following values: {}", cleansedProperties);
  }
}
