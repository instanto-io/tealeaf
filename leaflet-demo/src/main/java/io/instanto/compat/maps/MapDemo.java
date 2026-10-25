package io.instanto.compat.maps;

import com.gwidgets.api.leaflet.FeatureGroup;
import com.gwidgets.api.leaflet.L;
import com.gwidgets.api.leaflet.LatLng;
import com.gwidgets.api.leaflet.Layer;
import com.gwidgets.api.leaflet.Map;
import com.gwidgets.api.leaflet.Marker;
import com.gwidgets.api.leaflet.Popup;
import com.gwidgets.api.leaflet.TileLayer;
import com.gwidgets.api.leaflet.events.Event;
import com.gwidgets.api.leaflet.events.EventCallback;
import com.gwidgets.api.leaflet.events.MouseEvent;
import com.gwidgets.api.leaflet.options.*;
import com.gwidgets.api.leaflet.utils.TealeafResources;
import elemental2.dom.DomGlobal;

/** A map built entirely through the adapted Java Leaflet API. */
public final class MapDemo {
  private static Map map;
  private static FeatureGroup overlay;
  private static TileLayer tiles;
  private static int scene;
  private static Marker marker;
  private static EventCallback<MouseEvent> clickHandler;
  private static boolean listening;
  private static int clicks;
  private static int moves;
  private static int unloads;

  public static void main(String[] args) {
    if (DomGlobal.location.search.contains("verify-loader-error")) {
      TealeafResources.whenReady(
          "missing-leaflet/",
          () -> status("Unexpected load"),
          error -> DomGlobal.document.body.setAttribute("data-loader-error", error));
      return;
    }
    if (DomGlobal.location.search.contains("verify-api")) {
      TealeafResources.whenReady(
          () -> DomGlobal.document.body.setAttribute("data-loader-concurrent", "ready"),
          MapDemo::loadFailed);
    }
    TealeafResources.whenReady(MapDemo::start, MapDemo::loadFailed);
  }

  private static void loadFailed(String error) {
    status(error);
    DomGlobal.document.body.setAttribute("data-loader-error", error);
  }

