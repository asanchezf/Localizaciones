package com.antonioejemplo.localizaciones;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import volley.AppController;

public class Registro extends AppCompatActivity {

      //Parametros enviados al ws.
    public static final String KEY_USERNAME = "Username";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_EMAIL = "Email";
    public static final String KEY_ID_ANDROID = "ID_Android";
    public static final String KEY_TELEFONO = "Telefono";
    String id_Android = "";

    //Declaramos los controles con anotaciones de ButterKnife

    @BindView(R.id.btnRegistrarse) Button btnRegistrarse;
    @BindView(R.id.txtNombre) EditText txtNombre;
    @BindView(R.id.txtPassword) EditText txtPassword;
    @BindView(R.id.txtEmail) EditText txtEmail;
    @BindView(R.id.txtTelefono) EditText txtTelefono;

    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        //actionBar.setDisplayHomeAsUpEnabled(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Obtenermos el id del dispositivo
        id_Android = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        ButterKnife.bind(this);
    }





    private boolean validarEntrada() {

        final String Username = txtNombre.getText().toString().trim();
        final String Password = txtPassword.getText().toString().trim();
        final String Email = txtEmail.getText().toString().trim();
        final String Telefono = txtTelefono.getText().toString().trim();


        if (Username.isEmpty() || Password.isEmpty() || Email.isEmpty() || Telefono.isEmpty()) {

                //Toast.makeText(getApplicationContext(),"Para registrarte debes rellenar los campos nombre, email y contraseña",Toast.LENGTH_LONG).show();

                Snackbar snack = Snackbar.make(btnRegistrarse, R.string.avisoaltausuario, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                group.setBackground(ContextCompat.getDrawable(Registro.this,R.drawable.colorear_button));
            }else{

                group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }


            snack.show();


                return false;
            }

        return true;

    }


    private void enviaDatosAlServidor() {

        //SE DEJA EL CÓDIGO COMENTADO.
        //Llamada al Ws utilizando AsyncTask. Los parámetros los pasa en formato Json. Hay que descodificarlos desde el Ws.

        String URL=Conexiones.REGISTER_URL_ASYNCTACKS;

        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(URL);   // Parámetros que recibe doInBackground
    }




    public class ObtenerWebService extends AsyncTask<String,Void,String> {
        boolean podemoslogarnos=false;
        final String Username = txtNombre.getText().toString().trim();
        final String Password = txtPassword.getText().toString().trim();
        final String Email = txtEmail.getText().toString().trim();
        final String Telefono = txtTelefono.getText().toString().trim();
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
                //PASAMOS LOS PARAMETROS POR JSON. EN EL WS HAY QUE DECODIFICAR LOS VALORES....=============
                //Creo el Objeto JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put(KEY_USERNAME, Username);
                jsonParam.put(KEY_PASSWORD, Password);
                jsonParam.put(KEY_EMAIL, Email);
                jsonParam.put(KEY_ID_ANDROID, id_Android);
                jsonParam.put(KEY_TELEFONO, Telefono);

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
                    } else if (resultJSON == 3) {
                        devuelve = "Ese usuario ya existe en nuestra Base de Datos";
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


            Toast.makeText(getApplicationContext(),devuelve,Toast.LENGTH_LONG).show();

            /*Snackbar snack = Snackbar.make(btnRegistrarse, devuelve, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            snack.show();*/

            /*if(podemoslogarnos) {
                Intent intent = new Intent(Registro.this, MainActivity.class);
                startActivity(intent);

                //Animación
                overridePendingTransition(R.animator.login_in,
                        R.animator.login_out);

                finish();

            }*/
        }
    }


      @OnClick(R.id.btnRegistrarse)
    public void btnRegistrarse(){

          if (validarEntrada()) {
              //enviaDatosAlServidor();//Damos de alta el usuario utilizando un AsyncTacks
              registerUser();//Damos de alta el usuario utilizando Volley.
          }


    }


    private void registerUser(){
        //EL USUARIO NO EXISTÍA EN LA BBDD DE LA APP Y SE REGISTRA.
        String tag_json_obj_actual = "json_obj_req_actual";
        //String INSERT="http://petty.hol.es/Insertar_prueba.php";
        //String INSERT="http://petty.hol.es/insertar_usuario.php";
        //String INSERT="http://petty.hol.es/insertar_usuario_volley.php";


        final String Username = txtNombre.getText().toString().trim();
        final String Password = txtPassword.getText().toString().trim();
        final String Email = txtEmail.getText().toString().trim();
        final String Telefono = txtTelefono.getText().toString().trim();

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.transfiriendo_datos));
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Conexiones.REGISTER_URL_VOLLEY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            //DEVUELVE EL SIGUIENTE JSON: {"estado":1,"usuario":{"Id":"10","Username":"Pepe","Password":"1","Email":"email"}}

                            //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                            JSONObject respuestaJSON = null;   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                            respuestaJSON = new JSONObject(response.toString());
                            int resultJSON = Integer.parseInt(respuestaJSON.getString("estado"));   // estado es el nombre del campo en el JSON..Devuelve un entero


                            if (resultJSON == 1) {

                                pDialog.dismiss();

                                Snackbar snack = Snackbar.make(btnRegistrarse, R.string.alta_registro_ok, Snackbar.LENGTH_LONG);
                                ViewGroup group = (ViewGroup) snack.getView();
                                group.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                snack.show();


                            } else if (resultJSON == 3) {

                                pDialog.dismiss();
                                //El usuario ya existe... Le informamos
                                //Toast.makeText(Login.this,R.string.usuarionoexist, Toast.LENGTH_LONG).show();

                                Snackbar snack = Snackbar.make(btnRegistrarse, R.string.alta_registro_repeat, Snackbar.LENGTH_LONG);
                                ViewGroup group = (ViewGroup) snack.getView();
                                group.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                snack.show();

                            } else if (resultJSON == 2) {

                                pDialog.dismiss();
                                //El usuario no existe... Le informamos
                                //Toast.makeText(Login.this,R.string.usuarionoexist, Toast.LENGTH_LONG).show();

                                Snackbar snack = Snackbar.make(btnRegistrarse, R.string.alta_usuario_error, Snackbar.LENGTH_LONG);
                                ViewGroup group = (ViewGroup) snack.getView();
                                group.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                snack.show();

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Toast.makeText(Login.this,error.toString(),Toast.LENGTH_LONG ).show();
                        pDialog.dismiss();
                        Snackbar snack = Snackbar.make(btnRegistrarse, error.toString(), Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        snack.show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, Username);
                params.put(KEY_PASSWORD, Password);
                params.put(KEY_EMAIL, Email);
                params.put(KEY_ID_ANDROID, id_Android);
                params.put(KEY_TELEFONO, Telefono);
                return params;
            }
        };

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);

    }


}
