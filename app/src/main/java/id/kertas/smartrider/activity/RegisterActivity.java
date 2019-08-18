package id.kertas.smartrider.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import es.dmoral.toasty.Toasty;
import id.kertas.smartrider.R;
import id.kertas.smartrider.app.AppController;
import id.kertas.smartrider.util.CustomBluetoothProfile;
import id.kertas.smartrider.util.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    Boolean isListeningHeartRate = false;
    SharedPreferences sharedpreferences;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    private String mDeviceName;
    private String mDeviceAddress;
    private int heartRateValue;

    ProgressDialog pDialog;
    Button btn_hitung, btn_register;
    TextView btn_login;
    EditText txt_username, txt_password, txt_confirm_password, txt_nama, txt_email, txt_alamat,
            txt_nomor_tlp, txt_nomor_tujuan1, txt_nomor_tujuan2, txt_nomor_tujuan3;
    TextInputLayout inputLayoutUsername, inputLayoutPassword, inputLayoutPassswordConfirmation, inputLayoutNama, inputLayoutEmail,
            inputLayoutAlamat, inputLayoutNomorTlp, inputLayoutNomorTujuan1, inputLayoutNomorTujuan2, inputLayoutNomorTujuan3;
    DatePicker dp_tgl_lahir;
    TextView txt_detak_jantung_normal;
    Intent intent;

    int success;
    ConnectivityManager conMgr;

    private String url = Server.URL + "register.php";

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
                showTopToastError("No Internet Connection");
            }
        }


        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

        mDeviceName = sharedpreferences.getString(DeviceScanActivity.EXTRAS_DEVICE_NAME, null);
        mDeviceAddress = sharedpreferences.getString(DeviceScanActivity.EXTRAS_DEVICE_ADDRESS, null);

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

        inputLayoutUsername = findViewById(R.id.input_layout_username);
        inputLayoutPassword = findViewById(R.id.input_layout_password);
        inputLayoutPassswordConfirmation = findViewById(R.id.input_layout_password_confirmation);
        inputLayoutNama = findViewById(R.id.input_layout_nama);
        inputLayoutEmail = findViewById(R.id.input_layout_email);
        inputLayoutAlamat = findViewById(R.id.input_layout_alamat);
        inputLayoutNomorTlp = findViewById(R.id.input_layout_nomor_tlp);
        inputLayoutNomorTujuan1 = findViewById(R.id.input_layout_nomor_tujuan1);
        inputLayoutNomorTujuan2 = findViewById(R.id.input_layout_nomor_tujuan2);
        inputLayoutNomorTujuan3 = findViewById(R.id.input_layout_nomor_tujuan3);

        txt_username.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(txt_username, InputMethodManager.SHOW_IMPLICIT);

        dp_tgl_lahir = findViewById(R.id.dp_tgl_lahir);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btn_hitung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnecting();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Boolean isValid = true;
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

                if (username.isEmpty()) {
                    inputLayoutUsername.setError("Username tidak boleh kosong");
                    isValid = false;
                } else inputLayoutUsername.setErrorEnabled(false);

                if (password.isEmpty()) {
                    inputLayoutPassword.setError("Password tidak boleh kosong");
                    isValid = false;
                } else inputLayoutPassword.setErrorEnabled(false);

                if (confirm_password.isEmpty()) {
                    inputLayoutPassswordConfirmation.setError("Password Konfirmasi tidak sama");
                    isValid = false;
                } else inputLayoutPassword.setErrorEnabled(false);

                if (nama.isEmpty()) {
                    inputLayoutNama.setError("Nama tidak boleh kosong");
                    isValid = false;
                } else inputLayoutPassword.setErrorEnabled(false);

                if (email.isEmpty()) {
                    inputLayoutEmail.setError("Email tidak boleh kosong");
                    isValid = false;
                } else inputLayoutPassword.setErrorEnabled(false);

                if (alamat.isEmpty()) {
                    inputLayoutAlamat.setError("Alamat tidak boleh kosong");
                    isValid = false;
                } else inputLayoutPassword.setErrorEnabled(false);

                if (nomor_tlp.isEmpty()) {
                    inputLayoutNomorTlp.setError("Nomor Telepon tidak boleh kosong");
                    isValid = false;
                } else inputLayoutPassword.setErrorEnabled(false);

                if (nomor_tujuan1.isEmpty()) {
                    inputLayoutNomorTujuan1.setError("Nomor Tujuan 1 tidak boleh kosong");
                    isValid = false;
                } else inputLayoutPassword.setErrorEnabled(false);

                if (tgl_lahir.isEmpty()) {
                    dp_tgl_lahir.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(dp_tgl_lahir, InputMethodManager.SHOW_IMPLICIT);
                    isValid = false;
                }

                if (username.isEmpty()) {
                    txt_username.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_username, InputMethodManager.SHOW_IMPLICIT);
                } else if (password.isEmpty()) {
                    txt_password.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_password, InputMethodManager.SHOW_IMPLICIT);
                } else if (confirm_password.isEmpty()) {
                    txt_confirm_password.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_confirm_password, InputMethodManager.SHOW_IMPLICIT);
                } else if (nama.isEmpty()) {
                    txt_nama.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_nama, InputMethodManager.SHOW_IMPLICIT);
                } else if (email.isEmpty()) {
                    txt_email.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_email, InputMethodManager.SHOW_IMPLICIT);
                } else if (alamat.isEmpty()) {
                    txt_alamat.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_alamat, InputMethodManager.SHOW_IMPLICIT);
                } else if (nomor_tlp.isEmpty()) {
                    txt_nomor_tlp.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_nomor_tlp, InputMethodManager.SHOW_IMPLICIT);
                } else if (nomor_tujuan1.isEmpty()) {
                    txt_nomor_tujuan1.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_nomor_tujuan1, InputMethodManager.SHOW_IMPLICIT);
                } else if (tgl_lahir.isEmpty()) {
                    dp_tgl_lahir.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(dp_tgl_lahir, InputMethodManager.SHOW_IMPLICIT);
                }

                if (detak_jantung_normal.contains("Memindai"))
                    detak_jantung_normal = "80";

                if (isValid) {
                    if (conMgr.getActiveNetworkInfo() != null
                            && conMgr.getActiveNetworkInfo().isAvailable()
                            && conMgr.getActiveNetworkInfo().isConnected()) {
                        checkRegister(username, password, confirm_password, nama, email, alamat, nomor_tlp, nomor_tujuan1, nomor_tujuan2, nomor_tujuan3,
                                detak_jantung_normal, tgl_lahir);
                    } else {
                        showTopToastError("No Internet Connection");
                    }
                }
            }
        });

    }

    void startConnecting() {
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress);

        Log.v("test", "Connecting to " + mDeviceAddress);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

        Handler heartRateHandler = new Handler();
        heartRateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startScanHeartRate();
            }
        }, 1000);
    }

    void stateConnected() {
        bluetoothGatt.discoverServices();
        //txtState.setText("Terhubung");
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();
        //txtState.setText("Terputus");
    }

    void startScanHeartRate() {
        txt_detak_jantung_normal.setText("Memindai ...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[]{21, 2, 1});
        bluetoothGatt.writeCharacteristic(bchar);
    }

    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        isListeningHeartRate = true;
    }

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            byte[] data = characteristic.getValue();
            byte[] slice = Arrays.copyOfRange(data, 1, 2);
            heartRateValue = slice[0];
            txt_detak_jantung_normal.setText(Integer.toString(heartRateValue));
            while (heartRateValue < 60) {
                startConnecting();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            byte[] slice = Arrays.copyOfRange(data, 1, 2);
            heartRateValue = slice[0];
            txt_detak_jantung_normal.setText(Integer.toString(heartRateValue));
            while (heartRateValue < 60) {
                startConnecting();
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };

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

                        showTopToastSuccess(jObj.getString(TAG_MESSAGE));

                        intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        finish();
                        startActivity(intent);

                    } else {
                        showTopToastError(jObj.getString(TAG_MESSAGE));
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
                showTopToastError(error.getMessage());

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

    public void showTopToastError(String msg) {
        Toast toasty = Toasty.error(getApplicationContext(), msg, Toasty.LENGTH_LONG, true);
        toasty.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toasty.show();
    }

    public void showTopToastSuccess(String msg) {
        Toast toasty = Toasty.success(getApplicationContext(), msg, Toasty.LENGTH_LONG, true);
        toasty.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toasty.show();
    }
}