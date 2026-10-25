# Tealeaf Java API

This module maintains 82 Java sources from gwty-leaflet 1.2.1 plus classes for Leaflet 1.9.4 additions and a runtime loader. The build adapts
JsInterop declarations through `gwt-resources-compat-maven-plugin` and compiles
`target/teavm-sources`. Edit `src/main/java`, never the generated copy.

## Initialize a map

The module has a transitive Leaflet 1.9.4 WebJar dependency. Serve the WebJar assets
or copy them into your static site, then wait for the loader before using the API.
Provide a map container with a nonzero height.

```html
<div id="map" style="height:480px"></div>
```

```java
TealeafResources.whenReady(() -> createMap(), error -> showError(error));
// Or: TealeafResources.whenReady("/assets/leaflet/", ready, onError);
```

Import `com.gwidgets.api.leaflet.utils.TealeafResources`. Its default directory is
`webjars/leaflet/1.9.4/dist/` relative to the host page. Both CSS and JavaScript
must load before the success callback; errors and a 15-second timeout call the
error callback. Repeated calls for the same directory share the load. A failure
cleans up its inserted nodes and permits retry. The loader does not fetch map tiles.
The [demo POM](../leaflet-demo/pom.xml) shows static asset copying with marker images
and the upstream license retained. See [hosting details](../README.md#load-the-runtime).

Inside the ready callback:

```java
Map map = L.map("map",
    new MapOptions.Builder(L.latLng(51.505, -0.09), 13.0, 2.0)
        .maxZoom(19.0).keyboard(true).build());
EventCallback<MouseEvent> onClick = event -> {
    LatLng position = event.getLatlng();
    // Update application state using position.lat and position.lng.
};
map.on("click", onClick);
// Stop only this callback; other listeners remain registered.
map.off("click", onClick);
```

Imports come from `com.gwidgets.api.leaflet`, `.options` and `.events`; avoid
confusing the native `Map` with `java.util.Map`. Add a tile layer and attribution
as shown in [MapDemo](../leaflet-demo/src/main/java/io/instanto/compat/maps/MapDemo.java).
The old `LeafletResources.whenReady` loader remains for source compatibility and
expects GWT-style module-prefixed resource paths. The demo exercises the new
`TealeafResources` loader.

## Ownership and disposal

The view owns its native map and application callbacks. Store callback references
for matching `on`/`off` calls; constructing another lambda does not identify the
registered listener. For permanent disposal:

```java
map.off("click", onClick);
map.remove();       // Detach layers, controls and native map DOM listeners.
map.off();          // Clear remaining listeners on the now-disposed map.
map = null;        // Also clear application-held layer/callback references.
```

Wrap this sequence in a null/state guard if disposal can be requested twice.
A detached DOM container alone is not a disposal signal. Create a fresh map
when the view returns. Do not call `off()` indiscriminately on a map shared by
other owners. Hide/show or container layout changes may require
`map.invalidateSize(...)` with the appropriate overload; see the Java API.

## Local API corrections and boundaries

- `Map.hasLayer(Layer)` returns primitive `boolean` to match the native JavaScript
  result; this avoids attempting to unbox a Java Boolean in TeaVM.
- `Popup.getLatLng()` now returns `LatLng`; popup/tooltip `isOpen()` returns
  primitive `boolean`, as do `LatLngBounds.contains(...)` overloads. These correct native return values exposed by browser tests.
- `ImageOverlay.setOpacity`, `bringToFront` and `bringToBack` are instance methods,
  matching Leaflet. Replace any previous static calls with calls on an overlay.
- `DivIconOptions.popUpAnchor(...)` retains its Java spelling but writes Leaflet's
  `popupAnchor` property. A correctly spelled builder alias is also available.
- SVG/video overlay classes, map flights and the modern options listed in the
  [root README](../README.md#leaflet-additions-now-available) follow the existing
  factory/native-class/fluent-builder conventions.
- Added `off(String, EventCallback)` to preserve targeted callback removal.
- `MapOptions.Builder.keyboard` now writes the requested value.
- Native control/tile-layer/CRS/projection/transformation namespaces resolve under `L`.
- Zoom accessors and latitude/longitude use primitive `double` at the native
  boundary, avoiding boxed Java values around JavaScript numbers.

The shared adapter handles bounded generic callbacks and explicit native method
annotations. These changes belong to `teavm-compat`, rather than copied glue in
this library. Other legacy signatures and optional APIs remain to be reviewed;
Java compilation alone does not validate every native call or option.

The only module SpotBugs exclusion is native `Point.clone()`: Leaflet owns its
implementation, so the Java `Cloneable` warning does not apply. No Java cloning
implementation is suppressed. See [coverage](../leaflet-demo/README.md).

## Media ownership

An SVG overlay expects an `elemental2.svg.SVGElement` with a `viewBox`. Video
factories accept a URL, alternate URL array or `HTMLVideoElement`. The element
factory preserves the supplied video's playback configuration, matching Leaflet.
Pause a video before permanently discarding its overlay; removing a map does not
in itself guarantee that detached media stops playing. The showcase pauses and
clears media sources when changing examples or destroying the map.

Native setters retain the upstream `L` return convention. Prefer separate Java
statements over assuming subtype-specific fluent chaining. New builders leave
unspecified options absent so Leaflet's defaults continue to apply. String and
DOM getters represent alternatives: use the getter matching the content supplied.
