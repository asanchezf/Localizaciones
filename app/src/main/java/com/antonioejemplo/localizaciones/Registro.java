package com.antonioejemplo.localizaciones;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Registro extends AppCompatActivity {

    private static final String REGISTER_URL = "http://petty.hol.es/insertar_usuario.php";
    public static final String LOGIN_URL = "http://petty.hol.es/validar_usuario.php";
    public static final String KEY_USERNAME = "Username";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_EMAIL = "Email";
    public static final String KEY_ID_ANDROID = "ID_Android";
    public static final String KEY_TELEFONO = "Telefono";
    String Id_Android="";

    //Declaramos los controles con anotaciones de ButterKnife

    @Bind(R.id.btnRegistrarse) Button btnRegistrarse;
    @Bind(R.id.txtNombre) EditText txtNombre;
    @Bind(R.id.txtPassword) EditText txtPassword;
    @Bind(R.id.txtEmail) EditText txtEmail;
    @Bind(R.id.txtTelefono) EditText txtTelefono;

    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Id_Android = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        ButterKnife.bind(this);
    }


    public void limpiarDatos(){

        txtNombre.setText("");
        txtPassword.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");

    }


    private boolean validarEntrada() {

        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();
        final String telefono = txtTelefono.getText().toString().trim();


            if(username.isEmpty()||password.isEmpty()||email.isEmpty()||telefono.isEmpty()){

                //Toast.makeText(getApplicationContext(),"Para registrarte debes rellenar los campos nombre, email y contraseña",Toast.LENGTH_LONG).show();

                Snackbar snack = Snackbar.make(btnRegistrarse, R.string.avisoaltausuario, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snack.show();


                return false;
            }

        return true;

    }


    private void enviaDatosAlServidor() {

        String INSERT="http://petty.hol.es/insertar_usuario.php";

        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(INSERT);   // Parámetros que recibe doInBackground
    }




    public class ObtenerWebService extends AsyncTask<String,Void,String> {
        boolean podemoslogarnos=false;
        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
         final String email = txtEmail.getText().toString().trim();
        final String telefono = txtTelefono.getText().toString().trim();
        @Override
        protected String doInBackground(String... params) {


            String cadena = params[0];
            URL url = null; // Url de donde queremos obtener información
            String devuelve ="";

            try {
                HttpURLConnection urlConn;

                DataOutputStream printout;
                DataInputStream input;
                url = new URL(cadena);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.connect();
                //Creo el Objeto JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put(KEY_USERNAME,username);
                jsonParam.put(KEY_PASSWORD, password);
                jsonParam.put(KEY_EMAIL, email);
                jsonParam.put(KEY_ID_ANDROID, Id_Android);
                jsonParam.put(KEY_TELEFONO, telefono);

                // Envio los parámetros post.
                OutputStream os = urlConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();

                int respuesta = urlConn.getResponseCode();


                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        result.append(line);
                        //response+=line;
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    int resultJSON = Integer.parseInt(respuestaJSON.getString("estado"));   // estado es el nombre del campo en el JSON

                    if (resultJSON == 1) {      // hay un registro que mostrar
                        devuelve = "Te has dado de alta como usuario correctamente. Introduce tu login y tu contraseña para en entrar en la aplicación";
                         podemoslogarnos=true;

                    } else if (resultJSON == 2) {
                        devuelve = "El usuario no pudo insertarse correctamente";
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return devuelve;



            //return null;
        }



        @Override
        protected void onPostExecute(String devuelve) {
            super.onPostExecute(devuelve);
            //limpiarDatos();

        //Toast.makeText(getApplicationContext(),devuelve,Toast.LENGTH_LONG).show();


            //Toast.makeText(getApplicationContext(),"Para registrarte debes rellenar los campos nombre, email y contraseña",Toast.LENGTH_LONG).show();

            Snackbar snack = Snackbar.make(btnRegistrarse, devuelve, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            snack.show();

            if(podemoslogarnos) {
                Intent intent = new Intent(Registro.this, MainActivity.class);
                startActivity(intent);

                //Animación
                overridePendingTransition(R.animator.login_in,
                        R.animator.login_out);

                finish();

            }
        }
    }


      @OnClick(R.id.btnRegistrarse)
    public void btnRegistrarse(){

          if (validarEntrada()) {
              enviaDatosAlServidor();//Damos de alta el usuario utilizando un AsyncTacks
          }

          //registerUser();//Utilizando Volley

/*
        Toast.makeText(getApplicationContext(),"El usuario ha sido dado de alta. Inicia sesión para entrar en la aplicación",Toast.LENGTH_LONG).show();

        Intent intent=new Intent(Registro.this,MainActivity.class);
        startActivity(intent);

          //Animación
          overridePendingTransition(R.animator.login_in,
                  R.animator.login_out);

          finish();*/




    }


    private void registerUser(){
        //EL USUARIO NO EXISTÍA EN LA BBDD DE LA APP Y SE REGISTRA.
        String tag_json_obj_actual = "json_obj_req_actual";
        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        // final String email = txtEmail.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Registro.this,response+"Ya estás dado de alta en la aplicación. Ahora puedes logarte para utilizarla",Toast.LENGTH_LONG).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error insertando", "El usuario no se ha podido dar de alta");
                        Toast.makeText(Registro.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){

                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME,username);
                params.put(KEY_PASSWORD,password);
                //params.put(KEY_EMAIL, email);
                return params;
            }

        };

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);
        limpiarDatos();
    }


}
