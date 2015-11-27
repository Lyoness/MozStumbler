package org.mozilla.mozstumbler.client.util;

import org.json.JSONObject;

/**
 * Created by victorng on 15-11-26.
 */
public interface IAsyncJSONResponse {

    void processJSONResponse(JSONObject response);

}
