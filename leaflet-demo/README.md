# Tealeaf map demo

The demo builds a Leaflet 1.9.4 map through the Java API. It includes an OSM tile
layer, marker popup, polyline and scale control. Buttons move the marker, pause
and resume the same Java click callback, and destroy or recreate the map.

Follow the root [build instructions](../DEVELOPMENT.md). The static application
is `target/site`; serve it over HTTP. `org.webjars.npm:leaflet:1.9.4` supplies
JavaScript, CSS, marker PNGs and license locally. Tiles alone require network
access. Keep visible attribution and choose a tile provider suitable for your
application's traffic; the public demo URL is not a bundled tile service.

## Browser contract

`mvn verify` runs `MapDemoIT` in Chromium and WebKit. It checks Leaflet's runtime
version, map/layer/marker DOM, route and scale rendering, coordinate callbacks,
zoom, moving/popup behavior, exact callback removal/resubscription, responsive
layout, five destroy/recreate cycles, idempotent application disposal and absence
of JavaScript errors. It checks DOM counts and unload callbacks after disposal.

Tests intercept tile requests with deterministic SVG fixtures; they do not
exercise the live OSM service. Manual demo verification separately checks live
tiles. Screenshots are written to `target/map-demo-{chromium,webkit}.png`.
These checks detect retained native instances/DOM and duplicate callbacks in the
covered scenarios; they are not a heap profiler or a proof of zero memory leaks.

Page toolbar handlers live for the page lifetime. A reusable application view
should remove its own toolbar listeners when disposed, in addition to removing
the map. See [ownership guidance](../leaflet-compat/README.md).
