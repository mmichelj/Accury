package com.example.caro;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class FinActivity extends AppCompatActivity {

    private static final String TAG = "FinActivity";
    
    ImageButton btnMenu;
    ImageButton btnReplay;
    String strDevice;
    int game=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin);
        
        btnMenu=(ImageButton)findViewById(R.id.btnMenu);
        btnReplay=(ImageButton)findViewById(R.id.btnRepetir);
        getExtraData();
        MediaPlayer mediaPlayer = MediaPlayer.create(this,R.raw.end);
        mediaPlayer.start();
        while (mediaPlayer.isPlaying()){}


        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MenuActivity.class);
                intent.putExtra("device","00");
                startActivity(intent);
                finish();
            }
        });
        
        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),PlayActivity.class);
                intent.putExtra("device","00");
                intent.putExtra("game",game);
                Log.d(TAG, "onClick: game: " + game);
                startActivity(intent);
                finish();
            }
        });
        


    }

    @Override
    public void onBackPressed() {
    }

    void getExtraData()
    {
        Intent i = getIntent();
        if(i.hasExtra("game")){
            game=i.getIntExtra("game",0);
        }
    }
}
