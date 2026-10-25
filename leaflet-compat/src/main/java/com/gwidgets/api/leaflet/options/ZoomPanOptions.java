/**
 * Copyright 2016 G-Widgets Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gwidgets.api.leaflet.options;

import static jsinterop.annotations.JsPackage.GLOBAL;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * The Class ZoomPanOptions.
 *
 * @author <a href="mailto:zakaria.amine88@gmail.com">Zakaria Amine</a>
 */
@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public class ZoomPanOptions {
  /** Animation duration in seconds. */
  @JsProperty private double duration;

  @JsOverlay
  public final double getDuration() {
    return this.duration;
  }

  /** Panning animation easing factor. */
  @JsProperty private double easeLinearity;

  @JsOverlay
  public final double getEaseLinearity() {
    return this.easeLinearity;
  }

  /** Suppress the movestart event for panning. */
  @JsProperty private boolean noMoveStart;

  @JsOverlay
  public final boolean getNoMoveStart() {
    return this.noMoveStart;
  }

  private ZoomPanOptions() {}

  @JsProperty private boolean reset;

  @JsProperty private PanOptions pan;

  @JsProperty private ZoomOptions zoom;

  @JsProperty private boolean animate;

  /**
   * Gets whether the map view will be completely reset (without any animations). </br> default
   * false
   *
   * @return the reset
   */
  @JsOverlay
  public final boolean getReset() {
    return this.reset;
  }

  /**
   * Gets the options for the panning (without the zoom change) if it occurs.
   *
   * @return the pan
   */
  @JsOverlay
  public final PanOptions getPan() {
    return this.pan;
  }

  /**
   * Gets the options for the zoom change if it occurs.
   *
   * @return the zoom
   */
  @JsOverlay
  public final ZoomOptions getZoom() {
    return this.zoom;
  }

  /**
   * An equivalent of passing animate to both zoom and pan options (see below) </br> default true
   *
   * @return the animate
   */
  @JsOverlay
  public final boolean getAnimate() {
    return this.animate;
  }

  /** The Class Builder. */
  public static class Builder {
    private Double duration;

    /** Animation duration in seconds. Omit to use Leaflet's default. */
    public Builder duration(Double duration) {
      this.duration = duration;
      return this;
    }

    private Double easeLinearity;

    /** Panning animation easing factor. Omit to use Leaflet's default. */
    public Builder easeLinearity(Double easeLinearity) {
      this.easeLinearity = easeLinearity;
      return this;
    }

    private Boolean noMoveStart;

    /** Suppress the movestart event for panning. Omit to use Leaflet's default. */
    public Builder noMoveStart(Boolean noMoveStart) {
      this.noMoveStart = noMoveStart;
      return this;
    }

    /** The reset. */
    private Boolean reset = false;

    /** The pan. */
    private PanOptions pan;

    /** The zoom. */
    private ZoomOptions zoom;

    /** The animate. */
    private Boolean animate = true;

    /** Instantiates a new builder. */
    public Builder() {}

    /**
     * Reset.
     *
     * @param reset the reset
     * @return the builder
     */
    public Builder reset(Boolean reset) {
      this.reset = reset;
      return this;
    }

    /**
     * Pan.
     *
     * @param pan the pan
     * @return the builder
     */
    public Builder pan(PanOptions pan) {
      this.pan = pan;
      return this;
    }

    /**
     * Zoom.
     *
     * @param zoom the zoom
     * @return the builder
     */
    public Builder zoom(ZoomOptions zoom) {
      this.zoom = zoom;
      return this;
    }

    /**
     * Animate.
     *
     * @param animate the animate
     * @return the builder
     */
    public Builder animate(Boolean animate) {
      this.animate = animate;
      return this;
    }

    /**
     * Builds the.
     *
     * @return the zoom pan options
     */
    public ZoomPanOptions build() {
      ZoomPanOptions options = new ZoomPanOptions();
      if (this.duration != null) options.duration = this.duration;
      if (this.easeLinearity != null) options.easeLinearity = this.easeLinearity;
      if (this.noMoveStart != null) options.noMoveStart = this.noMoveStart;

      if (this.animate != null) options.animate = this.animate;
      if (this.pan != null) options.pan = this.pan;
      if (this.zoom != null) options.zoom = this.zoom;
      if (this.reset != null) options.reset = this.reset;

      return options;
    }
  }
}
