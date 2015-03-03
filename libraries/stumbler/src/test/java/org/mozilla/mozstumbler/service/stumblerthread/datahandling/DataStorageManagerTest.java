package org.mozilla.mozstumbler.service.stumblerthread.datahandling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.mozstumbler.service.stumblerthread.Reporter;
import org.mozilla.mozstumbler.service.stumblerthread.ReporterTest;
import org.mozilla.mozstumbler.service.stumblerthread.scanners.cellscanner.CellInfo;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DataStorageManagerTest {

    private DataStorageManager dm;
    private Reporter rp;
    private Context ctx;

    private LinkedList<Intent> receivedIntent = new LinkedList<Intent>();
    private final BroadcastReceiver callbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Just capture the intent for testing
            receivedIntent.add(intent);
        }
    };


    @Before
    public void setUp() {
        ctx = Robolectric.application;
        long maxBytes = 20000;
        int maxWeeks = 10;

        // Force the instance to be null on startup
        ClientDataStorageManager.sInstance = null;

        dm = ClientDataStorageManager.createGlobalInstance(ctx, maxBytes, maxWeeks);
        // Force the current reports to clear out between test runs.
        dm.mCurrentReports.clearReports();

        // register a listener for empty storage which should happen on insert()
        LocalBroadcastManager.getInstance(ctx).registerReceiver(callbackReceiver,
                new IntentFilter(DataStorageManager.ACTION_NOTIFY_STORAGE_EMPTY));

        rp = new Reporter();

        // The Reporter class needs a reference to a context
        rp.startup(ctx);
        assertEquals(0, dm.mCurrentReports.reportsCount());

        // Clear any captured intents
        receivedIntent.clear();
    }

    @Test
    public void testMaxReportsLength() throws JSONException, IOException {
        StumblerBundle bundle;

        assertEquals(0, dm.mCurrentReports.reportsCount());
        for (int locCount = 0; locCount < ReportBatchBuilder.MAX_REPORTS_IN_MEMORY - 1; locCount++) {
            Location loc = new Location("mock");
            loc.setLatitude(42 + (locCount * 0.1));
            loc.setLongitude(45 + (locCount * 0.1));

            bundle = new StumblerBundle(loc);

            for (int offset = 0; offset < StumblerBundle.MAX_WIFIS_PER_LOCATION * 20; offset++) {
                String bssid = Long.toHexString(offset | 0xabcd00000000L);
                ScanResult scan = ReporterTest.createScanResult(bssid, "caps", 3, 11, 10);
                bundle.addWifiData(bssid, scan);
            }

            for (int offset = 0; offset < StumblerBundle.MAX_CELLS_PER_LOCATION * 20; offset++) {
                CellInfo cell = ReporterTest.createCellInfo(1, 1, 2000 + offset, 1600199 + offset, 19);
                String key = cell.getCellIdentity();
                bundle.addCellData(key, cell);
            }

            JSONObject mlsObj = bundle.toMLSGeosubmit();
            int wifiCount = mlsObj.getJSONArray(DataStorageContract.ReportsColumns.WIFI).length();
            int cellCount = mlsObj.getJSONArray(DataStorageContract.ReportsColumns.CELL).length();
            try {
                dm.insert(mlsObj.toString(), wifiCount, cellCount);
            } catch (IOException ioEx) {
            }
        }

        assertEquals(ReportBatchBuilder.MAX_REPORTS_IN_MEMORY - 1,
                dm.mCurrentReports.reportsCount());


        Location loc = new Location("mock");
        loc.setLatitude(42);
        loc.setLongitude(45);

        bundle = new StumblerBundle(loc);

        for (int offset = 0; offset < StumblerBundle.MAX_WIFIS_PER_LOCATION * 20; offset++) {
            String bssid = Long.toHexString(offset | 0xabcd00000000L);
            ScanResult scan = ReporterTest.createScanResult(bssid, "caps", 3, 11, 10);
            bundle.addWifiData(bssid, scan);
        }

        for (int offset = 0; offset < StumblerBundle.MAX_CELLS_PER_LOCATION * 20; offset++) {
            CellInfo cell = ReporterTest.createCellInfo(1, 1, 2000 + offset, 1600199 + offset, 19);
            String key = cell.getCellIdentity();
            bundle.addCellData(key, cell);
        }

        JSONObject mlsObj = bundle.toMLSGeosubmit();
        int wifiCount = mlsObj.getJSONArray(DataStorageContract.ReportsColumns.WIFI).length();
        int cellCount = mlsObj.getJSONArray(DataStorageContract.ReportsColumns.CELL).length();
        dm.insert(mlsObj.toString(), wifiCount, cellCount);

        // This should go to 0 as hitting MAX_REPORTS_IN_MEMORY should invoke a flush
        // of collected data to the network
        assertEquals(0, dm.mCurrentReports.reportsCount());

    }

    @Test
    public void testSignalNotEmpty() throws JSONException, IOException {
        StumblerBundle bundle;

        Location loc = new Location("mock");
        loc.setLatitude(42);
        loc.setLongitude(45);

        bundle = new StumblerBundle(loc);

        for (int offset = 0; offset < StumblerBundle.MAX_WIFIS_PER_LOCATION * 20; offset++) {
            String bssid = Long.toHexString(offset | 0xabcd00000000L);
            ScanResult scan = ReporterTest.createScanResult(bssid, "caps", 3, 11, 10);
            bundle.addWifiData(bssid, scan);
        }

        for (int offset = 0; offset < StumblerBundle.MAX_CELLS_PER_LOCATION * 20; offset++) {
            CellInfo cell = ReporterTest.createCellInfo(1, 1, 2000 + offset, 1600199 + offset, 19);
            String key = cell.getCellIdentity();
            bundle.addCellData(key, cell);
        }

        JSONObject mlsObj = bundle.toMLSGeosubmit();
        int wifiCount = mlsObj.getJSONArray(DataStorageContract.ReportsColumns.WIFI).length();
        int cellCount = mlsObj.getJSONArray(DataStorageContract.ReportsColumns.CELL).length();

        assertEquals(0, receivedIntent.size());
        dm.insert(mlsObj.toString(), wifiCount, cellCount);
        assertEquals(1, receivedIntent.size());

        // Now check that we've got the right one
        Intent i = receivedIntent.get(0);
        assertEquals(DataStorageManager.ACTION_NOTIFY_STORAGE_EMPTY, i.getAction());
    }

}