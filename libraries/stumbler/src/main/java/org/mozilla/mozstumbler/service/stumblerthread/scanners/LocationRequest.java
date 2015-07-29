/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.service.stumblerthread.scanners;

// All intervals are specified in intervals with millisecond units
public class LocationRequest {

    private long expirationDuration;

    // This is primarily used for cell scans
    private long fastestInterval = 1000;

    private long expirationTime;
    private long interval = 4000;

    private long maxWaitTime;
    private long numUpdates;
    private long priority;
    private long smallestDisplacement;


    public long getWifiInterval() {
        if (wifiInterval == 0) {
            return getInterval();
        }
        return wifiInterval;
    }

    public void setWifiInterval(long wifiInterval) {
        this.wifiInterval = wifiInterval;
    }

    public long getCellScanInterval() {
        if (cellScanInterval == 0) {
            return getFastestInterval();
        }
        return cellScanInterval;
    }

    public void setCellScanInterval(long cellScanInterval) {
        this.cellScanInterval = cellScanInterval;
    }

    private long wifiInterval;
    private long cellScanInterval;

    public long getExpirationDuration() {
        return expirationDuration;
    }

    public long getFastestInterval() {
        return fastestInterval;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public long getInterval() {
        return interval;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public long getNumUpdates() {
        return numUpdates;
    }

    public long getPriority() {
        return priority;
    }

    public long getSmallestDisplacement() {
        return smallestDisplacement;
    }

    /*
     This is roughly equivalent to the interface exposed by
     com.google.android.gms.location.LocationRequest
     */

    LocationRequest setExpirationDuration(long millis) {
        expirationDuration = millis;
        return this;
    }
    LocationRequest setExpirationTime(long millis) {
        expirationTime = millis;
        return this;
    }
    LocationRequest setFastestInterval(long millis) {
        fastestInterval = millis;
        return this;
    }
    LocationRequest setInterval(long millis) {
        interval = millis;
        return this;
    }
    LocationRequest setMaxWaitTime(long millis) {
        maxWaitTime = millis;
        return this;
    }

    LocationRequest setNumUpdates(long num) {
        numUpdates = num;
        return this;
    }

    LocationRequest setPriority(long p) {
        priority = p;
        return this;
    }

    LocationRequest setSmallestDisplacement(long displacement) {
        smallestDisplacement = displacement;
        return this;
    }


}
