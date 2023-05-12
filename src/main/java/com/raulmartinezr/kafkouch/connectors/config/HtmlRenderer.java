package com.raulmartinezr.kafkouch.connectors.config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class HtmlRenderer {
  static final String PARAGRAPH_SEPARATOR = System.lineSeparator() + System.lineSeparator();

  public static String htmlToPlaintext(String html) {
    StringBuilder result = new StringBuilder();
    html = html.replaceAll("\\s+", " ");
    renderAsPlaintext(Jsoup.parse(html).body(), result);
    trimRight(result);
    return result.toString();
  }

  private static void renderAsPlaintext(Node node, StringBuilder out) {
    if (node instanceof TextNode) {
      String text = ((TextNode) node).text();
      if (out.length() == 0 || endsWithWhitespace(out)) {
        text = trimLeft(text);
      }
      out.append(text);
      return;
    }

    if (node instanceof Element) {
      Element e = (Element) node;

      if (e.tagName().equals("p") || e.tagName().equals("br")) {
        trimRight(out);
        if (out.length() > 0) {
          out.append(PARAGRAPH_SEPARATOR);
        }
      }

      for (Node child : e.childNodes()) {
        renderAsPlaintext(child, out);
      }
    }
  }

  private static void trimRight(StringBuilder out) {
    while (endsWithWhitespace(out)) {
      out.setLength(out.length() - 1);
    }
  }

  private static String trimLeft(String text) {
    return text.replaceFirst("^\\s+", "");
  }

  private static boolean endsWithWhitespace(CharSequence out) {
    return out.length() > 0 && Character.isWhitespace(out.charAt(out.length() - 1));
  }
}
