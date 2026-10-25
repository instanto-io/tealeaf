/*
 * Modified for Tealeaf in 2026 from gwty-leaflet 1.2.1:
 * source formatting and Java/Leaflet binding changes for TeaVM.
 * Existing upstream notices are retained. See NOTICE and the module's upstream.json.
 */
package com.gwidgets.api.leaflet;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class DivOverlay {

  protected DivOverlay() {}

  @JsMethod
  public native L setLatLng(LatLng latLng);

  @JsMethod
  public native LatLng getLatLng();

  @JsMethod
  public native L setContent(String content);

  @JsMethod
  public native L setContent(HTMLElement content);

  @JsMethod(name = "getContent")
  public native String getContentAsString();

  @JsMethod(name = "getContent")
  public native HTMLElement getContentAsHTMLElement();

  @JsMethod
  public native HTMLElement getElement();

  @JsMethod
  public native boolean isOpen();

  @JsMethod
  public native L update();

  @JsMethod
  public native L openOn(Map map);

  @JsMethod
  public native L close();

  @JsMethod
  public native L toggle(Layer layer);
}
