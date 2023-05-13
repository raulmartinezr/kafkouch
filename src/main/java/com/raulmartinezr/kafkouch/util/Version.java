package com.raulmartinezr.kafkouch.util;

import java.net.URL;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Version {
  private static final Logger log = LoggerFactory.getLogger(Version.class);
  private static String version = "unknown";

  static {
    try {
      CodeSource codeSource = Version.class.getProtectionDomain().getCodeSource();
      if (codeSource != null) {
        URL jarUrl = codeSource.getLocation();
        JarFile jarFile = new JarFile(jarUrl.getPath());
        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
          Attributes attributes = manifest.getMainAttributes();
          version = attributes.getValue("Implementation-Version");
        }
      }
    } catch (Exception e) {
      log.warn("Error while loading version:", e);
    }
  }

  public static String getVersion() {
    return version;
  }
}
