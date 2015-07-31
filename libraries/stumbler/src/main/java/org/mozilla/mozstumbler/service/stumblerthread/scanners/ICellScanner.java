/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.service.stumblerthread.scanners;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import java.util.ArrayList;

public interface ICellScanner {
    // Always call init with a non-null Context object before use.
    // Any intent listener registration should happen at the end of init().
    void init(Context ctx);

    // Start scanning with some configuration options
    // This method should be synchronized
    void start(LocationRequest config_param);

    void stop();

    // Instances of IWifiScanner must send wifi scans into this method
    // This is conceptually similar to an onReceive() method callback
    // using an IntentFilter, except that implementations of IWifiScanner
    // will probably use instances of WifiManagerProxy which run their
    // own intent listeners. The wifiScanCallback is not named 'onReceive'
    // to make it clear that the callback is not executed by
    // the Android runtime.
    void cellScanCallback(Context c, Intent intent);

}
