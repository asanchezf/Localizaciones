package com.antonioejemplo.localizaciones;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    //Declaramos los controles con anotaciones de ButterKnife
    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.btnRegistrarse) Button btnRegistrarse;

    private static long back_pressed;//Contador para cerrar la app al pulsar dos veces seguidas el btón de cerrar. Se gestiona en el evento onBackPressed
    AlertDialog alert = null;
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

        /*if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(this, R.string.eltiempo_salir, Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
        // super.onBackPressed();*/

        salidaControlada();
    }

    private void salidaControlada() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Seguro que deseas salir de la aplicación?")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (alert != null) {
            alert.dismiss();
        }
    }
}
