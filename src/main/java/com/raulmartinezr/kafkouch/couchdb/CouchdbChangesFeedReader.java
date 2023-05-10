package com.raulmartinezr.kafkouch.couchdb;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbAuthMethod;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbClientBuilder;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntryConverter;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CouchdbChangesFeedReader {

  private CouchdbClient client;
  private String since;
  private FeedType feed;
  private int heartbeat;
  private int timeout;
  private int sleepTime = 1000;
  private boolean stopReading;
  private BlockingQueue<ContinuousFeedEntry> changesQueue;
  private ContinuousFeedEntryConverter converter;
  private OrderedBuffer<ContinuousFeedEntry> buffer;
  private long maxBufferTimeInterval;
  private int maxBufferSize;

  /**
   * Instantiates a new CouchdbClient.
   */

  public CouchdbChangesFeedReader(CouchdbChangesFeedReaderBuilder builder) {

    this.client = new CouchdbClientBuilder().setUrl(builder.getUrl())
        .setUsername(builder.getUsername()).setPassword(builder.getPassword())
        .setAuthMethod(builder.getAuthMethod()).setConnect(builder.isConnect()).build();

    this.since = builder.getSince();
    this.feed = builder.getFeed();
    this.heartbeat = builder.getHeartbeat();
    this.timeout = builder.getTimeout();
    this.changesQueue = builder.getChangesQueue();

    this.converter = new ContinuousFeedEntryConverter();
    this.buffer = null;
    this.maxBufferTimeInterval = builder.getMaxBufferTimeInterval();
    this.maxBufferSize = builder.getMaxBufferSize();

  }

  public void startReadingChangesFeed() {
    String globalChangesFeedUrl =
        this.client.getUrl() + " /_db_updates?feed=" + this.feed.toString() + "&heartbeat="
            + this.heartbeat + "&timeout=" + this.timeout + "&since=" + this.since;
    Request request = new Request.Builder().url(globalChangesFeedUrl).build();

    this.buffer = new OrderedBuffer<ContinuousFeedEntry>(this.maxBufferSize,
        this.maxBufferTimeInterval, this.changesQueue); // Buffer up to 10 elements or flush every 5

    WebSocketListener listener = new WebSocketListener() {
      @Override
      public void onOpen(WebSocket webSocket, Response response) {
        System.out.println("Connected to CouchDB changes feed.");
        buffer.start();
      }

      @Override
      public void onMessage(WebSocket webSocket, String text) {
        System.out.println("Received change: " + text);
        ContinuousFeedEntry feedEntry = converter.convertToJavaObject(text);
        buffer.add(feedEntry);
      }

      @Override
      public void onClosing(WebSocket webSocket, int code, String reason) {
        System.out.println("Closing connection to CouchDB changes feed.");
        buffer.stop();
      }

      @Override
      public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        System.err.println("Error: " + t.getMessage());
      }
    };
    WebSocket webSocket = this.client.getHttpClient().newWebSocket(request, listener);
    while (!this.stopReading) {
      try {
        Thread.sleep(this.sleepTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
        break;
      }

    }
    webSocket.close(1000, "Closing WebSocket");
    this.client.getHttpClient().dispatcher().executorService().shutdown();

  }

  public void stopReadingChangesFeed() {
    this.stopReading = true;
  }

  public enum FeedType {
    NORMAL, LONGPOOLL, CONTINUOUS, EVENTSOURCE
  }

  public static class CouchdbChangesFeedReaderBuilder {

    private BlockingQueue<ContinuousFeedEntry> changesQueue;
    private int heartbeat = 60000;
    private int timeout = 60000;
    private FeedType feed = FeedType.CONTINUOUS;
    private String since = "now";
    private boolean connect = false;
    private String url;
    private String username;
    private String password;
    private CouchdbAuthMethod authMethod;
    private int maxBufferSize = 100;
    private long maxBufferTimeInterval = 500; // ms

    public CouchdbChangesFeedReaderBuilder() {}

    protected void validate() {
      /**
       * Validates all required inputs are defined and not empty.
       */
      assert this.changesQueue != null && (this.changesQueue instanceof BlockingQueue)
          : "changesQueue must be an instance of BlockingQueue";
      assert this.url != null && this.url.isEmpty() : "url must not be empty";
      assert this.username != null && this.username.isEmpty() : "username must not be empty";
      assert this.password != null && this.password.isEmpty() : "password must not be empty";
      assert this.authMethod != null && (this.authMethod instanceof CouchdbAuthMethod)
          : "authMethod must be an instance of CouchdbAuthMethod";
      assert this.feed != null && (this.feed instanceof FeedType)
          : "feed must be an instance of FeedType";
    }

    public CouchdbChangesFeedReader build() {
      /**
       * Builds a CouchdbClient instance.
       */
      this.validate();
      CouchdbChangesFeedReader feedReader = new CouchdbChangesFeedReader(this);
      return feedReader;
    }

    /**
     * @param connect the connect to set
     */
    public CouchdbChangesFeedReaderBuilder setConnect(boolean connect) {
      this.connect = connect;
      return this;
    }

    /**
     * @param url the url to set
     */
    public CouchdbChangesFeedReaderBuilder setUrl(String url) {
      this.url = url;
      return this;
    }

    /**
     * @param username the username to set
     * @return
     */
    public CouchdbChangesFeedReaderBuilder setUsername(String username) {
      this.username = username;
      return this;
    }

    /**
     * @param password the password to set
     */
    public CouchdbChangesFeedReaderBuilder setPassword(String password) {
      this.password = password;
      return this;
    }

    /**
     * @param authMethod the authMethod to set
     */
    public CouchdbChangesFeedReaderBuilder setAuthMethod(CouchdbAuthMethod authMethod) {
      this.authMethod = authMethod;
      return this;
    }

    /**
     * @param changesQueue the changesQueue to set
     */
    public CouchdbChangesFeedReaderBuilder setChangesQueue(
        BlockingQueue<ContinuousFeedEntry> changesQueue) {
      this.changesQueue = changesQueue;
      return this;
    }

    /**
     * @param heartbeat the heartbeat to set
     */
    public CouchdbChangesFeedReaderBuilder setHeartbeat(int heartbeat) {
      this.heartbeat = heartbeat;
      return this;
    }

    /**
     * @param timeout the timeout to set
     */
    public CouchdbChangesFeedReaderBuilder setTimeout(int timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * @param since the since to set
     */
    public CouchdbChangesFeedReaderBuilder setSince(String since) {
      this.since = since;
      return this;
    }

    /**
     * @param feed the feed to set
     */
    public CouchdbChangesFeedReaderBuilder setFeed(FeedType feed) {
      this.feed = feed;
      return this;
    }

    /**
     * @param maxBufferSize the maxBufferSize to set
     */
    public CouchdbChangesFeedReaderBuilder setMaxBufferSize(int maxBufferSize) {
      this.maxBufferSize = maxBufferSize;
      return this;
    }

    /**
     * @param maxBufferTimeInterval the maxBufferTimeInterval to set
     */
    public CouchdbChangesFeedReaderBuilder setMaxBufferTimeInterval(long maxBufferTimeInterval) {
      this.maxBufferTimeInterval = maxBufferTimeInterval;
      return this;
    }

    /**
     * @return the connect
     */
    public boolean isConnect() {
      return connect;
    }

    /**
     * @return the url
     */
    public String getUrl() {
      return url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
      return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
      return password;
    }

    /**
     * @return the authMethod
     */
    public CouchdbAuthMethod getAuthMethod() {
      return authMethod;
    }

    /**
     * @return the changesQueue
     */
    public BlockingQueue<ContinuousFeedEntry> getChangesQueue() {
      return changesQueue;
    }

    /**
     * @return the heartbeat
     */
    public int getHeartbeat() {
      return heartbeat;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
      return timeout;
    }

    /**
     * @return the since
     */
    public String getSince() {
      return since;
    }

    /**
     * @return the feed
     */
    public FeedType getFeed() {
      return feed;
    }

    /**
     * @return the maxBufferSize
     */
    public int getMaxBufferSize() {
      return maxBufferSize;
    }

    /**
     * @return the maxBufferTimeInterval
     */
    public long getMaxBufferTimeInterval() {
      return maxBufferTimeInterval;
    }

  }

  public class OrderedBuffer<T> {
    private BlockingDeque<T> buffer;
    private int maxBufferSize;
    private long maxTimeInterval;
    private Timer flushTimer;
    private BlockingQueue<T> queue;

    public OrderedBuffer(int maxBufferSize, long maxTimeInterval, BlockingQueue<T> queue) {
      this.buffer = new LinkedBlockingDeque<>();
      this.maxBufferSize = maxBufferSize;
      this.maxTimeInterval = maxTimeInterval;
      this.flushTimer = new Timer();
      this.queue = queue;
    }

    public void start() {
      flushTimer.schedule(new FlushTask(), maxTimeInterval, maxTimeInterval);
    }

    public void stop() {
      flushTimer.cancel();
      flush();
    }

    public void add(T data) {
      buffer.offer(data);
      if (buffer.size() >= maxBufferSize) {
        flush();
      }
    }

    private void flush() {
      while (!buffer.isEmpty()) {
        try {
          T data = buffer.pollFirst(maxTimeInterval, TimeUnit.MILLISECONDS);
          if (data != null) {
            queue.put(data);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

    private class FlushTask extends TimerTask {
      @Override
      public void run() {
        flush();
      }
    }
  }

}
