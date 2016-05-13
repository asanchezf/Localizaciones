package com.antonioejemplo.localizaciones;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Login extends AppCompatActivity  {

    private static final String REGISTER_URL = "http://petty.hol.es/insertar_usuario.php";
    public static final String LOGIN_URL = "http://petty.hol.es/validar_usuario.php";
    public static final String KEY_USERNAME = "Username";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_EMAIL = "Email";

    //Declaramos los controles con anotaciones de ButterKnife
    @Bind(R.id.btnLogin) Button btnLogin;
    @Bind(R.id.btnRegistrarse) Button btnRegistrarse;
    @Bind(R.id.txtNombre) EditText txtNombre;
    @Bind(R.id.txtPassword) EditText txtPassword;
    @Bind(R.id.txtEmail) EditText txtEmail;

    /*private Button btnLogin,btnRegistrarse;
    private EditText txtNombre,txtPassword;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*btnLogin=(Button)findViewById(R.id.btnLogin);
        btnRegistrarse=(Button)findViewById(R.id.btnRegistrarse);
        txtNombre=(EditText)findViewById(R.id.txtNombre);
        txtPassword=(EditText)findViewById(R.id.txtPassword);


        btnLogin.setOnClickListener(this);
        btnRegistrarse.setOnClickListener(this);*/

        //REGISTRAMOS PARA PODER UTILIZAR LOS CONTROLES DEFINIDOS
        ButterKnife.bind(this);

    }

    public void limpiarDatos(){

        txtNombre.setText("");
        txtPassword.setText("");
        txtEmail.setText("");


    }


    private void registerUser(){

        //EL USUARIO NO EXISTÍA EN LA BBDD DE LA APP Y SE REGISTRA.
        String tag_json_obj_actual = "json_obj_req_actual";
        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Login.this,response+"Vuelve a logarte para utilizar la aplicación",Toast.LENGTH_LONG).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error insertando", "El usuario no se ha podido dar de alta");
                        Toast.makeText(Login.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME,username);
                params.put(KEY_PASSWORD,password);
                params.put(KEY_EMAIL, email);
                return params;
            }

        };

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);
        limpiarDatos();
    }

    private void userLogin() {

        //EL USUARIO SE LOGA PARA ENTRAR EN LA APLICACIÓN
        final String KEY_USERNAME_VALIDAR = "username";
        final String KEY_PASSWORD_VALIDAR = "password";
        String tag_json_obj_actual = "json_obj_req_actual";
        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();



        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.trim().equals("success")){
                            //Validación correcta... abrimos mapas pasáncole el nombre de usuario introducido y validado
                            Toast.makeText(Login.this,"Usuario correcto",Toast.LENGTH_LONG).show();

                            //
                            Intent intentMapas=new Intent(Login.this,MapsActivity.class);
                            intentMapas.putExtra("USUARIO", username);
                            //intentMapas.putExtra("Email", em);
                            //intentMapas.putExtra("Direccion", direccion);

                            startActivity(intentMapas);

                        }else{
                            //El usuario no existe... Le informamos
                            Toast.makeText(Login.this,"El usuario no existe en la aplicación. Puedes registrarte",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Login.this,error.toString(),Toast.LENGTH_LONG ).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                map.put(KEY_USERNAME_VALIDAR,username);
                map.put(KEY_PASSWORD_VALIDAR,password);
                return map;
            }
        };

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);
        limpiarDatos();
    }


    private void enviaDatosAlServidor() {

        String INSERT="http://petty.hol.es/insertar_usuario.php";

        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(INSERT);   // Parámetros que recibe doInBackground


    }

    public class ObtenerWebService extends AsyncTask<String,Void,String> {

        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();
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

                    String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON

                    if (resultJSON == "1") {      // hay un registro que mostrar
                        devuelve = "Te has dado de alta como usuario correctamente. Lógate para en entrar en la aplicación";

                    } else if (resultJSON == "2") {
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
            //super.onPostExecute(s);

            Toast.makeText(getApplicationContext(),devuelve,Toast.LENGTH_LONG).show();
        }
    }


    @OnClick(R.id.btnRegistrarse)
    public void btnRegistrarse(){

        /*Intent intent=new Intent(Login.this,MapsActivity.class);
        startActivity(intent);*/
        //registerUser();//Utilizando Volley
        enviaDatosAlServidor();//Utilizando un AsyncTacks
    }


    @OnClick(R.id.btnLogin)
    public void btnLogin(){

        /*Intent intent=new Intent(Login.this,MapsActivity.class);
        startActivity(intent);*/

        userLogin();
    }

    /*@Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btnRegistrarse:

                //Intent intentRegistrarse=new Intent(Login.this,MapsActivity.class);
                Intent intentRegistrarse=new Intent(Login.this,MapsActivity.class);
                startActivity(intentRegistrarse);
                break;

            case R.id.btnLogin:

                Intent intentLogarse=new Intent(Login.this,MapsActivity.class);
                startActivity(intentLogarse);
                break;

             default:

                 break;
        }
    }*/



}
