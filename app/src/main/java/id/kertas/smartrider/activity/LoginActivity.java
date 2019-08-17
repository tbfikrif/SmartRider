package id.kertas.smartrider.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private Button btn_login;
    private Button btn_daftar;

    private ProgressDialog pDialog;
    private EditText txt_username, txt_password;
    private Intent intent;

    int success;
    ConnectivityManager conMgr;

    private String url = Server.URL + "login.php";
    private String deviceName;
    private String deviceAddress;

    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public final static String TAG_USERNAME = "username";
    public final static String TAG_NAMA = "nama";
    public final static String TAG_NOMOR_TLP = "nomor_tlp";
    public final static String TAG_NOMOR_TUJUAN = "nomor_tujuan1";

    String tag_json_obj = "json_obj_req";

    SharedPreferences sharedpreferences;
    Boolean session = false;
    String nama, username;
    public static final String my_shared_preferences = "my_shared_preferences";
    public static final String session_status = "session_status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_login);

        initializeConnection();
        initializeComponents();
        initializeEvents();
    }

    void initializeConnection() {
        conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    void initializeComponents() {
        deviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        btn_login = findViewById(R.id.btn_login);
        btn_daftar = findViewById(R.id.btndftr);
        txt_username = findViewById(R.id.username);
        txt_password = findViewById(R.id.password);
    }

    void initializeEvents() {
        // Cek session login jika TRUE maka langsung buka MainActivity
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        session = sharedpreferences.getBoolean(session_status, false);
        nama = sharedpreferences.getString(TAG_NAMA, null);
        username = sharedpreferences.getString(TAG_USERNAME, null);

        if (session) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(TAG_NAMA, nama);
            intent.putExtra(TAG_USERNAME, username);
            intent.putExtra(EXTRAS_DEVICE_NAME, deviceName);
            intent.putExtra(EXTRAS_DEVICE_ADDRESS, deviceAddress);
            finish();
            startActivity(intent);
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = txt_username.getText().toString();
                String password = txt_password.getText().toString();

                // mengecek kolom yang kosong
                if (username.trim().length() > 0 && password.trim().length() > 0) {
                    if (conMgr.getActiveNetworkInfo() != null
                            && conMgr.getActiveNetworkInfo().isAvailable()
                            && conMgr.getActiveNetworkInfo().isConnected()) {
                        checkLogin(username, password);
                    } else {
                        Toast.makeText(getApplicationContext() ,"No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                } else if (!(username.trim().length() > 0)){
                    Toast.makeText(getApplicationContext() ,"Username tidak boleh kosong", Toast.LENGTH_LONG).show();
                    txt_username.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_username, InputMethodManager.SHOW_IMPLICIT);
                } else if (!(password.trim().length() > 0)){
                    Toast.makeText(getApplicationContext() ,"Password tidak boleh kosong", Toast.LENGTH_LONG).show();
                    txt_password.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_password, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        btn_daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra(EXTRAS_DEVICE_NAME, deviceName);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, deviceAddress);
                startActivity(intent);
            }
        });
    }


    private void checkLogin(final String username, final String password) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Login Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {
                        String username = jObj.getString(TAG_USERNAME);
                        String nama = jObj.getString(TAG_NAMA);

                        Log.d("Successfully Login!", jObj.toString());

                        Toast.makeText(getApplicationContext(), jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                        // menyimpan login ke session
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(session_status, true);
                        editor.putString(TAG_NAMA, nama);
                        editor.putString(TAG_USERNAME, username);
                        editor.apply();

                        // Memanggil device_scan activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(TAG_NAMA, nama);
                        intent.putExtra(TAG_USERNAME, username);
                        intent.putExtra(EXTRAS_DEVICE_NAME, deviceName);
                        intent.putExtra(EXTRAS_DEVICE_ADDRESS, deviceAddress);
                        finish();
                        startActivity(intent);
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
                Log.e(TAG, "Login Error: " + error.getMessage());
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
                params.put("password", password);

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
