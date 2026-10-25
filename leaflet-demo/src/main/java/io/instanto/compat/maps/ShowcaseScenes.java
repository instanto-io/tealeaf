package io.instanto.compat.maps;

import com.gwidgets.api.leaflet.*;
import com.gwidgets.api.leaflet.options.*;
import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.svg.SVGElement;
import jsinterop.base.Js;

/** Small, self-contained examples using the public Tealeaf API. */
final class ShowcaseScenes {
  static final String[] TITLES = {
    "Start with a map",
    "Follow the river",
    "Draw service areas",
    "Bring your own GeoJSON",
    "Make every marker yours",
    "Put media on the map"
  };
  static final String[] DESCRIPTIONS = {
    "A marker, a popup and a line. Click anywhere to read coordinates, or move the marker from Java.",
    "Five London landmarks connected by an illustrative route. Select a pin to explore each stop; fit the map to see them all.",
    "A polygon, a 600 metre circle and a rectangular area. Click a shape for details, then hide and restore the overlay as a group.",
    "A local FeatureCollection becomes a point, a line and a polygon. The sample is embedded, so no data service is needed.",
    "An SVG image pin, a numbered HTML badge and a live DOM button. Drag a pin to the edge or use the keyboard to explore.",
    "An SVG survey diagram and a looping local video share geographic bounds. Zoom and pan to see them stay anchored to the map."
  };
  static final String[] DETAILS = {
    "Try the marker, zoom controls and click listener. The lifecycle controls below release and recreate the map.",
    "Westminster · London Eye · Tate Modern · Borough Market · Tower Bridge. Straight segments illustrate the API; this is not walking directions.",
    "Illustrative coverage only. Green: service district. Amber: radius in metres. Blue: rectangular bounds.",
    "GeoJSON coordinates use longitude, latitude. Leaflet LatLng takes latitude, longitude. The shapes here are demonstration data.",
    "Try Swap icon, the live counter, or drag the green pin. Auto-pan keeps dragged and keyboard-focused markers visible. Marker art is served locally.",
    "Illustrative media only: this diagram and generated animation are not geographic observations. The video is muted and plays inline. Both assets are local."
  };
  static final String[] CODE = {
    "Marker marker = L.marker(\n    L.latLng(51.505, -0.09),\n    new MarkerOptions.Builder().title(\"London stop\").build());\nmarker.addTo(map);\nmarker.bindPopup(\"Hello from Java\",\n    new PopupOptions.Builder().build());",
    "FeatureGroup stops = L.featureGroup(layers);\nstops.addTo(map);\nmap.fitBounds(stops.getBounds(),\n    new FitBoundsOptions.Builder()\n        .padding(L.point(32, 32, false))\n        .build());",
    "L.circle(L.latLng(51.505, -0.09),\n    new CircleOptions.Builder()\n        .radius(600.0)\n        .color(\"#b86e17\")\n        .fillOpacity(0.2)\n        .build()).addTo(map);",
    "GeoJSON features = L.geoJSON(\n    Js.cast(Global.JSON.parse(featureCollection)),\n    new GeoJSONOptions());\nfeatures.addTo(map);\nmap.fitBounds(features.getBounds(),\n    new FitBoundsOptions.Builder().build());",
    "L.divIcon(new DivIconOptions.Builder()\n    .html(buttonElement)\n    .iconSize(L.point(100, 40, false))\n    .build());\nmarker.setIcon(customIcon);\nIcon current = marker.getIcon();",
    "SVGOverlay survey = L.svgOverlay(svg, bounds,\n    new ImageOverlayOptions.Builder().build());\nsurvey.addTo(map);\nL.videoOverlay(\"media/survey.mp4\", bounds,\n    new VideoOverlayOptions.Builder()\n        .muted(true).playsInline(true).build())\n    .addTo(map);"
  };

  private ShowcaseScenes() {}

  private static VideoOverlay mediaVideo;
  private static Marker customMarker;
  private static Icon imageIcon;
  private static Icon badgeIcon;
  private static boolean swapped;

