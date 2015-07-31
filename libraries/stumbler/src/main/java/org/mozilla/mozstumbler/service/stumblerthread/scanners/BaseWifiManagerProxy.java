/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.service.stumblerthread.scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

import java.util.List;

public class BaseWifiManagerProxy extends BroadcastReceiver {
    static final String LOG_TAG = LoggerUtil.makeLogTag(BaseWifiManagerProxy.class);

    final Context mAppContext;
    WifiScanner mWifiScanner;

    public BaseWifiManagerProxy(Context appContext) {
        mAppContext = appContext.getApplicationContext();
    }

    public boolean isWifiScanEnabled() {
        WifiManager manager = _getWifiManager();
        boolean scanEnabled = manager.isWifiEnabled();
        if (Build.VERSION.SDK_INT >= 18) {
            scanEnabled |= manager.isScanAlwaysAvailable();
        }
        return scanEnabled;
    }

    public boolean runWifiScan() {
        return _getWifiManager().startScan();
    }

    public List<ScanResult> getScanResults() {
        WifiManager manager = _getWifiManager();
        if (manager == null) {
            return null;
        }
        return _getWifiManager().getScanResults();
    }

    private WifiManager _getWifiManager() {
        return (WifiManager) mAppContext.getSystemService(Context.WIFI_SERVICE);
    }

    public synchronized void registerReceiver(WifiScanner wifiScanner) {
        mWifiScanner = wifiScanner;
    }

    public void registerIntentListener() {
        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mAppContext.registerReceiver(this, i);
    }

    public void unregisterReceiver() {
        try {
            mAppContext.unregisterReceiver(this);
        } catch (IllegalArgumentException ex) {
            // doesn't matter - this is safe to ignore as it just means that
            // we've just been running in simulation mode.
        }
    }

    public void onReceive(Context c, Intent intent) {
        mWifiScanner.wifiScanCallback(c, intent);
    }

    public WifiManager.WifiLock createWifiLock() {
        return _getWifiManager().createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "MozStumbler");
    }


}
