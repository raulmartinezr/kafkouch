package com.raulmartinezr.kafkouch.connectors;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raulmartinezr.kafkouch.connectors.config.ConfigHelper;
import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceConfig;
import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader;
import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader.CouchdbChangesFeedReaderBuilder;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbAuthMethod;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;

public class CouchdbSourceConnector extends SourceConnector {

  private static final Logger log = LoggerFactory.getLogger(CouchdbSourceConnector.class);

  BlockingQueue<ContinuousFeedEntry> changesQueue = null;
  CouchdbChangesFeedReader couchdbChangesFeedReader = null;
  ReconfigurationThread reconfigurationThread = null;
  private Map<String, String> configProperties;

  @Override
  public String version() {
    return this.readVersionFromManifest();
  }

  @Override
  public ConfigDef config() {
    return ConfigHelper.define(CouchdbSourceConfig.class);
  }

  @Override
  public void start(Map<String, String> properties) {
    /*
     * Starts connector
     */
    this.configProperties = properties;
    CouchdbSourceConfig config =
        ConfigHelper.parse(CouchdbSourceConfig.class, this.configProperties);
    this.setLogLevel(config.logLevel().toString());

    changesQueue = new LinkedBlockingQueue<ContinuousFeedEntry>();
    this.couchdbChangesFeedReader = new CouchdbChangesFeedReaderBuilder().setUrl(config.url())
        .setUsername(config.username()).setPassword(config.password().value())
        .setAuthMethod(config.authMethod()).setConnect(true).setSince(properties.get("since"))
        .setChangesQueue(changesQueue).build();

    this.couchdbChangesFeedReader.startReadingChangesFeed();
    this.manageReconfigurationsBasedOnChanges();
  }

  private void manageReconfigurationsBasedOnChanges() {
    this.reconfigurationThread = new ReconfigurationThread(this.changesQueue);
    this.reconfigurationThread.start();
  }

  @Override
  public void stop() {
    this.couchdbChangesFeedReader.stopReadingChangesFeed();
    this.reconfigurationThread.stopThread();
    // Wait for the thread to complete (optional)
    try {
      this.reconfigurationThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Class<? extends Task> taskClass() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'taskClass'");
  }

  @Override
  public List<Map<String, String>> taskConfigs(int arg0) {
    /*
     * Each entry is the configuration for a single task. Then we can control here the numner of
     * tasks and the configuration for each one of them.
     */
    throw new UnsupportedOperationException("Unimplemented method 'taskConfigs'");
  }

  private String readVersionFromManifest() {
    CodeSource codeSource = CouchdbSourceConnector.class.getProtectionDomain().getCodeSource();
    if (codeSource != null) {
      URL jarUrl = codeSource.getLocation();
      try (JarFile jarFile = new JarFile(jarUrl.getPath())) {
        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
          Attributes attributes = manifest.getMainAttributes();
          return attributes.getValue("Implementation-Version");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private void setLogLevel(String logLevel) {
    if (logLevel == null || !logLevel.matches("(TRACE|DEBUG|INFO|WARN|ERROR)")) {
      log.warn("Invalid log level: " + logLevel);
    } else {
      // Set log level in MDC context
      MDC.put("logLevel", logLevel);
    }

  }

}


class ReconfigurationThread extends Thread {
  private volatile boolean shouldStop = false;

  private BlockingQueue<?> changesQueue = null;

  /**
   *
   */
  public ReconfigurationThread(BlockingQueue<?> changesQueue) {
    this.changesQueue = changesQueue;
  }

  public void stopThread() {
    shouldStop = true;
  }

  @Override
  public void run() {
    while (!shouldStop) {
      // Perform the thread's main logic here
      try {
        Object record = this.changesQueue.take(); // Wait for a record to be available
      } catch (InterruptedException e) {
        // Handle interrupted exception (if required)
        Thread.currentThread().interrupt();
      }
    }

  }

}
