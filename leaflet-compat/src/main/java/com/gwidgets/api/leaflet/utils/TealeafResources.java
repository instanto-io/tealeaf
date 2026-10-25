package com.gwidgets.api.leaflet.utils;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLLinkElement;
import elemental2.dom.HTMLScriptElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import jsinterop.base.Js;

/** Loads the bundled Leaflet runtime and stylesheet once per asset directory. */
public final class TealeafResources {
  /** Relative to the host page; servlet WebJar hosting and static copies use the same layout. */
  public static final String DEFAULT_BASE_URL = "webjars/leaflet/1.9.4/dist/";

  private static final java.util.Map<String, Load> LOADS = new HashMap<>();

  private TealeafResources() {}

  /** Calls {@code ready} once both JavaScript and CSS are loaded, or {@code error} on failure. */
  public static void whenReady(Runnable ready, Consumer<String> error) {
    whenReady(DEFAULT_BASE_URL, ready, error);
  }

  /** Uses an asset directory containing leaflet.js, leaflet.css and the images directory. */
  public static void whenReady(String baseUrl, Runnable ready, Consumer<String> error) {
    load(baseUrl, "leaflet.js", ready, error);
  }

  static void load(String baseUrl, String scriptName, Runnable ready, Consumer<String> error) {
    if (baseUrl == null || ready == null || error == null) {
      throw new IllegalArgumentException("Asset directory and both callbacks are required");
    }
    String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    String key = base + scriptName;
    Load load = LOADS.get(key);
    if (load == null) {
      load = new Load(key, base, scriptName);
      LOADS.put(key, load);
      load.callbacks.add(new Callback(ready, error));
      load.start();
    } else if (load.ready) {
      ready.run();
    } else {
      load.callbacks.add(new Callback(ready, error));
    }
  }

  private static final class Callback {
    final Runnable ready;
    final Consumer<String> error;

    Callback(Runnable ready, Consumer<String> error) {
      this.ready = ready;
      this.error = error;
    }
  }

  private static final class Load {
    final String key;
    final String base;
    final String scriptName;
    final List<Callback> callbacks = new ArrayList<>();
    HTMLLinkElement style;
    HTMLScriptElement script;
    boolean cssLoaded;
    boolean scriptLoaded;
    boolean ready;
    boolean failed;
    double timeout;

    Load(String key, String base, String scriptName) {
      this.key = key;
      this.base = base;
      this.scriptName = scriptName;
    }

    void start() {
      timeout =
          DomGlobal.setTimeout(ignored -> fail("Timed out loading Leaflet from " + base), 15000);
      style = (HTMLLinkElement) DomGlobal.document.createElement("link");
      style.rel = "stylesheet";
      style.href = base + "leaflet.css";
      style.addEventListener(
          "load",
          event -> {
            cssLoaded = true;
            complete();
          });
      style.addEventListener("error", event -> fail("Could not load " + base + "leaflet.css"));
      DomGlobal.document.head.appendChild(style);
      scriptLoaded = Js.global().get("L") != null;
      if (scriptLoaded) {
        complete();
        return;
      }
      script = (HTMLScriptElement) DomGlobal.document.createElement("script");
      script.src = base + scriptName;
      script.addEventListener(
          "load",
          event -> {
            if (Js.global().get("L") == null)
              fail("Leaflet script did not define L: " + base + scriptName);
            else {
              scriptLoaded = true;
              complete();
            }
          });
      script.addEventListener("error", event -> fail("Could not load " + base + scriptName));
      DomGlobal.document.head.appendChild(script);
    }

    void complete() {
      if (failed || ready || !cssLoaded || !scriptLoaded) return;
      ready = true;
      DomGlobal.clearTimeout(timeout);
      List<Callback> pending = new ArrayList<>(callbacks);
      callbacks.clear();
      for (Callback callback : pending) callback.ready.run();
    }

    void fail(String message) {
      if (failed || ready) return;
      failed = true;
      DomGlobal.clearTimeout(timeout);
      if (style != null) style.remove();
      if (script != null) script.remove();
      LOADS.remove(key);
      List<Callback> pending = new ArrayList<>(callbacks);
      callbacks.clear();
      for (Callback callback : pending) callback.error.accept(message);
    }
  }
}
