# Tealeaf

Tealeaf brings the established **gwty-leaflet Java API** to TeaVM and provides a
working **Leaflet 1.9.4** map demo. The upstream wrapper is archived; this project
maintains a source fork of its 1.2.1 API while preserving `com.gwidgets.api.leaflet`
packages so existing Java mapping code has a familiar starting point.

## Viewing the map demo

With the prerequisites in [Development](DEVELOPMENT.md) installed:

```sh
mvn clean verify
python3 -m http.server 8767 --bind 127.0.0.1 --directory leaflet-demo/target/site
```

Open <http://127.0.0.1:8767/>. The map shows London, a marker and popup, a route,
zoom controls and a metric scale. Java controls move the marker, pause/resume
click handling, and destroy/recreate the map. Leaflet JavaScript, CSS and marker
images are served locally. The interactive demo fetches OpenStreetMap tiles.
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
since both artifacts define the same Java classes. Consumers must also load
Leaflet's JavaScript and CSS before invoking this API.

## Compatibility and ownership

All 82 Java source files compile. Browser coverage includes tile layers, markers,
popups, polylines, scale controls, zoom, Java events and repeated map disposal.
This is a tested subset of Leaflet 1.9.4, not a complete or modernized binding of
every Leaflet API. Leaflet 2 and third-party plugins have not been validated.
The port targets TeaVM JavaScript, not JVM rendering or WebAssembly.

Keep callback references so `off(type, callback)` removes exactly your listener.
Call `map.remove()` when the owning view is disposed, clear any remaining
application event registrations, and release Java references to the map/layers.
See [API and lifecycle guidance](leaflet-compat/README.md) for examples and limits.

## Source and licensing

The baseline is [gwty-leaflet](https://github.com/gwidgets/gwty-leaflet)
`com.gwidgets:gwty-leaflet:1.2.1:sources` under Apache 2.0. Original copyright
notices are retained. [upstream.json](leaflet-compat/upstream.json) records the
source archive and SHA-256. [NOTICE](NOTICE) identifies the fork and local changes.
Leaflet 1.9.4 is obtained separately as a WebJar; its license is copied into the
demo's `leaflet/` directory. See [LICENSE](LICENSE).
