/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.service.stumblerthread.scanners;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/*
 This interface is used for implementations that simplify the use of the WifiManager
 system service that Android provides.

 We need to export an interface so that you can register a delegate that will receive intents
 and control the IntentFilter
 */
public interface IWifiManagerProxy {

    interface IWifiScannerDelegate {
        // This is conceptually similar to an onReceive() method callback
        // using an IntentFilter, except that implementations of IWifiScanner
        // will probably use instances of WifiManagerProxy which run their
        // own intent listeners. The wifiScanCallback is not named 'onReceive'
        // to make it clear that the callback is not executed by
        // the Android runtime.
        void wifiScanCallback(Context c, Intent intent);
    }

    // This registers a callback site that will have
    void registerDelegate(IWifiScannerDelegate wifiScanner);

    // Allow over-riding the default behavior of the intent listener.
    // Usually you won't need to override these in BaseWifiManagerProxy
    void registerIntentListener();
    void unregisterIntentListener();


    // IWifiScannerDelegate.wifiScanCallback will receive an intent of action
    // WifiManager.SCAN_RESULTS_AVAILABLE_ACTION which is a notification that
    // new scan results are available.  Call getScanResults to fetch actual scan
    // results at that time.
    List<ScanResult> getScanResults();

    // These methods are all implemented by the BaseWifiManagerProxy
    // and are specific to running the low level wifi scan
    boolean isWifiScanEnabled();
    boolean runWifiScan();
    WifiManager.WifiLock createWifiLock();

}