  private static void start() {
    TealeafResources.whenReady(
        () -> DomGlobal.document.body.setAttribute("data-loader-repeated", "ready"),
        MapDemo::loadFailed);
    if (DomGlobal.location.search.contains("verify-api")) ApiContractChecks.run();
    create();
    for (int i = 0; i < ShowcaseScenes.TITLES.length; i++) {
      final int selected = i;
      DomGlobal.document
          .getElementById("scene-" + i)
          .addEventListener(
              "click",
              event -> {
                scene = selected;
                if (map == null) create();
                else showScene();
              });
    }
    DomGlobal.document
        .getElementById("fit")
        .addEventListener(
            "click",
            event -> {
              if (map != null) fit();
            });
    DomGlobal.document
        .getElementById("overlay")
        .addEventListener(
            "click",
            event -> {
              if (map == null) return;
              if (map.hasLayer(overlay)) map.removeLayer(overlay);
              else overlay.addTo(map);
              layerButtons();
            });
    DomGlobal.document
        .getElementById("tiles")
        .addEventListener(
            "click",
            event -> {
              if (map == null) return;
              if (map.hasLayer(tiles)) map.removeLayer(tiles);
              else tiles.addTo(map);
              layerButtons();
            });
    DomGlobal.document
        .getElementById("move")
        .addEventListener(
            "click",
            event -> {
              if (map == null || marker == null) return;
              if (!map.hasLayer(overlay)) overlay.addTo(map);
              layerButtons();
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
    DomGlobal.document
        .getElementById("swap")
        .addEventListener("click", event -> ShowcaseScenes.swapIcon());
    DomGlobal.document
        .getElementById("fly")
        .addEventListener(
            "click",
            event -> {
              if (map == null) return;
              map.flyTo(
                  L.latLng(51.505, -0.09), 15, new ZoomPanOptions.Builder().duration(0.7).build());
              status("Flying to the river");
            });
    DomGlobal.document
        .getElementById("fly-bounds")
        .addEventListener(
            "click",
            event -> {
              if (map == null) return;
              map.flyToBounds(
                  overlay.getBounds(),
                  new FitBoundsOptions.Builder()
                      .padding(L.point(32, 32, false))
                      .maxZoom(15.0)
                      .duration(0.7)
                      .build());
              status("Flying to all features");
            });
    DomGlobal.document
        .getElementById("stop")
        .addEventListener(
            "click",
            event -> {
              if (map != null) {
                map.stop();
                status("Animation stopped");
              }
            });
    DomGlobal.document
        .getElementById("note")
        .addEventListener(
            "click",
            event -> {
              if (map == null) return;
              Popup note =
                  L.popup(
                      L.latLng(51.505, -0.09),
                      new PopupOptions.Builder()
                          .content("Created with a position + content option")
                          .build());
              note.openOn(map);
              map.panInside(
                  note.getLatLng(),
                  new PanInsideOptions.Builder().padding(L.point(80, 80, false)).build());
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
            .tapHold(true)
            .zoomAnimation(false)
            .fadeAnimation(false)
            .build();
    map = L.map("map", options);
    map.createPane("customShadows");
    tiles =
        L.tileLayer(
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            new TileLayerOptions.Builder()
                .maxZoom(19)
                .referrerPolicy("strict-origin-when-cross-origin")
                .attribution(
                    "&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors")
                .build());
    tiles.addTo(map);
    showScene();
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
    setControlsEnabled(true);
    status("Ready");
  }

  private static void destroy() {
    if (map != null) {
      ShowcaseScenes.release();
      map.off("click", clickHandler);
      map.remove();
      map.off();
      map = null;
      marker = null;
      overlay = null;
      tiles = null;
      clickHandler = null;
    }
    setControlsEnabled(false);
    status("Destroyed");
  }

  private static void showScene() {
    map.stop();
    if (overlay != null) map.removeLayer(overlay);
    ShowcaseScenes.release();
    marker = null;
    if (scene == 0) {
      marker =
          L.marker(
              L.latLng(51.505, -0.09),
              new MarkerOptions.Builder().title("London stop").alt("London stop").build());
      marker.bindPopup("A Java marker on a Leaflet map", new PopupOptions.Builder().build());
      overlay =
          L.featureGroup(
              new Layer[] {
                marker,
                L.polyline(
                    new LatLng[][] {
                      {L.latLng(51.50, -0.115), L.latLng(51.505, -0.09), L.latLng(51.512, -0.075)}
                    },
                    new PolylineOptions.Builder().color("#24785a").weight(4.0).build())
              });
    } else if (scene == 1) overlay = ShowcaseScenes.route();
    else if (scene == 2) overlay = ShowcaseScenes.areas();
    else if (scene == 3) overlay = ShowcaseScenes.geoJson();
    else if (scene == 4) overlay = ShowcaseScenes.customMarkers();
    else overlay = ShowcaseScenes.media();
    overlay.addTo(map);
    if (scene == 0) map.setView(L.latLng(51.505, -0.09), 13, new ZoomPanOptions.Builder().build());
    else fit();
    for (int i = 0; i < ShowcaseScenes.TITLES.length; i++) {
      DomGlobal.document
          .getElementById("scene-" + i)
          .setAttribute("aria-pressed", String.valueOf(scene == i));
    }
    DomGlobal.document.getElementById("scene-title").textContent = ShowcaseScenes.TITLES[scene];
    DomGlobal.document.getElementById("scene-description").textContent =
        ShowcaseScenes.DESCRIPTIONS[scene];
    DomGlobal.document.getElementById("scene-details").textContent = ShowcaseScenes.DETAILS[scene];
    DomGlobal.document.getElementById("code").textContent = ShowcaseScenes.CODE[scene];
    setControlsEnabled(true);
    layerButtons();
    status("Ready");
  }

  private static void fit() {
    map.fitBounds(
        overlay.getBounds(),
        new FitBoundsOptions.Builder()
            .padding(L.point(32, 32, false))
            .maxZoom(15.0)
            .animate(false)
            .build());
  }

  private static void layerButtons() {
    boolean visible = map.hasLayer(overlay);
    DomGlobal.document.getElementById("overlay").textContent =
        visible ? "Hide features" : "Show features";
    DomGlobal.document
        .getElementById("overlay")
        .setAttribute("aria-pressed", String.valueOf(visible));
    boolean basemap = map.hasLayer(tiles);
    DomGlobal.document.getElementById("tiles").textContent =
        basemap ? "Hide basemap" : "Show basemap";
    DomGlobal.document
        .getElementById("tiles")
        .setAttribute("aria-pressed", String.valueOf(basemap));
  }

  private static void setControlsEnabled(boolean enabled) {
    for (String id :
        new String[] {
          "move", "listen", "fit", "overlay", "tiles", "swap", "fly", "fly-bounds", "stop", "note"
        }) {
      var button = DomGlobal.document.getElementById(id);
      if (enabled && (!id.equals("move") || marker != null) && (!id.equals("swap") || scene == 4))
        button.removeAttribute("disabled");
      else button.setAttribute("disabled", "");
    }
  }

  private static void zoom() {
    DomGlobal.document.getElementById("zoom").textContent = "Zoom: " + map.getZoom();
  }

  private static void status(String text) {
    DomGlobal.document.getElementById("status").textContent = text;
  }
}
