# Tealeaf

Tealeaf is based on [**gwty-leaflet**](https://github.com/gwidgets/gwty-leaflet),
the GWT JsInterop wrapper created by **Zakaria Amine and the GWidgets contributors**.
Their Java API and original implementation are the foundation of this project.

Tealeaf adapts the **gwty-leaflet 1.2.1 Java API for TeaVM JavaScript** and targets
**Leaflet 1.9.4**. It preserves the `com.gwidgets.api.leaflet` package names and
native-class / `L` factory / fluent-builder style. It is an independently
maintained source fork, with additional bindings and native API corrections.

Compatibility is partial: the Chromium and WebKit contracts cover the examples
and methods described below, not every inherited signature. This is not a
complete drop-in replacement for gwty-leaflet, and Leaflet 2, plugins, JVM
rendering and WebAssembly are outside the validated scope.

Migration changes to review include:

- `Popup.getLatLng()` returns `LatLng`, correcting the old `L` return type.
- Popup/tooltip `isOpen()`, `Map.hasLayer(...)` and bounds `contains(...)` use
  primitive `boolean`; native zoom and coordinate values use primitive `double`.
  Recompile consumers rather than reusing binaries built against the old API.
- `ImageOverlay.setOpacity()`, `bringToFront()` and `bringToBack()` are instance
  methods. Replace static calls with calls on the intended overlay.
- Popup/tooltip position belongs in the coordinate-first `L.popup(...)` or
  `L.tooltip(...)` factory, not a `latLng` option. HTML content can be a String
  or DOM element; select the corresponding getter for the content supplied.

See the [API correction notes](leaflet-compat/README.md#local-api-corrections-and-boundaries)
for the remaining fixes and migration details.

## Explore the showcase

With the prerequisites in [Development](DEVELOPMENT.md) installed:

```sh
mvn clean verify
python3 -m http.server 8767 --bind 127.0.0.1 --directory leaflet-demo/target/site
```

Open <http://127.0.0.1:8767/>. Six selectable examples explore London:

- **Markers & events:** popups, marker movement and Java click callbacks.
- **A river walk:** five landmark stops, a polyline and fitting bounds.
- **Service areas:** a polygon, a circle measured in metres and a rectangle.
- **GeoJSON:** an embedded FeatureCollection containing a point, line and polygon.
- **Custom markers:** an SVG image pin, HTML badge, live DOM counter, icon replacement and drag auto-pan.
- **SVG & video:** a geographic SVG diagram and a generated local video with a tooltip caption.

Each example includes a Java excerpt. Toggle features and the basemap independently,
fit or fly to the features, stop a flight, open a positioned note, or destroy and
recreate the selected example. The layout
adapts to desktop and mobile screens. Leaflet JavaScript, CSS and marker images are
served locally; the interactive showcase fetches OpenStreetMap tiles.
See the [demo guide](leaflet-demo/README.md) for tests and hosting details.

## Maven modules and coordinates

| Module | Purpose |
| --- | --- |
| `leaflet-compat` | Maintained Java API, transformed to TeaVM JSO at build time |
| `leaflet-demo` | Static map application and Chromium/WebKit browser contracts |

```xml
<dependency>
  <groupId>io.instanto</groupId>
  <artifactId>leaflet-compat</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The repository/project name is Tealeaf; the Maven artifact remains
`leaflet-compat`. Replace the upstream `com.gwidgets:gwty-leaflet` dependency,
since both artifacts define the same Java classes.

### Load the runtime

`leaflet-compat` brings **`org.webjars.npm:leaflet:1.9.4` transitively**, including
JavaScript, CSS and marker images. Call the loader before creating a map:

```java
import com.gwidgets.api.leaflet.utils.TealeafResources;

TealeafResources.whenReady(
    () -> { /* Create the map and layers here. */ },
    error -> { /* Show or log the load failure. */ });
```

The callback waits for both JavaScript and CSS. Concurrent and repeated calls
share one load per directory; a failed load is removed so a later call can retry.
An existing global `L` is reused, so the host is responsible for its version.

The default directory is `webjars/leaflet/1.9.4/dist/`, relative to the host page.
For an application context, another static path or a CDN, pass the directory:

```java
TealeafResources.whenReady("/my-app/webjars/leaflet/1.9.4/dist/", ready, onError);
```

**A Maven dependency does not itself expose an HTTP URL.** Configure your server
to serve the WebJar's `META-INF/resources/webjars/` content, or unpack/copy it into
your static output. Preserve `dist/leaflet.js`, `dist/leaflet.css` and
`dist/images/` together so CSS-relative marker images resolve. The
[demo POM](leaflet-demo/pom.xml) copies the WebJar paths into `target/site` and
its host page needs no handwritten Leaflet script or stylesheet tags. Leaflet's
license is retained at `webjars/leaflet/1.9.4/LICENSE`.

The old `LeafletResources.whenReady(boolean, Element.OnloadFn)` signature remains
available with its original GWT module-prefixed resource paths. New TeaVM
applications should use `TealeafResources`, which also reports failures and waits
for CSS.

## Custom markers

Leaflet supports custom image markers through `L.icon(...)` and HTML/CSS markers
through `L.divIcon(...)`. Tealeaf inherits both factories, their option builders,
`MarkerOptions.Builder.icon(...)` and `Marker.setIcon(...)` from gwty-leaflet.
Examples include branded SVG or PNG images, numbered stops, status badges and
inline SVG symbols inside a `DivIcon`. See Leaflet's
[custom icons tutorial](https://leafletjs.com/examples/custom-icons/) and
[DivIcon reference](https://leafletjs.com/reference.html#divicon).

The showcase now exercises image, HTML and DOM markers, including `setIcon()` and
`getIcon()`. `DivIconOptions.Builder.html(HTMLElement)` passes the original node,
so its Java event listener continues working. The no-argument builder avoids
requiring an image URL for an HTML marker. Existing constructors remain available.

```java
Icon badge = L.divIcon(new DivIconOptions.Builder()
    .html("<strong>07</strong>")
    .className("my-marker")
    .iconSize(L.point(44, 44, false))
    .build());
Marker marker = L.marker(position,
    new MarkerOptions.Builder().icon(badge).autoPanOnFocus(true).build());
marker.addTo(map);
```

## Leaflet additions now available

A focused comparison of gwty-leaflet 1.2.1 with the
[Leaflet changelog](https://github.com/Leaflet/Leaflet/blob/v1.9.4/CHANGELOG.md)
and the bundled 1.9.4 source identified these gaps. They are now implemented in
the same style: native classes, `L` factories, option getters and fluent builders.

| Leaflet addition | Available since | Tealeaf binding |
| --- | --- | --- |
| Video overlays | 1.1 | `VideoOverlay`, `VideoOverlayOptions`; URL, URL-array and video-element factories |
| Marker auto-pan during dragging | 1.3 | `autoPan`, `autoPanPadding`, `autoPanSpeed` |
| SVG overlays | 1.5 | `SVGOverlay`, `L.svgOverlay`; reuse `ImageOverlayOptions` |
| DOM elements as `DivIcon` content | 1.5 | `html(HTMLElement)` alongside `html(String)` |
| Marker icon inspection and shadow pane | 1.5 | `getIcon()`, `getElement()`, `shadowPane` |
| Marker focus auto-pan | 1.8 | `autoPanOnFocus` |
| Tile request referrer policy | 1.8 | `TileLayerOptions.Builder.referrerPolicy(String)` |
| Mobile long-press handling | 1.8 | `MapOptions.Builder.tapHold(Boolean)` |
| Popup/tooltip constructor content and position | 1.9 | String/DOM `content(...)`; `L.popup(LatLng, PopupOptions)` and `L.tooltip(LatLng, TooltipOptions)` |

Position is a **factory argument**, not a `latLng` option in Leaflet 1.9.4. For example:

```java
Popup popup = L.popup(position,
    new PopupOptions.Builder().content("Hello from Java").build());
popup.openOn(map);
```

Older omissions now covered include `Map.flyTo()`, `flyToBounds()`, `panInside()`
and `stop()`. `ZoomPanOptions` includes duration, easing and `noMoveStart`;
`PanInsideOptions` exposes padding. Shared popup/tooltip content, position,
element, open/close/toggle and update methods are available through `DivOverlay`.
Image overlays have instance opacity/stacking methods, URL/bounds/z-index setters,
bounds/center/element getters and an existing-image-element factory.

This is a focused gap closure, not an exhaustive compatibility audit. Function
callbacks for popup/tooltip constructor content, some older union overloads and
other legacy signatures remain outside the tested subset. `tapHold` is wired
and verified as an option; physical-device Safari long-press behavior has not
been tested. The legacy `tap` builder remains for source compatibility.

## Compatibility and ownership

The maintained Java API and resource loader compile. Browser coverage includes tile layers, markers,
popups, polylines, polygons, circles, rectangles, GeoJSON loading, feature groups,
bounds fitting, layer visibility, scale controls, zoom, Java events and repeated
map disposal, custom icons, DOM content, drag/focus auto-pan, SVG/video overlays,
local video playback and cleanup, positioned popups/tooltips and animated navigation.
This is a tested subset of Leaflet 1.9.4, not a complete or modernized binding of
every Leaflet API. Leaflet 2 and third-party plugins have not been validated.
The port targets TeaVM JavaScript, not JVM rendering or WebAssembly.

Keep callback references so `off(type, callback)` removes exactly your listener.
Call `map.remove()` when the owning view is disposed, clear any remaining
application event registrations, and release Java references to the map/layers.
See [API and lifecycle guidance](leaflet-compat/README.md) for examples and limits.

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
