package com.raulmartinezr.kafkouch.connectors.config;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {
  private DurationParser() {
    throw new AssertionError("not instantiable");
  }

  private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)(.+)");

  private static final Map<String, TimeUnit> qualifierToTimeUnit;

  static {
    final Map<String, TimeUnit> temp = new HashMap<>();
    temp.put("ms", TimeUnit.MILLISECONDS);
    temp.put("s", TimeUnit.SECONDS);
    temp.put("m", TimeUnit.MINUTES);
    temp.put("h", TimeUnit.HOURS);
    temp.put("d", TimeUnit.DAYS);
    qualifierToTimeUnit = Collections.unmodifiableMap(temp);
  }

  public static Duration parseDuration(String s) {
    return Duration.ofMillis(parseDuration(s, TimeUnit.MILLISECONDS));
  }

  public static long parseDuration(String s, TimeUnit resultUnit) {
    s = s.trim().toLowerCase(Locale.ROOT);
    if (s.equals("0")) {
      return 0;
    }
    final Matcher m = DURATION_PATTERN.matcher(s);
    if (!m.matches() || !qualifierToTimeUnit.containsKey(m.group(2))) {
      throw new IllegalArgumentException("Unable to parse duration '" + s + "'."
          + " Please specify an integer followed by a time unit (ms = milliseconds, s = seconds, m = minutes, h = hours, d = days)."
          + " For example, to specify 30 minutes: 30m");
    }

    final long value = Long.parseLong(m.group(1));
    final TimeUnit unit = qualifierToTimeUnit.get(m.group(2));
    return divideRoundUp(unit.toMillis(value), resultUnit.toMillis(1));
  }

  private static long divideRoundUp(long num, long divisor) {
    // assume both inputs are positive
    return (num + divisor - 1) / divisor;
  }
}
