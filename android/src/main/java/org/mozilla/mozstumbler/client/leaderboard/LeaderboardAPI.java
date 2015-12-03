package org.mozilla.mozstumbler.client.leaderboard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.mozstumbler.BuildConfig;
import org.mozilla.mozstumbler.client.util.JSONAPIHelper;
import org.mozilla.mozstumbler.service.core.http.IResponse;
import org.mozilla.mozstumbler.svclocator.ServiceLocator;
import org.mozilla.mozstumbler.svclocator.services.log.ILogger;
import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

import java.util.HashMap;

import static org.mozilla.mozstumbler.BuildConfig.*;

/**
 * Created by victorng on 15-11-26.
 */
public class LeaderboardAPI implements ILeaderboardAPI {

    private static final String LOG_TAG = LoggerUtil.makeLogTag(LeaderboardAPI.class);
    private final Context mContext;
    ILogger Log = (ILogger) ServiceLocator.getInstance().getService(ILogger.class);

    JSONAPIHelper jsonHelper = new JSONAPIHelper();

    public LeaderboardAPI(Context ctx) {
        mContext = ctx.getApplicationContext();
    }

    public void postLeaderboardReadRequest(String uid) {
        Log.i(LOG_TAG, "postLeaderboardReadRequest started: ["+uid+"]");
        GetLeaderName task = new GetLeaderName(mContext, BuildConfig.LB_SUBMIT_URL);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uid);
        else
            task.execute(uid);


        Log.i(LOG_TAG, "GetLeaderName::execute returned");
    }

    public boolean updateLeaderName(String name, String access_token) {

        // TODO
        return false;
    }
}
