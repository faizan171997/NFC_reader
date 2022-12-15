package com.cmpe272.taptoauth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class APIHandler {

    // constants
    public static String BACKEND_URL = "http://10.0.0.215:3000";
    public static String IOT_URL = "http://192.168.4.1";

    // vars
    RequestQueue queue;

    public APIHandler(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public void postAuth(String hash, String id, Callback callback) {
        Log.e("API", "postAuth");

        String url = BACKEND_URL + "/auth";

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.e("API_RESPONSE", "postAuth");
                    callback.run(null);
                },
                Throwable::printStackTrace
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("hash", hash);
                    jsonBody.put("id", id);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 60*1000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(stringRequest);
    }

    public void getUser(String hash, Callback callback) {
        Log.e("API", "getUser");

        String url = BACKEND_URL + "/user?hash=" + hash;

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.e("API_RESPONSE", "getUser");
                    callback.run(response);
                },
                Throwable::printStackTrace
        );

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 60*1000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(stringRequest);
    }

    public void postAddCode(String code, String hash, Callback callback) {
        Log.e("API", "postAddCode");

        String url = BACKEND_URL + "/addCode";

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.e("API_RESPONSE", "postAddCode");
                    callback.run(null);
                },
                Throwable::printStackTrace
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("hash", hash);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("code", code);
                    final String mRequestBody = jsonBody.toString();
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 60*1000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(stringRequest);
    }

    public void postAddCodeIot(String code, Callback callback) {
        Log.e("API", "postAddCodeIot");

        String url = IOT_URL + "/postCode";
        Log.e("SHIT", url);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.e("API_RESPONSE", "postAddCodeIot");
                    callback.run(null);
                },
                Throwable::printStackTrace
        ) {
            @Override
            public String getBodyContentType() {
                return "text/plain; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return code.getBytes(StandardCharsets.UTF_8);
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 60*1000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(stringRequest);
    }

    public void postHashIot(String hash, Callback callback, Callback errorCallback) {
        Log.e("API", "postHashIot");

        String url = IOT_URL + "/postHash";

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.e("API_RESPONSE", "postHashIot");
                    callback.run(null);
                },
                errorCallback::run
        ) {
            @Override
            public String getBodyContentType() {
                return "text/plain; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return hash.getBytes(StandardCharsets.UTF_8);
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 10 * 1000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 10;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(stringRequest);
    }

    public interface Callback {
        public Object run(Object obj);
    }
}