package org.mozilla.mozstumbler.service.stumblerthread.scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.mozilla.mozstumbler.service.Prefs;
import org.mozilla.mozstumbler.service.core.logging.ClientLog;
import org.mozilla.mozstumbler.svclocator.ServiceLocator;
import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

import java.util.LinkedList;
import java.util.List;

/*
 This class provides an abstraction around android.net.wifi.WifiManager
 so that we can properly mock it out and simulate inbound intents.
 */
public class WifiManagerProxy extends BaseWifiManagerProxy {
    private static final String LOG_TAG = LoggerUtil.makeLogTag(WifiManagerProxy.class);

    public WifiManagerProxy(Context appContext) {
        super(appContext);
    }

    @Override
    public boolean runWifiScan() {
        if (Prefs.getInstance(mAppContext).isSimulateStumble()) {
            // This intent will signal the WifiScanner class to ask for new scan results
            // by invoking getScanResults
            Intent i = new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            onReceive(mAppContext, i);
            return true;
        }

        // Fallback to baseclass behavior
        return super.runWifiScan();
    }

    @Override
    public List<ScanResult> getScanResults() {
        if (Prefs.getInstance(mAppContext).isSimulateStumble()) {
            LinkedList<ScanResult> result = new LinkedList<ScanResult>();

            ISimulatorService simSvc = (ISimulatorService) ServiceLocator.getInstance()
                    .getService(ISimulatorService.class);

            try {
                List<ScanResult> wifiBlock = simSvc.getNextMockWifiBlock();
                // fetch scan results from the context
                if (wifiBlock != null) {
                    result.addAll(wifiBlock);
                }
            } catch (ClassCastException ex) {
                ClientLog.e(LOG_TAG, "Simulation was enabled, but invalid context was found", ex);
            }
            return result;
        }

        return super.getScanResults();
    }

    @Override
    public synchronized void registerReceiver(WifiScanner wifiScanner) {
        super.registerReceiver(wifiScanner);
        if (!Prefs.getInstance(mAppContext).isSimulateStumble()) {
            IntentFilter i = new IntentFilter();
            i.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mAppContext.registerReceiver(this, i);
        }
    }
}
