# Tealeaf

Tealeaf provides Java bindings for **Leaflet 1.9.4** in GWT and TeaVM applications. It is
based on **gwty-leaflet 1.2.1**, created by Zakaria Amine and the GWidgets
contributors, and keeps the `com.gwidgets.api.leaflet` packages, `L` factories
and option builders.

**Alpha — `0.1.0-SNAPSHOT`.** Compatibility is partial. The examples and browser
tests cover the features described here; other inherited APIs still need review.

[Showcase](https://instanto-io.github.io/tealeaf/) · [API guide](leaflet-compat/README.md)

## Add the library

```xml
<dependency>
  <groupId>io.instanto</groupId>
  <artifactId>leaflet-compat</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Use `leaflet-compat` in a TeaVM application with the Instanto compatibility
toolchain. For GWT 2.13.1, use `leaflet-gwt` instead and inherit
`com.gwidgets.Leaflet` in your GWT module. Both artifacts compile the same
GWT/JsInterop sources. TeaVM uses the shared source adapter and the GWT,
Elemental2 and JsInterop compatibility implementations; no parallel handwritten
TeaVM API is maintained. Choose one artifact for your compiler.
The coordinates use the current alpha snapshot, which can be installed from
source. Maven Central publication is planned. This artifact replaces `com.gwidgets:gwty-leaflet`; both libraries define the same Java
classes and should not be included together.

## Load the runtime

The dependency includes the Leaflet 1.9.4 WebJar, containing JavaScript, CSS and
marker images. Serve its `META-INF/resources/` contents from your application,
or copy them into the static output as shown in [the demo POM](leaflet-demo/pom.xml).
Preserve the `dist/images/` directory alongside the CSS. The library does not
supply a map tile service.

Provide a container with a height:

```html
<div id="map" style="height:480px"></div>
```

Wait for the JavaScript and CSS before creating the map:

```java
import com.gwidgets.api.leaflet.utils.TealeafResources;

TealeafResources.whenReady(
    () -> createMap(),
    error -> showError(error));
```

Here `createMap` and `showError` are application methods. The default asset path
is `webjars/leaflet/1.9.4/dist/`, relative to the page. For another location, use
`TealeafResources.whenReady("/assets/leaflet/", ready, onError)`.
Repeated calls share the load; failed loads can be retried. An existing global
`L` is reused, so the host application must provide the intended Leaflet version.

## Create a map and add layers

Inside `createMap`, use the `L` factories and option builders:

```java
import com.gwidgets.api.leaflet.L;
import com.gwidgets.api.leaflet.Map;
import com.gwidgets.api.leaflet.Marker;
import com.gwidgets.api.leaflet.options.*;

Map map = L.map("map",
    new MapOptions.Builder(L.latLng(51.505, -0.09), 13.0, 2.0)
        .maxZoom(19.0).keyboard(true).build());

Marker marker = L.marker(L.latLng(51.505, -0.09),
    new MarkerOptions.Builder().title("Location").build());
marker.bindPopup("Location details", new PopupOptions.Builder().build());
marker.addTo(map);
```

Keep the map and layer references in the component that owns them. Add a
`TileLayer` for a basemap, using your tile provider's URL and attribution. The
[showcase source](leaflet-demo/src/main/java/io/instanto/compat/maps/MapDemo.java)
uses OpenStreetMap tiles and includes attribution.

Polylines, polygons, circles, rectangles and GeoJSON form additional layers.
Group related layers with `L.featureGroup(...)` to add, remove or fit the view to
them together. Native setters retain the upstream return conventions; use
separate statements when a setter does not return the layer's Java type.

## Custom markers

Use `L.icon(...)` for an image marker or `L.divIcon(...)` for HTML or a DOM element:

```java
Icon badge = L.divIcon(new DivIconOptions.Builder()
    .html("<strong>07</strong>")
    .className("map-badge")
    .iconSize(L.point(44, 44, false))
    .build());
marker.setIcon(badge);
```

`Icon` comes from `com.gwidgets.api.leaflet`. The application supplies the CSS
for `map-badge`. A DOM element passed to `html(...)` is retained, including its
Java event listeners. `getIcon()` and `getElement()` expose the current marker
icon and element. Marker options include dragging, drag auto-pan, focus auto-pan
and a shadow pane.

## Events and disposal

Java callbacks receive Leaflet events:

```java
EventCallback<MouseEvent> onClick = event -> {
  LatLng position = event.getLatlng();
  // Update application state with position.lat and position.lng.
};
map.on("click", onClick);
```

Import event types from `com.gwidgets.api.leaflet.events` and `LatLng` from
`com.gwidgets.api.leaflet`. Keep each callback reference for targeted removal:

```java
map.off("click", onClick);
map.remove();
```

Dispose of the map when its view is removed, and release application-held
references to layers and callbacks. Removing the container alone does not dispose
of the map. Pause and release video sources before discarding media overlays.
After a layout change, call the appropriate `map.invalidateSize(...)` overload.
See the [lifecycle guide](leaflet-compat/README.md#ownership-and-disposal).

## Implemented features

The Chromium and WebKit contracts exercise these features:

| Area | Java API coverage |
| --- | --- |
| Maps and navigation | Tile layers, zoom, bounds fitting, `flyTo`, `flyToBounds`, `panInside`, `stop`, scale controls and layer visibility |
| Markers | Standard, image, HTML and DOM icons; icon replacement; drag/focus auto-pan |
| Shapes and data | Polylines, polygons, circles, rectangles, GeoJSON and feature groups |
| Popups and tooltips | String/DOM content, positioning, open/close/toggle and updates |
| Overlays | Image, SVG and video factories; bounds, opacity, stacking and media elements |
| Events and lifecycle | Java event callbacks, targeted listener removal and repeated map disposal |

## Leaflet additions now available

The port extends the gwty-leaflet baseline with SVG/video overlays, DOM icon
content, marker icon inspection and auto-pan options. It also adds tile referrer
policy, `tapHold`, positioned popup/tooltip factories and animated map navigation.
These use the existing factory and builder style. For example, a positioned
popup takes its location as a factory argument:

```java
Popup popup = L.popup(L.latLng(51.505, -0.09),
    new PopupOptions.Builder().content("Location details").build());
popup.openOn(map);
```

`tapHold` is tested as an option; physical-device Safari long-press behavior has
not been verified. Some older overloads and function-valued popup/tooltip
constructor content remain outside the tested subset. Leaflet 2, third-party
plugins, JVM rendering and WebAssembly are outside the validated scope.

Migrating gwty-leaflet applications should review the
[API corrections](leaflet-compat/README.md#local-api-corrections-and-boundaries).
These include primitive numeric/boolean return values, `Popup.getLatLng()`
returning `LatLng`, and image overlay methods corrected to instance methods.
Recompile consumers against this library.

## Showcase and build

The [showcase](https://instanto-io.github.io/tealeaf/) contains six examples:
markers/events, routes, service areas, GeoJSON, custom markers, and SVG/video
overlays. Each includes a Java excerpt. Leaflet assets are served locally;
basemap tiles are fetched from OpenStreetMap.

Use JDK 21 and Maven 3.9+, with the matching Instanto TeaVM parent and compatibility
artifacts installed. With the project's Playwright browsers available:

```sh
mvn clean verify
```

`mvn install` also installs the alpha artifacts into your local Maven repository.
The same showcase and browser contracts run on GWT 2.13.1 and TeaVM in Chromium
and WebKit. The TeaVM site is written to `leaflet-demo/target/site`, and the GWT
site to `leaflet-gwt-demo/target/site`. See
[Development](DEVELOPMENT.md) for browser setup and the
[demo guide](leaflet-demo/README.md) for hosting details.

## Credits, source and licensing

Thank you to **Zakaria Amine and the GWidgets contributors** for
[gwty-leaflet](https://github.com/gwidgets/gwty-leaflet), and to
**Volodymyr Agafonkin and the Leaflet contributors** for
[Leaflet](https://leafletjs.com/). Tealeaf builds on their work.

The baseline is [gwty-leaflet](https://github.com/gwidgets/gwty-leaflet)
`com.gwidgets:gwty-leaflet:1.2.1:sources` under Apache 2.0. Original copyright
notices are retained. [upstream.json](leaflet-compat/upstream.json) records the
source archive and SHA-256. [NOTICE](NOTICE) identifies the fork and local changes.
Leaflet 1.9.4 is a transitive WebJar dependency under its upstream BSD-2-Clause
license; its license is retained in the demo WebJar layout. See [LICENSE](LICENSE).

The showcase's **Licenses & notices** link opens its packaged `legal/` page, with
the project and API module licenses/notices and the pinned upstream source record.
Inherited Java files that differ from that archive carry local modification
notices, including files changed only by formatting. Original notices and both
inherited license texts are retained. This is not a complete dependency audit.

The runtime inventory at `leaflet-demo/src/main/webapp/legal/runtime/` contains
pinned license/notice copies and provenance hashes. Refresh those static copies
and their provenance when upgrading the corresponding runtime dependencies.
