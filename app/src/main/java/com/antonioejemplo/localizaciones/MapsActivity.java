package com.antonioejemplo.localizaciones;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {

    public static final String LOGTAG ="OBTENER MARCADORES" ;
    private GoogleMap mMap;

    //=========

    private static final long TIEMPO_MIN = 30 * 1000; // 60 segundos
    private static final long DISTANCIA_MIN = 10; // 10 metros
    private static final String[] A = {"n/d", "preciso", "impreciso"};
    private static final String[] P = {"n/d", "bajo", "medio", "alto"};
    private static final String[] E = {"fuera de servicio",
            "temporalmente no disponible ", "disponible"};

    private static String LOGCAT;
    private LocationManager manejador;
    private String proveedor;
    private Context context;

    double longitud;
    double latitud;
    float velocidad;
    double altitud;
    String direccion;
    String calle;
    String poblacion;
    String numero;
    String velocidad_dir;
    String usuarioMapas;
    Calendar fechaHora;
    Calendar modificacion;
    long fechaHora2;
    String Stringfechahora;

    // IP de mi Url
    String IP = "http://petty.hol.es/";
    // Rutas de los Web Services
    String GET = IP + "insertar_localizacion.php";
    private RequestQueue requestQueue;//Cola de peticiones de Volley. se encarga de gestionar automáticamente el envió de las peticiones, la administración de los hilos, la creación de la caché y la publicación de resultados en la UI.

   // private JsonObjectRequest myjsonObjectRequest;

    LatLng milocalizacion;

    //PATRONES DE BÚSQUEDA APLICADOS EN traerMarcadoresWebService DEPENDIENDO DE LA PESTAÑA QUE SE ABRA.
    private String patron_Busqueda_Url="http://petty.hol.es/obtener_localizaciones.php";

    private int metodo_Get_POST;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_maps);

        ////UTILIZANDO TABS////////////////////////
        setContentView(R.layout.activity_inicio);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Resources res = getResources();

        TabHost tabs=(TabHost)findViewById(android.R.id.tabhost);

        tabs.setup();

        TabHost.TabSpec spec=tabs.newTabSpec("mitab1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Última localización de todos",
                res.getDrawable(R.drawable.icono_ruta));

        tabs.addTab(spec);

        spec=tabs.newTabSpec("mitab2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Todas las localizaciones de todos",
                res.getDrawable(R.drawable.icono_ruta));
        tabs.addTab(spec);

        spec=tabs.newTabSpec("mitab3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Todas tus localizaciones",
                res.getDrawable(R.drawable.icono_ruta));
        tabs.addTab(spec);

        tabs.setCurrentTab(0);
        //////////////////////////////////////


        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
        muestraProveedores();
        /*CRITERIOS PARA ELEGIR EL PROVEEDOR:SIN COSTE, QUE MUESTRE ALTITUD, Y QUE TENGA PRECISIÓN FINA. CON ESTOS
        * SERÁ ELEGIDO AUTOMÁTICAMENTE EL PROVEEDOR A UTILIZAR POR EL PROPIO TERMINAL*/
        Criteria criterio = new Criteria();
        criterio.setCostAllowed(false);
        criterio.setAltitudeRequired(false);
        criterio.setAccuracy(Criteria.ACCURACY_FINE);
        proveedor = manejador.getBestProvider(criterio, true);
        Log.v(LOGCAT, "Mejor proveedor: " + proveedor + "\n");
        Log.v(LOGCAT, "Comenzamos con la última localización conocida:");

        //PANTALLA SIEMPRE ENCENDIDA...
        getWindow (). addFlags ( WindowManager. LayoutParams . FLAG_KEEP_SCREEN_ON );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        requestQueue = Volley.newRequestQueue(this);
        Location localizacion = manejador.getLastKnownLocation(proveedor);

        muestraLocaliz(localizacion);
        muestradireccion(localizacion);

        //////////////////////////////////////////////////SE ANULA LA PRIMERA LLAMADA AL MÉTODO////////////////////////////////////////////////////
        //enviaDatosAlServidor( );

        Bundle bundle = getIntent().getExtras();
        usuarioMapas=bundle.getString("USUARIO");

        Toast.makeText(getApplicationContext(),"Me alegro de verte... "+usuarioMapas,Toast.LENGTH_SHORT).show();

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                //Recuperamos el contexto de la actividad
                Context contexto = MapsActivity.this;

                if(tabId.equals("mitab1")) {
                    //Traemos todas la última ubicación de cada usuario
                    patron_Busqueda_Url = "http://petty.hol.es/obtener_localizaciones.php";
                    //Método GET
                    metodo_Get_POST=0;
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    //mapFragment.getMapAsync((OnMapReadyCallback) getApplicationContext());
                    mapFragment.getMapAsync((OnMapReadyCallback) contexto);

                }

                if(tabId.equals("mitab2")) {
                    //Traemos todas las localizaciones de todos los usuarios
                    patron_Busqueda_Url = "http://petty.hol.es/obtener_localizaciones_todas.php";
                    //Método GET
                    metodo_Get_POST=0;
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map2);
                    //mapFragment.getMapAsync((OnMapReadyCallback) getApplicationContext());
                    mapFragment.getMapAsync((OnMapReadyCallback) contexto);
                }


                if(tabId.equals("mitab3")) {
                    patron_Busqueda_Url = "http://petty.hol.es/obtener_todas_por_usuario.php";

                    //Método POST
                    metodo_Get_POST=1;

                    //traerMarcadoresPost();


                    /*String INSERT="http://petty.hol.es/obtener_todas_por_usuario.php";

                    TraerMarcadoresAsyncTacks hiloconexion = new TraerMarcadoresAsyncTacks();
                    hiloconexion.execute(INSERT);   // Parámetros que recibe doInBackground
*/

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map3);
                    //mapFragment.getMapAsync((OnMapReadyCallback) getApplicationContext());
                    mapFragment.getMapAsync((OnMapReadyCallback) contexto);
                }

            }
        });





        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    public class TraerMarcadoresAsyncTacks extends AsyncTask<String,Void,String> {



         String KEY_USERNAME = "Usuario";
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
                jsonParam.put(KEY_USERNAME,"Pepe");
                /*jsonParam.put(KEY_PASSWORD, password);
                jsonParam.put(KEY_EMAIL, email);*/

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


                    String usuario = "";
                    String poblacion = "";
                    String calle = "";
                    String numero = "";
                    Double latitud = null;
                    Double longitud = null;
                    double velocidad =  0.0;

                    String fechaHora = "";

                    String resultado= String.valueOf(result);

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = null;   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    respuestaJSON = new JSONObject(resultado.toString());
                    int resultJSON = Integer.parseInt(respuestaJSON.getString("estado"));

                   // if (resultJSON == 1) {      // hay un registro que mostrar
                    //int resultJSON = Integer.parseInt(resultado.getString("estado"));


                    Log.v(LOGTAG, "Valor de estado: " + resultJSON);

                        JSONArray json_array = respuestaJSON.getJSONArray("alumnos");
                        for (int z = 0; z < json_array.length(); z++) {
                            usuario = json_array.getJSONObject(z).getString("Usuario");
                            poblacion = json_array.getJSONObject(z).getString("Poblacion");
                            calle = json_array.getJSONObject(z).getString("Calle");
                            numero = json_array.getJSONObject(z).getString("Numero");
                            longitud = json_array.getJSONObject(z).getDouble("Longitud");
                            latitud = json_array.getJSONObject(z).getDouble("Latitud");

                            //Da error de conversión de datos. Probar...


                            // velocidad = json_array.getJSONObject(z).getDouble("Velocidad");


                            //velocidad = json_array.getJSONObject(z).getDouble("Velocidad");


                            velocidad = (int) conversionVelocidad((int) json_array.getJSONObject(z).getDouble("Velocidad"));

                            fechaHora = json_array.getJSONObject(z).getString("FechaHora");


                            milocalizacion = new LatLng(latitud, longitud);

                            if (usuario.equalsIgnoreCase("Antonio")) {

                                mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                        .position(milocalizacion));

                            }

                            //mMap.addMarker(new MarkerOptions().position(milocalizacion).title(usuario+" está en "+direccion+" "+calle+" "+numero));

                            else if (usuario.equalsIgnoreCase("Susana")) {
                                mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                        .position(milocalizacion));
                            } else if (usuario.equalsIgnoreCase("Dario")) {
                                mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                        .position(milocalizacion));
                            } else {
                                mMap.addMarker(new MarkerOptions()
                                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                        //.anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                        .position(milocalizacion));
                            }

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 10));

                       // }//Fin del JsonArray


                        devuelve = "Te has dado de alta como usuario correctamente. Lógate para en entrar en la aplicación";

                    } /*else if (resultJSON == 2) {
                        devuelve = "El usuario no pudo insertarse correctamente";
                    }*/

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

            Toast.makeText(getApplicationContext(),devuelve,Toast.LENGTH_LONG).show();

           /* Snackbar snack = Snackbar.make(btnRegistrarse, devuelve, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            snack.show();*/
        }
    }

    private void traerMarcadoresWebService() {
        //PETICIÓN PARA TRAER LOS MARCADORES DE TODOS LOS USUARIOS DEL WEBSERVICES:

        /*RESULTADOS QUE TRAE LA PRIMERA PESTAÑA:
        "http://petty.hol.es/obtener_localizaciones.php"==GET
                TRAE LAS ÚLTIMAS LOCALIZACIONES DE TODOS LOS USUARIOS
        {"estado":1,"alumnos":[{"Id":"501","Usuario":"Antonio","Poblacion":"Madrid","Calle":"Cuesta San Vicente","Numero":" 1","Longitud":"-3.71533","Latitud":"40.4202",
        "Velocidad":"50","FechaHora":"15-05-2016 16:19:27","Modificado":"0000-00-00"},{"Id":"583"....}]*/


        /*RESULTADOS QUE TRAE LA SEGUNDA PESTAÑA:
        "http://petty.hol.es/obtener_localizaciones_todas.php"==GET
        {"estado":1,"alumnos":[{"Id":"388","Usuario":"Antonio","Poblacion":"M\u00f3stoles","Calle":"Calle Rubens","Numero":" 14","Longitud":"-3.87157","Latitud":"40.3292",
        "Velocidad":"0","FechaHora":"13-05-2016 20:40:37","Modificado":"0000-00-00"},{"Id":"389"...}]*/


      /*  RESULTADOS QUE TRAE LA TERCERA PESTAÑA:
        "http://petty.hol.es/obtener_todas_por_usuario.php"
        {"estado":1,"alumnos":[{"Id":"389","Usuario":"Pepe","Poblacion":"M\u00f3stoles","Calle":"Calle Rubens","Numero":" 12","Longitud":"-3.871","Latitud":"40.3295",
       "Velocidad":"0","FechaHora":"13-05-2016 20:51:04","Modificado":"0000-00-00"},{"Id":"390"...}]*/

        String tag_json_obj_actual = "json_obj_req_actual";
         //final String KEY_USERNAME_MARCADOR = "Usuario";

        //Log.d(LOGCAT,"Valor de usuarioMaps_KEY "+ KEY_USERNAME_MARCADOR);
        Log.d(LOGCAT,"Valor de usuarioMaps_Valor "+ usuarioMapas);



        String uri = String.format(patron_Busqueda_Url);

        ////Prueba.....
        /*Map<String, String> params = new HashMap();
        params.put(KEY_USERNAME_MARCADOR,usuarioMapas);
        JSONObject parameters = new JSONObject(params);*/
        ///////////////////////////////

        HashMap<String, String> parametros = new HashMap();
        parametros.put("usuario", "Pepe");


        //JsonObjectRequest myjsonObjectRequest
        JsonObjectRequest myjsonObjectRequest = new JsonObjectRequest(
                metodo_Get_POST,
                uri,
                new JSONObject(parametros),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response2) {

                        String usuario = "";
                        String poblacion = "";
                        String calle = "";
                        String numero = "";
                        Double latitud = null;
                        Double longitud = null;
                        double velocidad =  0.0;

                        String fechaHora = "";

                        try {





                            for (int i = 0; i < response2.length(); i++) {
                                //JSONObject json_estado = response2.getJSONObject("estado");
                                int resultJSON = Integer.parseInt(response2.getString("estado"));
                                Log.v(LOGTAG, "Valor de estado: " + resultJSON);


                           if (resultJSON == 1){
                                    JSONArray json_array = response2.getJSONArray("alumnos");
                                for (int z = 0; z < json_array.length(); z++) {
                                    usuario = json_array.getJSONObject(z).getString("Usuario");
                                    poblacion = json_array.getJSONObject(z).getString("Poblacion");
                                    calle = json_array.getJSONObject(z).getString("Calle");
                                    numero = json_array.getJSONObject(z).getString("Numero");
                                    longitud = json_array.getJSONObject(z).getDouble("Longitud");
                                    latitud = json_array.getJSONObject(z).getDouble("Latitud");

                                    //Da error de conversión de datos. Probar...


                                    // velocidad = json_array.getJSONObject(z).getDouble("Velocidad");


                                    //velocidad = json_array.getJSONObject(z).getDouble("Velocidad");


                                    velocidad = (int) conversionVelocidad((int) json_array.getJSONObject(z).getDouble("Velocidad"));

                                    fechaHora = json_array.getJSONObject(z).getString("FechaHora");


                                    milocalizacion = new LatLng(latitud, longitud);

                                    if (usuario.equalsIgnoreCase("Antonio")) {

                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                                .anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));

                                    }

                                    //mMap.addMarker(new MarkerOptions().position(milocalizacion).title(usuario+" está en "+direccion+" "+calle+" "+numero));

                                    else if (usuario.equalsIgnoreCase("Susana")) {
                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                                .anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));
                                    } else if (usuario.equalsIgnoreCase("Dario")) {
                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                                .anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));
                                    } else {
                                        mMap.addMarker(new MarkerOptions()
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                                //.anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));
                                    }

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 10));

                                }//Fin del JsonArray


                            }  //Fin de resultJSON == "1"


                                if (resultJSON == 2){

                                    Toast.makeText(context,"No se obtuvo ningún registro asociado a ese Usuario",Toast.LENGTH_LONG).show();
                                }

                                if (resultJSON == 3){

                                    Toast.makeText(context,"No se han informado los parámetros",Toast.LENGTH_LONG).show();
                                }

                            }//Fin del response

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(LOGTAG, "Error Respuesta en JSON: ");
                        }

                        //priority = Request.Priority.IMMEDIATE;

                    }//fin onresponse

                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOGTAG, "Error Respuesta en JSON: " + error.getMessage());
                        VolleyLog.d(LOGTAG, "Error: " + error.getMessage());
                        Toast.makeText(context, "Se ha producido un error conectando al Servidor "+error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }//Fin errorListener


        )


        /*{

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                map.put(KEY_USERNAME_MARCADOR,usuarioMapas);
                //map.put(KEY_PASSWORD_VALIDAR,password);
                return map;
            }


        }*/;

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(myjsonObjectRequest, tag_json_obj_actual);



    }

  public void traerMarcadoresPost(){

      String tag_json_obj_actual = "json_obj_req_actual";
      final String KEY_USERNAME_MARCADOR = "Usuario";
      final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";

      Log.d(LOGCAT,"Valor de usuarioMaps_KEY "+ KEY_USERNAME_MARCADOR);
      Log.d(LOGCAT,"Valor de usuarioMaps_Valor "+ usuarioMapas);

      String uri = String.format(patron_Busqueda_Url);


      final ProgressDialog pDialog = new ProgressDialog(this);
      pDialog.setMessage("Cargando...");
      pDialog.show();


      StringRequest stringRequest = new StringRequest(metodo_Get_POST, uri,
              new Response.Listener<String>() {
                  @Override
                  public void onResponse(String response) {
                    pDialog.hide();
                      String usuario = "";
                      String poblacion = "";
                      String calle = "";
                      String numero = "";
                      Double latitud = null;
                      Double longitud = null;
                      double velocidad =  0.0;

                      String fechaHora = "";

                      try {



                          //Creating JsonObject from response String
                          //JSONObject jsonObject= new JSONObject(response.toString());
                          //extracting json array from response string
                          //JSONArray jsonArray = jsonObject.getJSONArray("arrname");
                          //JSONObject jsonRow = jsonArray.getJSONObject(0);
                          //get value from jsonRow
                          //String resultStr = jsonRow.getString("result");

                          //ES UN STRINGREQUEST---HAY QUE CREAR PRIMERO UN JSONObject PARA PODER EXTRAER TODO....
                          JSONObject json_Object = new JSONObject(response.toString());


                         // for (int i = 0; i < response.length(); i++) {

                              //Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();

                              //Sacamos el valor de estado
                              int resultJSON= Integer.parseInt(json_Object.getString("estado"));
                              Log.v(LOGTAG, "Valor de estado: " + resultJSON);


                              JSONArray json_array = json_Object.getJSONArray("alumnos");
                                  //JSONArray json_array = response.getJSONArray("alumnos");
                                  for (int z = 0; z < json_array.length(); z++) {
                                      usuario = json_array.getJSONObject(z).getString("Usuario");
                                      poblacion = json_array.getJSONObject(z).getString("Poblacion");
                                      calle = json_array.getJSONObject(z).getString("Calle");
                                      numero = json_array.getJSONObject(z).getString("Numero");
                                      longitud = json_array.getJSONObject(z).getDouble("Longitud");
                                      latitud = json_array.getJSONObject(z).getDouble("Latitud");

                                      //Da error de conversión de datos. Probar...


                                      // velocidad = json_array.getJSONObject(z).getDouble("Velocidad");


                                      //velocidad = json_array.getJSONObject(z).getDouble("Velocidad");


                                      velocidad = (int) conversionVelocidad((int) json_array.getJSONObject(z).getDouble("Velocidad"));

                                      fechaHora = json_array.getJSONObject(z).getString("FechaHora");


                                      milocalizacion = new LatLng(latitud, longitud);

                                      if (usuario.equalsIgnoreCase("Antonio")) {

                                          mMap.addMarker(new MarkerOptions()
                                                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                                  .anchor(0.0f, 1.0f)
                                                  .title(usuario)
                                                  .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                  .position(milocalizacion));

                                      }

                                      //mMap.addMarker(new MarkerOptions().position(milocalizacion).title(usuario+" está en "+direccion+" "+calle+" "+numero));

                                      else if (usuario.equalsIgnoreCase("Susana")) {
                                          mMap.addMarker(new MarkerOptions()
                                                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                                  .anchor(0.0f, 1.0f)
                                                  .title(usuario)
                                                  .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                  .position(milocalizacion));
                                      } else if (usuario.equalsIgnoreCase("Dario")) {
                                          mMap.addMarker(new MarkerOptions()
                                                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                                  .anchor(0.0f, 1.0f)
                                                  .title(usuario)
                                                  .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                  .position(milocalizacion));
                                      } else {
                                          mMap.addMarker(new MarkerOptions()
                                                  //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                                  //.anchor(0.0f, 1.0f)
                                                  .title(usuario)
                                                  .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                  .position(milocalizacion));
                                      }

                                      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 10));

                                  }//Fin del JsonArray







                         // }//Fin del response

                      } catch (JSONException e) {
                          e.printStackTrace();
                          Log.d(LOGTAG, "Error Respuesta en JSON: ");
                                                }

                      //priority = Request.Priority.IMMEDIATE;

                  }
              },
              new Response.ErrorListener() {
                  @Override
                  public void onErrorResponse(VolleyError error) {
                      Log.d(LOGTAG, "Error Respuesta en JSON leyendo MarcadoresPost: " + error.getMessage());
                      VolleyLog.d(LOGTAG, "Error: " + error.getMessage());
                      Toast.makeText(context, "Se ha producido un error leyendo MarcadoresPost "+error.getMessage(), Toast.LENGTH_SHORT).show();
                  }
              })
      {
          @Override
          protected Map<String, String> getParams() throws AuthFailureError {
              Map<String,String> map = new HashMap<String,String>();
              map.put(KEY_USERNAME_MARCADOR,usuarioMapas);
              return map;
          }
      };

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

      // Añadir petición a la cola
      AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);

  }


    // Métodos para mostrar información
    private void log(String cadena) {
        //salida.append(cadena + "\n");
    }
    private void muestraLocaliz(Location localizacion) {
        if (localizacion == null)
            log("Localización desconocida\n");
        else
            log(localizacion.toString() + "\n");
    }


    private double conversionVelocidad(float speed) {



        double speedConvertida=(double) (speed/1000) * 3600;

        return speedConvertida;
    }

    private void muestradireccion(Location location) {

        this.context = getApplicationContext();
        //location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        Geocoder geo;


        if (location != null) {
            //Devolvemos los datos
            latitud = location.getLatitude();
            longitud = location.getLongitude();

            //velocidad = conversionVelocidad(location.getSpeed());


            //velocidad= conversionVelocidad(location.getSpeed());

            velocidad = location.getSpeed();
            altitud = location.getAltitude();

            //PARA OBTENER LA DIRECCIÓN
            geo = new Geocoder(context, Locale.getDefault());

            try {
                List<Address> list =
                        geo.getFromLocation(Double.valueOf(location.getLatitude()),
                                Double.valueOf(location.getLongitude()), 1);

                if (list != null && list.size() > 0) {
                    Address address = list.get(0);
                    direccion = address.getAddressLine(0);
                    calle = direccion.split(",")[0];
                    if (direccion.split(",").length == 2) {
                        numero = direccion.split(",")[1];
                    }

                    poblacion = address.getLocality();

                    if (poblacion == null) {

                        poblacion = "";

                        //velocidad_dir = Float.toString(location.getSpeed());


                    }

                    log("Dirección de localización:+ \n "+direccion+ " "+poblacion);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void muestraProveedores() {
        log("Proveedores de localización: \n ");
        List<String> proveedores = manejador.getAllProviders();
        for (String proveedor : proveedores) {
            muestraProveedor(proveedor);
        }
    }
    private void muestraProveedor(String proveedor) {
        LocationProvider info = manejador.getProvider(proveedor);
        log("LocationProvider[ " + "getName=" + info.getName()
                + ", isProviderEnabled="
                + manejador.isProviderEnabled(proveedor) + ", getAccuracy="
                + A[Math.max(0, info.getAccuracy())] + ", getPowerRequirement="
                + P[Math.max(0, info.getPowerRequirement())]
                + ", hasMonetaryCost=" + info.hasMonetaryCost()
                + ", requiresCell=" + info.requiresCell()
                + ", requiresNetwork=" + info.requiresNetwork()
                + ", requiresSatellite=" + info.requiresSatellite()
                + ", supportsAltitude=" + info.supportsAltitude()
                + ", supportsBearing=" + info.supportsBearing()
                + ", supportsSpeed=" + info.supportsSpeed() + " ]\n");
    }

    private void enviaDatosAlServidor() {

        //PREPARA Y HACE LA LLAMADA PARA LA INSERCCIÓN AUTOMÁTICA DE LAS LOCALIZACIONES DEL USUARIO CONECTADO

        String INSERT="http://petty.hol.es/insertar_localizacion.php";
        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));

        Calendar c1 = GregorianCalendar.getInstance();
        //System.out.println("Fecha actual: "+c1.getTime().toLocaleString());
        //usuario="Antonio";
        //usuario="Susana";
        fechaHora2=System.currentTimeMillis();

        System.out.println("Fecha del sistema: "+fechaHora2);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Stringfechahora = sdf.format(fechaHora2);
        //System.out.println("Fecha del sistema: "+dateString);
        Log.v("","Fecha del sistema: "+Stringfechahora);
        modificacion=calendarNow;


        //fechaHora2=c1;
        modificacion=calendarNow;

        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(INSERT);   // Parámetros que recibe doInBackground

    }

    public class ObtenerWebService extends AsyncTask<String,Void,String> {


        //CONECTA E INSERTA LAS LOCALIZACIONES AUTOMÁTICAS DEL USUARIO CONECTADO
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
                jsonParam.put("Usuario", usuarioMapas);
                jsonParam.put("Poblacion", poblacion);
                jsonParam.put("Calle", calle);
                jsonParam.put("Numero", numero);
                jsonParam.put("Longitud", longitud);
                jsonParam.put("Latitud", latitud);

                //jsonParam.put("Velocidad", velocidad_dir);

                jsonParam.put("Velocidad", velocidad);
                jsonParam.put("FechaHora", Stringfechahora);
                jsonParam.put("Modificado", modificacion);
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
                        devuelve = "Localización insertada correctamente";

                    } else if (resultJSON == "2") {
                        devuelve = "La localización no pudo insertarse";
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
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
       /* LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

         milocalizacion=new LatLng(latitud,longitud);
       ///////////////////////////////////////////////////////
        //traerMarcadoresWebService();
        traerMarcadoresPost();

    }


    @Override//Cada vez que cambian los parámetros de la localización...
    public void onLocationChanged(Location location) {
        muestraLocaliz(location);
        muestradireccion(location);
        enviaDatosAlServidor();
        //traerMarcadoresWebService();
        traerMarcadoresPost();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(context,"El Proveedor está habilitado",Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(context,"El Proveedor está deshabilitado",Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN,
                this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manejador.removeUpdates(this);
    }



}
