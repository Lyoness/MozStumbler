/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.mozstumbler.client.navdrawer;

import android.content.Context;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mozilla.mozstumbler.R;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MetricsViewTest {

    private Context ctx;
    private MainDrawerActivity activity;

    @Before
    public void setUp() throws Exception {
        ctx = Robolectric.application;
        activity = Robolectric.newInstanceOf(MainDrawerActivity.class);
        activity = spy(activity);
    }

    @Test
    public void testActivityShouldNotBeNull() {
        assertNotNull(activity);

        View mockView = spy(View.class);

        doReturn(ctx).when(mockView).getContext();

        MetricsView metricsView = spy(new MetricsView(mockView));
        doNothing().when(metricsView).updateShowMLS();
        doNothing().when(metricsView).updatePowerSavingsLabels();
        doNothing().when(metricsView).updateQueuedStats();
        doNothing().when(metricsView).updateThisSessionStats();
        doNothing().when(metricsView).setUploadButtonToSyncing(anyBoolean());
        doNothing().when(metricsView).sendLastUploadedTime();

        //doNothing().when(metricsView).updateSentStats();

        metricsView.update();

        // this is silly, but we need to check that updateSentStats was invoked once
        // after the update method was called.
        verify(metricsView, times(1)).sendLastUploadedTime();
    }

}

