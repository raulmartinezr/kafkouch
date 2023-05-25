package com.raulmartinezr.kafkouch.couchdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonValue;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbAuthMethod;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbClientBuilder;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntryConverter;
import com.raulmartinezr.kafkouch.util.ThreadSafeSetHandler;

import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class CouchdbChangesFeedReader {

  private static final Logger log = LoggerFactory.getLogger(CouchdbChangesFeedReader.class);

  private CouchdbClient client;
  private String since;
  private FeedType feed;
  private long heartbeat;
  private long timeout;
  // private int sleepTime = 1000;
  // private boolean stopReading;
  private BlockingQueue<ContinuousFeedEntry> changesQueue;
  private ThreadSafeSetHandler<String> changedDatabases;
  private ContinuousFeedEntryConverter converter;
  private OrderedBuffer buffer;
  private long maxBufferTimeInterval;
  private int maxBufferSize;

  private CountDownLatch shutdownLatch;

  /**
   * Instantiates a new CouchdbClient.
   */

  public CouchdbChangesFeedReader(CouchdbChangesFeedReaderBuilder builder) {

    this.client =
        new CouchdbClientBuilder().setUrl(builder.getUrl()).setUsername(builder.getUsername())
            .setPassword(builder.getPassword()).setAuthMethod(builder.getAuthMethod())
            .setConnect(builder.isConnect()).setReadTimeout(builder.getReadTimeout()).build();

    this.since = builder.getSince();
    this.feed = builder.getFeed();
    this.heartbeat = builder.getHeartbeat();
    this.timeout = builder.getTimeout();
    this.changesQueue = builder.getChangesQueue();
    this.changedDatabases = builder.getChangedDatabases();
    this.converter = new ContinuousFeedEntryConverter();
    this.buffer = null;
    this.maxBufferTimeInterval = builder.getMaxBufferTimeInterval();
    this.maxBufferSize = builder.getMaxBufferSize();

    this.shutdownLatch = new CountDownLatch(1);

  }

  public void startReadingChangesFeed() {
    String globalChangesFeedUrl = this.client.getUrl() + "/_db_updates?feed=" + this.feed.value
        + "&heartbeat=" + this.heartbeat + "&timeout=" + this.timeout + "&since=" + this.since;

    List<Cookie> cookies =
        this.client.getCookieJar().loadForRequest(HttpUrl.parse(this.client.getUrl()));
    Headers.Builder headerBuilder = new Headers.Builder();

    for (Cookie cookie : cookies) {
      headerBuilder.add("Cookie", cookie.name() + "=" + cookie.value());
    }

    Request request =
        new Request.Builder().url(globalChangesFeedUrl).headers(headerBuilder.build()).build();

    this.buffer = new OrderedBuffer(this.maxBufferSize, this.maxBufferTimeInterval,
        this.changesQueue, this.changedDatabases); // Buffer up to 10 elements or flush
                                                   // every 5

    try (Response response = this.client.getHttpClient().newCall(request).execute()) {
      if (response.isSuccessful()) {
        this.buffer.start();
        InputStream inputStream = response.body().byteStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null && shutdownLatch.getCount() > 0) {
          // Process each line of the response
          // log.info(line);
          if (!line.isBlank()) {
            ContinuousFeedEntry feedEntry = converter.convertToJavaObject(line);
            log.info("{}|{} -> {}", feedEntry.getType(), feedEntry.getDbName(), feedEntry.getSeq());
            buffer.add(feedEntry);
            // log.info("Change seq buffered: {}", feedEntry.getSeq());
            // this.changedDatabases.add(feedEntry.getDbName()); -> Not here, in buffer
            // flush

          }
        }

        // Remember to close the input stream and reader when you're done
        reader.close();
        inputStream.close();
        this.buffer.stop();
      } else {
        log.error("Error: " + response.code() + " " + response.message());
        printFeedRequest(request);
      }
    } catch (IOException e) {
      log.error("Request failed: " + e.getMessage(), e);
      printFeedRequest(request);
    }

    // WebSocketListener listener = new WebSocketListener() {
    // @Override
    // public void onOpen(WebSocket webSocket, Response response) {
    // System.out.println("Connected to CouchDB changes feed.");
    // buffer.start();
    // }

    // @Override
    // public void onMessage(WebSocket webSocket, String text) {
    // System.out.println("Received change: " + text);
    // ContinuousFeedEntry feedEntry = converter.convertToJavaObject(text);
    // buffer.add(feedEntry);
    // }

    // @Override
    // public void onClosing(WebSocket webSocket, int code, String reason) {
    // System.out.println("Closing connection to CouchDB changes feed.");
    // buffer.stop();
    // }

    // @Override
    // public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    // System.err.println("Error: " + t.getMessage());
    // printFeedRequest(request);
    // }
    // };
    // WebSocket webSocket = this.client.getHttpClient().newWebSocket(request,
    // listener);
    // while (!this.stopReading) {
    // try {
    // Thread.sleep(this.sleepTime);
    // } catch (InterruptedException e) {
    // break;
    // }

    // }
    // webSocket.close(1000, "Closing WebSocket");
    this.client.getHttpClient().dispatcher().executorService().shutdown();

  }

  private void printFeedRequest(Request request) {
    log.info("Request URL: " + request.url());
    log.info("Request Method: " + request.method());
    log.info("Headers: ");
    Headers headers = request.headers();
    for (String name : headers.names()) {
      log.info(name + ": " + headers.get(name));

    }
    RequestBody requestBody = request.body();
    if (requestBody != null) {
      log.info("Request Body: " + requestBodyToString(requestBody));
    }
  }

  private static String requestBodyToString(RequestBody requestBody) {
    Buffer buffer = new Buffer();
    try {
      requestBody.writeTo(buffer);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return buffer.readUtf8();
  }

  public void stopReadingChangesFeed() {
    log.info("Shutting down couchdb changes feed reader");
    shutdownLatch.countDown();
  }

  public enum FeedType {
    NORMAL("normal"), LONGPOOLL("longpoll"), CONTINUOUS("continuous"), EVENTSOURCE("eventsource");

    private final String value;

    FeedType(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }
  }

  public static class CouchdbChangesFeedReaderBuilder {

    private BlockingQueue<ContinuousFeedEntry> changesQueue;
    private ThreadSafeSetHandler<String> changedDatabases;
    private long heartbeat = 60000;
    private long timeout = 60000;
    private long readTimeout = 60000;
    private FeedType feed = FeedType.CONTINUOUS;
    private String since = "now";
    private boolean connect = true;
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
      assert this.changedDatabases != null
          && (this.changedDatabases instanceof ThreadSafeSetHandler)
          : "changedDatabases must be an instance of ThreadSafeSetHandler";
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
    public CouchdbChangesFeedReaderBuilder setHeartbeat(long heartbeat) {
      this.heartbeat = heartbeat;
      return this;
    }

    /**
     * @param timeout the timeout to set
     */
    public CouchdbChangesFeedReaderBuilder setTimeout(long timeout) {
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
     * @param readTimeout the readTimeout to set
     */
    public CouchdbChangesFeedReaderBuilder setReadTimeout(long readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    public CouchdbChangesFeedReaderBuilder setChangedDatabases(
        ThreadSafeSetHandler<String> changedDatabases) {
      this.changedDatabases = changedDatabases;
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
    public long getHeartbeat() {
      return heartbeat;
    }

    /**
     * @return the timeout
     */
    public long getTimeout() {
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

    /**
     * @return the readTimeout
     */
    public long getReadTimeout() {
      return readTimeout;
    }

    /**
     * @return the changedDatabases
     */
    public ThreadSafeSetHandler<String> getChangedDatabases() {
      return changedDatabases;
    }

  }

  public class OrderedBuffer {

    private final Logger log = LoggerFactory.getLogger(OrderedBuffer.class);

    private BlockingDeque<ContinuousFeedEntry> buffer;
    private int maxBufferSize;
    private long maxTimeInterval;
    private Timer flushTimer;
    private BlockingQueue<ContinuousFeedEntry> queue;
    private ThreadSafeSetHandler<String> classifier;

    public OrderedBuffer(int maxBufferSize, long maxTimeInterval,
        BlockingQueue<ContinuousFeedEntry> queue, ThreadSafeSetHandler<String> classifier) {
      this.buffer = new LinkedBlockingDeque<>();
      this.maxBufferSize = maxBufferSize;
      this.maxTimeInterval = maxTimeInterval;
      this.flushTimer = new Timer();
      this.queue = queue;
      this.classifier = classifier;

    }

    public void start() {
      log.info("Starting OrderedBuffer flush timer");
      flushTimer.schedule(new FlushTask(), maxTimeInterval, maxTimeInterval);
    }

    public void stop() {
      log.info("Stopping OrderedBuffer flush timer");
      flushTimer.cancel();
      flush();
    }

    public void reset() {
      log.info("Reseting OrderedBuffer flush timer");
      flushTimer.cancel();
      flushTimer = new Timer();
      flushTimer.schedule(new FlushTask(), maxTimeInterval, maxTimeInterval);
    }

    public void add(ContinuousFeedEntry data) {
      buffer.offer(data);
      if (buffer.size() >= maxBufferSize) {
        log.info("New size {}. Max buffer size of {} reached, flushing...", buffer.size(),
            maxBufferSize);
        flush();
        reset();
      }
    }

    private void flush() {
      while (!buffer.isEmpty()) {
        try {
          ContinuousFeedEntry data = buffer.pollFirst(maxTimeInterval, TimeUnit.MILLISECONDS);
          if (data != null) {
            this.queue.put(data);
            this.classifier.add(data.getDbName());
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

    private class FlushTask extends TimerTask {
      @Override
      public void run() {
        log.info("Flushing buffer due to timer");
        flush();
      }
    }
  }

}
