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

import es.dmoral.toasty.Toasty;
import id.kertas.smartrider.app.AppController;
import id.kertas.smartrider.util.Server;

public class ApiKecelakaan {

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private ProgressDialog pDialog;

    private String tag_json_obj = "json_obj_req";

    private int success;

    public void setKecelakaan(final Context context, final String TAG, final String username, final double longitude,
                               final double latitude, final String link_maps, final String waktu_kecelakaan) {
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
        pDialog.setMessage("Detecting ...");
        showDialog();

        String url = Server.URL + "setkecelakaan.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Save Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        Log.d("Successfully Record!", jObj.toString());

                        Toasty.success(context, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG,true).show();

                    } else {
                        Toasty.warning(context, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG,true).show();

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
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("longitude", String.valueOf(longitude));
                params.put("latitude", String.valueOf(latitude));
                params.put("link_maps", link_maps);
                params.put("waktu_kecelakaan", waktu_kecelakaan);

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