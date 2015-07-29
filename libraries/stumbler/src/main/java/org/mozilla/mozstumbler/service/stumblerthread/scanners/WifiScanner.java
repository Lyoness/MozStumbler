/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.service.stumblerthread.scanners;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.mozilla.mozstumbler.service.AppGlobals;
import org.mozilla.mozstumbler.service.stumblerthread.blocklist.BSSIDBlockList;
import org.mozilla.mozstumbler.service.stumblerthread.blocklist.SSIDBlockList;
import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class WifiScanner implements IWifiScanner {
    public static final String ACTION_BASE = AppGlobals.ACTION_NAMESPACE + ".WifiScanner.";
    public static final String ACTION_WIFIS_SCANNED = ACTION_BASE + "WIFIS_SCANNED";
    public static final String ACTION_WIFIS_SCANNED_ARG_RESULTS = "scan_results";
    public static final String ACTION_WIFIS_SCANNED_ARG_TIME = AppGlobals.ACTION_ARG_TIME;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_WIFI_DISABLED = -1;

    private static final String LOG_TAG = LoggerUtil.makeLogTag(WifiScanner.class);

    private static final long DEFAULT_WIFI_MIN_UPDATE_TIME = 4000; // milliseconds
    private final long WIFI_MIN_UPDATE_TIME;  // milliseconds

    private Context mAppContext;
    private SimulationWifiManagerProxy simulationWifiManagerProxy;
    private boolean mStarted;
    private AtomicInteger mScanCount = new AtomicInteger();
    private WifiLock mWifiLock;
    private Timer mWifiScanTimer;
    private AtomicInteger mVisibleAPs = new AtomicInteger();

    public WifiScanner(LocationRequest lr) {
        WIFI_MIN_UPDATE_TIME = lr.getWifiInterval();
    }

    @Override
    synchronized public void init(Context ctx) {
        mAppContext = ctx.getApplicationContext();
        simulationWifiManagerProxy = new SimulationWifiManagerProxy(mAppContext);
    }

    public static boolean shouldLog(ScanResult scanResult) {
        if (SSIDBlockList.isOptOut(scanResult)) {
            Log.d(LOG_TAG, "Blocked opt-out SSID");
            return false;
        }
        if (BSSIDBlockList.contains(scanResult)) {
            Log.w(LOG_TAG, "Blocked BSSID: " + scanResult);
            return false;
        }
        if (SSIDBlockList.contains(scanResult)) {
            Log.w(LOG_TAG, "Blocked SSID: " + scanResult);
            return false;
        }
        return true;
    }

    private boolean isWifiScanEnabled() {
        return simulationWifiManagerProxy.isWifiScanEnabled();
    }

    private List<ScanResult> getScanResults() {
        return simulationWifiManagerProxy.getScanResults();
    }

    @Override
    public synchronized void start(LocationRequest config_param) {


        // If the scan timer is active, this will reset the number of times it has run
        mScanCount.set(0);

        if (mStarted) {
            return;
        }
        mStarted = true;

        if (isWifiScanEnabled()) {
            activatePeriodicScan();
        }

        simulationWifiManagerProxy.registerReceiver(this);
    }



    public synchronized void stop() {
        if (mStarted) {
            simulationWifiManagerProxy.unregisterReceiver();
        }
        deactivatePeriodicScan();
        mStarted = false;
    }

    public int getVisibleAPCount() {
        return mVisibleAPs.get();
    }

    public synchronized int getStatus() {
        if (!mStarted) {
            return STATUS_IDLE;
        }
        if (mWifiScanTimer == null) {
            return STATUS_WIFI_DISABLED;
        }
        return STATUS_ACTIVE;
    }

    synchronized private void activatePeriodicScan() {
        if (mWifiScanTimer != null) {
            return;
        }

        if (AppGlobals.isDebug) {
            Log.d(LOG_TAG, "Activate Periodic Scan");
        }

        mWifiLock = simulationWifiManagerProxy.createWifiLock();
        mWifiLock.acquire();

        // Ensure that we are constantly scanning for new access points.
        mWifiScanTimer = new Timer();
        mWifiScanTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (mScanCount.incrementAndGet() > AppGlobals.MAX_SCANS_PER_GPS) {
                    stop(); // set mWifiScanTimer to null
                    return;
                }
                if (AppGlobals.isDebug) {
                    Log.d(LOG_TAG, "WiFi Scanning Timer fired");
                }
                simulationWifiManagerProxy.runWifiScan();
            }
        }, 0, WIFI_MIN_UPDATE_TIME);
    }

    synchronized private void deactivatePeriodicScan() {
        if (mWifiScanTimer == null) {
            return;
        }

        if (AppGlobals.isDebug) {
            Log.d(LOG_TAG, "Deactivate periodic scan");
        }

        mWifiLock.release();
        mWifiLock = null;

        mWifiScanTimer.cancel();
        mWifiScanTimer = null;
    }

    public void reportScanResults(ArrayList<ScanResult> scanResults) {
        if (scanResults.isEmpty()) {
            return;
        }

        Intent i = new Intent(ACTION_WIFIS_SCANNED);
        i.putParcelableArrayListExtra(ACTION_WIFIS_SCANNED_ARG_RESULTS, scanResults);
        i.putExtra(ACTION_WIFIS_SCANNED_ARG_TIME, System.currentTimeMillis());
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(i);



        mVisibleAPs.set(scanResults.size());

    }

    public void wifiScanCallback(Context c, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            if (isWifiScanEnabled()) {
                activatePeriodicScan();
            } else {
                deactivatePeriodicScan();
            }
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            final List<ScanResult> scanResultList = simulationWifiManagerProxy.getScanResults();
            if (scanResultList == null) {
                return;
            }

            final ArrayList<ScanResult> scanResults = new ArrayList<ScanResult>();
            for (ScanResult scanResult : scanResultList) {
                scanResult.BSSID = BSSIDBlockList.canonicalizeBSSID(scanResult.BSSID);
                if (shouldLog(scanResult)) {
                    // Once we've checked that we want this scan result, we can safely discard
                    // the SSID and capabilities.
                    scanResult.SSID = "";
                    scanResult.capabilities = "";
                    scanResults.add(scanResult);
                }
            }
            reportScanResults(scanResults);
        }
    }

}
