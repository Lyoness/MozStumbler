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
public class GetLeaderName extends AsyncTask<String, Void, JSONObject> {

    private static final String LOG_TAG = LoggerUtil.makeLogTag(GetLeaderName.class);
    final String leaderboardEndpoint;
    final Context mContext;

    public GetLeaderName(Context ctx, String baseURL) {
        mContext = ctx;
        leaderboardEndpoint = baseURL;
    }

    public String getLeaderboardEndpoint() {
        return leaderboardEndpoint;
    }

    protected JSONObject doInBackground(String... strings) {

        Log.i(LOG_TAG, "doInBackground started: " + strings);

        if (strings.length != 1 ||
                TextUtils.isEmpty(strings[0])) {
            Log.i(LOG_TAG, "Error with arguments.  Early abort of " + this.getClass().getCanonicalName() + " args: "+strings);
            return null;
        }

        String uid = strings[0];

        if (TextUtils.isEmpty(uid)) {
            Log.w(LOG_TAG, "Early abort. Contributor UID must be set: [" + uid + "]");
            return null;
        }

        HttpUtil httpUtil = new HttpUtil(System.getProperty("http.agent")  + " " +
                FxAGlobals.appName + "/" + FxAGlobals.appVersionName);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        String getContributorNameUrl =  getLeaderboardEndpoint() + "/api/v1/leaders/profile/"+ uid +"/";

        Log.i(LOG_TAG, "Fetching data from ["+getContributorNameUrl+"]");
        HTTPResponse resp = httpUtil.get(getContributorNameUrl, headers);

        String body = resp.body();
        Log.i(LOG_TAG, "Content body is : " + body);
        try {
            JSONObject jsonBody = new JSONObject(body);
            String tmp =  jsonBody.getString("name");
            JSONObject result = new JSONObject();
            result.put("name", tmp);
            Log.i(LOG_TAG, "Sending JSONObject: " + result);
            return result;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error extracting name from JSON blob." + e);
            Intent intent = new Intent(PreferencesScreen.RECEIVED_LEADERNAME_FAILURE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            return null;
        }
    }

    public AsyncTask<String, Void, JSONObject> execute(String uid) {
        Log.i(LOG_TAG, "Invoking execute on task! uid=["+uid+"]");
        return super.execute(uid);
    }

    @Override
    protected void onPostExecute (JSONObject nameBlob) {
        Log.i(LOG_TAG, "onPostExecute initiated: ["+nameBlob+"]");

        String name = null;
        try {
            name = nameBlob.getString("name");
        } catch (JSONException e) {
            name = "";
        }
        if (TextUtils.isEmpty(name)) {
            name = "_blank_name_";
        }
        Intent intent = new Intent(PreferencesScreen.RECEIVED_LEADERNAME);
        intent.putExtra("name", name);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
