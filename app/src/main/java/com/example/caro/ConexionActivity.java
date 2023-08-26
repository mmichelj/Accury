package com.example.caro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ConexionActivity extends AppCompatActivity {

    private static final String TAG = "ConexionActivity";
    private final BTReceiver btReceiver = new BTReceiver();
    ImageButton btnComenzar;
    LinearLayout layoutComenzar;
    ProgressBar progressBar;
    TextView txtProgress;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    String strDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conexion);
        btnComenzar=(ImageButton)findViewById(R.id.btnComenzar);
        layoutComenzar=(LinearLayout)findViewById(R.id.layoutComenzar);
        txtProgress=findViewById(R.id.txtProgress);
        progressBar=findViewById(R.id.progressBar);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        getExtraData();

        Intent intent=new Intent(this,BluetoothDataService.class);
        intent.putExtra("bluetooth_device",strDevice);
        intent.putExtra("secure_connection",true);
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction("Connected");
        registerReceiver(btReceiver,filter);

        btnComenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMenu = new Intent(getApplicationContext(),MenuActivity.class);
                intentMenu.putExtra("device",strDevice);
                startActivity(intentMenu);
                //stopService(new Intent(getApplicationContext(),BluetoothDataService.class));
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    void getExtraData()
    {
        int position=0;
        Intent i = getIntent();
        strDevice = i.getStringExtra("devices");
        Log.d(TAG, "getExtraData: Dispositivo conectado: "+strDevice);
       // Toast.makeText(getApplicationContext(),strDevice,Toast.LENGTH_LONG).show();
    }


    private class BTReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Received data "+intent);
            String result = intent.getStringExtra("Result");
            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            layoutComenzar.setVisibility(View.VISIBLE);
            txtProgress.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(btReceiver);
        stopService(new Intent(getApplicationContext(),BluetoothDataService.class));
        super.onDestroy();
    }



    @Override
    protected void onStop() {

        unregisterReceiver(btReceiver);
        super.onStop();
    }
}
