package org.mozilla.mozstumbler.client.util;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.mozstumbler.service.core.http.IHttpUtil;
import org.mozilla.mozstumbler.service.core.http.IResponse;
import org.mozilla.mozstumbler.svclocator.ServiceLocator;
import org.mozilla.mozstumbler.svclocator.services.log.ILogger;
import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by victorng on 15-11-26.
 */
public class JSONAPIHelper {

    private static final String LOG_TAG = LoggerUtil.makeLogTag(JSONAPIHelper.class);
    ILogger Log = (ILogger) ServiceLocator.getInstance().getService(ILogger.class);

    IHttpUtil httpUtil = (IHttpUtil) ServiceLocator.getInstance().getService(IHttpUtil.class);
    // Invoke this to do an asynchronous GET and execute the callback
    public JSONObject getLeader(String url) {
        IHttpUtil httpUtil = (IHttpUtil) ServiceLocator.getInstance().getService(IHttpUtil.class);
        try {
            return new JSONObject(httpUtil.getUrlAsString(url));
        } catch (Exception e) {
           Log.e(LOG_TAG, "Error with get URL" + e.toString());
            return null;
        }
    }

    public IResponse post(HashMap<String, String> headers, String updateContributorNameUrl, JSONObject payload) {
        IResponse httpResponse = httpUtil.post(updateContributorNameUrl, payload.toString().getBytes(), headers, false);
        return httpResponse;
    }

    public JSONObject get(String url) {
        IHttpUtil httpUtil = (IHttpUtil) ServiceLocator.getInstance().getService(IHttpUtil.class);
        try {
            JSONObject result = new JSONObject(httpUtil.getUrlAsString(url));
            Log.i(LOG_TAG, "Found a real JSON result: " + result);
            return result;
        } catch (Throwable thr) {
            Log.e(LOG_TAG, "Error getting URL as a string", thr);
        }
        return null;
    }
}
