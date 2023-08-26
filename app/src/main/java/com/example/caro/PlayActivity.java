package com.example.caro;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.caro.BluetoothDataService.LocalBinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";
    private static final String TAG_aciertos = "aciertos";
    private static final String TAG_juego = "juego";
    private static final String TAG_numeros = "numeros";
    private static final String TAG_estado = "estado";

    private static final String str_iniciado ="INICIADO";
    private static final String str_correcto="CORRECTO";
    private static final String str_incorrecto="INCORRECTO";
    private static final String str_fin="FIN";

    private final BTReceiver btReceiver = new BTReceiver();

    MediaPlayer mediaPlayer;
    boolean mBounded;
    boolean flag=false;

    LinearLayout layoutCountdown;
    RelativeLayout layoutPlay;
    TextView txtCountdown;
    TextView txtAciertos;
    BluetoothDataService btService;
    BluetoothDataService btServiceInstance;
    String strDevice;

    private long timeLeftMilliseconds=5000;
    int juego=0;
    int[] numeros;


    //Variables de juego
    int game=0,aciertos=0,conde=0,arrayLength,resourceCount=0,mresourceCount=0;
    String estado="";

    String result="{}";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        layoutCountdown = (LinearLayout)findViewById(R.id.layoutCountdown);
        layoutPlay=(RelativeLayout)findViewById(R.id.layoutPlay);
        txtCountdown=(TextView)findViewById(R.id.txtCountdown);
        txtAciertos=(TextView)findViewById(R.id.txtAciertos);
        game=0;

        getExtraData();
        startTimer();
        //registerReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("Connected");
        registerReceiver(btReceiver,filter);





    }

    public void playNumbers()
    {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        int resourceID;

        if(resourceCount%2==0){
            Log.d(TAG, "playNumbers: resource count is even: "+resourceCount);
            resourceID = getApplicationContext().getResources().getIdentifier("m"+numeros[mresourceCount],"raw",getApplicationContext().getPackageName());
            mresourceCount++;
        }else {
            resourceID = R.raw.y_;
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), resourceID);

        if(mediaPlayer != null) {
            
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {

                    Log.d(TAG, "onCompletion: Accessed.");

                    game=juego+3;

                    if(resourceCount<(arrayLength-1)*2){
                        resourceCount++;
                        playNumbers();
                    }else{
                        Log.d(TAG, "onCompletion: Terminó de decir números");
                        resourceCount=0;
                        unbindService(service_conn);
                        bindService();
                    }


                    /*if(estado.equals(str_fin))
                    {
                        Intent intentFin = new Intent(PlayActivity.this,FinActivity.class);
                        intentFin.putExtra("Aciertos",aciertos);
                        intentFin.putExtra("game",juego);
                        Log.d(TAG, "onCompletion: Extra game: " + juego);
                        startActivity(intentFin);
                        finish();
                    }else if(!estado.equals(str_incorrecto)){
                        Log.d(TAG, "onCompletion: valor de j: "+conde+"ArrayLength: "+arrayLength);
                        unbindService(service_conn);
                        bindService();
                    }*/
                }
            });

            mediaPlayer.start();

        }

    }



    /////////////
    public void playSound(int _id)
    {
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), _id);
        if(mediaPlayer != null) {

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {

                    Log.d(TAG, "onCompletion: Accessed.");

                    game=juego+3;

                    if(estado.equals(str_fin))
                    {
                        Intent intentFin = new Intent(PlayActivity.this,FinActivity.class);
                        intentFin.putExtra("Aciertos",aciertos);
                        intentFin.putExtra("game",juego);
                        Log.d(TAG, "onCompletion: Extra game: " + juego);
                        startActivity(intentFin);
                        finish();
                    }else if(!estado.equals(str_incorrecto)){
                        Log.d(TAG, "onCompletion: valor de j: "+conde+"ArrayLength: "+arrayLength);
                        unbindService(service_conn);
                        bindService();
                    }
                }
            });

            mediaPlayer.start();

        }

    }
    ///////



    void bindService(){
        Intent intent=new Intent(this,BluetoothDataService.class);
        intent.putExtra("bluetooth_device",strDevice);
        intent.putExtra("secure_connection",true);
        bindService(intent, service_conn, BIND_AUTO_CREATE);
    }
    void getExtraData()
    {
        Intent i = getIntent();
        strDevice = i.getStringExtra("device");
        game=i.getIntExtra("game",0);
        Log.d(TAG, "getExtraData: Dispositivo conectado: "+strDevice);
        Log.d(TAG, "getExtraData: Juego elegido: "+game);
    }

    void sendDataJSON(String json){
        Log.d(TAG, "onServiceConnected: btService is not null.");
        try {
            JSONObject obj = new JSONObject(json);
            btService.sendData(obj.toString());
            Log.d(TAG, "onServiceConnected: JSON string sent: " + obj.toString());

        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + json + "\"");
        }
    }

    public void startTimer(){

        CountDownTimer countDownTimer=new CountDownTimer(timeLeftMilliseconds,1000) {
            @Override
            public void onTick(long l) {
                timeLeftMilliseconds = l;
                int seconds =(int) timeLeftMilliseconds/1000;
                String secondsText = String.valueOf(seconds+1);
                txtCountdown.setText(secondsText);
                int resID = getApplicationContext().getResources().getIdentifier("m"+seconds,"raw",getApplicationContext().getPackageName());
                Log.d(TAG, "onTick: "+timeLeftMilliseconds);
            }

            @Override
            public void onFinish() {
                layoutCountdown.setVisibility(View.GONE);
                layoutPlay.setVisibility(View.VISIBLE);
                bindService();
            }
        }.start();
        Log.d(TAG, "startTimer: started");
    }

    private ServiceConnection service_conn=new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected: Accessed");
            LocalBinder mLocalBinder = (LocalBinder)service;
            Log.d(TAG, "onServiceConnected: "+mLocalBinder.getService());
            btService = mLocalBinder.getService();
            Log.d(TAG, "onServiceConnected: "+btService);
           //sendDataBT(btService);

            if(btService==null){
                game=0;
                Log.d(TAG, "onServiceConnected: btService is null");
                return;
            }
            switch (game){
                case 0:
                    Log.d(TAG, "onServiceConnected: An error ocurred");
                    Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    //Send START GAME ONE command
                    sendDataJSON(getResources().getString(R.string.startGameOne));
                    break;
                case 2:
                    //Send START GAME TWO command
                    sendDataJSON(getResources().getString(R.string.startGameTwo));
                    break;
                case 3:
                    //Send START GAME THREE command
                    sendDataJSON(getResources().getString(R.string.startGameThree));
                    break;
                case 4:
                    //Send GAME 1 AUDIO_OK command
                    sendDataJSON(getResources().getString(R.string.gameOneAudioOk));
                    break;
                case 5:
                    //Send GAME 2 AUDIO_OK command
                    sendDataJSON(getResources().getString(R.string.gameTwoAudioOk));
                    break;
                case 6:
                    //Send GAME 2 AUDIO_OK command
                    sendDataJSON(getResources().getString(R.string.gameThreeAudioOk));
                    break;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: Service disconnected.");
            unbindService(service_conn);
            btService = null;
        }
    };
    private class BTReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive: Received data from bluetooth stream.");
            result = intent.getStringExtra("msg");
            byte[] buffer=intent.getByteArrayExtra("buff");
            int mByte=intent.getIntExtra("mByte",0);
            int jArrayL=0;
            Log.d(TAG, "onReceive: Received data: "+result);
            Log.d(TAG, "onReceive: Byte count: "+mByte);

            try {

                JSONObject jObject = new JSONObject(result);
                aciertos = jObject.getInt(TAG_aciertos);
                juego = jObject.getInt(TAG_juego);


                //{"juego": 1, "aciertos": 0, "comando": "REPRODUCIR", "numeros": [5]}

                if(jObject.has(TAG_estado)){
                    Log.d(TAG, "onReceive: JOBJECT ESTADO: "+jObject.getString(TAG_estado)+" Estado actual: "+estado);
                    estado = jObject.getString(TAG_estado);
                    switch (estado){
                        case str_iniciado:
                            //Log de estado iniciado
                            Log.d(TAG, "onReceive: Estado iniciado");
                            break;
                        case str_correcto:
                            //Si el estado es "CORRECTO" entonces el campo de aciertos
                            //se actualiza y suena la campana
                            Log.d(TAG, "onReceive: Estado correcto");
                            txtAciertos.setText(String.valueOf(aciertos));
                            playSound(R.raw.acierto);
                            break;
                        case str_incorrecto:
                            //Log de estado incorrecto
                            Log.d(TAG, "onReceive: Estado incorrecto");
                            break;
                        case str_fin:
                            //Si el estado es "FIN" entonces suena y luego se inicia el activityfin
                            Log.d(TAG, "onReceive: Estado fin");
                            playSound(R.raw.error);
                            break;
                    }
                }

                if(jObject.has(TAG_numeros)){
                    JSONArray jArray = jObject.getJSONArray(TAG_numeros);
                    jArrayL=jArray.length();
                    Log.d(TAG, "onReceive:  jArrayL "+jArrayL);
                    numeros=new int[jArrayL];
                    for(int j=0;j<jArrayL;j++){
                        conde=j;
                        arrayLength=jArrayL;
                        numeros[j]=jArray.getInt(j);
                        Log.d(TAG, "onReceive: numeros["+j+"]"+"="+numeros[j]);
                    }

                    playNumbers();

                }

            }catch (JSONException jsonExceptionon){
                Log.e(TAG, "onReceive: Error: JSONException triggered: ", jsonExceptionon);
            }catch (Exception e){
                Log.e(TAG, "onReceive: Exception: ", e);
            }

            
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(service_conn);
        unregisterReceiver(btReceiver);
        Log.d(TAG, "onDestroy: Service terminated and receiver unregistered");
        mBounded = false;
        super.onDestroy();
    }

    @Override
    protected void onStop() {

        if(mBounded) {
            unbindService(service_conn);
            unregisterReceiver(btReceiver);
            Log.d(TAG, "onStop: Service terminated and receiver unregistered");
            mBounded = false;
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
    }
}
