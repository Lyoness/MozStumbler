/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.service.stumblerthread.scanners;

public class LocationRequestConfig {

    private long expirationDuration;
    private long fastestInterval;
    private long expirationTime;
    private long interval;
    private long maxWaitTime;
    private long numUpdates;
    private long priority;
    private long smallestDisplacement;


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

    LocationRequestConfig setExpirationDuration(long millis) {
        expirationDuration = millis;
        return this;
    }
    LocationRequestConfig setExpirationTime(long millis) {
        expirationTime = millis;
        return this;
    }
    LocationRequestConfig setFastestInterval(long millis) {
        fastestInterval = millis;
        return this;
    }
    LocationRequestConfig setInterval(long millis) {
        interval = millis;
        return this;
    }
    LocationRequestConfig setMaxWaitTime(long millis) {
        maxWaitTime = millis;
        return this;
    }
    
    LocationRequestConfig setNumUpdates(long num) {
        numUpdates = num;
        return this;
    }

    LocationRequestConfig setPriority(long p) {
        priority = p;
        return this;
    }

    LocationRequestConfig setSmallestDisplacement(long displacement) {
        smallestDisplacement = displacement;
        return this;
    }


}
