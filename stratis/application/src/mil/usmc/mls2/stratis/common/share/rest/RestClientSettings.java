package mil.usmc.mls2.stratis.common.share.rest;

public interface RestClientSettings {

  int connectTimeoutSeconds();

  int readTimeoutSeconds();

  boolean keyMaterialEnabled();

  boolean trustMaterialTrustAll();

  String keyStoreFile();

  String keyStoreType();

  String keyStorePass();

  String keyStoreAlias();

  String trustStoreFile();

  String trustStoreType();

  String trustStorePass();
}
