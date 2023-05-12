package com.raulmartinezr.kafkouch.connectors.config.common;

import org.apache.kafka.common.config.types.Password;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Dependents;
import com.raulmartinezr.kafkouch.connectors.config.annotation.DisplayName;
import com.raulmartinezr.kafkouch.connectors.config.annotation.EnvironmentVariable;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Width;

import static org.apache.kafka.common.config.ConfigDef.Width.LONG;

public interface SecurityConfig {
  /**
   * Use secure connection to Couchdb Server.
   * <p>
   * If true, you must also tell the connector which certificate to trust. Specify a certificate
   * file with 'couchdb.trust.certificate.path', or a Java keystore file with
   * 'couchdb.trust.store.path' and 'couchdb.trust.store.password'.
   */
  @Dependents({"couchdb.trust.certificate.path", "couchdb.trust.store.path",
      "couchdb.trust.store.password", "couchdb.enable.hostname.verification",
      "couchdb.client.certificate.path", "couchdb.client.certificate.password",})
  @Default("false")
  @DisplayName("Enable TLS")
  boolean enableTls();

  /**
   * Set this to `false` to disable TLS hostname verification for Couchdb connections. Less secure,
   * but might be required if for some reason you can't issue a certificate whose Subject
   * Alternative Names match the hostname used to connect to the server. Only disable if you
   * understand the impact and can accept the risks.
   */
  @Default("true")
  @DisplayName("Enable TLS Hostname Verification")
  boolean enableHostnameVerification();

  /**
   * Absolute filesystem path to the Java keystore holding the CA certificate used by Couchdb
   * Server.
   * <p>
   * If you want to use a PEM file instead of a Java keystore, specify
   * `couchdb.trust.certificate.path` instead.
   */
  @Width(LONG)
  @Default
  String trustStorePath();

  /**
   * Password for accessing the trust store.
   */
  @EnvironmentVariable("KAFKA_COUCHDB_TRUST_STORE_PASSWORD")
  @Default
  Password trustStorePassword();

  /**
   * Absolute filesystem path to the PEM file containing the CA certificate used by Couchdb Server.
   * <p>
   * If you want to use a Java keystore instead of a PEM file, specify `couchdb.trust.store.path`
   * instead.
   */
  @Width(LONG)
  @Default
  String trustCertificatePath();

  /**
   * Absolute filesystem path to a Java keystore or PKCS12 bundle holding the private key and
   * certificate chain to use for client certificate authentication (mutual TLS).
   */
  @Width(LONG)
  @Default
  String clientCertificatePath();

  /**
   * Password for accessing the client certificate.
   */
  @EnvironmentVariable("KAFKA_COUCHDB_CLIENT_CERTIFICATE_PASSWORD")
  @Default
  Password clientCertificatePassword();
}
