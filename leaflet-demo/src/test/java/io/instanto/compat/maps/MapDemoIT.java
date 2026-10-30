package io.instanto.compat.maps;

import static org.junit.Assert.*;

import com.microsoft.playwright.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MapDemoIT {
  @Parameterized.Parameters(name = "{0}")
  public static Object[] browsers() {
    return new Object[] {"chromium", "webkit"};
  }

  private final String engine;

  public MapDemoIT(String engine) {
    this.engine = engine;
  }

  @Test
  public void rendersEventsAndReleasesMaps() throws Exception {
    Path site = Path.of("target/site").toAbsolutePath().normalize();
    assertEquals(
        Files.readString(Path.of("../LICENSE")),
        Files.readString(site.resolve("legal/project/LICENSE")));
    assertEquals(
        Files.readString(Path.of("../NOTICE")),
        Files.readString(site.resolve("legal/project/NOTICE")));
    assertEquals(
        Files.readString(Path.of("../leaflet-compat/src/main/resources/META-INF/LICENSE")),
        Files.readString(site.resolve("legal/leaflet-compat/LICENSE")));
    assertEquals(
        Files.readString(Path.of("../leaflet-compat/src/main/resources/META-INF/NOTICE")),
        Files.readString(site.resolve("legal/leaflet-compat/NOTICE")));
    assertTrue(Files.isRegularFile(site.resolve("legal/leaflet-compat/upstream.json")));
    assertTrue(Files.isRegularFile(site.resolve("legal/runtime/index.html")));
    assertTrue(Files.isRegularFile(site.resolve("webjars/leaflet/1.9.4/LICENSE")));
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          String request = exchange.getRequestURI().getPath();
          Path file =
              site.resolve(request.equals("/") ? "index.html" : request.substring(1)).normalize();
          if (Files.isDirectory(file)) file = file.resolve("index.html");
          if (!file.startsWith(site) || !Files.isRegularFile(file)) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
          }
          byte[] bytes = Files.readAllBytes(file);
          exchange
              .getResponseHeaders()
              .set(
                  "Content-Type",
                  Files.probeContentType(file) == null
                      ? "application/octet-stream"
                      : Files.probeContentType(file));
          exchange.sendResponseHeaders(200, bytes.length);
          try (var output = exchange.getResponseBody()) {
            output.write(bytes);
          }
        });
    server.start();
    try (var playwright = Playwright.create();
        var browser =
            (engine.equals("webkit") ? playwright.webkit() : playwright.chromium()).launch()) {
      var page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1100, 850));
      List<String> errors = new ArrayList<>();
      page.onPageError(
          error -> {
            errors.add(error);
            System.out.println("Browser error: " + error);
          });
      page.onConsoleMessage(message -> System.out.println("Browser: " + message.text()));
      // Keep CI deterministic and avoid contacting the public tile service during repeated tests.
      page.route(
          "https://tile.openstreetmap.org/**",
          route ->
              route.fulfill(
                  new Route.FulfillOptions()
                      .setContentType("image/svg+xml")
                      .setBody(
                          "<svg xmlns='http://www.w3.org/2000/svg' width='256' height='256'><rect width='256' height='256' fill='#dfe9e6'/><path d='M0 128H256M128 0V256' stroke='#fff' stroke-width='8'/><text x='16' y='30' fill='#526777'>Test map tile</text></svg>")));
      // Capture the actual map for behavioral assertions; calls under test still originate in Java.
      page.addInitScript("window.addEventListener('error', e => window.__testError=e.message)");
      page.route(
          "**/webjars/leaflet/1.9.4/dist/leaflet.js",
          route ->
              route.fulfill(
                  new Route.FulfillOptions()
                      .setContentType("text/javascript")
                      .setBody(
                          readScript(site)
                              + "\nL.Map.addInitHook(function(){window.__testMap=this;});")));
      page.navigate("http://127.0.0.1:" + server.getAddress().getPort() + "/?verify-api");
      page.waitForFunction("document.body.dataset.apiContracts === 'passed' || window.__testError");
      assertTrue(errors.toString(), errors.isEmpty());
      page.waitForFunction("document.querySelector('#status').textContent === 'Ready'");
      assertEquals("1.9.4", page.evaluate("L.version"));
      assertEquals("ready", page.locator("body").getAttribute("data-loader-concurrent"));
      assertEquals("ready", page.locator("body").getAttribute("data-loader-repeated"));
      assertEquals(1, page.locator("script[src$='/dist/leaflet.js']").count());
      assertEquals(1, page.locator("link[href$='/dist/leaflet.css']").count());
      assertEquals(
          "absolute",
          page.evaluate("getComputedStyle(document.querySelector('.leaflet-pane')).position"));
      page.waitForFunction(
          "Array.from(document.querySelectorAll('.leaflet-marker-icon, .leaflet-marker-shadow')).every(i => i.complete && i.naturalWidth > 0)");
      assertEquals(1, page.locator(".leaflet-map-pane").count());
      assertEquals(1, page.locator(".leaflet-marker-icon").count());
      page.waitForFunction(
          "Array.from(document.querySelectorAll('.leaflet-tile')).some(i => i.complete && i.naturalWidth > 0)");
      assertTrue(page.locator(".leaflet-overlay-pane path").count() > 0);
      assertEquals(1, page.locator(".leaflet-control-scale").count());
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/map-demo-" + engine + ".png"))
              .setFullPage(true));
      page.locator("#map").click(new Locator.ClickOptions().setPosition(100, 150));
      page.waitForFunction("document.querySelector('#clicks').textContent === 'Map clicks: 1'");
      assertTrue(page.locator("#position").textContent().startsWith("Last click: 51."));
      page.locator("#listen").click();
      page.locator("#map").click(new Locator.ClickOptions().setPosition(100, 150));
      assertEquals("Map clicks: 1", page.locator("#clicks").textContent());
      page.locator("#listen").click();
      page.locator("#map").click(new Locator.ClickOptions().setPosition(100, 150));
      page.waitForFunction("document.querySelector('#clicks').textContent === 'Map clicks: 2'");
      page.locator(".leaflet-control-zoom-in").click();
      page.waitForFunction("document.querySelector('#zoom').textContent.includes('14')");
      page.locator("#move").click();
      assertEquals("Marker moved · 1", page.locator("#status").textContent());
      assertEquals(
          "A Java marker on a Leaflet map", page.locator(".leaflet-popup-content").textContent());
      page.setViewportSize(650, 850);
      assertTrue(page.locator("#map").boundingBox().width < 650);
      for (int i = 0; i < 5; i++) {
        page.locator("#destroy").click();
        assertEquals("Destroyed", page.locator("#status").textContent());
        assertEquals(0, page.locator(".leaflet-map-pane").count());
        assertEquals(0, page.locator(".leaflet-marker-icon").count());
        assertEquals("Maps released: " + (i + 1), page.locator("#unloads").textContent());
        page.locator("#recreate").click();
        assertEquals("Ready", page.locator("#status").textContent());
        assertEquals(1, page.locator(".leaflet-map-pane").count());
      }
      page.locator("#map").click(new Locator.ClickOptions().setPosition(100, 150));
      page.waitForFunction("document.querySelector('#clicks').textContent === 'Map clicks: 3'");
      page.locator("#recreate").click();
      assertEquals("Maps released: 6", page.locator("#unloads").textContent());
      assertEquals(1, page.locator(".leaflet-map-pane").count());
      page.locator("#destroy").click();
      page.locator("#destroy").click();
      assertEquals("Maps released: 7", page.locator("#unloads").textContent());
      // Each scene uses the public Java API and can be replaced without retaining its layers.
      page.setViewportSize(1360, 1000);
      page.locator("#scene-1").click();
      assertEquals("Follow the river", page.locator("#scene-title").textContent());
      assertEquals("true", page.locator("#scene-1").getAttribute("aria-pressed"));
      assertEquals(5, page.locator(".leaflet-marker-icon").count());
      page.locator(".leaflet-marker-icon[title='Westminster']").click();
      assertTrue(page.locator(".leaflet-popup-content").textContent().contains("Westminster"));
      page.locator("#overlay").click();
      assertEquals(0, page.locator(".leaflet-marker-icon").count());
      assertEquals(0, page.locator(".leaflet-overlay-pane path").count());
      page.locator("#overlay").click();
      assertEquals(5, page.locator(".leaflet-marker-icon").count());
      page.locator("#tiles").click();
      assertEquals(0, page.locator(".leaflet-tile").count());
      page.locator("#tiles").click();
      page.waitForFunction("document.querySelectorAll('.leaflet-tile').length > 0");
      page.locator(".leaflet-control-zoom-in").click();
      String zoomed = page.locator("#zoom").textContent();
      page.locator("#fit").click();
      assertNotEquals(zoomed, page.locator("#zoom").textContent());
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/showcase-desktop-" + engine + ".png"))
              .setFullPage(true));

      for (int i = 0; i < 3; i++) {
        page.locator("#scene-2").click();
        assertEquals(0, page.locator(".leaflet-marker-icon").count());
        assertEquals(3, page.locator(".leaflet-overlay-pane path").count());
        assertTrue(page.locator("#move").isDisabled());
        assertFalse(page.locator(".leaflet-overlay-pane").innerHTML().contains("NaN"));
        page.locator("#overlay").click();
        assertEquals(0, page.locator(".leaflet-overlay-pane path").count());
        page.locator("#overlay").click();
        assertEquals(3, page.locator(".leaflet-overlay-pane path").count());
        page.locator("#scene-3").click();
        assertEquals(1, page.locator(".leaflet-marker-icon").count());
        assertEquals(2, page.locator(".leaflet-overlay-pane path").count());
        page.locator(".leaflet-marker-icon").click();
        assertTrue(page.locator(".leaflet-popup-content").textContent().contains("GeoJSON"));
        page.locator("#destroy").click();
        assertEquals(0, page.locator(".leaflet-map-pane").count());
        assertTrue(page.locator("#fit").isDisabled());
        page.locator("#recreate").click();
        assertEquals(1, page.locator(".leaflet-marker-icon").count());
        assertEquals(2, page.locator(".leaflet-overlay-pane path").count());
      }
      page.setViewportSize(390, 844);
      page.locator("#scene-2").click();
      page.locator("#fit").click();
      assertEquals(
          Boolean.TRUE, page.evaluate("document.documentElement.scrollWidth <= innerWidth"));
      assertTrue(page.locator("#map").boundingBox().width < 390);
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/showcase-mobile-" + engine + ".png"))
              .setFullPage(true));
      page.locator("#scene-0").click();
      assertFalse(page.locator("#move").isDisabled());
      assertEquals(1, page.locator(".leaflet-marker-icon").count());
      assertEquals(1, page.locator(".leaflet-overlay-pane path").count());
      page.locator("#map").click(new Locator.ClickOptions().setPosition(70, 100));
      assertEquals("Map clicks: 4", page.locator("#clicks").textContent());
      assertEquals(Boolean.TRUE, page.evaluate("__testMap.options.tapHold"));
      assertEquals(
          "strict-origin-when-cross-origin",
          page.locator(".leaflet-tile").first().getAttribute("referrerpolicy"));
      page.setViewportSize(1360, 1000);
      page.locator("#scene-4").click();
      assertEquals(3, page.locator(".leaflet-marker-icon").count());
      page.waitForFunction("document.querySelector('img.leaflet-marker-icon').naturalWidth === 42");
      assertEquals(1, page.locator(".leaflet-customShadows-pane .leaflet-marker-shadow").count());
      page.locator("#marker-counter").click();
      page.locator("#marker-counter").click();
      assertEquals("Live · 2", page.locator("#marker-counter").textContent());
      page.locator("#swap").click();
      assertEquals("Icon changed · HTML badge", page.locator("#status").textContent());
      assertEquals(2, page.locator(".numbered-pin").count());
      page.locator("#swap").click();
      assertEquals("Icon changed · SVG image", page.locator("#status").textContent());
      page.evaluate(
          "window.__dragMarker=Object.values(__testMap._layers).find(l=>l instanceof L.Marker && l.options.title==='Custom SVG marker')");
      assertEquals(
          Boolean.TRUE,
          page.evaluate(
              "__dragMarker.options.autoPan && __dragMarker.options.autoPanOnFocus && __dragMarker.options.autoPanSpeed===12 && __dragMarker.options.autoPanPadding.x===65"));
      // Keyboard focus brings an offscreen marker back into view.
      page.evaluate("__testMap.panBy([900,0],{animate:false}); __dragMarker.getElement().focus()");
      page.waitForFunction("__testMap.getBounds().contains(__dragMarker.getLatLng())");
      page.locator("#fit").click();
      var pin = page.locator("img.leaflet-marker-icon").boundingBox();
      var mapBox = page.locator("#map").boundingBox();
      page.evaluate("window.__beforeDrag=__testMap.getCenter().lng");
      page.mouse().move(pin.x + pin.width / 2, pin.y + pin.height / 2);
      page.mouse().down();
      page.mouse()
          .move(
              mapBox.x + mapBox.width - 8,
              mapBox.y + mapBox.height / 2,
              new Mouse.MoveOptions().setSteps(12));
      page.waitForFunction("Math.abs(__testMap.getCenter().lng-__beforeDrag)>0.00001");
      page.mouse().up();
      page.locator("#fit").click();
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/custom-markers-" + engine + ".png"))
              .setFullPage(true));
      page.locator("#note").click();
      assertEquals(
          "Created with a position + content option",
          page.locator(".leaflet-popup-content").textContent());
      page.locator(".leaflet-popup-close-button").click();
      page.locator("#fly").click();
      try {
        page.waitForFunction(
            "__testMap.getZoom()===15 && __testMap.getCenter().distanceTo(L.latLng(51.505,-0.09))<5",
            null,
            new Page.WaitForFunctionOptions().setTimeout(5000));
      } catch (RuntimeException failure) {
        System.out.println(
            "Flight state: "
                + page.evaluate(
                    "JSON.stringify({center:__testMap.getCenter(),zoom:__testMap.getZoom(),status:document.querySelector('#status').textContent})"));
        throw failure;
      }
      page.evaluate(
          "window.__flightEnded=false; __testMap.once('moveend',()=>window.__flightEnded=true)");
      page.locator("#fly-bounds").click();
      page.waitForFunction("window.__flightEnded");
      assertEquals(
          Boolean.TRUE,
          page.evaluate(
              "Object.values(__testMap._layers).filter(l=>l instanceof L.Marker).every(l=>__testMap.getBounds().contains(l.getLatLng()))"));
      page.locator("#fly").click();
      page.locator("#stop").click();
      assertEquals("Animation stopped", page.locator("#status").textContent());
      page.locator("#scene-5").click();
      assertEquals(0, page.locator(".leaflet-marker-icon").count());
      assertEquals(1, page.locator("svg.survey-svg").count());
      assertEquals(1, page.locator("video.survey-video").count());
      assertEquals(
          "LOCAL VIDEO · illustrative animation", page.locator(".leaflet-tooltip").textContent());
      page.waitForFunction(
          "document.querySelector('video.survey-video').readyState>=2 && document.querySelector('video.survey-video').currentTime>0");
      assertEquals(
          Boolean.TRUE,
          page.evaluate(
              "(()=>{let v=document.querySelector('video');return v.muted && v.loop && v.autoplay && v.playsInline})()"));
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/media-overlays-" + engine + ".png"))
              .setFullPage(true));
      page.locator("#overlay").click();
      assertEquals(0, page.locator("video.survey-video").count());
      page.locator("#overlay").click();
      assertEquals(1, page.locator("video.survey-video").count());
      page.evaluate("window.__oldVideo=document.querySelector('video')");
      page.locator("#scene-4").click();
      assertEquals(Boolean.TRUE, page.evaluate("__oldVideo.paused && !__oldVideo.isConnected"));
      page.setViewportSize(390, 844);
      page.locator("#fit").click();
      assertEquals(Boolean.TRUE, page.evaluate("document.documentElement.scrollWidth<=innerWidth"));
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/custom-markers-mobile-" + engine + ".png"))
              .setFullPage(true));
      page.locator("#scene-5").click();
      page.locator("#destroy").click();
      assertEquals(0, page.locator("video, svg.survey-svg, .leaflet-map-pane").count());
      page.locator("#recreate").click();
      assertEquals(1, page.locator("video.survey-video").count());
      assertTrue(errors.toString(), errors.isEmpty());
      // Capture while the application realm is still alive; the legal and
      // loader-error pages below replace it.
      System.out.println(
          "__TEAVM_COVERAGE_V1__:"
              + page.evaluate("JSON.stringify(globalThis.__teavmCoverageV1 || {})"));
      page.locator("footer a[href='legal/']").click();
      assertEquals("Licenses & notices", page.locator("h1").textContent());
      assertEquals(8, page.locator("li a").count());
      assertEquals(1, page.locator("a[href='runtime/gwt-2.13.1/COPYING']").count());
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/licenses-" + engine + ".png"))
              .setFullPage(true));
      assertEquals(1, page.locator("a[href='runtime/index.html']").count());
      for (int i = 0; i < page.locator("li a").count(); i++) {
        String href = page.locator("li a").nth(i).getAttribute("href");
        APIResponse response =
            page.request().get(java.net.URI.create(page.url()).resolve(href).toString());
        assertEquals("Missing packaged legal document: " + href, 200, response.status());
        response.dispose();
      }
      page.navigate("http://127.0.0.1:" + server.getAddress().getPort() + "/?verify-loader-error");
      page.waitForFunction("document.body.dataset.loaderError");
      assertTrue(
          page.locator("body").getAttribute("data-loader-error").contains("missing-leaflet/"));
      assertEquals(
          0, page.locator("script[src*='missing-leaflet'], link[href*='missing-leaflet']").count());
    } finally {
      server.stop(0);
    }
  }

  private static String readScript(Path site) {
    try {
      return Files.readString(site.resolve("webjars/leaflet/1.9.4/dist/leaflet.js"));
    } catch (java.io.IOException error) {
      throw new java.io.UncheckedIOException(error);
    }
  }
}