  static void swapIcon() {
    swapped = !swapped;
    customMarker.setIcon(swapped ? badgeIcon : imageIcon);
    DomGlobal.document.getElementById("status").textContent =
        customMarker.getIcon() == (swapped ? badgeIcon : imageIcon)
            ? "Icon changed · " + (swapped ? "HTML badge" : "SVG image")
            : "Icon change failed";
  }

  static void release() {
    if (mediaVideo != null) {
      mediaVideo.getElement().pause();
      mediaVideo.getElement().removeAttribute("src");
      mediaVideo.getElement().innerHTML = "";
      mediaVideo.getElement().load();
      mediaVideo = null;
    }
    customMarker = null;
    imageIcon = null;
    badgeIcon = null;
  }

  static FeatureGroup customMarkers() {
    imageIcon =
        L.icon(
            new IconOptions.Builder("media/leaf-pin.svg")
                .iconSize(L.point(42, 54, false))
                .iconAnchor(L.point(21, 54, false))
                .popupAnchor(L.point(0, -48, false))
                .shadowUrl("leaflet/images/marker-shadow.png")
                .shadowSize(L.point(41, 41, false))
                .shadowAnchor(L.point(12, 40, false))
                .build());
    badgeIcon =
        L.divIcon(
            new DivIconOptions.Builder()
                .html("<span>07</span>")
                .className("numbered-pin")
                .iconSize(L.point(44, 44, false))
                .popupAnchor(L.point(0, -20, false))
                .build());
    customMarker =
        L.marker(
            L.latLng(51.507, -0.112),
            new MarkerOptions.Builder()
                .icon(imageIcon)
                .title("Custom SVG marker")
                .alt("Leaf-shaped map pin")
                .draggable(true)
                .autoPan(true)
                .autoPanPadding(L.point(65, 65, false))
                .autoPanSpeed(12.0)
                .autoPanOnFocus(true)
                .shadowPane("customShadows")
                .build());
    customMarker.bindPopup(
        "Custom SVG image · drag me to the edge", new PopupOptions.Builder().build());
    Marker numbered =
        L.marker(
            L.latLng(51.505, -0.097),
            new MarkerOptions.Builder().icon(badgeIcon).title("Numbered HTML marker").build());
    numbered.bindPopup("A numbered HTML marker", new PopupOptions.Builder().build());
    HTMLElement button = (HTMLElement) DomGlobal.document.createElement("button");
    button.id = "marker-counter";
    button.textContent = "Live · 0";
    button.setAttribute("aria-label", "Increment live marker counter");
    final int[] count = {0};
    button.addEventListener(
        "click",
        event -> {
          event.stopPropagation();
          button.textContent = "Live · " + ++count[0];
        });
    Marker live =
        L.marker(
            L.latLng(51.509, -0.083),
            new MarkerOptions.Builder()
                .icon(
                    L.divIcon(
                        new DivIconOptions.Builder()
                            .html(button)
                            .className("live-pin")
                            .iconSize(L.point(100, 40, false))
                            .build()))
                .title("Live DOM marker")
                .build());
    swapped = false;
    return L.featureGroup(new Layer[] {customMarker, numbered, live});
  }

