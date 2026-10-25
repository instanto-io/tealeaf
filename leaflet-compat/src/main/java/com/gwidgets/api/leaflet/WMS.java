/*
 * Modified for Tealeaf in 2026 from gwty-leaflet 1.2.1:
 * source formatting.
 * Existing upstream notices are retained. See NOTICE and the module's upstream.json.
 */
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
package com.gwidgets.api.leaflet;

import com.gwidgets.api.leaflet.options.TileLayerWMSOptions;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Used to display WMS services as tile layers on the map.
 *
 * @author <a href="mailto:zakaria.amine88@gmail.com">Zakaria Amine</a>
 */
@JsType(isNative = true)
public class WMS extends TileLayer {

  private WMS() {}

  /**
   * Merges an object with the new parameters and re-requests tiles on the current screen (unless
   * noRedraw was set to true).
   *
   * @param params the params
   * @param noRedraw the no redraw
   * @return the l
   */
  @JsMethod
  public static native L setParams(TileLayerWMSOptions params, Boolean noRedraw);
}
