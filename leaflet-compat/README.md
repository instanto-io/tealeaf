# Tealeaf Java API

This module maintains 82 Java sources from gwty-leaflet 1.2.1. The build adapts
JsInterop declarations through `gwt-resources-compat-maven-plugin` and compiles
`target/teavm-sources`. Edit `src/main/java`, never the generated copy.

## Initialize a map

Load Leaflet 1.9.4 CSS and JavaScript before the compiled TeaVM entry point and
provide an element with a nonzero height. The [demo POM](../leaflet-demo/pom.xml)
copies these assets, including marker images, from a pinned WebJar.

```html
<link rel="stylesheet" href="leaflet/leaflet.css">
<div id="map" style="height:480px"></div>
<script src="leaflet/leaflet.js"></script>
```

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
expects GWT-style module-prefixed resource paths. Explicit asset loading, as in
the demo, is the supported setup exercised here.

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
