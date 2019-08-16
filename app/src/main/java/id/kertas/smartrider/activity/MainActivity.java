package id.kertas.smartrider.activity;

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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import id.kertas.smartrider.R;
import id.kertas.smartrider.api.ApiClient;
import id.kertas.smartrider.api.ApiInterface;
import id.kertas.smartrider.api.ApiKecelakaan;
import id.kertas.smartrider.api.ApiMengantuk;
import id.kertas.smartrider.api.ApiNomorTujuan;
import id.kertas.smartrider.api.ApiPengguna;
import id.kertas.smartrider.api.ApiSMSGateway;
import id.kertas.smartrider.model.MessageResponse;
import id.kertas.smartrider.util.Config;
import id.kertas.smartrider.util.CustomBluetoothProfile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    Boolean isListeningHeartRate = false;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    Button btnStartConnecting, btnStopConnecting, btnStopVibrate, btnDemoAlarm, btnDemoSendInformation;
    TextView txtState, txtHeartValue, txtProcess, txtX, txtY, txtZ, txtAcceleration, txt_nama;
    private String mDeviceName;
    private String mDeviceAddress;
    private int heartRateValue;
    private int restHeartRate;
    private int normalHeartRate;

    private Handler heartRateHandler;
    private Runnable heartRateRunnable;
    private MediaPlayer weakupAlarm;
    private Vibrator vibrator;

    private SensorManager sensorManager;
    private boolean color = false;
    private boolean alarm_sound = true, alarm_vibrate = true, riding, drowse = false;
    private long lastUpdate;
    private String FROM_NUMBER = "", TO_NUMBER = "", MESSAGE = "";
    private String currentTime;
    private SimpleDateFormat sdf;

    private String name, number, link;

    private FusedLocationProviderClient client;
    private double latitude, longtitude;

    String nama, username;
    SharedPreferences sharedpreferences;

    public static final String TAG_NAMA = "nama";
    public static final String TAG_USERNAME = "username";

    private ApiNomorTujuan apiNomorTujuan;
    private ApiKecelakaan apiKecelakaan;
    private ApiMengantuk apiMengantuk;
    private ApiSMSGateway apiSMSGateway;
    private ApiPengguna apiPengguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initilaizeComponents();
        initializeObjects();
        initializeEvents();
        initializeValue();
        requestPermission();
        setupSharedPreferences();

        getBoundedDevice();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, ChangeNumberActivity.class));
        } else if (item.getItemId() == R.id.setting) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.help) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(LoginActivity.session_status, false);
            editor.putString(TAG_NAMA, null);
            editor.putString(TAG_USERNAME, null);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }

        return true;
    }

    void getBoundedDevice() {
        mDeviceName = sharedpreferences.getString(DeviceScanActivity.EXTRAS_DEVICE_NAME, null);
        mDeviceAddress = sharedpreferences.getString(DeviceScanActivity.EXTRAS_DEVICE_ADDRESS, null);
    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        weakupAlarm = MediaPlayer.create(this, R.raw.fire_alarm_sound);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

        client = LocationServices.getFusedLocationProviderClient(this);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

        nama = getIntent().getStringExtra(TAG_NAMA);
        username = getIntent().getStringExtra(TAG_USERNAME);

        txt_nama.setText(nama);

        apiMengantuk.getMengantuk(this,TAG,username);
    }

    void initilaizeComponents() {
        btnStartConnecting = findViewById(R.id.btnStartConnecting);
        btnStopConnecting = findViewById(R.id.btnStopConnecting);
        btnStopVibrate = findViewById(R.id.btnStopVibrate);
        txt_nama = findViewById(R.id.txt_nama_tampilan);
        txtState = findViewById(R.id.txtState);
        txtHeartValue = findViewById(R.id.txtByte);
        txtProcess = findViewById(R.id.txtProcess);
        txtX = findViewById(R.id.txtX);
        txtY = findViewById(R.id.txtY);
        txtZ = findViewById(R.id.txtZ);
        txtAcceleration = findViewById(R.id.txtAcceleration);
        btnDemoAlarm = findViewById(R.id.btnDemoAlarm);
        btnDemoSendInformation = findViewById(R.id.btnDemoSendInformation);

        apiNomorTujuan = new ApiNomorTujuan();
        apiMengantuk = new ApiMengantuk();
        apiKecelakaan = new ApiKecelakaan();
        apiSMSGateway = new ApiSMSGateway();
        apiPengguna = new ApiPengguna();
    }

    void initializeEvents() {
        btnStartConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnecting();
                btnStartConnecting.setVisibility(View.INVISIBLE);
                btnStopConnecting.setVisibility(View.VISIBLE);
                btnStopVibrate.setVisibility(View.VISIBLE);
                riding = true;

                apiMengantuk.getMengantuk(MainActivity.this, TAG, username);
                apiNomorTujuan.getNomorTujuan(MainActivity.this, TAG, username);
                apiPengguna.getPengguna(MainActivity.this, TAG, username);

                normalHeartRate = apiMengantuk.detak_jantung_normal;
                txtHeartValue.setText("80");
                restHeartRate = (int) (normalHeartRate - (0.2 * normalHeartRate));
            }
        });
        btnStopConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (riding) {
                    heartRateHandler.removeCallbacks(heartRateRunnable);
                    btnStartConnecting.setVisibility(View.VISIBLE);
                    btnStopConnecting.setVisibility(View.INVISIBLE);
                    btnStopVibrate.setVisibility(View.INVISIBLE);
                    riding = false;
                    drowse = false;
                }
            }
        });
        btnStopVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drowse) {
                    stopVibrate();
                    weakupAlarm.pause();
                    vibrator.cancel();
                    drowse = false;
                }
            }
        });
        btnDemoAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnAlarm();
            }
        });
        btnDemoSendInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInformation();
            }
        });
    }

    void initializeValue() {
        heartRateValue = 100;
        sdf = new SimpleDateFormat("HH:mm");
        currentTime = sdf.format(new Date());
        name = nama;
        number = "089650561515";
        FROM_NUMBER = "LetsRide";
        TO_NUMBER = "6281931390150";
        MESSAGE = name + " mengalami kecelakaan\n" +
                "Kontak : " + number + "\n" +
                "Waktu : " + currentTime + "\n" +
                "Lokasi : " + link + "\n|";
        getLastLocation();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void turnOnAlarm() {
        startVibrate();
        if (alarm_sound) {
            weakupAlarm.setLooping(true);
            weakupAlarm.start();
        }
        if (alarm_vibrate) {
            vibrator=(Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

            vibrator.vibrate(2000);
        }
        btnStopVibrate.setVisibility(View.VISIBLE);

        int jumlah_kantuk = apiMengantuk.jumlah_kantuk + 1;
        apiMengantuk.setMengantuk(this,TAG,username, jumlah_kantuk);
    }

    private void sendInformation() {
        currentTime = sdf.format(new Date());
        getLastLocation();
        MESSAGE = name + " mengalami kecelakaan\n" +
                "Kontak : " + apiPengguna.nomor_tlp + "\n" +
                "Waktu : " + currentTime + "\n" +
                "Lokasi : " + link;

        //Nexmo
//        if (validateAllFields()) {
//            makeSendSMSApiRequest(FROM_NUMBER, TO_NUMBER, MESSAGE);
//        } else {
//            showShortToast("Gagal Validasi");
//        }

        //MedanSMS
        String nomor_tujuan = apiNomorTujuan.nomor_tujuan1
                + "," + apiNomorTujuan.nomor_tujuan2
                + "," + apiNomorTujuan.nomor_tujuan3;
        apiSMSGateway.sendSMS(MainActivity.this, TAG, "kirim_sms", "tbfikrif@gmail.com", "Hm123123", nomor_tujuan, MESSAGE);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = sdf.format(new Date());
        apiKecelakaan.setKecelakaan(this,TAG,username,longtitude,latitude,link,currentDateTime);
    }

    void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    DecimalFormat df = new DecimalFormat(".####");
                    latitude = location.getLatitude();
                    longtitude = location.getLongitude();
                    link = "http://www.google.com/maps/place/" + df.format(latitude) + "," + df.format(longtitude);
                }
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    void startConnecting() {
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress);

        Log.v("test", "Connecting to " + mDeviceAddress);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

        heartRateHandler = new Handler();
        heartRateHandler.postDelayed(heartRateRunnable = new Runnable() {
            @Override
            public void run() {
                startScanHeartRate();
                heartRateHandler.postDelayed(this, 20000);
                if (heartRateValue <= restHeartRate) {
                    showShortToast("Mengantuk");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drowse = true;
                            turnOnAlarm();
                        }
                    }, 3000);
                }
            }
        }, 3000);
    }

    void stateConnected() {
        bluetoothGatt.discoverServices();
        txtState.setText("Terhubung");
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();
        txtState.setText("Terputus");
    }

    void startScanHeartRate() {
        //txtHeartValue.setText("...");
        txtProcess.setText("Memindai");
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

    void startVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.AlertNotification.service)
                .getCharacteristic(CustomBluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{2});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    void stopVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.AlertNotification.service)
                .getCharacteristic(CustomBluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{0});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed stop vibrate", Toast.LENGTH_SHORT).show();
        }
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
            txtHeartValue.setText(Integer.toString(heartRateValue));
            txtProcess.setText(" ");
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
            txtHeartValue.setText(Integer.toString(heartRateValue));
            txtProcess.setText(" ");
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

