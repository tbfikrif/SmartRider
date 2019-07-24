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

public class ApiSMSGateway {
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private ProgressDialog pDialog;

    private String tag_json_obj = "json_obj_req";

    private int success;

    public void sendSMS(final Context context, final String TAG, final String action, final String email,
                        final String passkey, final String no_tujuan, final String pesan) {
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
        pDialog.setMessage("Mengirim Pesan ...");
        showDialog();

        String url = "https://reguler.medansms.co.id/sms_api.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Save Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    Log.d("test", jObj.toString());
                    Toast.makeText(context, "Berhasil Terkirim", Toast.LENGTH_SHORT).show();

                    if (success == 1) {
                        Toast.makeText(context, "Berhasil Mengirim Pesan", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Gagal Mengirim Pesan", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.d("test", e.getMessage());
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
                params.put("action", action);
                params.put("email", email);
                params.put("passkey", passkey);
                params.put("no_tujuan", no_tujuan);
                params.put("pesan", pesan);
                params.put("json", "1");

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
