package id.kertas.smartrider.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.kertas.smartrider.app.AppController;
import id.kertas.smartrider.util.Server;

public class ApiNomorTujuan {

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    public final static String TAG_NOMOR_TUJUAN1 = "nomor_tujuan1";
    public final static String TAG_NOMOR_TUJUAN2 = "nomor_tujuan2";
    public final static String TAG_NOMOR_TUJUAN3 = "nomor_tujuan3";

    private ProgressDialog pDialog;

    private String tag_json_obj = "json_obj_req";

    private int success;

    public String nomor_tujuan1, nomor_tujuan2, nomor_tujuan3;

    public void getNomorTujuan(final Context context, final String TAG, final String username) {
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
        pDialog.setMessage("Logging in ...");
        showDialog();

        String url = Server.URL + "getnomortujuan.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Check Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {
                        nomor_tujuan1 = jObj.getString(TAG_NOMOR_TUJUAN1);
                        nomor_tujuan2 = jObj.getString(TAG_NOMOR_TUJUAN2);
                        nomor_tujuan3 = jObj.getString(TAG_NOMOR_TUJUAN3);

                        Log.d("Successfully Show!", jObj.toString());

                    } else {
                        Toast.makeText(context, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
