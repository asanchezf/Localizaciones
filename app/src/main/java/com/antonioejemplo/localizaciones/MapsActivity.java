package com.antonioejemplo.localizaciones;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {//FragmentActivity

    public static final String LOGTAG = "OBTENER MARCADORES";
    private GoogleMap mMap;

    //=========

    private static final long TIEMPO_MIN = 300 * 1000; // 300000 milisegundos==> 5 minutos
    private static final long DISTANCIA_MIN = 100; // 50 metros
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
    //VARIABLES RECOGIDAS DE LA ACTIVITY LOGIN
    String id;
    String usuarioMapas;
    String email;
    String android_id;
    String telefono;
    String telefonowsp;
//Variables utilizadas en los marcadores
    /*String usuariomarcador = "";
    String poblacionmarcador = "";
    String callemarcador = "";
    String numeromarcador = "";
    Double latitudmarcador = null;
    Double longitudmarcador = null;
    double velocidadmarcador =  0.0;
    String telefonomarcador="";
    String emailmarcador="";
    String fechaHoramarcadro = "";*/

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
    private String patron_Busqueda_Url = "http://petty.hol.es/obtener_localizaciones.php";

    private int metodo_Get_POST;
    private float zoom = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_maps);

        ////UTILIZANDO TABS////////////////////////
        setContentView(R.layout.activity_inicio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Resources res = getResources();

        TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);

        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("mitab1");
        spec.setContent(R.id.tab1);//Última localización de todos
        spec.setIndicator(res.getString(R.string.info_tab1),
                res.getDrawable(R.drawable.icono_ruta));

        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab2");//Todas las localizaciones de todos..res.getString(R.string.info_tab2)
        spec.setContent(R.id.tab2);
        spec.setIndicator(res.getString(R.string.info_tab2),
                res.getDrawable(R.drawable.icono_ruta));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab3");//Todas tus localizaciones
        spec.setContent(R.id.tab3);
        spec.setIndicator(res.getString(R.string.info_tab3),
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("ID");
        usuarioMapas = bundle.getString("USUARIO");
        email = bundle.getString("EMAIL");
        android_id = bundle.getString("ANDROID_ID");
        telefono = bundle.getString("TELEFONO");

        //usuarioMapas="Pepe";

        Toast.makeText(getApplicationContext(), "Me alegro de verte... " + usuarioMapas, Toast.LENGTH_SHORT).show();

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                //Recuperamos el contexto de la actividad
                Context contexto = MapsActivity.this;

                if (tabId.equals("mitab1")) {
                    //Traemos todas la última ubicación de cada usuario
                    patron_Busqueda_Url = "http://petty.hol.es/obtener_localizaciones.php";
                    //Método GET
                    metodo_Get_POST = 0;

                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    //mapFragment.getMapAsync((OnMapReadyCallback) getApplicationContext());
                    mapFragment.getMapAsync((OnMapReadyCallback) contexto);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                }

                if (tabId.equals("mitab2")) {
                    //Traemos todas las localizaciones de todos los usuarios
                    patron_Busqueda_Url = "http://petty.hol.es/obtener_localizaciones_todas.php";
                    //Método GET
                    metodo_Get_POST = 0;
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map2);
                    //mapFragment.getMapAsync((OnMapReadyCallback) getApplicationContext());
                    mapFragment.getMapAsync((OnMapReadyCallback) contexto);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
                }


                if (tabId.equals("mitab3")) {
                    patron_Busqueda_Url = "http://petty.hol.es/obtener_todas_por_usuario.php";

                    //Método POST
                    metodo_Get_POST = 1;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map3);
                    //mapFragment.getMapAsync((OnMapReadyCallback) getApplicationContext());
                    mapFragment.getMapAsync((OnMapReadyCallback) contexto);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
                }

            }
        });

        // Registrar escucha onMapReadyCallback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
      /*  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));//Probar Da error*/


    }

    @Override
    public void onMarkerDragStart(Marker marker) {//EVENTO GENERADO AL ARRASTRAR MARCADORES. AL INICIO

        //Toast.makeText(context, "Telefono:" + marker.getSnippet().toString(), Toast.LENGTH_LONG).show();
        //Toast.makeText(context, "Telefono:" + telefonowsp+" "+telefono, Toast.LENGTH_LONG).show();


        if (patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones.php") {//Solo para la primera pestaña que tiene el teléfono en el snippe
            String telf_wsp = marker.getSnippet();
            String telf_wsp2 = telf_wsp.substring(telf_wsp.length() - 9, telf_wsp.length());//Sacamos los nueve últimos caracteres para extraer el teléfono...
            Toast.makeText(context, "Telefono:" + telf_wsp2, Toast.LENGTH_LONG).show();


            Uri uri = Uri.parse("smsto:" + telf_wsp2);
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);

            //i.putExtra("sms_body", smsText);
            i.setPackage("com.whatsapp");
            startActivity(i);
        }

    }

    @Override
    public void onMarkerDrag(Marker marker) {//EVENTO GENERADO AL ARRASTRAR MARCADORES. MIENTRAS SE ARRASTRA

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {//EVENTO GENERADO AL ARRASTRAR MARCADORES.AL FINALIZAR EL ARRASTRE

        //Generamos los marcadores y borramos el que se ha creado al arrastrar el que ha generado el evento
        traerMarcadoresNew();
        marker.remove();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {//EVENTO GENERADO AL HACER CLICK SOBRE LOS  MARCADORES.

        if (zoom <= 10) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 18));
            zoom =18;
        }

