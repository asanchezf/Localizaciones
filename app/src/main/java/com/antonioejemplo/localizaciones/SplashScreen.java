package com.antonioejemplo.localizaciones;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends Activity {

    ImageView imagen1, imagen2;
    TextView txtTitulo, desarrollador, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        imagen1 = (ImageView) findViewById(R.id.imageView3);
        imagen2 = (ImageView) findViewById(R.id.imageView2);
        txtTitulo = (TextView) findViewById(R.id.textView6);
        desarrollador = (TextView) findViewById(R.id.desarrollador);
        email = (TextView) findViewById(R.id.email);
        aparecer();
        rotar();
        dilatar();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 8000);//splash de 8 segundos.
    }

    public void rotar() {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotacion);
        animation.setFillAfter(true);//Que se mantenga al final en la misma posición
        //imagen1.setAnimation(animation);
        //imagen2.setAnimation(animation);
        txtTitulo.setAnimation(animation);
    }

    public void aparecer() {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.aparicion);
        animation.setFillAfter(true);//Que se mantenga al final en la misma posición
        //imagen1.setAnimation(animation);
        //imagen2.setAnimation(animation);
        //txtTitulo.setAnimation(animation);
        desarrollador.setAnimation(animation);
        email.setAnimation(animation);
    }

    public void dilatar() {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.dilatacion);
        animation.setFillAfter(true);//Que se mantenga al final en la misma posición
        imagen1.setAnimation(animation);
        imagen2.setAnimation(animation);


    }



}
