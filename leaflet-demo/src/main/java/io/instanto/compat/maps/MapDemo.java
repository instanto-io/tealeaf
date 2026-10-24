package io.instanto.compat.maps;

import com.gwidgets.api.leaflet.L;
import com.gwidgets.api.leaflet.LatLng;
import com.gwidgets.api.leaflet.Map;
import com.gwidgets.api.leaflet.Marker;
import com.gwidgets.api.leaflet.events.Event;
import com.gwidgets.api.leaflet.events.EventCallback;
import com.gwidgets.api.leaflet.events.MouseEvent;
import com.gwidgets.api.leaflet.options.*;
import elemental2.dom.DomGlobal;

/** A map built entirely through the adapted Java Leaflet API. */
public final class MapDemo {
  private static Map map;
  private static Marker marker;
  private static EventCallback<MouseEvent> clickHandler;
  private static boolean listening;
  private static int clicks;
  private static int moves;
  private static int unloads;

  public static void main(String[] args) {
    create();
    DomGlobal.document
        .getElementById("move")
        .addEventListener(
            "click",
            event -> {
              if (map == null) return;
              moves++;
              marker.setLatLng(L.latLng(51.505 + (moves % 3) * 0.003, -0.09));
              marker.openPopup();
              status("Marker moved · " + moves);
            });
    DomGlobal.document
        .getElementById("listen")
        .addEventListener(
            "click",
            event -> {
              if (map == null) return;
              if (listening) map.off("click", clickHandler);
              else map.on("click", clickHandler);
              listening = !listening;
              DomGlobal.document.getElementById("listen").textContent =
                  listening ? "Pause clicks" : "Resume clicks";
            });
    DomGlobal.document.getElementById("destroy").addEventListener("click", event -> destroy());
    DomGlobal.document
        .getElementById("recreate")
        .addEventListener(
            "click",
            event -> {
              destroy();
              create();
            });
  }

  private static void create() {
    MapOptions options =
        new MapOptions.Builder(L.latLng(51.505, -0.09), 13.0, 2.0)
            .maxZoom(19.0)
            .keyboard(true)
            .zoomAnimation(false)
            .fadeAnimation(false)
            .build();
    map = L.map("map", options);
    L.tileLayer(
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            new TileLayerOptions.Builder()
                .maxZoom(19)
                .attribution(
                    "&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors")
                .build())
        .addTo(map);
    marker =
        L.marker(
            L.latLng(51.505, -0.09),
            new MarkerOptions.Builder().title("London stop").alt("London stop").build());
    marker.addTo(map);
    marker.bindPopup("A Java marker on a Leaflet map", new PopupOptions.Builder().build());
    L.polyline(
            new LatLng[][] {
              {L.latLng(51.50, -0.115), L.latLng(51.505, -0.09), L.latLng(51.512, -0.075)}
            },
            new PolylineOptions.Builder().color("#4f46e5").weight(4.0).build())
        .addTo(map);
    L.control.scale(new ControlScaleOptions.Builder().imperial(false).build()).addTo(map);
    clickHandler =
        event -> {
          clicks++;
          LatLng point = event.getLatlng();
          DomGlobal.document.getElementById("clicks").textContent = "Map clicks: " + clicks;
          DomGlobal.document.getElementById("position").textContent =
              "Last click: " + point.lat + ", " + point.lng;
        };
    map.on("click", clickHandler);
    map.on("zoomend", (EventCallback<Event>) event -> zoom());
    map.once(
        "unload",
        (EventCallback<Event>)
            event -> {
              unloads++;
              DomGlobal.document.getElementById("unloads").textContent =
                  "Maps released: " + unloads;
            });
    listening = true;
    DomGlobal.document.getElementById("listen").textContent = "Pause clicks";
    zoom();
    status("Ready");
  }

  private static void destroy() {
    if (map != null) {
      map.off("click", clickHandler);
      map.remove();
      map.off();
      map = null;
      marker = null;
      clickHandler = null;
    }
    status("Destroyed");
  }

  private static void zoom() {
    DomGlobal.document.getElementById("zoom").textContent = "Zoom: " + map.getZoom();
  }

  private static void status(String text) {
    DomGlobal.document.getElementById("status").textContent = text;
  }
}
