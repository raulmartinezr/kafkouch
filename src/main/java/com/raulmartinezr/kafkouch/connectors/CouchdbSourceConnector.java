package com.raulmartinezr.kafkouch.connectors;

import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

public class CouchdbSourceConnector extends SourceConnector {

  @Override
  public String version() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'version'");
  }

  @Override
  public ConfigDef config() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'config'");
  }

  @Override
  public void start(Map<String, String> arg0) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'start'");
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'stop'");
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

}