  static FeatureGroup media() {
    SVGElement svg =
        Js.cast(DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "svg"));
    svg.setAttribute("viewBox", "0 0 400 250");
    svg.setAttribute("role", "img");
    svg.setAttribute("aria-label", "Illustrative survey diagram");
    svg.innerHTML =
        "<rect width='400' height='250' rx='18' fill='#f0f7e9' fill-opacity='.92'/>"
            + "<path d='M20 180L115 60L220 170L375 55' fill='none' stroke='#24785a' stroke-width='12'/>"
            + "<circle cx='115' cy='60' r='20' fill='#d5a346'/><circle cx='220' cy='170' r='20' fill='#d5a346'/>"
            + "<text x='24' y='225' font-family='sans-serif' font-size='23' fill='#183d32'>SVG · Survey sketch</text>";
    SVGOverlay survey =
        L.svgOverlay(
            svg,
            L.latLngBounds(L.latLng(51.504, -0.122), L.latLng(51.513, -0.102)),
            new ImageOverlayOptions.Builder().interactive(true).className("survey-svg").build());
    survey.bindPopup("SVG overlay · geographic bounds", new PopupOptions.Builder().build());
    VideoOverlay video =
        L.videoOverlay(
            new String[] {"media/survey.webm", "media/survey.mp4"},
            L.latLngBounds(L.latLng(51.499, -0.098), L.latLng(51.508, -0.078)),
            new VideoOverlayOptions.Builder()
                .autoplay(true)
                .loop(true)
                .muted(true)
                .playsInline(true)
                .keepAspectRatio(false)
                .className("survey-video")
                .opacity(0.9)
                .build());
    mediaVideo = video;
    HTMLElement caption = (HTMLElement) DomGlobal.document.createElement("span");
    caption.textContent = "LOCAL VIDEO · illustrative animation";
    Tooltip label =
        L.tooltip(
            L.latLng(51.4985, -0.088),
            new TooltipOptions.Builder()
                .content(caption)
                .permanent(true)
                .direction("bottom")
                .build());
    return L.featureGroup(new Layer[] {survey, video, label});
  }

  static FeatureGroup route() {
    LatLng[] points = {
      L.latLng(51.5007, -0.1246),
      L.latLng(51.5033, -0.1195),
      L.latLng(51.5076, -0.0994),
      L.latLng(51.5055, -0.0910),
      L.latLng(51.5055, -0.0754)
    };
    String[] names = {"Westminster", "London Eye", "Tate Modern", "Borough Market", "Tower Bridge"};
    Layer[] layers = new Layer[6];
    layers[0] =
        L.polyline(
            new LatLng[][] {points},
            new PolylineOptions.Builder().color("#24785a").weight(4.0).build());
    for (int i = 0; i < points.length; i++) {
      Marker stop =
          L.marker(points[i], new MarkerOptions.Builder().title(names[i]).alt(names[i]).build());
      stop.bindPopup(
          "<strong>" + (i + 1) + ". " + names[i] + "</strong><br>River walk · London",
          new PopupOptions.Builder().build());
      layers[i + 1] = stop;
    }
    return L.featureGroup(layers);
  }

  static FeatureGroup areas() {
    Layer district =
        L.polygon(
            new LatLng[][] {
              {
                L.latLng(51.511, -0.113),
                L.latLng(51.516, -0.098),
                L.latLng(51.508, -0.083),
                L.latLng(51.502, -0.098)
              }
            },
            new PolylineOptions.Builder().color("#24785a").fillOpacity(0.22).weight(2.0).build());
    district.bindPopup("Service district · example polygon", new PopupOptions.Builder().build());
    Layer circle =
        L.circle(
            L.latLng(51.505, -0.09),
            new CircleOptions.Builder()
                .radius(600.0)
                .color("#b86e17")
                .fillOpacity(0.2)
                .weight(2.0)
                .build());
    circle.bindPopup("Coverage radius · 600 metres", new PopupOptions.Builder().build());
    Layer rectangle =
        L.rectangle(
            L.latLngBounds(L.latLng(51.498, -0.12), L.latLng(51.505, -0.106)),
            new PathOptions.Builder()
                .color("#366fa3")
                .fillOpacity(0.15)
                .weight(2.0)
                .dashArray("6 5")
                .build());
    rectangle.bindPopup("Survey area · rectangular bounds", new PopupOptions.Builder().build());
    return L.featureGroup(new Layer[] {district, circle, rectangle});
  }

  static FeatureGroup geoJson() {
    String data =
        """
        {"type":"FeatureCollection","features":[
          {"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-0.09,51.505]}},
          {"type":"Feature","properties":{},"geometry":{"type":"LineString","coordinates":[[-0.12,51.50],[-0.10,51.503],[-0.075,51.51]]}},
          {"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[-0.106,51.509],[-0.096,51.516],[-0.083,51.512],[-0.106,51.509]]]}}
        ]}
        """;
    GeoJSON features = L.geoJSON(Js.cast(Global.JSON.parse(data)), new GeoJSONOptions());
    features.bindPopup(
        "Loaded from the embedded GeoJSON FeatureCollection", new PopupOptions.Builder().build());
    return features;
  }
}
