package com.angelbrv.instru;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.gauge_voltmeter) CustomGauge gauge;
    @BindView(R.id.gauge_text) TextView gauge_text;
    @BindView(R.id.bluetooth_detail) TextView bluetooth_detail;
    @BindView(R.id.test_button) Button test;
    @BindView(R.id.connect_button) Button connect;

    private int i;
    private int device_supports_bluetooth;
    private StringBuilder dataBuilder;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket mBluetoothSocket = null;
    private ConnectedThread mConnectedThread = null;
    private PersistentData persistentData = null;

    private final static Logger LOGGER = Logger.getLogger(MainActivity.class.getCanonicalName());
    private final static UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static float RESISTOR_1 = 510.0f;
    private final static float RESISTOR_2 = 8200.0f;
    private final int handlerState = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dataBuilder = new StringBuilder();
        persistentData = new PersistentData(MainActivity.this);
        readFromBluetooth();

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gauge.setEndValue(60);
                gauge.setValue(0);
                gauge_text.setText(String.format(Locale.getDefault(), "%1dv", gauge.getValue()));
                new Thread() {
                    public void run() {
                        for (i = 0; i <= 60; i++) {
                            updateGauge(i);
                        }
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BluetoothDevices.class));
                MainActivity.this.finish();
            }
        });

        verificarBluetooth();
        if(device_supports_bluetooth == 0){
            connect.setEnabled(false);
        }else{
            connect.setEnabled(true);

            if(persistentData.getString("mac") != null){
                bluetooth_detail.setText("Intentando conectar al dispositivo Bluetooth");
                LOGGER.log(Level.INFO, "Dirección MAC del dispositivo Bluetooth: ".concat(persistentData.getString("mac")));
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(persistentData.getString("mac"));
                try{
                    mBluetoothSocket = createBluetoothSocket(device);
                    mBluetoothSocket.connect();
                }catch(IOException e){
                    LOGGER.log(Level.SEVERE, e.getMessage());
                    try{
                        mBluetoothSocket.close();
                    }catch(IOException e2){
                        LOGGER.log(Level.SEVERE, e2.getMessage());
                    }
                }
                mConnectedThread = new ConnectedThread(mBluetoothSocket);
                mConnectedThread.start();
            }else{
                bluetooth_detail.setText("No se ha seleccionado el dispositivo Bluetooth");
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private void readFromBluetooth(){
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                LOGGER.log(Level.INFO, "From Arduino: ".concat(msg.toString()));
                if(msg.what == handlerState){
                    String readMessage = (String) msg.obj;
                    dataBuilder.append(readMessage);

                    int endOfData = dataBuilder.indexOf("$");
                    if(dataBuilder.toString().contains("$")){
                        String toPrint = dataBuilder.substring(0, endOfData);
                        bluetooth_detail.setText("Serial: ".concat(toPrint));
                        dataBuilder.delete(0, dataBuilder.length());

                        if(toPrint.matches("^[01]+$")){
                            int decimal = Integer.parseInt(toPrint, 2);
                            float outputValue = decimal * (19.6078f * (float)1e-3);
                            float gaugeValue = (outputValue * (RESISTOR_1 + RESISTOR_2)) / RESISTOR_1;
                            updateGauge(gaugeValue);
                        }
                    }
                }
            }
        };
    }

    public int getDecimal(int binary){
        int decimal = 0;
        int n = 0;
        while(true){
            if(binary == 0){
                break;
            } else {
                int temp = binary%10;
                decimal += temp*Math.pow(2, n);
                binary = binary/10;
                n++;
            }
        }
        return decimal;
    }

    private void updateGauge(final float value){
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(value > gauge.getEndValue()){
                        gauge.setValue(0);
                        gauge_text.setText("MAX");
                    }else{
                        gauge.setValue((int)(value));
                        gauge_text.setText(String.format(Locale.getDefault(), "%.2fv", value));
                    }

                }
            });
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBluetoothSocket != null)
            try {
                // Cuando se sale de la aplicación esta parte permite
                // que no se deje abierto el socket
                mBluetoothSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
    }

    public void verificarBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            device_supports_bluetooth = 0;
            bluetooth_detail.setText("Este dispositivo no soporta Bluetooth");
        }else{
            if(mBluetoothAdapter.isEnabled()){
                device_supports_bluetooth = 1;
                bluetooth_detail.setText("Bluetooth encendido pero sin conexión");
            }else{
                device_supports_bluetooth = 0;
                bluetooth_detail.setText("Bluetooth desactivado, favor de encender el Bluetooth de tu dispositivo.");
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(@NonNull BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)  {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            bluetooth_detail.setText("Dispsitivo conectado");
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    mHandler.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                bluetooth_detail.setText("No se pudo establecer la conexión");
                finish();
            }
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    } */
}
