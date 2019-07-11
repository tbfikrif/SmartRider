package id.kertas.smartrider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import id.kertas.smartrider.app.AppController;
import id.kertas.smartrider.util.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    Button btn_hitung, btn_register;
    TextView btn_login;
    EditText txt_username, txt_password, txt_confirm_password, txt_nama, txt_email, txt_alamat,
            txt_nomor_tlp, txt_nomor_tujuan1, txt_nomor_tujuan2, txt_nomor_tujuan3;
    DatePicker dp_tgl_lahir;
    TextView txt_detak_jantung_normal;
    Intent intent;

    int success;
    ConnectivityManager conMgr;

    private String url = Server.URL + "register.php";

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_register);

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

        btn_hitung = findViewById(R.id.btn_hitung_detak_jantung);
        btn_login = findViewById(R.id.btn_txtlogin);
        btn_register = findViewById(R.id.btn_register);

        txt_username = findViewById(R.id.txt_username);
        txt_password = findViewById(R.id.txt_password);
        txt_confirm_password = findViewById(R.id.txt_confirm_password);
        txt_nama = findViewById(R.id.txt_nama);
        txt_email = findViewById(R.id.txt_email);
        txt_alamat = findViewById(R.id.txt_alamat);
        txt_nomor_tlp = findViewById(R.id.txt_nomor_tlp);
        txt_nomor_tujuan1 = findViewById(R.id.txt_nomor_tujuan_1);
        txt_nomor_tujuan2 = findViewById(R.id.txt_nomor_tujuan_2);
        txt_nomor_tujuan3 = findViewById(R.id.txt_nomor_tujuan_3);
        txt_detak_jantung_normal = findViewById(R.id.txt_detak_jantung_normal);

        dp_tgl_lahir = findViewById(R.id.dp_tgl_lahir);

        btn_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String username = txt_username.getText().toString();
                String password = txt_password.getText().toString();
                String confirm_password = txt_confirm_password.getText().toString();
                String nama = txt_nama.getText().toString();
                String email = txt_email.getText().toString();
                String alamat = txt_alamat.getText().toString();
                String nomor_tlp = txt_nomor_tlp.getText().toString();
                String nomor_tujuan1 = txt_nomor_tujuan1.getText().toString();
                String nomor_tujuan2 = txt_nomor_tujuan2.getText().toString();
                String nomor_tujuan3 = txt_nomor_tujuan3.getText().toString();
                String detak_jantung_normal = txt_detak_jantung_normal.getText().toString();
                String tgl_lahir = dp_tgl_lahir.getYear() + "-" + dp_tgl_lahir.getDayOfMonth() + "-" + dp_tgl_lahir.getMonth();

                if (conMgr.getActiveNetworkInfo() != null
                        && conMgr.getActiveNetworkInfo().isAvailable()
                        && conMgr.getActiveNetworkInfo().isConnected()) {
                    checkRegister(username, password, confirm_password, nama, email, alamat, nomor_tlp, nomor_tujuan1, nomor_tujuan2, nomor_tujuan3,
                            detak_jantung_normal, tgl_lahir);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void checkRegister(final String username, final String password, final String confirm_password, final String nama, final String email,
                               final String alamat, final String nomor_tlp, final String nomor_tujuan1, final String nomor_tujuan2,
                               final String nomor_tujuan3, final String detak_jantung_normal, final String tgl_lahir) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Register ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {

                        Log.e("Successfully Register!", jObj.toString());

                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                        intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
                params.put("confirm_password", confirm_password);
                params.put("nama", nama);
                params.put("email", email);
                params.put("alamat", alamat);
                params.put("nomor_tlp", nomor_tlp);
                params.put("nomor_tujuan1", nomor_tujuan1);
                params.put("nomor_tujuan2", nomor_tujuan2);
                params.put("nomor_tujuan3", nomor_tujuan3);
                params.put("detak_jantung_normal", detak_jantung_normal);
                params.put("tgl_lahir", tgl_lahir);

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

    @Override
    public void onBackPressed() {
        intent = new Intent(RegisterActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}