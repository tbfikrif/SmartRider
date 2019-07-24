package id.kertas.smartrider.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.kertas.smartrider.R;
import id.kertas.smartrider.app.AppController;
import id.kertas.smartrider.util.Server;

import static id.kertas.smartrider.activity.MainActivity.TAG_USERNAME;

public class ChangeNumberActivity extends AppCompatActivity {

    private static final String TAG = ChangeNumberActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    public final static String TAG_NOMOR_TLP = "nomor_tlp";
    public final static String TAG_NOMOR_TUJUAN1 = "nomor_tujuan1";
    public final static String TAG_NOMOR_TUJUAN2 = "nomor_tujuan2";
    public final static String TAG_NOMOR_TUJUAN3 = "nomor_tujuan3";

    String tag_json_obj = "json_obj_req";

    private ProgressDialog pDialog;

    private int success;

    private EditText txt_nomor_tujuan1, txt_nomor_tujuan2, txt_nomor_tujuan3;
    private Button btn_change_number;

    SharedPreferences sharedpreferences;
    Boolean session = false;
    private String username;
    public String nomor_tujuan1, nomor_tujuan2, nomor_tujuan3;
    public static final String my_shared_preferences = "my_shared_preferences";
    public static final String session_status = "session_status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_number);

        initializeObject();
        initializeComponent();
        initializeEvent();

        getNomorTujuan(username);
    }

    private void initializeObject(){
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        session = sharedpreferences.getBoolean(session_status, false);
        username = sharedpreferences.getString(TAG_USERNAME, null);
    }

    private void initializeComponent() {
        txt_nomor_tujuan1 = findViewById(R.id.txt_nomor_tujuan1);
        txt_nomor_tujuan2 = findViewById(R.id.txt_nomor_tujuan2);
        txt_nomor_tujuan3 = findViewById(R.id.txt_nomor_tujuan3);
        btn_change_number = findViewById(R.id.btn_change_number);
    }

    private void initializeEvent(){
        btn_change_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nomor_tujuan1 = txt_nomor_tujuan1.getText().toString();
                nomor_tujuan2 = txt_nomor_tujuan2.getText().toString();
                nomor_tujuan3 = txt_nomor_tujuan3.getText().toString();

                setNomorTujuan(username, nomor_tujuan1, nomor_tujuan2, nomor_tujuan3);
            }
        });
    }

    public void getNomorTujuan(final String username) {
        pDialog = new ProgressDialog(this);
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

                        txt_nomor_tujuan1.setText(nomor_tujuan1);
                        txt_nomor_tujuan2.setText(nomor_tujuan2);
                        txt_nomor_tujuan3.setText(nomor_tujuan3);

                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

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
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

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

    private void setNomorTujuan(final String username, final String nomor_tujuan1, final String nomor_tujuan2, final String nomor_tujuan3) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Logging in ...");
        showDialog();

        String url = Server.URL + "setnomortujuan.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Save Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {
                        Log.d("Successfully Change!", jObj.toString());

                        Toast.makeText(getApplicationContext(), jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

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
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("nomor_tujuan1", nomor_tujuan1);
                params.put("nomor_tujuan2", nomor_tujuan2);
                params.put("nomor_tujuan3", nomor_tujuan3);

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
