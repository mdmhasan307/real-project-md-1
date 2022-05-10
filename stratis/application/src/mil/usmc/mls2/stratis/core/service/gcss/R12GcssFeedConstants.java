package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.Getter;
import lombok.ToString;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Component
@ToString(exclude = {"keyStorePass", "trustStorePass", "proxyServerPassword"})
@Profile(Profiles.LEGACY)
@SuppressWarnings("unused")
public class R12GcssFeedConstants {

  @Value("${stratis.ws.gcss.r12.baseUrl:GCSSMC_BASEURL_NOT_CONFIGURED}")
  private String endpointBaseUrl;

  @Value("${stratis.ws.gcss.r12.urlPath:/gateway/services/}")
  private String endpointUrlPath;

  @Value("${stratis.ws.gcss.r12.connectionTimeout:800000}")
  private int connectionTimeoutInMilliseconds;

  @Value("${stratis.ws.gcss.r12.receiveTimeout:800000}")
  private int receiveTimeoutInMilliseconds;

  @Value("${stratis.ws.gcss.r12.retryWaitPeriod:30000}")
  private int retryWaitPeriodInMilliseconds;

  @Value("${stratis.ws.gcss.r12.maxPageSize:10000}")
  private int maxPageSize;

  @Value("${stratis.ws.gcss.r12.maxPageAttempts:10}")
  private int maxPageAttempts;

  @Value("${stratis.ws.gcss.r12.sslChecks.enabled:true}")
  private boolean sslChecksEnabled;

  @Value("${stratis.ws.gcss.r12.ssl.preconfigure:true}")
  private boolean preConfigureSSL;

  @Value("${stratis.ws.gcss.r12.compression.enabled:true}")
  private boolean compressionEnabled;

  @Value("${stratis.ws.gcss.r12.fastInfoset.enabled:true}")
  private boolean fastInfoSetEnabled;

  @Value("${stratis.ws.gcss.r12.soap.logging.enabled:false}")
  private boolean soapLoggingEnabled;

  @Value("${stratis.ws.gcss.r12.cryptoProvider:org.apache.wss4j.common.crypto.Merlin}")
  private String cryptoProvider;

  @Value("${stratis.ws.gcss.r12.keyStoreType:jks}")
  private String keyStoreType;

  @Value("${stratis.ws.gcss.r12.keyStorePass:}")
  private String keyStorePass;

  @Value("${stratis.ws.gcss.r12.keyStoreFile:}")
  private String keyStoreFile;

  @Value("${stratis.ws.gcss.r12.trustStoreFile:}")
  private String trustStoreFile;

  @Value("${stratis.ws.gcss.r12.trustStorePass:}")
  private String trustStorePass;

  @Value("${stratis.ws.gcss.r12.trustStoreType:jks}")
  private String trustStoreType;

  @Value("${stratis.ws.gcss.r12.proxy.enabled:false}")
  private boolean proxyEnabled;

  @Value("${stratis.ws.gcss.r12.proxy.serverAddress:}")
  private String proxyServerAddress;

  @Value("${stratis.ws.gcss.r12.proxy.serverPort:0}")
  private int proxyServerPort;

  @Value("${stratis.ws.gcss.r12.proxy.serverType:http}")
  private String proxyServerType;

  @Value("${stratis.ws.gcss.r12.proxy.serverUserName:}")
  private String proxyServerUserName;

  @Value("${stratis.ws.gcss.r12.proxy.serverPassword:}")
  private String proxyServerPassword;

  @Value("${stratis.ws.gcss.r12.signature.alias:client}")
  private String signatureAlias;

  @Value("${stratis.ws.gcss.r12.signature.parts:{}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;{}{http://schemas.xmlsoap.org/soap/envelope/}Body}")
  private String signatureParts;

  @Value("${stratis.ws.gcss.r12.signature.keyId:DirectReference}") // DirectReference is BinarySecurityToken
  private String signatureKeyId;

  @Value("${stratis.ws.gcss.r12.encryption.enabled:true}")
  private boolean encryptionEnabled;

  @Value("${stratis.ws.gcss.r12.encryption.alias:server}")
  private String encryptionAlias;

  @Value("${stratis.ws.gcss.r12.encryption.symAlgorithm:http://www.w3.org/2001/04/xmlenc#aes128-cbc}")
  private String encryptionSymAlgorithm;

  @Value("${stratis.ws.gcss.r12.encryption.parts:{}{http://www.w3.org/2000/09/xmldsig#}Signature;{}{http://schemas.xmlsoap.org/soap/envelope/}Body}")
  private String encryptionParts;

  @Value("${stratis.ws.gcss.r12.encryption.keyId:DirectReference}")
  // both seem to work: IssuerSerial, DirectReference results in the use of BinarySecurityToken
  private String encryptionKeyId;

  // KEEP AS STRING
  @Value("${stratis.ws.gcss.r12.wss.ttl:300}")
  private String ttl;

  // KEEP AS STRING
  @Value("${stratis.ws.gcss.r12.wss.futureTtl:300}")
  private String futureTtl;

  @Value("${stratis.gcss.refresh.gatherStats:true}")
  private Boolean stagingGcssRefreshGatherStats;

  @Value("${stratis.xde.refresh.gatherStats:true}")
  private Boolean xdeRefreshGatherStats;

  @Value("${stratis.xde.refresh.br2.bulk.enabled:false}")
  private Boolean xdeRefreshBr2UseBulk;

  @Value("${stratis.xde.refresh.br2.bulk.pageSize:10000}")
  private Integer xdeRefreshBr2BulkPageSize;

  @Value("${stratis.xde.refresh.transform.bulk.enabled:false}")
  private Boolean xdeRefreshTransformationsUseBulk;

  @Value("${stratis.xde.refresh.transform.bulk.pageSize:10000}")
  private Integer xdeRefreshTransformationsBulkPageSize;
}
