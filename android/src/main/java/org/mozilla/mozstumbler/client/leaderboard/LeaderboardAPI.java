package org.mozilla.mozstumbler.client.leaderboard;

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
public class LeaderboardAPI {

    private static final String LOG_TAG = LoggerUtil.makeLogTag(LeaderboardAPI.class);
    ILogger Log = (ILogger) ServiceLocator.getInstance().getService(ILogger.class);

    private static final String GET_LEADER_URL = LB_SUBMIT_URL + "/api/v1/leaders/profile/";
    private static final String UPDATE_CONTRIBUTOR_NAME_URL = LB_SUBMIT_URL + "/api/v1/contributors/update/" ;


    JSONAPIHelper jsonHelper = new JSONAPIHelper();

    public String getLeaderName(String uid) {
        JSONObject response  = jsonHelper.get(null, GET_LEADER_URL + uid + "/");

        if (response == null) {
            return null;
        }

        try {
            return response.getString("name");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error extracting 'name' from : " + response.toString());
            return null;
        }
    }

    public boolean updateLeaderName(String name, String access_token) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + access_token);
        JSONObject payload = new JSONObject();
        try {
            payload.put("name", name);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error setting name in updateLeader: " + e.toString());
            return false;
        }

        IResponse resp = jsonHelper.post(headers, UPDATE_CONTRIBUTOR_NAME_URL, payload);

        if (resp != null && resp.isSuccessCode2XX()) {
            return true;
        }

        if (resp != null) {
            Log.w(LOG_TAG, "Error updating leader name: " + resp.toString());
        }
        return false;
    }
}
