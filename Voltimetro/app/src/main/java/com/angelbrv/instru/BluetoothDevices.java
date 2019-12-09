package com.angelbrv.instru;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BluetoothDevices extends AppCompatActivity {

    @BindView(R.id.bluetooth_detail) TextView bluetooth_detail;
    @BindView(R.id.list_bluetooth_devices) ListView bluetooth_list;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBluetoothAdapter;
    private Set <BluetoothDevice> pairedDevices;
    private ItemBluetoothList listPairedDevices;
    private PersistentData persistentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);
        ButterKnife.bind(BluetoothDevices.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        persistentData = new PersistentData(BluetoothDevices.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        verificarBluetooth();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        listPairedDevices = new ItemBluetoothList(BluetoothDevices.this);

        bluetooth_list.setAdapter(listPairedDevices);
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                listPairedDevices.add(device);
            }
        }

        bluetooth_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String address = listPairedDevices.getItem(position).getAddress();
                persistentData.setString("mac", address);

                Intent i = new Intent(BluetoothDevices.this, MainActivity.class);
                i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(i);
            }
        });
    }

    public void verificarBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            BluetoothDevices.this.finish();
        }else{
            if(mBluetoothAdapter.isEnabled()){
                bluetooth_detail.setText("Bluetooth encendido pero sin conexión");
            }else{
                BluetoothDevices.this.finish();
            }
        }
    }

    private class ItemBluetoothList extends ArrayAdapter<BluetoothDevice> {

        private Context context;

        private ItemBluetoothList(Context context) {
            super(context, R.layout.item_listview_bluetooth_devices);
            this.context = context;
        }

        private class ViewHolder{
            private TextView title;
            private TextView mac;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;
            if(row == null){
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(R.layout.item_listview_bluetooth_devices, parent, false);

                holder = new ViewHolder();
                holder.title = row.findViewById(R.id.title);
                holder.mac = row.findViewById(R.id.mac_address);
                row.setTag(holder);
            }
            holder = (ViewHolder) row.getTag();

            BluetoothDevice device = getItem(position);

            holder.title.setText(device.getName());
            holder.mac.setText(device.getAddress());
            return row;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.finish();
    }
}