/*        if (patron_Busqueda_Url != "http://petty.hol.es/obtener_localizaciones.php") {//Solo para la primera pestaña que tiene el teléfono en el snippe
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 18));
        }
        else{

            marker.setSnippet(calle+" "+numero+ "//"+ fechaHora + "//" +velocidad );
        }*/

        return false;
    }


    public class TraerMarcadoresAsyncTacks extends AsyncTask<String, Void, String> {


        String KEY_USERNAME = "Usuario";

        @Override
        protected String doInBackground(String... params) {

            String cadena = params[0];
            URL url = null; // Url de donde queremos obtener información
            String devuelve = "";

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
                jsonParam.put(KEY_USERNAME, "Pepe");
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
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                        //response+=line;
                    }


                    String usuario = "";
                    String poblacion = "";
                    String calle = "";
                    String numero = "";
                    Double latitud = null;
                    Double longitud = null;
                    double velocidad = 0.0;

                    String fechaHora = "";

                    String resultado = String.valueOf(result);

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

            Toast.makeText(getApplicationContext(), devuelve, Toast.LENGTH_LONG).show();

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
        Log.d(LOGCAT, "Valor de usuarioMaps_Valor " + usuarioMapas);


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
                        double velocidad = 0.0;

                        String fechaHora = "";

                        try {


                            for (int i = 0; i < response2.length(); i++) {
                                //JSONObject json_estado = response2.getJSONObject("estado");
                                int resultJSON = Integer.parseInt(response2.getString("estado"));
                                Log.v(LOGTAG, "Valor de estado: " + resultJSON);


                                if (resultJSON == 1) {
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


                                if (resultJSON == 2) {

                                    Toast.makeText(context, "No se obtuvo ningún registro asociado a ese Usuario", Toast.LENGTH_LONG).show();
                                }

                                if (resultJSON == 3) {

                                    Toast.makeText(context, "No se han informado los parámetros", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(context, "Se ha producido un error conectando al Servidor " + error.getMessage(), Toast.LENGTH_SHORT).show();

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


    public void traerMarcadoresNew() {
        String tag_json_obj_actual = "json_obj_req_actual";
        final String KEY_USERNAME_MARCADOR = "Usuario";
        final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";

        Log.d(LOGCAT, "Valor de usuarioMaps_KEY " + KEY_USERNAME_MARCADOR);
        Log.d(LOGCAT, "Valor de usuarioMaps_Valor " + usuarioMapas);

        String uri = String.format(patron_Busqueda_Url);


        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Obteniedo posiciones espera por favor...");
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
                        double velocidad = 0.0;

                        String fechaHora = "";
                        String telefonomarcador = "";


                        try {

                            //ES UN STRINGREQUEST---HAY QUE CREAR PRIMERO UN JSONObject PARA PODER EXTRAER TODO....
                            JSONObject json_Object = new JSONObject(response.toString());

                            //Sacamos el valor de estado
                            int resultJSON = Integer.parseInt(json_Object.getString("estado"));
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


                                velocidad = (int) conversionVelocidad((int) json_array.getJSONObject(z).getDouble("Velocidad"));
                                fechaHora = json_array.getJSONObject(z).getString("FechaHora");
                                milocalizacion = new LatLng(latitud, longitud);



            //CASO 1--http://petty.hol.es/obtener_localizaciones.php
           if (patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones.php") {

               telefonomarcador = json_array.getJSONObject(z).getString("Telefono");
               telefonowsp=telefonomarcador;


                    if (usuario.equals("Antonio")) {
                        MarkerOptions markerOptions =
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)

                                        .draggable(true)
                                        //.flat(true)
                                        .position(milocalizacion);

                        Marker marker = mMap.addMarker(markerOptions);
                        //marker.showInfoWindow();

                    }

                    else if (usuario.equalsIgnoreCase("Susana")) {

                        MarkerOptions markerOptions =
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                        .draggable(true)
                                        .position(milocalizacion)
                                        .flat(true);

                        Marker marker = mMap.addMarker(markerOptions);
                        marker.showInfoWindow();

                    } else if (usuario.equalsIgnoreCase("Dario")) {

                        MarkerOptions markerOptions =
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
                                        .draggable(true)
                                        .position(milocalizacion)
                                        .flat(true);


                        Marker marker = mMap.addMarker(markerOptions);
                        //marker.showInfoWindow();

                    }

                    //USUARIOS QUE NO TIENEN ICONO PROPIO
                    else {

                        if (z == 0) {

                            MarkerOptions markerOptions =
                                    new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                            .title(usuario)
                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                            .draggable(true)
                                            .position(milocalizacion)
                                            .flat(true);

                            Marker marker = mMap.addMarker(markerOptions);


                        }//Fin de z=0

                        if (z == 1) {
                            //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;

                            MarkerOptions markerOptions =
                                    new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                            .title(usuario)
                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                            .draggable(true)
                                            .position(milocalizacion)
                                            .flat(true);

                            Marker marker = mMap.addMarker(markerOptions);
                            //marker.showInfoWindow();
                        }//Fin del if del módulo

                        if (z % 2 == 0 && z != 0) {//Números pares y diferente de cero...
                            //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;

                            MarkerOptions markerOptions =
                                    new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                            .title(usuario)
                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                            .draggable(true)
                                            .position(milocalizacion)
                                            .flat(true);

                            Marker marker = mMap.addMarker(markerOptions);
                            //marker.showInfoWindow();
                        }//Fin del if del módulo

                        if ((z % 2 != 0) && z != 1 && z != 3 && z != 7 && z != 9) {//Número impar y diferente de 1

                            MarkerOptions markerOptions =
                                    new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                            .title(usuario)
                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                            .draggable(true)
                                            .position(milocalizacion)
                                            .flat(true);

                            Marker marker = mMap.addMarker(markerOptions);
                            //marker.showInfoWindow();
                        }//Fin del if del módulo

                        //Color del marcador por defecto
                        else{
                            MarkerOptions markerOptions =
                                    new MarkerOptions()
                                            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                            .title(usuario)
                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                            .draggable(true)
                                            .position(milocalizacion)
                                            .flat(true);

                            Marker marker = mMap.addMarker(markerOptions);
                        }


                    }//Fin else usuarios SIN ICONO PROPIO


               ////AQUI/////

               mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

              }//Fin patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones.php"--PRIMERA PESTAÑA


        //CASO 2-Ponemos marcadores para todas las posiciones de todos: ponemos marcadores por defecto
                else if (metodo_Get_POST == Request.Method.GET && patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones_todas.php") {

                    if (usuario.equals("Antonio")) {
                        MarkerOptions markerOptions =
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad+" KM/H.")

                                        //.draggable(true)
                                        //.flat(true)
                                        .position(milocalizacion);

                        Marker marker = mMap.addMarker(markerOptions);
                       // marker.showInfoWindow();

                    }

                    else if (usuario.equalsIgnoreCase("Susana")) {

                        MarkerOptions markerOptions =
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad+" KM/H.")
                                        //.draggable(true)
                                        .position(milocalizacion)
                                        .flat(true);

                        Marker marker = mMap.addMarker(markerOptions);
                        //marker.showInfoWindow();

                    } else if (usuario.equalsIgnoreCase("Dario")) {

                        MarkerOptions markerOptions =
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                        .anchor(0.0f, 1.0f)
                                        .title(usuario)
                                        .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad+" KM/H.")
                                        //.draggable(true)
                                        .position(milocalizacion)
                                        .flat(true);


                        Marker marker = mMap.addMarker(markerOptions);
                        //marker.showInfoWindow();

                    }



                    else {
                        //Usuarios que no están predefinidos

                        mMap.addMarker(new MarkerOptions()
                                .title(usuario)
                                .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad+" KM/H.")
                                .position(milocalizacion));
                    }


               ////AQUI/////

               mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                }

           //CASO 3-Ponemos marcadores para todas las posiciones del usuario que se ha logado: ponemos marcadores violetas y cambiamos el snipped
           else if (metodo_Get_POST == Request.Method.POST) {

                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                                .title(usuario)
                                                .snippet(calle + " " + numero + "/-/" + fechaHora + "/Vel/" + velocidad+" KM/H.")
                                                .position(milocalizacion));

                                    }

           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                                }//fin del else de marcadores

            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                            //Fin del JsonArray


                            // }//Fin del response

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(LOGTAG, "Error Respuesta en JSON: ");
                        }



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOGTAG, "Error Respuesta en JSON leyendo MarcadoresPost: " + error.getMessage());
                        VolleyLog.d(LOGTAG, "Error: " + error.getMessage());
                        Toast.makeText(context, "Se ha producido un error leyendo MarcadoresPost " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_USERNAME_MARCADOR, usuarioMapas);
                return map;
            }
        };


        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);


    }



    public void traerMarcadores() {

        String tag_json_obj_actual = "json_obj_req_actual";
        final String KEY_USERNAME_MARCADOR = "Usuario";
        final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";

        Log.d(LOGCAT, "Valor de usuarioMaps_KEY " + KEY_USERNAME_MARCADOR);
        Log.d(LOGCAT, "Valor de usuarioMaps_Valor " + usuarioMapas);

        String uri = String.format(patron_Busqueda_Url);


        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Obteniedo posiciones espera por favor...");
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
                        double velocidad = 0.0;

                        String fechaHora = "";
                        String telefonomarcador = "";


                        try {



                            //ES UN STRINGREQUEST---HAY QUE CREAR PRIMERO UN JSONObject PARA PODER EXTRAER TODO....
                            JSONObject json_Object = new JSONObject(response.toString());

                            //Sacamos el valor de estado
                            int resultJSON = Integer.parseInt(json_Object.getString("estado"));
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


                                //El teléfono solo viene informado en el caso de hacer la join con Usuarios...
                                if (patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones.php") {
                                    telefonomarcador = json_array.getJSONObject(z).getString("Telefono");
                                    telefonowsp=telefonomarcador;

                                }


                                velocidad = (int) conversionVelocidad((int) json_array.getJSONObject(z).getDouble("Velocidad"));

                                fechaHora = json_array.getJSONObject(z).getString("FechaHora");


                                milocalizacion = new LatLng(latitud, longitud);


                                if (usuario.equals("Antonio")) {

                                   /* mMap.addMarker(new MarkerOptions()

                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(telefonomarcador)
                                            .draggable(true)
                                            .position(milocalizacion));*/

                                    MarkerOptions markerOptions =
                                            new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                                    .anchor(0.0f, 1.0f)
                                                    .title(usuario)
                                                    .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)

                                                    .draggable(true)
                                                    //.flat(true)
                                                    .position(milocalizacion);



                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.showInfoWindow();



                                }

                                else if (usuario.equalsIgnoreCase("Susana")) {

                                    MarkerOptions markerOptions =
                                            new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                                    .anchor(0.0f, 1.0f)
                                                    .title(usuario)
                                                    .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                                    .draggable(true)
                                                    .position(milocalizacion)
                                                    .flat(true);


                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.showInfoWindow();

                                } else if (usuario.equalsIgnoreCase("Dario")) {

                                    MarkerOptions markerOptions =
                                            new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                                    .anchor(0.0f, 1.0f)
                                                    .title(usuario)
                                                    .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                                    .draggable(true)
                                                    .position(milocalizacion)
                                                    .flat(true);


                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.showInfoWindow();


                                } else {
                                    //GESTIONAMOS LOS COLORES DE LOS MARCADORES DEPENDIENDO DEL TIPO DE LLAMADA Y DEL SERVICIO AL QUE SE LLAME...

                                    //CASO 1-Ponemos marcadores para las últimas posiciones de todos
                                    if (metodo_Get_POST == Request.Method.GET && patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones.php") {

                                        if (z == 0) {

                                            MarkerOptions markerOptions =
                                                    new MarkerOptions()
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                                            .title(usuario)
                                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                                            .draggable(true)
                                                            .position(milocalizacion)
                                                            .flat(true);

                                            Marker marker = mMap.addMarker(markerOptions);


                                        }//Fin de z=0

                                        if (z == 1) {
                                            //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;

                                            MarkerOptions markerOptions =
                                                    new MarkerOptions()
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                                            .title(usuario)
                                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                                            .draggable(true)
                                                            .position(milocalizacion)
                                                            .flat(true);

                                            Marker marker = mMap.addMarker(markerOptions);
                                            //marker.showInfoWindow();
                                        }//Fin del if del módulo

                                        if (z % 2 == 0 && z != 0) {//Números pares y diferente de cero...
                                            //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;

                                            MarkerOptions markerOptions =
                                                    new MarkerOptions()
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                            .title(usuario)
                                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                                            .draggable(true)
                                                            .position(milocalizacion)
                                                            .flat(true);

                                            Marker marker = mMap.addMarker(markerOptions);
                                            //marker.showInfoWindow();
                                        }//Fin del if del módulo

                                        if ((z % 2 != 0) && z != 1 && z != 3 && z != 7 && z != 9) {//Número impar y diferente de 1


                                            MarkerOptions markerOptions =
                                                    new MarkerOptions()
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                            .title(usuario)
                                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                                            .draggable(true)
                                                            .position(milocalizacion)
                                                            .flat(true);

                                            Marker marker = mMap.addMarker(markerOptions);
                                            //marker.showInfoWindow();
                                        }//Fin del if del módulo


                                        else{//Color del marcador por defecto
                                            MarkerOptions markerOptions =
                                                    new MarkerOptions()
                                                            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                            .title(usuario)
                                                            .snippet("Día: "+fechaHora + " - Teléf: " + telefonomarcador)
                                                            .draggable(true)
                                                            .position(milocalizacion)
                                                            .flat(true);

                                            Marker marker = mMap.addMarker(markerOptions);
                                        }


                                    }

                                    //CASO 2-Ponemos marcadores para todas las posiciones de todos: ponemos marcadores por defecto
                                    else if (metodo_Get_POST == Request.Method.GET && patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones_todas.php") {

                                        mMap.addMarker(new MarkerOptions()
                                                .title(usuario)
                                                .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad)
                                                .position(milocalizacion));

                                    }

                                    //CASO 3-Ponemos marcadores para todas las posiciones del usuario que se ha logado: ponemos marcadores violetas y cambiamos el snipped
                                    else if (metodo_Get_POST == Request.Method.POST) {

                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                .title(usuario)
                                                .snippet(calle + " " + numero + "/-/" + fechaHora + "/Vel/" + velocidad)
                                                .position(milocalizacion));

                                    }


                                }//fin del else de marcadores

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                            }//Fin del JsonArray


                            // }//Fin del response

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(LOGTAG, "Error Respuesta en JSON: ");
                        }



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOGTAG, "Error Respuesta en JSON leyendo MarcadoresPost: " + error.getMessage());
                        VolleyLog.d(LOGTAG, "Error: " + error.getMessage());
                        Toast.makeText(context, "Se ha producido un error leyendo MarcadoresPost " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_USERNAME_MARCADOR, usuarioMapas);
                return map;
            }
        };


        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);

    }

    public void traerMarcadoresPostTodas() {

        String tag_json_obj_actual = "json_obj_req_actual";
        final String KEY_USERNAME_MARCADOR = "Usuario";
        final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";

        Log.d(LOGCAT, "Valor de usuarioMaps_KEY " + KEY_USERNAME_MARCADOR);
        Log.d(LOGCAT, "Valor de usuarioMaps_Valor " + usuarioMapas);

        String uri = String.format(patron_Busqueda_Url);


        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Obteniendo posiciones espera por favor...");
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
                        double velocidad = 0.0;

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
                            int resultJSON = Integer.parseInt(json_Object.getString("estado"));
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

                                //if (usuario.equalsIgnoreCase("Antonio")) {

                                if (usuario.equals("Antonio")) {
                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));

                                }

                                //mMap.addMarker(new MarkerOptions().position(milocalizacion).title(usuario+" está en "+direccion+" "+calle+" "+numero));

                                //else if (usuario.equalsIgnoreCase("Susana")) {
                                else if (usuario.equalsIgnoreCase("Susana")) {
                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));

                                }

                                //else if (usuario.equalsIgnoreCase("Dario")) {

                                else if (usuario.equalsIgnoreCase("Dario")) {
                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));
                                } else {
                                    //Gestionamos cambios en los colores de los marcadores.....

                                    if (z == 0) {
                                        //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;


                                        mMap.addMarker(new MarkerOptions()
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                                //.anchor(0.0f, 1.0f)
                                                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                                                //.icon(BitmapDescriptorFactory.defaultMarker(float hue))

                                                //.icon(getMarkerIcon(hue))
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))


                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));
                                    }//Fin de z=0

                                    if (z == 1) {
                                        //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;


                                        mMap.addMarker(new MarkerOptions()
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                                //.anchor(0.0f, 1.0f)
                                                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                                                //.icon(BitmapDescriptorFactory.defaultMarker(float hue))

                                                //.icon(getMarkerIcon(hue))
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))


                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));
                                    }//Fin del if del módulo

                                    if (z % 2 == 0 && z != 0) {//Números pares y diferente de cero...
                                        //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;


                                        mMap.addMarker(new MarkerOptions()
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                                //.anchor(0.0f, 1.0f)
                                                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                                                //.icon(BitmapDescriptorFactory.defaultMarker(float hue))

                                                //.icon(getMarkerIcon(hue))
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))


                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));
                                    }//Fin del if del módulo

                                    if ((z % 2 != 0) && z != 1) {//Número impar y diferente de 1
                                        //final float colormarcador = BitmapDescriptorFactory.HUE_AZURE;


                                        mMap.addMarker(new MarkerOptions()
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                                //.anchor(0.0f, 1.0f)
                                                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                                                //.icon(BitmapDescriptorFactory.defaultMarker(float hue))

                                                //.icon(getMarkerIcon(hue))
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))


                                                .title(usuario)
                                                .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                                .position(milocalizacion));
                                    }//Fin del if del módulo


                                }//fin del else de marcadores


                               /* else {
                                    mMap.addMarker(new MarkerOptions()
                                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                            //.anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));
                                }*/


                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

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
                        Toast.makeText(context, "Se ha producido un error leyendo MarcadoresPost " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_USERNAME_MARCADOR, usuarioMapas);
                return map;
            }
        };

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);

    }

    public void traerMarcadoresPostPropias() {

        String tag_json_obj_actual = "json_obj_req_actual";
        final String KEY_USERNAME_MARCADOR = "Usuario";
        final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";

        Log.d(LOGCAT, "Valor de usuarioMaps_KEY " + KEY_USERNAME_MARCADOR);
        Log.d(LOGCAT, "Valor de usuarioMaps_Valor " + usuarioMapas);

        String uri = String.format(patron_Busqueda_Url);


        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Obteniendo posiciones espera por favor...");
        pDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
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
                        double velocidad = 0.0;

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
                            int resultJSON = Integer.parseInt(json_Object.getString("estado"));
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

                                //if (usuario.equalsIgnoreCase("Antonio")) {

                                if (usuario.equals("Antonio")) {
                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));

                                }

                                //mMap.addMarker(new MarkerOptions().position(milocalizacion).title(usuario+" está en "+direccion+" "+calle+" "+numero));

                                //else if (usuario.equalsIgnoreCase("Susana")) {
                                else if (usuario.equalsIgnoreCase("Susana")) {
                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));

                                }

                                //else if (usuario.equalsIgnoreCase("Dario")) {

                                else if (usuario.equalsIgnoreCase("Dario")) {
                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));
                                } else {

                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                            .title(usuario)
                                            .snippet(calle + " " + numero + ">" + fechaHora + ">" + velocidad)
                                            .position(milocalizacion));


                                }//fin del else de marcadores


                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

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
                        Toast.makeText(context, "Se ha producido un error leyendo MarcadoresPost " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_USERNAME_MARCADOR, usuarioMapas);
                return map;
            }
        };

        /*RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);

    }

    // Para cambiar las tonalidades en el color de los marcadores
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
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


        double speedConvertida = (double) (speed / 1000) * 3600;

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

                    log("Dirección de localización:+ \n " + direccion + " " + poblacion);

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

        String INSERT = "http://petty.hol.es/insertar_localizacion.php";
        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));

        Calendar c1 = GregorianCalendar.getInstance();
        //System.out.println("Fecha actual: "+c1.getTime().toLocaleString());
        //usuario="Antonio";
        //usuario="Susana";
        fechaHora2 = System.currentTimeMillis();

        System.out.println("Fecha del sistema: " + fechaHora2);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Stringfechahora = sdf.format(fechaHora2);
        //System.out.println("Fecha del sistema: "+dateString);
        Log.v("", "Fecha del sistema: " + Stringfechahora);
        modificacion = calendarNow;


        //fechaHora2=c1;
        modificacion = calendarNow;

        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(INSERT);   // Parámetros que recibe doInBackground

    }

    public class ObtenerWebService extends AsyncTask<String, Void, String> {


        //CONECTA E INSERTA LAS LOCALIZACIONES AUTOMÁTICAS DEL USUARIO CONECTADO
        @Override
        protected String doInBackground(String... params) {

            String cadena = params[0];
            URL url = null; // Url de donde queremos obtener información
            String devuelve = "";

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
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line = br.readLine()) != null) {
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

            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
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

        milocalizacion = new LatLng(latitud, longitud);
        ///////////////////////////////////////////////////////
        //traerMarcadoresWebService();

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
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return false;
            }
        });


        mMap.getUiSettings().setMapToolbarEnabled(false);//Deshabilitamos los iconos con accesos a googlemaps

        mMap.getUiSettings().setMyLocationButtonEnabled(true);//Botón de ubicación activado.
        mMap.getUiSettings().setZoomControlsEnabled(true);

        traerMarcadoresNew();

        mMap.setOnMarkerDragListener(this);//Se implementan los tres métodos de la interfaz...

        mMap.setOnMarkerClickListener(this);


    }


    @Override//Cada vez que cambian los parámetros de la localización...
    public void onLocationChanged(Location location) {
        muestraLocaliz(location);
        muestradireccion(location);
        enviaDatosAlServidor();
        //traerMarcadoresWebService();


        traerMarcadoresNew();
        //traerMarcadoresPostPropias();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();



        if (id == R.id.mapamundi_tipo_terreno) {
            zoom=10;
            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
            return true;
        }



        if (id == R.id.mapamundi_tipo_satelite) {
            zoom=10;
            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
            return true;
        }


        if (id == R.id.mapamundi_tipo_hibrido) {
            zoom=10;
            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
            return true;
        }


        if (id == R.id.mapamundi_tipo_normal) {
            zoom=10;
            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
            return true;
        }

        if (id == R.id.mapa_tipo_terreno) {//En menú

            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 0));

            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(milocalizacion, 0)));
            return true;
        }

        if (id == R.id.mapa_tipo_satelite) {//En menú
            //Tipo satélite sin zomm
            zoom=0;
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 0));

            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom)));
            return true;
        }

        if (id == R.id.mapa_tipo_hibrido) {//En menú
            //Tipo híbrido sin zoom
            zoom=0;
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 0));

            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom)));
            return true;
        }


        if (id == R.id.mapa_tipo_normal) {//En menú
            zoom=0;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //Poniendo un zomm alto por defecto
            //Google ha realizado también mapas de interiores de algunos edificios.Coordenadas,profundidad Se pone un ejemplo....
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),15));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 0));

            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom)));

            return true;
        }

        return super.onOptionsItemSelected(item);
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