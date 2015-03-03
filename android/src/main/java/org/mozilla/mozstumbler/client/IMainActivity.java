/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.client;

public interface IMainActivity {
    // Call from main thread only
    public void setUploadState(boolean isUploadingObservations);

    // Call from main thread only
    public void keepScreenOn(boolean isEnabled);

    // Call from main thread only
    public void isPausedDueToNoMotion(boolean isPaused);

    // Call from main thread only
    // unclear what this does
    public void stop();

    public void startStumblerService();

    public void stopStumblerService();

    public void toggleStumblerService();

    public void restartStumblerService();

}

