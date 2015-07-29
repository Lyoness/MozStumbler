/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.service.stumblerthread.scanners;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import java.util.ArrayList;

/*
 This is a basic interface for a periodic WifiScanner.  Note that this interface
 should not be used to run an immediate wifi scan. If you really need to
 do that, just use the WifiManager system service directly.

 Construction of instances of IWifiScanner is always two phase.

 Call new() and then init(android.content.Context).

 No operations on IWifiScanner instances are safe until *after* the init
 is called.  This is so that any objects that require access to a Context
 can be properly initialized (such as Intent listeners).
 */
public interface IWifiScanner {

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
    void wifiScanCallback(Context c, Intent intent);

    // If you only need to capture the scan results, you probably only care
    // about overloading this method.
    void reportScanResults(ArrayList<ScanResult> scanResults);
}
