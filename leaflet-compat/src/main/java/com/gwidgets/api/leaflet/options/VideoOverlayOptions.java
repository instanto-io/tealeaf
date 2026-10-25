package com.gwidgets.api.leaflet.options;

import static jsinterop.annotations.JsPackage.GLOBAL;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** Leaflet 1.9.4 options for VideoOverlay. */
@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public class VideoOverlayOptions {
  private VideoOverlayOptions() {}

  /** Start playback automatically, subject to browser policy. */
  @JsProperty private boolean autoplay;

  @JsOverlay
  public final boolean getAutoplay() {
    return autoplay;
  }

  /** Loop the video. */
  @JsProperty private boolean loop;

  @JsOverlay
  public final boolean getLoop() {
    return loop;
  }

  /** Mute playback. */
  @JsProperty private boolean muted;

  @JsOverlay
  public final boolean getMuted() {
    return muted;
  }

  /** Keep playback inline on mobile browsers. */
  @JsProperty private boolean playsInline;

  @JsOverlay
  public final boolean getPlaysInline() {
    return playsInline;
  }

  /** Preserve the video aspect ratio. */
  @JsProperty private boolean keepAspectRatio;

  @JsOverlay
  public final boolean getKeepAspectRatio() {
    return keepAspectRatio;
  }

  /** Overlay opacity from zero to one. */
  @JsProperty private double opacity;

  @JsOverlay
  public final double getOpacity() {
    return opacity;
  }

  /** Attribution text. */
  @JsProperty private String attribution;

  @JsOverlay
  public final String getAttribution() {
    return attribution;
  }

  /** Allow interaction with the overlay. */
  @JsProperty private boolean interactive;

  @JsOverlay
  public final boolean getInteractive() {
    return interactive;
  }

  /** Propagate mouse events to the map. */
  @JsProperty private boolean bubblingMouseEvents;

  @JsOverlay
  public final boolean getBubblingMouseEvents() {
    return bubblingMouseEvents;
  }

  /** Map pane for the overlay. */
  @JsProperty private String pane;

  @JsOverlay
  public final String getPane() {
    return pane;
  }

  /** Overlay stacking order. */
  @JsProperty private double zIndex;

  @JsOverlay
  public final double getZIndex() {
    return zIndex;
  }

  /** CSS class for the video element. */
  @JsProperty private String className;

  @JsOverlay
  public final String getClassName() {
    return className;
  }

  /** Leaves unset properties to Leaflet defaults. */
  public static class Builder {
    public Builder() {}

    private Boolean autoplay;

    /** Start playback automatically, subject to browser policy. */
    public Builder autoplay(Boolean autoplay) {
      this.autoplay = autoplay;
      return this;
    }

    private Boolean loop;

    /** Loop the video. */
    public Builder loop(Boolean loop) {
      this.loop = loop;
      return this;
    }

    private Boolean muted;

    /** Mute playback. */
    public Builder muted(Boolean muted) {
      this.muted = muted;
      return this;
    }

    private Boolean playsInline;

    /** Keep playback inline on mobile browsers. */
    public Builder playsInline(Boolean playsInline) {
      this.playsInline = playsInline;
      return this;
    }

    private Boolean keepAspectRatio;

    /** Preserve the video aspect ratio. */
    public Builder keepAspectRatio(Boolean keepAspectRatio) {
      this.keepAspectRatio = keepAspectRatio;
      return this;
    }

    private Double opacity;

    /** Overlay opacity from zero to one. */
    public Builder opacity(Double opacity) {
      this.opacity = opacity;
      return this;
    }

    private String attribution;

    /** Attribution text. */
    public Builder attribution(String attribution) {
      this.attribution = attribution;
      return this;
    }

    private Boolean interactive;

    /** Allow interaction with the overlay. */
    public Builder interactive(Boolean interactive) {
      this.interactive = interactive;
      return this;
    }

    private Boolean bubblingMouseEvents;

    /** Propagate mouse events to the map. */
    public Builder bubblingMouseEvents(Boolean bubblingMouseEvents) {
      this.bubblingMouseEvents = bubblingMouseEvents;
      return this;
    }

    private String pane;

    /** Map pane for the overlay. */
    public Builder pane(String pane) {
      this.pane = pane;
      return this;
    }

    private Double zIndex;

    /** Overlay stacking order. */
    public Builder zIndex(Double zIndex) {
      this.zIndex = zIndex;
      return this;
    }

    private String className;

    /** CSS class for the video element. */
    public Builder className(String className) {
      this.className = className;
      return this;
    }

    public VideoOverlayOptions build() {
      VideoOverlayOptions options = new VideoOverlayOptions();
      if (this.autoplay != null) options.autoplay = this.autoplay;
      if (this.loop != null) options.loop = this.loop;
      if (this.muted != null) options.muted = this.muted;
      if (this.playsInline != null) options.playsInline = this.playsInline;
      if (this.keepAspectRatio != null) options.keepAspectRatio = this.keepAspectRatio;
      if (this.opacity != null) options.opacity = this.opacity;
      if (this.attribution != null) options.attribution = this.attribution;
      if (this.interactive != null) options.interactive = this.interactive;
      if (this.bubblingMouseEvents != null) options.bubblingMouseEvents = this.bubblingMouseEvents;
      if (this.pane != null) options.pane = this.pane;
      if (this.zIndex != null) options.zIndex = this.zIndex;
      if (this.className != null) options.className = this.className;
      return options;
    }
  }
}
