/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.client;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.mozilla.mozstumbler.service.AppGlobals;
import org.mozilla.mozstumbler.service.stumblerthread.StumblerService;
import org.mozilla.mozstumbler.service.stumblerthread.datahandling.ClientDataStorageManager;
import org.mozilla.mozstumbler.service.uploadthread.UploadAlarmReceiver;
import org.mozilla.mozstumbler.service.utils.BatteryCheckReceiver;
import org.mozilla.mozstumbler.service.utils.BatteryCheckReceiver.BatteryCheckCallback;
import org.mozilla.mozstumbler.svclocator.ServiceLocator;
import org.mozilla.mozstumbler.svclocator.services.ISystemClock;
import org.mozilla.mozstumbler.svclocator.services.log.ILogger;
import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

// Used as a bound service (with foreground priority) in Mozilla Stumbler, a.k.a. active scanning mode.
// -- In accordance with Android service docs -and experimental findings- this puts the service as low
//    as possible on the Android process kill list.
// -- Binding functions are commented in this class as being unused in the stand-alone service mode.
public class ClientStumblerService extends StumblerService {
    private static final String LOG_TAG = LoggerUtil.makeLogTag(StumblerService.class);
    ILogger Log = (ILogger) ServiceLocator.getInstance().getService(ILogger.class);
    ISystemClock clock = (ISystemClock) ServiceLocator.getInstance().getService(ISystemClock.class);

    private static final String ACTION_BASE = AppGlobals.ACTION_NAMESPACE + ".CLIENT_SVC";

    // These two actions are used to request the service to start or stop gracefully
    public static final String ACTION_ACTIVE_SCANNING_START_REQUEST = ACTION_BASE + ".REQUEST_START";
    public static final String ACTION_ACTIVE_SCANNING_STOP_REQUEST = ACTION_BASE + ".REQUEST_STOP";
    public static final String ACTION_ACTIVE_SCANNING_TOGGLE_REQUEST = ACTION_BASE + ".REQUEST_TOGGLE";
    public static final String ACTION_ACTIVE_SCANNING_RESTART_REQUEST = ACTION_BASE + ".REQUEST_RESTART";

    // These two intents are broadcast only *after* the service has started or stopped
    // scanning.
    public static final String ACTION_ACTIVE_SCANNING_STARTED = ACTION_BASE + ".ACTIVE_SCANNING_STARTED";
    public static final String ACTION_ACTIVE_SCANNING_STOPPED = ACTION_BASE + ".ACTIVE_SCANNING_STOPPED";

    public final long MAX_BYTES_DISK_STORAGE = 1000 * 1000 * 20; // 20MB for Mozilla Stumbler by default, is ok?
    public final int MAX_WEEKS_OLD_STORED = 4;

    private BatteryCheckReceiver mBatteryChecker;


    private final BatteryCheckCallback mBatteryCheckCallback = new BatteryCheckCallback() {
        private boolean waitForBatteryOkBeforeSendingNotification;

        @Override
        public void batteryCheckCallback(BatteryCheckReceiver receiver) {
            int minBattery = ClientPrefs.getInstance(ClientStumblerService.this).getMinBatteryPercent();
            boolean isLow = receiver.isBatteryNotChargingAndLessThan(minBattery);
            if (isLow && !waitForBatteryOkBeforeSendingNotification) {
                waitForBatteryOkBeforeSendingNotification = true;
                LocalBroadcastManager.getInstance(ClientStumblerService.this).
                        sendBroadcast(new Intent(MainApp.ACTION_LOW_BATTERY));
            } else if (receiver.isBatteryNotChargingAndGreaterThan(minBattery)) {
                waitForBatteryOkBeforeSendingNotification = false;
            }
        }
    };



    @Override
    protected void init() {
        ClientDataStorageManager.createGlobalInstance(this.getApplicationContext(),
                MAX_BYTES_DISK_STORAGE,
                MAX_WEEKS_OLD_STORED);
        super.init();
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // Do init() in all cases, there is no cost, whereas it is easy to add code that depends on this.
        init();

        if (intent == null) {
            return;
        }

        boolean hasFilesWaiting = !ClientDataStorageManager.getInstance().isDirEmpty();
        if (AppGlobals.isDebug) {
            Log.d(LOG_TAG, "Files waiting:" + hasFilesWaiting);
        }
        if (hasFilesWaiting) {
            UploadAlarmReceiver.scheduleAlarm(this,
                    FREQUENCY_IN_SEC_OF_UPLOAD_IN_ACTIVE_MODE,
                    false /* no repeat*/);
        }

        if (intent.getAction().equals(ACTION_ACTIVE_SCANNING_START_REQUEST)) {
            startScanning();
        } else if (intent.getAction().equals(ACTION_ACTIVE_SCANNING_STOP_REQUEST)) {
            stopScanning();
        }
        // TODO: add toggle, restart and default action handlers here

    }

    private void stopScanning() {
        if (mScanManager.stopScanning()) {
            mReporter.flush();
        }

        if (mBatteryChecker != null) {
            mBatteryChecker.stop();
        }

        // notify everyone that scanning has started
        LocalBroadcastManager
                .getInstance(this.getApplicationContext())
                .sendBroadcast(new Intent(ACTION_ACTIVE_SCANNING_STOPPED));
        Log.i(LOG_TAG, "Stumbling has stopped!!!");
    }

    @Override
    public synchronized void startScanning() {
        super.startScanning();

        if (mBatteryChecker == null) {
            mBatteryChecker = new BatteryCheckReceiver(this, mBatteryCheckCallback);
        }

        mBatteryChecker.start();

        // notify everyone that scanning has started
        LocalBroadcastManager
                .getInstance(this.getApplicationContext())
                .sendBroadcast(new Intent(ACTION_ACTIVE_SCANNING_STARTED));

        Log.i(LOG_TAG, "Stumbling has started!!!");
    }


}

