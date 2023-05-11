package com.raulmartinezr.kafkouch.connectors.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataSizeParser {
    private DataSizeParser() {
        throw new AssertionError("not instantiable");
    }

    private static final Pattern PATTERN = Pattern.compile("(\\d+)(.+)");

    private static final Map<String, Integer> qualifierToScale;

    static {
        final Map<String, Integer> temp = new HashMap<>();
        temp.put("b", 1);
        temp.put("k", 1024);
        temp.put("m", 1024 * 1024);
        temp.put("g", 1024 * 1024 * 1024);
        qualifierToScale = Collections.unmodifiableMap(temp);
    }

    public static DataSize parseDataSize(String s) {
        s = s.trim().toLowerCase(Locale.ROOT);

        if (s.equals("0")) {
            return DataSize.ofBytes(0);
        }

        final Matcher m = PATTERN.matcher(s);
        if (!m.matches() || !qualifierToScale.containsKey(m.group(2))) {
            throw new IllegalArgumentException("Unable to parse size '" + s + "'." +
                    " Please specify an integer followed by a size unit (b = bytes, k = kilobytes, m = megabytes, g = gigabytes)."
                    +
                    " For example, to specify 64 megabytes: 64m");
        }

        final long value = Long.parseLong(m.group(1));
        final Integer unit = qualifierToScale.get(m.group(2));
        return DataSize.ofBytes(value * unit);
    }
}