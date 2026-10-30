package io.instanto.compat.maps;

import com.gwidgets.api.leaflet.*;
import com.gwidgets.api.leaflet.options.*;
import elemental2.dom.*;
import elemental2.svg.SVGElement;
import jsinterop.base.Js;

/** Browser-only regression checks for native signatures, enabled by ?verify-api. */
final class ApiContractChecks {
  private ApiContractChecks() {}

  static void run() {
    HTMLElement host = (HTMLElement) DomGlobal.document.createElement("div");
    host.id = "api-contract-map";
    host.setAttribute("style", "position:absolute;left:-10000px;width:400px;height:300px");
    DomGlobal.document.body.appendChild(host);
    Map map =
        L.map(
            host.id,
            new MapOptions.Builder(L.latLng(51.505, -0.09), 13.0, 2.0)
                .maxZoom(19.0)
                .tapHold(false)
                .zoomAnimation(false)
                .build());
    try {
      LatLng position = L.latLng(51.505, -0.09);
      LatLngBounds bounds = L.latLngBounds(L.latLng(51.50, -0.10), L.latLng(51.51, -0.08));
      HTMLElement content = (HTMLElement) DomGlobal.document.createElement("span");
      content.textContent = "DOM content";
      DivIconOptions iconOptions =
          new DivIconOptions.Builder()
              .html("old")
              .html(content)
              .popUpAnchor(L.point(0, -12, false))
              .build();
      check(iconOptions.getHtmlAsElement() == content, "DivIcon element option");
      check(iconOptions.getPopUpAnchor().y == -12, "popupAnchor spelling");
      Marker marker =
          L.marker(position, new MarkerOptions.Builder().icon(L.divIcon(iconOptions)).build());
      marker.addTo(map);
      check(marker.getElement().contains(content), "DivIcon inserts original node");
      Icon imageIcon = L.icon(new IconOptions.Builder("media/leaf-pin.svg").build());
      marker.setIcon(imageIcon);
      check(marker.getIcon() == imageIcon, "getIcon identity after replacement");
      check(marker.getElement().tagName.equals("IMG"), "image icon replacement");
      marker.remove();

      Popup popup =
          L.popup(position, new PopupOptions.Builder().content("old").content(content).build());
      popup.openOn(map);
      check(popup.isOpen(), "popup opens");
      check(popup.getContentAsHTMLElement() == content, "popup constructor DOM content");
      check(popup.getLatLng().lat == position.lat, "popup coordinate return type");
      check(popup.getElement().contains(content), "popup DOM element");
      popup.setContent("replacement");
      popup.update();
      check(popup.getContentAsString().equals("replacement"), "popup content replacement");
      popup.close();
      check(!popup.isOpen(), "popup close");
      marker.addTo(map);
      popup.toggle(marker);
      check(popup.isOpen(), "popup toggle");
      popup.close();
      marker.remove();

      // Leaflet applies maxWidth/minWidth to the measured content width, not as
      // CSS properties, so read the rendered content node back.
      Popup widePopup =
          L.popup(
              position,
              new PopupOptions.Builder().content("x".repeat(400)).maxWidth(120.0).build());
      widePopup.openOn(map);
      HTMLElement wideContent =
          (HTMLElement) widePopup.getElement().querySelector(".leaflet-popup-content");
      check(popupContentWidth(wideContent) <= 121, "popup maxWidth option");
      widePopup.close();

      Popup narrowPopup =
          L.popup(
              position,
              new PopupOptions.Builder().content("sized").minWidth(200.0).build());
      narrowPopup.openOn(map);
      HTMLElement narrowContent =
          (HTMLElement) narrowPopup.getElement().querySelector(".leaflet-popup-content");
      check(popupContentWidth(narrowContent) >= 200, "popup minWidth option");
      narrowPopup.close();

      Tooltip tooltip =
          L.tooltip(
              position,
              new TooltipOptions.Builder()
                  .content(content)
                  .content("label")
                  .permanent(true)
                  .build());
      tooltip.openOn(map);
      check(tooltip.isOpen(), "tooltip opens independently");
      check(tooltip.getContentAsString().equals("label"), "tooltip constructor string content");
      tooltip.setContent(content);
      tooltip.setLatLng(position);
      tooltip.update();
      check(tooltip.getContentAsHTMLElement() == content, "tooltip DOM content setter");
      check(tooltip.getLatLng().lng == position.lng, "tooltip coordinate getter");
      tooltip.close();

      HTMLImageElement image = (HTMLImageElement) DomGlobal.document.createElement("img");
      image.src = "media/leaf-pin.svg";
      ImageOverlay imageOverlay =
          L.imageOverlay(
              image,
              bounds,
              new ImageOverlayOptions.Builder().className("contract-image").zIndex(5.0).build());
      imageOverlay.addTo(map);
      check(imageOverlay.getElementAsImage() == image, "image element factory identity");
      imageOverlay.setUrl("media/leaf-pin.svg?updated");
      imageOverlay.setOpacity(0.4);
      imageOverlay.setZIndex(7);
      imageOverlay.setBounds(bounds);
      imageOverlay.bringToFront();
      imageOverlay.bringToBack();
      check(image.getAttribute("src").endsWith("?updated"), "image URL instance setter");
      check(
          image.style.getPropertyValue("opacity").equals("0.4"), "overlay opacity instance setter");
      check(image.style.getPropertyValue("z-index").equals("7"), "overlay z-index setter");
      check(imageOverlay.getBounds() == bounds, "overlay bounds getter");
      check(
          Math.abs(imageOverlay.getCenter().lat - position.lat) < 0.000001,
          "overlay center getter");
      imageOverlay.remove();

      SVGElement svg =
          Js.cast(DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "svg"));
      svg.setAttribute("viewBox", "0 0 100 100");
      SVGOverlay svgOverlay = L.svgOverlay(svg, bounds, new ImageOverlayOptions.Builder().build());
      svgOverlay.addTo(map);
      check(svgOverlay.getElement() == svg, "SVG element factory and getter");
      svgOverlay.setBounds(bounds);
      svgOverlay.setOpacity(0.8);
      svgOverlay.remove();

      VideoOverlay urlVideo =
          L.videoOverlay(
              "media/survey.mp4",
              bounds,
              new VideoOverlayOptions.Builder()
                  .autoplay(false)
                  .loop(false)
                  .muted(true)
                  .playsInline(true)
                  .build());
      urlVideo.addTo(map);
      check(urlVideo.getElement().tagName.equals("VIDEO"), "video URL factory and getter");
      check(
          !urlVideo.getElement().autoplay && !urlVideo.getElement().loop,
          "explicit false video options");
      urlVideo.remove();
      HTMLVideoElement element = (HTMLVideoElement) DomGlobal.document.createElement("video");
      VideoOverlay elementVideo =
          L.videoOverlay(element, bounds, new VideoOverlayOptions.Builder().build());
      elementVideo.addTo(map);
      check(elementVideo.getElement() == element, "video element factory identity");
      elementVideo.remove();

      map.flyTo(
          position,
          14,
          new ZoomPanOptions.Builder()
              .animate(false)
              .duration(0.2)
              .easeLinearity(0.5)
              .noMoveStart(true)
              .build());
      check(map.getZoom() == 14, "flyTo native call");
      map.flyToBounds(bounds, new FitBoundsOptions.Builder().animate(false).build());
      check(map.getBounds().contains(bounds), "flyToBounds native call");
      map.panInside(
          position,
          new PanInsideOptions.Builder()
              .padding(L.point(20, 20, false))
              .paddingTopLeft(L.point(30, 30, false))
              .paddingBottomRight(L.point(40, 40, false))
              .build());
      map.stop();
      DomGlobal.document.body.setAttribute("data-api-contracts", "passed");
    } finally {
      map.remove();
      map.off();
      host.remove();
    }
  }

  private static double popupContentWidth(HTMLElement content) {
    return Double.parseDouble(content.style.getPropertyValue("width").replace("px", ""));
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new IllegalStateException("Leaflet API contract: " + message);
  }
}
