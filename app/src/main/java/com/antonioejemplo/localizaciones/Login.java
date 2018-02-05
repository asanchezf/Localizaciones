package com.antonioejemplo.localizaciones;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import volley.AppController;


public class Login extends AppCompatActivity  {


    public static final String KEY_USERNAME = "Username";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_EMAIL = "Email";

    //Declaramos los controles con anotaciones de ButterKnife
    @BindView(R.id.btnLogin) Button btnLogin;
    //@Bind(R.id.btnRegistrarse) Button btnRegistrarse;

    @BindView(R.id.txtNombre) EditText txtNombre;
    @BindView(R.id.txtPassword) EditText txtPassword;
    //@Bind(R.id.txtEmail) EditText txtEmail;



    /*private Button btnLogin,btnRegistrarse;
    private EditText txtNombre,txtPassword;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*setTheme(R.style.AppTheme_Theme_Dialog);*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*btnLogin=(Button)findViewById(R.id.btnLogin);
        btnRegistrarse=(Button)findViewById(R.id.btnRegistrarse);
        txtNombre=(EditText)findViewById(R.id.txtNombre);
        txtPassword=(EditText)findViewById(R.id.txtPassword);


        btnLogin.setOnClickListener(this);
        btnRegistrarse.setOnClickListener(this);*/

        //REGISTRAMOS PARA PODER UTILIZAR LOS CONTROLES DEFINIDOS
        ButterKnife.bind(this);

    }



    private boolean validarEntrada() {

        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();


            if(username.isEmpty()||password.isEmpty()){

                //Toast.makeText(getApplicationContext(),"Para logarte en la aplicación debes rellenar los campos nombre y contraseña",Toast.LENGTH_LONG).show();

                Snackbar snack = Snackbar.make(btnLogin, R.string.avisologarseusuario, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();
                //group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    group.setBackground(ContextCompat.getDrawable(Login.this,R.drawable.colorear_button));
                }else{

                    group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }


                snack.show();

                return false;
            }
        return true;

    }

    private void userLogin(final Button btnLogin) {


        //http://petty.hol.es/validar_usuario.php

        //EL USUARIO SE LOGA PARA ENTRAR EN LA APLICACIÓN

        //Parámetros que se envían el Ws
        final String KEY_USERNAME_VALIDAR = "username";
        final String KEY_PASSWORD_VALIDAR = "password";

        String tag_json_obj_actual = "json_obj_req_actual";
        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Iniciando sesión... espera por favor");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Conexiones.LOGIN_URL_VOLLEY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //pDialog.hide();
                        int id;
                        String nombre="";
                        String email="";
                        String androidID="";
                        String telefono="";



                        try {
                            //DEVUELVE EL SIGUIENTE JSON: {"estado":1,"usuario":{"Id":"10","Username":"Pepe","Password":"dshdsjkhencryptada","Email":"email"}}

                        //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                        JSONObject respuestaJSON = null;   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                        respuestaJSON = new JSONObject(response.toString());
                        int resultJSON = Integer.parseInt(respuestaJSON.getString("estado"));   // estado es el nombre del campo en el JSON..Devuelve un entero


                            if (resultJSON==1){

                                //session.setLogin(true);
                                JSONObject usuarioJSON = respuestaJSON.getJSONObject("usuario");
                                id = usuarioJSON.getInt("Id");
                                nombre=usuarioJSON.getString("Username");
                                email=usuarioJSON.getString("Email");
                                androidID=usuarioJSON.getString("ID_Android");
                                telefono=usuarioJSON.getString("Telefono");

                                Intent intentInicio=new Intent(Login.this,MapsActivity.class);
                                intentInicio.putExtra("ID", id);
                                intentInicio.putExtra("USUARIO", nombre);
                                intentInicio.putExtra("EMAIL", email);
                                intentInicio.putExtra("ANDROID_ID", androidID);
                                intentInicio.putExtra("TELEFONO", telefono);

                                pDialog.dismiss();
                                startActivity(intentInicio);
                                //Se quita la Animación porque se superpone con el dialog de configuración del gps.
                               /* overridePendingTransition(R.animator.login_in,
                                        R.animator.login_out);*/
                                finish();

                            } else  if (resultJSON==2) {

                                pDialog.dismiss();
                                //El usuario no existe... Le informamos
                                //Toast.makeText(Login.this,R.string.usuarionoexist, Toast.LENGTH_LONG).show();

                                Snackbar snack = Snackbar.make(btnLogin, R.string.usuarionoexist, Snackbar.LENGTH_LONG);
                                ViewGroup group = (ViewGroup) snack.getView();
                                group.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                //group.setBackground(RippleDrawable);
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
                        Snackbar snack = Snackbar.make(btnLogin, error.toString(), Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        snack.show();
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
        //limpiarDatos();
    }


   /* private void enviaDatosAlServidor() {

        String INSERT="http://petty.hol.es/insertar_usuario.php";

        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(INSERT);   // Parámetros que recibe doInBackground


    }

    public class ObtenerWebService extends AsyncTask<String,Void,String> {

        final String username = txtNombre.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
       // final String email = txtEmail.getText().toString().trim();
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
                //jsonParam.put(KEY_EMAIL, email);

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
            super.onPostExecute(devuelve);
            limpiarDatos();
            //Toast.makeText(getApplicationContext(),devuelve,Toast.LENGTH_LONG).show();

            Snackbar snack = Snackbar.make(btnLogin, devuelve, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            snack.show();
        }
    }
*/




    @OnClick(R.id.btnLogin)
    public void btnLogin(){

      if(validarEntrada()) {


          userLogin(btnLogin);

      }

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
