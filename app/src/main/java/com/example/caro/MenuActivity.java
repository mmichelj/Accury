package com.example.caro;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";

    LinearLayout layoutMenu;
    LinearLayout layoutInstrucciones;
    Button btnComenzar;
    ImageButton btnBotones;
    ImageButton btnEstira;
    ImageButton btnGira;
    ImageView imgNumero;
    ImageView imgTitulo;
    TextView txtSalir;
    TextView txtInstrucciones;
    MediaPlayer mediaPlayer;
    int game;
    String strDevice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        layoutMenu=(LinearLayout)findViewById(R.id.layoutMenu);
        layoutInstrucciones=(LinearLayout)findViewById(R.id.layoutIns1);
        btnComenzar=(Button)findViewById(R.id.btnComenzar);
        btnBotones =(ImageButton)findViewById(R.id.btnBotones);
        btnEstira =(ImageButton)findViewById(R.id.btnPresionaestira);
        btnGira =(ImageButton)findViewById(R.id.btnPresionagira);
        imgNumero=(ImageView)findViewById(R.id.imgNumero);
        imgTitulo=(ImageView)findViewById(R.id.imgTitulo);
        txtInstrucciones=(TextView)findViewById(R.id.txtInstrucciones);
        txtSalir=(TextView)findViewById(R.id.txtSalir);
        game=0;
        getExtraData();


    }


    public void onClickComenzar(View v){
        mediaPlayer.stop();
        Intent intent = new Intent(this,PlayActivity.class);
        intent.putExtra("device",strDevice);
        intent.putExtra("game",game);
        startActivity(intent);
        finish();
    }
    public void onClickBotones(View v){
        layoutMenu.setVisibility(View.GONE);
        imgTitulo.setImageResource(R.drawable.botonestitulo);
        imgNumero.setImageResource(R.drawable.circulosss_1);
        txtInstrucciones.setText(R.string.txt_instrucciones_botones);
        txtInstrucciones.setCompoundDrawablePadding(250);
        txtInstrucciones.setTextSize(22);
        imgNumero.setVisibility(View.VISIBLE);
        layoutInstrucciones.setVisibility(View.VISIBLE);
        game=1;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.j1);
        mediaPlayer.start();

    }
    public void onClickEstira(View v){
        layoutMenu.setVisibility(View.GONE);
        imgTitulo.setImageResource(R.drawable.presionaestiratitulo);
        imgNumero.setImageResource(R.drawable.circulosss_2);
        txtInstrucciones.setText(R.string.txt_instrucciones_presionaestira);
        txtInstrucciones.setCompoundDrawablePadding(0);
        txtInstrucciones.setTextSize(22);
        imgNumero.setVisibility(View.VISIBLE);
        layoutInstrucciones.setVisibility(View.VISIBLE);
        game=2;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.j2);
        mediaPlayer.start();
    }
    public void onClickGira(View v){
        layoutMenu.setVisibility(View.GONE);
        imgTitulo.setImageResource(R.drawable.presionagiratitulo);
        imgNumero.setImageResource(R.drawable.circulosss_3);
        txtInstrucciones.setText(R.string.txt_instrucciones_presionagira);
        imgNumero.setVisibility(View.VISIBLE);
        txtInstrucciones.setCompoundDrawablePadding(0);
        txtInstrucciones.setTextSize(18);
        layoutInstrucciones.setVisibility(View.VISIBLE);
        game=3;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.j3);
        mediaPlayer.start();
    }
    public void onClickSalir(View v){
        mediaPlayer.stop();
        finish();
        System.exit(0);
        moveTaskToBack(true);
    }

    @Override
    public void onBackPressed() {
        if (layoutMenu.getVisibility() == View.GONE) {
            layoutMenu.setVisibility(View.VISIBLE);
            layoutInstrucciones.setVisibility(View.GONE);
            imgNumero.setVisibility(View.GONE);
            mediaPlayer.stop();
        }
    }



    public void toast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
    void getExtraData()
    {
        int position=0;
        Intent i = getIntent();
        strDevice = i.getStringExtra("device");
        Log.d(TAG, "getExtraData: Dispositivo conectado: "+strDevice);
    }




}
