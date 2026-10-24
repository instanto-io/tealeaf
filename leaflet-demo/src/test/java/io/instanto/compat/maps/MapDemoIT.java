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
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          String request = exchange.getRequestURI().getPath();
          Path file =
              site.resolve(request.equals("/") ? "index.html" : request.substring(1)).normalize();
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
      page.onPageError(errors::add);
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
      page.navigate("http://127.0.0.1:" + server.getAddress().getPort());
      page.waitForFunction("document.querySelector('#status').textContent === 'Ready'");
      assertEquals("1.9.4", page.evaluate("L.version"));
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
      assertTrue(errors.toString(), errors.isEmpty());
    } finally {
      server.stop(0);
    }
  }
}
