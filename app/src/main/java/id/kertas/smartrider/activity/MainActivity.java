package id.kertas.smartrider.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import id.kertas.smartrider.R;
import id.kertas.smartrider.api.ApiClient;
import id.kertas.smartrider.api.ApiInterface;
import id.kertas.smartrider.model.MessageResponse;
import id.kertas.smartrider.util.Config;
import id.kertas.smartrider.util.CustomBluetoothProfile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    Boolean isListeningHeartRate = false;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    Button btnStartConnecting, btnStopConnecting, btnStopVibrate, btnDemoAlarm, btnDemoSendInformation;
    EditText txtPhysicalAddress;
    TextView txtState, txtByte, txtProcess, txtX, txtY, txtZ, txtAcceleration;
    private String mDeviceName;
    private String mDeviceAddress;
    private int heartRateValue;
    private int restHeartRate;

    private Handler heartRateHandler;
    private Runnable heartRateRunnable;
    private MediaPlayer weakupAlarm;

    private SensorManager sensorManager;
    private boolean color = false;
    private long lastUpdate;
    private String FROM_NUMBER = "", TO_NUMBER = "", MESSAGE = "";
    private String currentTime;
    private SimpleDateFormat sdf;

    private String name, number, link;

    private FusedLocationProviderClient client;
    private double latitude, longtitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeObjects();
        initilaizeComponents();
        initializeEvents();
        initializeValue();
        requestPermission();

        getBoundedDevice();
    }

    void getBoundedDevice() {

        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);
        txtPhysicalAddress.setText(mDeviceAddress);

        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 3")) {
                txtPhysicalAddress.setText(bd.getAddress());
            }
        }
    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        weakupAlarm = MediaPlayer.create(this, R.raw.fire_alarm_sound);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

        client = LocationServices.getFusedLocationProviderClient(this);
    }

    void initilaizeComponents() {
        btnStartConnecting = findViewById(R.id.btnStartConnecting);
        btnStopConnecting = findViewById(R.id.btnStopConnecting);
        btnStopVibrate = findViewById(R.id.btnStopVibrate);
        txtPhysicalAddress = findViewById(R.id.txtPhysicalAddress);
        txtState = findViewById(R.id.txtState);
        txtByte = findViewById(R.id.txtByte);
        txtProcess = findViewById(R.id.txtProcess);
        txtX = findViewById(R.id.txtX);
        txtY = findViewById(R.id.txtY);
        txtZ = findViewById(R.id.txtZ);
        txtAcceleration = findViewById(R.id.txtAcceleration);
        btnDemoAlarm = findViewById(R.id.btnDemoAlarm);
        btnDemoSendInformation = findViewById(R.id.btnDemoSendInformation);
    }

    void initializeEvents() {
        btnStartConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnecting();
                btnStartConnecting.setVisibility(View.GONE);
                btnStopConnecting.setVisibility(View.VISIBLE);
                btnDemoAlarm.setVisibility(View.VISIBLE);
                btnDemoSendInformation.setVisibility(View.VISIBLE);
                btnStopVibrate.setVisibility(View.VISIBLE);
            }
        });
        btnStopConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartRateHandler.removeCallbacks(heartRateRunnable);
                btnStartConnecting.setVisibility(View.VISIBLE);
                btnStopConnecting.setVisibility(View.GONE);
                btnDemoAlarm.setVisibility(View.GONE);
                btnDemoSendInformation.setVisibility(View.GONE);
                btnStopVibrate.setVisibility(View.GONE);
            }
        });
        btnStopVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVibrate();
                weakupAlarm.pause();
                btnStopVibrate.setVisibility(View.GONE);
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
        restHeartRate = 60;
        sdf = new SimpleDateFormat("HH:mm");
        currentTime = sdf.format(new Date());
        name = "Demo";
        number = "089650561515";
        FROM_NUMBER = "SmartRider";
        TO_NUMBER = "6281931390150";
        MESSAGE = "Pengendara " + name + " mengalami kecelakaan\n" +
                "Kontak : " + number + "\n" +
                "Waktu : " + currentTime + "\n" +
                "Lokasi : " + link + "\n|";
        getLastLocation();
    }

    private void turnOnAlarm(){
        startVibrate();
        weakupAlarm.setLooping(true);
        weakupAlarm.start();
        btnStopVibrate.setVisibility(View.VISIBLE);
    }

    private void sendInformation(){
        currentTime = sdf.format(new Date());
        getLastLocation();
        MESSAGE = name + " mengalami kecelakaan\n" +
                "Kontak : " + number + "\n" +
                "Waktu : " + currentTime + "\n" +
                "Lokasi : " + link + "\n|";
        if (validateAllFields()) {
            makeSendSMSApiRequest(FROM_NUMBER, TO_NUMBER, MESSAGE);
        } else {
            showShortToast("Gagal Validasi");
        }
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
                    link = "http://www.google.com/maps/place/"+df.format(latitude)+","+df.format(longtitude);
                    //showLongToast(link);
                }
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    void startConnecting() {
        String address = txtPhysicalAddress.getText().toString();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

        heartRateHandler = new Handler();
        heartRateHandler.postDelayed(heartRateRunnable = new Runnable() {
            @Override
            public void run() {
                startScanHeartRate();
                heartRateHandler.postDelayed(this, 20000);
                if (heartRateValue < restHeartRate) {
                    Toast.makeText(MainActivity.this, "Mengantuk", Toast.LENGTH_LONG).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
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
        //txtByte.setText("...");
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
            byte[] slice = Arrays.copyOfRange(data, 1,2);
            heartRateValue = slice[0];
            txtByte.setText(Integer.toString(heartRateValue));
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
            byte[] slice = Arrays.copyOfRange(data, 1,2);
            heartRateValue = slice[0];
            txtByte.setText(Integer.toString(heartRateValue));
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
        txtAcceleration.setText(String.format("%.4f", accelationSquareRoot) + " m/s2");

        if (accelationSquareRoot >= 10) //
        {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;
            if (color) {
                txtAcceleration.setBackgroundColor(Color.RED);
            }
            else {
                txtAcceleration.setBackgroundColor(Color.CYAN);
            }
            color = !color;

            if ((x < -23.06f || x > 3.86) && (y < -4.38f || y > 10.67) && (z < -17.20f || x > 24.74)) {
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

    public void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public void showLongToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
