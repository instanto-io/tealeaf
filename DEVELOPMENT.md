# Development

## Build and run

Use **JDK 21** and Maven 3.9 or newer. Java sources target Java 17; the build
and formatting tools require JDK 21. This is a snapshot port, not a Maven Central
release. It depends on the Instanto TeaVM parent/toolchain and the matching
[teavm-compat](https://github.com/instanto-io/teavm-compat) snapshots.
Install those prerequisites using their repository instructions before building
this repository. In a sibling checkout, install the compatibility reactor with:

```sh
mvn -f ../teavm-compat/pom.xml install
```

Install the matching parent, toolchain and compatibility snapshots locally before
building Tealeaf. Maven Central publication is planned.

```sh
mvn clean verify
mvn install
python3 -m http.server 8767 --bind 127.0.0.1 --directory leaflet-demo/target/site
```

Open <http://127.0.0.1:8767/>; stop the server with Ctrl+C. `package` produces
the static demo; `verify` also runs Playwright integration tests in Chromium and
WebKit. Install both browser engines before verification if necessary:

```sh
mvn -pl leaflet-demo dependency:build-classpath -Dmdep.outputFile=target/test-classpath.txt
java -cp "$(cat leaflet-demo/target/test-classpath.txt)" com.microsoft.playwright.CLI install chromium webkit
```

On Linux the engines also require Playwright's documented system dependencies.
Generated sources, JavaScript, source maps, reports and screenshots stay under
`target/` and are not committed. `mvn install` is local; it does not deploy.

## Verification and contribution

Run `mvn clean verify` before committing. The build applies Google Java Format,
compiles the Java API, creates source/Javadoc artifacts, runs browser contracts,
and checks high-priority SpotBugs findings. Inspect the diff after formatting.
Browser success covers the scenarios documented in the demo, not every upstream
API. Keep new compatibility transformations in `teavm-compat` with focused tests;
keep library-specific API corrections and examples in this repository.

## Continuous integration and publication

Every push to `main`, trusted pull request and manual workflow run verifies the
complete reactor, including the Chromium and WebKit contracts in `leaflet-demo` and
`leaflet-gwt-demo`.
CI uses JDK 21, the browser-equipped Instanto container runners, and a fresh Maven
repository so locally installed snapshots cannot hide missing dependencies.
Fork pull requests need a maintainer to move the reviewed change to a trusted branch.

After verification succeeds on `main`, the same workflow revision runs `clean deploy`
through the shared `instanto-poms` Maven workflow. Publication uses the artifacts
from the verified source revision.

Publication includes the parent POM, `leaflet-compat` and `leaflet-gwt` artifacts. The full demo is
built and tested, but retains `maven.deploy.skip`: its `target/site` is a static
website, not a library release.

The same `main` run publishes that verified TeaVM showcase to GitHub Pages at
<https://instanto-io.github.io/tealeaf/>. The upload captures
`leaflet-demo/target/site` from the ordinary `clean verify` before the TeaCoCo
coverage run rebuilds it, so the site is the uninstrumented bundle the browser
contracts proved. A separate `deploy` job depends on `verify`, and pull requests
never deploy. A manual run on `main` repeats verification before publishing;
pull requests and manual runs on other branches do not publish snapshots or the
website.

For local evaluation, use `mvn install` as shown above. This installs the artifacts
in your local Maven repository for use by other projects.
