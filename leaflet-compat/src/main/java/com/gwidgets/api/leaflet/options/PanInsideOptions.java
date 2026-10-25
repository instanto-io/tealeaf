package com.gwidgets.api.leaflet.options;

import static jsinterop.annotations.JsPackage.GLOBAL;

import com.gwidgets.api.leaflet.Point;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** Leaflet 1.9.4 options for PanInside. */
@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public class PanInsideOptions {
  private PanInsideOptions() {}

  /** Padding on all sides, in pixels. */
  @JsProperty private Point padding;

  @JsOverlay
  public final Point getPadding() {
    return padding;
  }

  /** Top and left padding, in pixels. */
  @JsProperty private Point paddingTopLeft;

  @JsOverlay
  public final Point getPaddingTopLeft() {
    return paddingTopLeft;
  }

  /** Bottom and right padding, in pixels. */
  @JsProperty private Point paddingBottomRight;

  @JsOverlay
  public final Point getPaddingBottomRight() {
    return paddingBottomRight;
  }

  /** Leaves unset properties to Leaflet defaults. */
  public static class Builder {
    public Builder() {}

    private Point padding;

    /** Padding on all sides, in pixels. */
    public Builder padding(Point padding) {
      this.padding = padding;
      return this;
    }

    private Point paddingTopLeft;

    /** Top and left padding, in pixels. */
    public Builder paddingTopLeft(Point paddingTopLeft) {
      this.paddingTopLeft = paddingTopLeft;
      return this;
    }

    private Point paddingBottomRight;

    /** Bottom and right padding, in pixels. */
    public Builder paddingBottomRight(Point paddingBottomRight) {
      this.paddingBottomRight = paddingBottomRight;
      return this;
    }

    public PanInsideOptions build() {
      PanInsideOptions options = new PanInsideOptions();
      if (this.padding != null) options.padding = this.padding;
      if (this.paddingTopLeft != null) options.paddingTopLeft = this.paddingTopLeft;
      if (this.paddingBottomRight != null) options.paddingBottomRight = this.paddingBottomRight;
      return options;
    }
  }
}
