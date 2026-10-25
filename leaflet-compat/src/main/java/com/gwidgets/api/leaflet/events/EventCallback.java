/*
 * Modified for Tealeaf in 2026 from gwty-leaflet 1.2.1:
 * source formatting.
 * Existing upstream notices are retained. See NOTICE and the module's upstream.json.
 */
package com.gwidgets.api.leaflet.events;

import jsinterop.annotations.JsFunction;

/** Created by zakaria on 7/2/2017. */
@JsFunction
public interface EventCallback<T extends Event> {

  void call(T event);
}
