package org.mozilla.mozstumbler.client.leaderboard;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.accounts.fxa.FxAGlobals;
import org.mozilla.accounts.fxa.LoggerUtil;
import org.mozilla.accounts.fxa.net.HTTPResponse;
import org.mozilla.accounts.fxa.net.HttpUtil;
import org.mozilla.mozstumbler.client.subactivities.PreferencesScreen;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by victorng on 15-12-01.
 */
public class SetLeaderName extends AsyncTask<String, Void, Boolean> {

    private static final String LOG_TAG = LoggerUtil.makeLogTag(SetLeaderName.class);
    private final String leaderboardEndpoint;
    final Context mContext;

    public SetLeaderName(Context ctx, String baseURL) {
        mContext = ctx.getApplicationContext();
        leaderboardEndpoint = baseURL;
    }

    public String getLeaderboardEndpoint() {
        return leaderboardEndpoint;
    }

    protected Boolean doInBackground(String... strings) {
        if (strings.length != 2 ||
                TextUtils.isEmpty(strings[0]) ||
                TextUtils.isEmpty(strings[1])
                ) {
            return null;
        }

        String bearerToken = strings[0];
        String name = strings[1];

        return setLeaderName(bearerToken, name);
    }

    public AsyncTask<String, Void, Boolean> execute(String bearerToken, String name) {
        return super.execute(bearerToken, name);
    }

    /*
         Set the display name (nickname) for a user on the FxA profile server
         */
    public boolean setLeaderName(String bearerToken, String name) {
        if (TextUtils.isEmpty(bearerToken) || TextUtils.isEmpty(name)) {
            Log.w(LOG_TAG, "Contributor name and bearer token must be set: [" + bearerToken +
                    ", " + name + "]");
            return false;
        }


        HttpUtil httpUtil = new HttpUtil(System.getProperty("http.agent")  + " " +
                FxAGlobals.appName + "/" + FxAGlobals.appVersionName);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + bearerToken);
        headers.put("Content-Type", "application/json");
        String updateContributorNameUrl =  getLeaderboardEndpoint() + "/api/v1/contributors/update/";


        JSONObject blob = new JSONObject();
        try {
            blob.put("name", name);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error setting name: ["+name+"]", e);
            return false;
        }

        HTTPResponse resp = httpUtil.post(updateContributorNameUrl,
                blob.toString().getBytes(),
                headers);

        if (resp.isSuccessCode2XX()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute (Boolean result) {
        if (!result) {
            Intent intent = new Intent(PreferencesScreen.UPDATE_LEADERNAME_FAILURE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            return;
        }

        Intent intent = new Intent(PreferencesScreen.UPDATE_LEADERNAME);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
