package org.mozilla.mozstumbler.client.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.mozstumbler.service.core.http.IHttpUtil;
import org.mozilla.mozstumbler.service.core.http.IResponse;
import org.mozilla.mozstumbler.svclocator.ServiceLocator;
import org.mozilla.mozstumbler.svclocator.services.log.ILogger;
import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

import java.util.HashMap;

/**
 * Created by victorng on 15-11-26.
 */
public class JSONAPIHelper {

    private static final String LOG_TAG = LoggerUtil.makeLogTag(JSONAPIHelper.class);
    ILogger Log = (ILogger) ServiceLocator.getInstance().getService(ILogger.class);

    IHttpUtil httpUtil = (IHttpUtil) ServiceLocator.getInstance().getService(IHttpUtil.class);

    // Invoke this to do an asynchronous GET and execute the callback
    public JSONObject get(final HashMap<String, String> headers, String url) {
        IResponse httpResponse = httpUtil.get(url, headers);

        try {
            return new JSONObject(httpResponse.body());
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error extracting JSON from response: " + e.toString());
            return null;
        }
    }


    public IResponse post(HashMap<String, String> headers, String updateContributorNameUrl, JSONObject payload) {
        IResponse httpResponse = httpUtil.post(updateContributorNameUrl, payload.toString().getBytes(), headers, false);
        return httpResponse;
    }
}
