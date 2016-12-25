package br.com.versalius.akijob.network;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by Giovanne on 28/06/2016.
 */
public interface ResponseCallback {
    void onSuccess(JSONObject response);
    void onFail(VolleyError error);
}