package com.gwidgets.api.leaflet;

import elemental2.dom.HTMLVideoElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/** Leaflet VideoOverlay; create instances with the corresponding L factory. */
@JsType(isNative = true)
public class VideoOverlay extends ImageOverlay {
  protected VideoOverlay() {
    super();
  }

  @Override
  @JsMethod
  public native HTMLVideoElement getElement();
}
