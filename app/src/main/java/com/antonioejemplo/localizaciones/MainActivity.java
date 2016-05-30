package com.antonioejemplo.localizaciones;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    //Declaramos los controles con anotaciones de ButterKnife
    @Bind(R.id.btnLogin) Button btnLogin;
    @Bind(R.id.btnRegistrarse) Button btnRegistrarse;

    private static long back_pressed;//Contador para cerrar la app al pulsar dos veces seguidas el btón de cerrar. Se gestiona en el evento onBackPressed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principio);
        if (Build.VERSION.SDK_INT >= 21) {
            Resources res = getResources();
            //
            getWindow().setStatusBarColor(res.getColor(R.color.colorPrimary));
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //REGISTRAMOS PARA PODER UTILIZAR LOS CONTROLES DEFINIDOS
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btnLogin)
    public void btnLogin(){

        Intent intent=new Intent(MainActivity.this,Login.class);
        startActivity(intent);

        /*final Dialog iniciarLogin = new Dialog(this);
        iniciarLogin.setTitle("Tamaño de borrado:");
        iniciarLogin.setContentView(R.layout.activity_login);
        iniciarLogin.dismiss();*/

    }


    @OnClick(R.id.btnRegistrarse)
    public void btnRegistrarse(){

        Intent intent=new Intent(MainActivity.this,Registro.class);
        startActivity(intent);
     /* if(validarEntrada("login")) {

          userLogin(btnLogin);


      }*/


    }


    @Override
    public void onBackPressed() {
/**
 * Cierra la app cuando se ha pulsado dos veces seguidas en un intervalo inferior a dos segundos.
 */

        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(this, R.string.eltiempo_salir, Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
        // super.onBackPressed();
    }
}
