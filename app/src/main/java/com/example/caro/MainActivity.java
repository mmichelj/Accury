package com.example.caro;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    RecyclerViewAdapter recyclerViewAdapter;
    Button btnBuscar;
    LinearLayout layoutNodisp;
    BluetoothAdapter bluetoothAdapter;
    RecyclerView recyclerView;
    ArrayList<String> mNames =new ArrayList<>();
    ArrayList<String> mNamesBT =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Asignacion de variables
        btnBuscar=(Button)findViewById(R.id.btnBuscar);
        layoutNodisp=(LinearLayout)findViewById(R.id.layoutNodisp);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);


        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnBuscar: Discovering.");
                recyclerView.setVisibility(View.VISIBLE);
                layoutNodisp.setVisibility(View.GONE);
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnBuscar: Cancelling discovery.");
                    checkBTPermissions();
                    bluetoothAdapter.startDiscovery();
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                    registerReceiver(receiver, filter);
                }

                if(!bluetoothAdapter.isDiscovering()){
                    checkBTPermissions();
                    bluetoothAdapter.startDiscovery();
                    Log.d(TAG, "btnBuscar: Starting discovery: ");
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(receiver, filter);
                }
            }
        });

        //Inicializacion de bt
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnable(bluetoothAdapter);
        initRrecyclerView();
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: Action found.");
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: Device found! "+device.getName()+" "+device.getAddress());
                mNames.add(device.getAddress());
                if(TextUtils.isEmpty(device.getName())) {
                    mNamesBT.add(device.getAddress());
                }else{
                    mNamesBT.add(device.getName());
                }
                recyclerViewAdapter.notifyItemInserted(mNames.size()-1);
            }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Toast.makeText(getApplicationContext(),"DISPOSITIVO CONECTADO",Toast.LENGTH_LONG).show();
                Log.d(TAG, "onReceive: Disp conectadooooooo");
            }
        }
    };


    private void initRrecyclerView(){
        Log.d(TAG, "initRrecyclerView: init RecyclerView");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(this,mNames,mNamesBT,bluetoothAdapter,mBTDevices);
        recyclerView.setAdapter(recyclerViewAdapter);
    }
    public void btEnable(BluetoothAdapter bluetoothAdapter){
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta bluetooth
            new AlertDialog.Builder(this)
                    .setTitle("No compatible")
                    .setMessage("Tu celular no soporta bluetooth.")
                    .setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT >=23){
            Log.d(TAG, "checkBTPermissions: Requested");
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

}
