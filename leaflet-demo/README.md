# Tealeaf interactive showcase

The showcase builds a Leaflet 1.9.4 map through the Java API, with six examples:

| Example | Demonstrates |
| --- | --- |
| Markers & events | A movable marker, popup, polyline, zoom, scale and Java click callbacks |
| A river walk | Five landmark markers, an illustrative route and fitting feature bounds |
| Service areas | Polygon styling, a 600 metre circle and rectangular bounds |
| GeoJSON | Embedded point, line and polygon features with popups |
| Custom markers | Local SVG image, HTML badge, DOM counter, icon replacement, drag/focus auto-pan and custom shadow pane |
| SVG & video | Geographic SVG overlay, generated local video, coordinate-first tooltip and playback cleanup |

The sidebar switches examples without replacing the map. Each example includes an
API excerpt and field notes. Features and basemap can be hidden independently;
lifecycle controls destroy and recreate the selected example. The Move marker
button applies to the first example. Swap icon applies to Custom markers. Fly to river/features, Stop flight and Open
note exercise modern map navigation and positioned popup factories. Map controls
are disabled after disposal.

[ShowcaseScenes.java](src/main/java/io/instanto/compat/maps/ShowcaseScenes.java)
contains the example data and layer construction;
[MapDemo.java](src/main/java/io/instanto/compat/maps/MapDemo.java) owns navigation,
controls, callbacks and disposal. The route and service areas are illustrative,
not walking directions or real operational coverage.

Follow the root [build instructions](../DEVELOPMENT.md). The static application
is `target/site`; serve it over HTTP. `org.webjars.npm:leaflet:1.9.4` supplies
JavaScript, CSS, marker PNGs and license transitively from `leaflet-compat`.
The POM preserves the `webjars/leaflet/1.9.4/` layout in the static output;
`TealeafResources` loads the runtime and stylesheet before constructing the map.
The host page does not contain Leaflet asset tags. Tiles alone require network
access. Keep visible attribution and choose a tile provider suitable for your
application's traffic; the public demo URL is not a bundled tile service.

## Browser contract

`mvn verify` runs `MapDemoIT` in Chromium and WebKit. It checks Leaflet's runtime
version, concurrent/repeated loader calls, one script and stylesheet, loaded default
marker images, CSS readiness, failure callbacks/cleanup, map/layer/marker DOM, route and scale rendering, coordinate callbacks,
zoom, moving/popup behavior, exact callback removal/resubscription, responsive
layout, five initial destroy/recreate cycles, idempotent application disposal and
absence of JavaScript errors. Additional checks cover every scene, route popups,
feature and basemap toggles, fitting bounds, three rounds of scene replacement and
GeoJSON disposal/recreation, plus a 390 px mobile layout without horizontal overflow.
It checks DOM counts and unload callbacks after disposal. Additional contracts
check SVG/image/HTML marker rendering, preserved DOM event handlers, icon replacement,
marker shadow panes, actual drag and focus auto-pan, tile referrer policy,
animated map navigation, positioned popup/tooltip content, local video playback,
media DOM replacement and paused playback after scene disposal.

Opening `?verify-api` runs the Java-side checks in
[ApiContractChecks.java](src/main/java/io/instanto/compat/maps/ApiContractChecks.java)
on a temporary map. These validate native return types, String/DOM option aliases,
coordinate-first constructors, all video factory overloads, SVG/image element
identity, image instance methods and shared popup/tooltip methods. Browser tests
require the completion marker and absence of JavaScript errors.

Tests intercept tile requests with deterministic SVG fixtures; they do not
exercise the live OSM service. Manual demo verification separately checks live
tiles. Screenshots are written to `target/map-demo-{chromium,webkit}.png`,
`target/showcase-desktop-{chromium,webkit}.png` and
`target/showcase-mobile-{chromium,webkit}.png`. Custom marker, mobile marker and
media screenshots use `target/custom-markers-*.png`,
`target/custom-markers-mobile-*.png` and `target/media-overlays-*.png`.
These checks detect retained native instances/DOM and duplicate callbacks in the
covered scenarios; they are not a heap profiler or a proof of zero memory leaks.

Page toolbar handlers live for the page lifetime. A reusable application view
should remove its own toolbar listeners when disposed, in addition to removing
the map. See [ownership guidance](../leaflet-compat/README.md).

## Local media

`media/leaf-pin.svg` is original vector artwork for this demo. `media/survey.webm` and `media/survey.mp4`
are two encodings (VP8 and H.264) of a four-second, silent, generated animation
(400 × 250, 20 fps), with no
third-party footage. These assets are distributed under this repository's Apache 2.0
license. The animation illustrates bounds and playback; it is not real survey data.
SVG markup and DOM content are built from local, trusted example data.

## Packaged notices

The footer's Licenses & notices link opens `legal/index.html`. Maven copies the
root `LICENSE`/`NOTICE` to `legal/project/`, the API module's originals to
`legal/leaflet-compat/`, and its `upstream.json` beside them. Leaflet's BSD license
remains at its WebJar path. Browser contracts check these copies and the visible
link. The legal page also links the runtime license and provenance inventory at
`legal/runtime/index.html`. It describes the packaged inventory without claiming
a complete audit.

Runtime license copies are pinned static resources. When a runtime dependency
version changes, refresh `src/main/webapp/legal/runtime/` and its provenance
hashes before publishing a new showcase build.
