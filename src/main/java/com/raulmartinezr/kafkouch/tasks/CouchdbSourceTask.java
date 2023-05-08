package com.raulmartinezr.kafkouch.tasks;

import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

public class CouchdbSourceTask extends SourceTask {

  @Override
  public String version() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'version'");
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'poll'");
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

}
