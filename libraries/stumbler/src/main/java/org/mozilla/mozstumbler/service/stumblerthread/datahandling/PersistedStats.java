/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.mozstumbler.service.stumblerthread.datahandling;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.mozilla.mozstumbler.service.AppGlobals;
import org.mozilla.mozstumbler.service.utils.TelemetryWrapper;
import org.mozilla.mozstumbler.svclocator.ServiceLocator;
import org.mozilla.mozstumbler.svclocator.services.ISystemClock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PersistedStats {
    private final File mStatsFile;
    private final Context mContext;

    public static final String ACTION_PERSISTENT_SYNC_STATUS_UPDATED = AppGlobals.ACTION_NAMESPACE + ".PERSISTENT_SYNC_STATUS_UPDATED";
    public static final String EXTRAS_PERSISTENT_SYNC_STATUS_UPDATED = ACTION_PERSISTENT_SYNC_STATUS_UPDATED + ".EXTRA";

    private ISystemClock clock = (ISystemClock) ServiceLocator.getInstance().getService(ISystemClock.class);

    public PersistedStats(String baseDir, Context context) {
        mStatsFile = new File(baseDir, "upload_stats.ini");
        mContext = context.getApplicationContext();
    }

    void forceBroadcastOfSyncStats() {
        try {
            sendToListeners(readSyncStats());
        } catch (IOException ex) {}
    }

    public synchronized Properties readSyncStats() throws IOException {
        if (!mStatsFile.exists()) {
            return createStatsProp(clock.currentTimeMillis(), 0, 0, 0, 0, 0);
        }

        final FileInputStream input = new FileInputStream(mStatsFile);
        try {
            final Properties props = new Properties();
            props.load(input);
            return props;
        } finally {
            input.close();
        }
    }

    public synchronized void incrementSyncStats(long bytesSent, long reports, long cells, long wifis) throws IOException {
        if (reports + cells + wifis < 1) {
            return;
        }

        Properties properties = readSyncStats();
        final long time = clock.currentTimeMillis();
        final long lastUploadTime = Long.parseLong(properties.getProperty(DataStorageContract.Stats.KEY_LAST_UPLOAD_TIME, "0"));
        final long storedObsPerDay = Long.parseLong(properties.getProperty(DataStorageContract.Stats.KEY_OBSERVATIONS_PER_DAY, "0"));
        long observationsToday = reports;
        if (lastUploadTime > 0) {
            long dayLastUploaded = TimeUnit.MILLISECONDS.toDays(lastUploadTime);
            long dayDiff = TimeUnit.MILLISECONDS.toDays(time) - dayLastUploaded;
            if (dayDiff > 0) {
                // send value of store obs per day
                TelemetryWrapper.addToHistogram(AppGlobals.TELEMETRY_OBSERVATIONS_PER_DAY,
                        Long.valueOf(storedObsPerDay / dayDiff).intValue());
            } else {
                observationsToday += storedObsPerDay;
            }
        }

        Properties newProps = writeSyncStats(time,
                Long.parseLong(properties.getProperty(DataStorageContract.Stats.KEY_BYTES_SENT, "0")) + bytesSent,
                Long.parseLong(properties.getProperty(DataStorageContract.Stats.KEY_OBSERVATIONS_SENT, "0")) + reports,
                Long.parseLong(properties.getProperty(DataStorageContract.Stats.KEY_CELLS_SENT, "0")) + cells,
                Long.parseLong(properties.getProperty(DataStorageContract.Stats.KEY_WIFIS_SENT, "0")) + wifis,
                observationsToday);

        sendToListeners(newProps);

        final int timeDiffSec = Long.valueOf((time - lastUploadTime) / 1000).intValue();
        if (lastUploadTime > 0 && timeDiffSec > 0) {
            TelemetryWrapper.addToHistogram(AppGlobals.TELEMETRY_TIME_BETWEEN_UPLOADS_SEC, timeDiffSec);
            TelemetryWrapper.addToHistogram(AppGlobals.TELEMETRY_BYTES_UPLOADED_PER_SEC, Long.valueOf(bytesSent).intValue() / timeDiffSec);
        }
        TelemetryWrapper.addToHistogram(AppGlobals.TELEMETRY_BYTES_PER_UPLOAD, Long.valueOf(bytesSent).intValue());
        TelemetryWrapper.addToHistogram(AppGlobals.TELEMETRY_OBSERVATIONS_PER_UPLOAD, Long.valueOf(reports).intValue());
        TelemetryWrapper.addToHistogram(AppGlobals.TELEMETRY_WIFIS_PER_UPLOAD, Long.valueOf(wifis).intValue());
        TelemetryWrapper.addToHistogram(AppGlobals.TELEMETRY_CELLS_PER_UPLOAD, Long.valueOf(cells).intValue());
    }

    private void sendToListeners(Properties newProps) {
        if (newProps == null || newProps.keySet().size() < 1) {
            System.out.println("Nothing to send!");
            return;
        }

        Intent intent = new Intent();
        intent.setAction(ACTION_PERSISTENT_SYNC_STATUS_UPDATED);
        Bundle extras = new Bundle();
        extras.putSerializable(EXTRAS_PERSISTENT_SYNC_STATUS_UPDATED, newProps);
        intent.putExtras(extras);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        System.out.println("Send messages!");

    }

    private synchronized Properties writeSyncStats(long time, long bytesSent, long totalObs,
                                            long totalCells, long totalWifis, long obsPerDay) throws IOException {
        final FileOutputStream out = new FileOutputStream(mStatsFile);
        final Properties props = createStatsProp(time, bytesSent, totalObs, totalCells, totalWifis, obsPerDay);

        try {
            props.store(out, null);
        } finally {
            out.close();
        }
        return props;
    }

    private Properties createStatsProp(long time, long bytesSent, long totalObs, long totalCells, long totalWifis, long obsPerDay) {
        final Properties props = new Properties();
        props.setProperty(DataStorageContract.Stats.KEY_LAST_UPLOAD_TIME, String.valueOf(time));
        props.setProperty(DataStorageContract.Stats.KEY_BYTES_SENT, String.valueOf(bytesSent));
        props.setProperty(DataStorageContract.Stats.KEY_OBSERVATIONS_SENT, String.valueOf(totalObs));
        props.setProperty(DataStorageContract.Stats.KEY_CELLS_SENT, String.valueOf(totalCells));
        props.setProperty(DataStorageContract.Stats.KEY_WIFIS_SENT, String.valueOf(totalWifis));
        props.setProperty(DataStorageContract.Stats.KEY_VERSION, String.valueOf(DataStorageContract.Stats.VERSION_CODE));
        props.setProperty(DataStorageContract.Stats.KEY_OBSERVATIONS_PER_DAY, String.valueOf(obsPerDay));
        return props;
    }
}