//    public static boolean between(float i, float min, float max) {
//        return (i >= min && i <= max);
//    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;

        txtX.setText(String.format("%.2f", x) + " m/s2");
        txtY.setText(String.format("%.2f", y) + " m/s2");
        txtZ.setText(String.format("%.2f", z) + " m/s2");
        //txtAcceleration.setText(String.format("%.4f", accelationSquareRoot) + " m/s2");

        if (x > 7.0f){
            txtAcceleration.setText("Miring Kiri");
        } else if (x <- 7.0f) {
            txtAcceleration.setText("Miring Kanan");
        } else {
            txtAcceleration.setText("Normal");
            txtAcceleration.setTextColor(Color.BLUE);
        }

        if (accelationSquareRoot >= 5)
        {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;

            if (x > 8.0f){
                txtAcceleration.setText("Miring Kiri");
                txtAcceleration.setTextColor(Color.RED);
                sendInformation();
            } else if (x <-8.0f) {
                txtAcceleration.setText("Miring Kanan");
                txtAcceleration.setTextColor(Color.RED);
                sendInformation();
            }
        }
    }

    private void makeSendSMSApiRequest(String fromNumber, String toNumber, String message) {
        ApiInterface sendSMSapiInterface =
                ApiClient.getClient().create(ApiInterface.class);

        Call<MessageResponse> call = sendSMSapiInterface.getMessageResponse(Config.ApiKey, Config.ApiSecret,
                fromNumber, toNumber, message);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                try {
                    Log.d(TAG, String.valueOf(response.code()));
                    if (response.code() == 200) {
                        Log.d(TAG, response.body().toString());
                        Log.d(TAG, response.body().getMessages().toString());
                        Log.d(TAG, response.body().getMessageCount());
                        for (int i = 0; i < response.body().getMessageCount().length(); i++) {
                            Log.d(TAG, response.body().getMessages()[i].getTo());
                            Log.d(TAG, response.body().getMessages()[i].getMessageId());
                            Log.d(TAG, response.body().getMessages()[i].getStatus());
                            Log.d(TAG, response.body().getMessages()[i].getRemainingBalance());
                            Log.d(TAG, response.body().getMessages()[i].getMessagePrice());
                            Log.d(TAG, response.body().getMessages()[i].getNetwork());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
                showShortToast("Gagal Mengirim Pesan");
            }
        });
    }

    private boolean validateAllFields() {
        if (!Patterns.PHONE.matcher(TO_NUMBER).matches()) {
            showShortToast("Tolong masukan nomor yang benar");
            return false;
        } else if (MESSAGE.length() == 0) {
            showShortToast("Pesan Kosong");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        if (riding) {
            if (drowse) {
                stopVibrate();
                weakupAlarm.pause();
                vibrator.cancel();
                drowse = false;
            }

            heartRateHandler.removeCallbacks(heartRateRunnable);
            btnStartConnecting.setVisibility(View.VISIBLE);
            btnStopConnecting.setVisibility(View.INVISIBLE);
            btnStopVibrate.setVisibility(View.INVISIBLE);
            riding = false;
        } else {
            super.onBackPressed();
        }
    }

    public void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showLongToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("alarm_sound")) {
            //setTextVisible(sharedPreferences.getBoolean("display_text",true));
            alarm_sound = sharedPreferences.getBoolean("alarm_sound", true);
        }
        if (key.equals("alarm_vibrate")) {
            alarm_vibrate = sharedPreferences.getBoolean("alarm_vibrate", true);
        }
    }
}
