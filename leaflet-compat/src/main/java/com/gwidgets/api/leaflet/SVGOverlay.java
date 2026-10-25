package com.gwidgets.api.leaflet;

import elemental2.svg.SVGElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/** Leaflet SVGOverlay; create instances with the corresponding L factory. */
@JsType(isNative = true)
public class SVGOverlay extends ImageOverlay {
  protected SVGOverlay() {
    super();
  }

  @Override
  @JsMethod
  public native SVGElement getElement();
}
