package com.antonioejemplo.localizaciones;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
import java.util.Objects;
import java.util.TimeZone;

import volley.AppController;

import static com.antonioejemplo.localizaciones.R.id.tab1;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

 /*********************************************** EXPLICACIÓN DE LOS MÉTODOS SOBREESCRITOS POR LA INTERFACES:******************************************************

 * OnMapReadyCallback==>onMapReady(GoogleMap var1)//EL MAPA ESTÁ LISTA PARA MOSTRAR POSCIONES.
 *
 * LocationListener==>ES EL ESCUCHADOR DE LOS CAMBIOS QUE SE PRODUCEN QUE TENGAN QUE VER CON EL  MAPA
  *          onLocationChanged(Location location)//CUANDO SE PRODUCEN CAMBIOS EN LAS POSICIONES
 *          onStatusChanged(String provider, int status, Bundle extras);//CUANDO SE CAMBIA DE PROVEEDOR
 *          onProviderEnabled(String provider);//CUANDO SE HABILITA EL PROVEEDOR
 *          onProviderDisabled(String provider);CUANDO SE DESHABILITA EL PROVEEDOR
 *
 * GoogleMap.OnMarkerDragListener==>EVENTO QUE SE PRODUCE AL ARRASTRAR UN MARCADOR
 *      onMarkerDragStart//AL EMPEZAR A ARRASTRARLO
 *      onMarkerDrag//CUANDO SE ESTÁ ARRASTRANDO
 *      onMarkerEnd//CUANDO SE HA DEJADO DE ARRASTRAR
 *
 * GoogleMap.OnMarkerClickListener==>AL HACER CLICK EN UN MARCADOR
 *      onMarkerClick(Marker var1);EN ESTE CASO AL HACER CLICK MOVEREMOS LA CÁMARA HACIENDO ZOOM
  *******************************************************************************************************************************************************************/


    public static final String LOGTAG = "OBTENER MARCADORES";
    private static final int SOLICITUD_ACCESS_FINE_LOCATION = 1;//Para control de permisos en Android M o superior
    private GoogleMap mMap;
    //private static final long TIEMPO_MIN = 60 * 1000; //Un minuto son 6000 milisegundos
    private static final long TIEMPO_MIN = 300 * 1000; //==> 5 minutos.300000 milisegundos.
    private static final long DISTANCIA_MIN = 100; // 100 metros
    private static final String[] A = {"n/d", "preciso", "impreciso"};
    private static final String[] P = {"n/d", "bajo", "medio", "alto"};
    private static final String[] E = {"fuera de servicio",
            "temporalmente no disponible ", "disponible"};

    private static String LOGCAT;
    private LocationManager manejador;
    private String proveedor;
    private Context context;

    //VARIABLES QUE VA A UTILIZAR EL SERVICIO
    static double longitud;
    static double latitud;
    static float velocidad;
    static double altitud;
    static String direccion;
    static String calle;
    static String poblacion;
    static String numero;

    String velocidad_dir;
    //VARIABLES RECOGIDAS DE LA ACTIVITY LOGIN
    static int id;
    String usuarioMapas;
    String email;
    String android_id;
    String telefono;
    String telefonowsp;

    static Calendar modificacion;
    long fechaHora2;
    static String Stringfechahora;

    private RequestQueue requestQueue;//Cola de peticiones de Volley. se encarga de gestionar automáticamente el envió de las peticiones, la administración de los hilos, la creación de la caché y la publicación de resultados en la UI.
    LatLng milocalizacion;

    //PATRONES DE BÚSQUEDA APLICADOS EN traerMarcadoresWebService DEPENDIENDO DE LA PESTAÑA QUE SE ABRA.
    private String patron_Busqueda_Url = Conexiones.ULTIMAS_UBICACIONES_URL_VOLLEY;
    private int metodo_Get_POST;
    private float zoom = 10;
    AlertDialog alert = null;
    //Controla la salida para que no se intente volver a ejecutar el servicio en onRestart();
    private boolean salir = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        //Recogemos los datos enviados por la activity login
        Bundle bundle = getIntent().getExtras();
        id = bundle.getInt("ID");
        usuarioMapas = bundle.getString("USUARIO");
        email = bundle.getString("EMAIL");
        android_id = bundle.getString("ANDROID_ID");
        telefono = bundle.getString("TELEFONO");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //PANTALLA SIEMPRE ENCENDIDA...
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //manejador es el LocationManager
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GpsNoHabilitado();//Avisa de que el GPS no está habilitado y da la posibiliddd de habilitarlo
        }

        //Cola de peticiones de Volley
        requestQueue = Volley.newRequestQueue(this);
       Toast.makeText(getApplicationContext(), getString(R.string.saludo) + usuarioMapas, Toast.LENGTH_SHORT).show();

        init();

        // Registrar escucha onMapReadyCallback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Gestionamos los permisos
        permisosPorAplicacion();

    }
    private void init(){

        Resources res = getResources();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabHost tabs = findViewById(android.R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("mitab1");
        spec.setContent(tab1);//Última localización de todos
        spec.setIndicator(res.getString(R.string.info_tab1), ContextCompat.getDrawable(MapsActivity.this,R.drawable.icono_ruta));
        tabs.addTab(spec);


        spec = tabs.newTabSpec("mitab2");//Todas las localizaciones de todos..res.getString(R.string.info_tab2)
        spec.setContent(R.id.tab2);
        spec.setIndicator(res.getString(R.string.info_tab2),  ContextCompat.getDrawable(MapsActivity.this,R.drawable.icono_ruta));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab3");//Todas tus localizaciones
        spec.setContent(R.id.tab3);
        spec.setIndicator(res.getString(R.string.info_tab3),  ContextCompat.getDrawable(MapsActivity.this,R.drawable.icono_ruta));
        tabs.addTab(spec);
        tabs.setCurrentTab(0);

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                //Recuperamos el contexto de la actividad
                Context contexto = MapsActivity.this;
                if (tabId.equals("mitab1")) {

                    mMap.clear();
                    //Traemos todas la última ubicación de cada usuario
                    patron_Busqueda_Url = Conexiones.ULTIMAS_UBICACIONES_URL_VOLLEY;
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
                    mMap.clear();
                    //Traemos todas las localizaciones de todos los usuarios
                    patron_Busqueda_Url = Conexiones.TODAS_UBICACIONES_URL_VOLLEY;
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
                    mMap.clear();
                    patron_Busqueda_Url = Conexiones.POR_USUARIO_UBICACIONES_URL_VOLLEY;
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

    }


    private void permisosPorAplicacion() {

        //Gestionamos los permisos según la versión. A partir de Android M algnos permisos catalogados como peligrosos se gestionan en tiempo de ejecución
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //1-La aplicación tiene permisos....

                utilizamosGps();

                //Toast.makeText(this, "1 Permiso Concedido", Toast.LENGTH_SHORT).show();

            } else {//No tiene permisos

                //explicarUsoPermiso();
                //solicitarPermiso();

                solicitarPermisoGPS();
            }

        } else {//No es Android M o superior. Ejecutamos de manera normal porque el permiso ya viene dado en el Manifiest

            utilizamosGps();
        }


    }

    private void solicitarPermisoGPS() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            //4-Pequeña explicación de para qué queremos los permisos
            LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
            Snackbar.make(contenedor, "Para que la app. funcione correctamente debe "
                    + " aceptarse el permiso para poder utilizar el GPS.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    SOLICITUD_ACCESS_FINE_LOCATION);
                        }
                    })
                    .show();
        } else {
            //5-Se muetra cuadro de diálogo predeterminado del sistema para que concedamos o denegemos el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    SOLICITUD_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        //Si se preguntara por más permisos el resultado se gestionaría desde aquí.
        if (requestCode == SOLICITUD_ACCESS_FINE_LOCATION) {//6-Se ha concedido los permisos... procedemos a ejecutar el proceso
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                utilizamosGps();

            }

            else {//7-NO se han concedido los permisos. No se puede ejecutar el proceso. Se le informa de ello al usuario.

                /*Snackbar.make(vista, "Sin el permiso, no puedo realizar la" +
                        "acción", Snackbar.LENGTH_SHORT).show();*/
                    //1-Seguimos el proceso de ejecucion sin esta accion: Esto lo recomienda Google
                    //2-Cancelamos el proceso actual
                    //3-Salimos de la aplicacion
                    Toast.makeText(this, "No se ha concedido el permiso necesario para que la aplicación utilice el GPS del dispositivo.", Toast.LENGTH_SHORT).show();
                    solicitarPermisosManualmente();
                }

        }
    }

    private void solicitarPermisosManualmente() {
        final CharSequence[] opciones = {"Si", "No"};
        final android.support.v7.app.AlertDialog.Builder alertOpciones = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Si")) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Los permisos no fueron aceptados. La aplicación no funcionará correctamente.", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();

    }

    private void utilizamosGps() {
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //NO HACEMOS NADA. A ESTE NIVEL EL PERMISO DE USO DE GPS YA ESTÁ APROVADO POR EL USUARIO.
            return;
        }
        Location localizacion = manejador.getLastKnownLocation(proveedor);
        muestraLocaliz(localizacion);
        muestradireccion(localizacion);
        traerMarcadoresNew();


    }

    private void GpsNoHabilitado() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS no está activado. La aplicación no funcionará correctamente. ¿Deseas activarlo ahora?")
                .setCancelable(false)
                .setPositiveButton(R.string.configurargps, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {/*EVENTO GENERADO AL ARRASTRAR MARCADORES. AL INICIO*/


        if (Objects.equals(patron_Busqueda_Url, "http://petylde.esy.es/obtener_localizaciones.php")) {//Solo para la primera pestaña que tiene el teléfono en el snippe

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
    public void onMarkerDragEnd(Marker marker) {/*EVENTO GENERADO AL ARRASTRAR MARCADORES.AL FINALIZAR EL ARRASTRE*/

        //Generamos los marcadores y borramos el que se ha creado al arrastrar el que ha generado el evento
        //mMap.clear();

        traerMarcadoresNew();
        marker.remove();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {/*EVENTO GENERADO AL HACER CLICK SOBRE LOS  MARCADORES*/

        if (zoom <= 10) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 18));
            zoom = 18;
        }

        return false;
    }

    public void traerMarcadoresNew() {
        String tag_json_obj_actual = "json_obj_req_actual";
        final String KEY_USERNAME_MARCADOR = "Usuario";
        //final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";
        final String LOGIN_URL = "http://petylde.esy.es/obtener_todas_por_usuario.php";

        Log.d(LOGCAT, "Valor de usuarioMaps_KEY " + KEY_USERNAME_MARCADOR);
        Log.d(LOGCAT, "Valor de usuarioMaps_Valor " + usuarioMapas);

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Obteniedo posiciones espera por favor...");
        pDialog.show();

        if (mMap !=null){
            mMap.clear();
        }


        StringRequest stringRequest = new StringRequest(metodo_Get_POST, patron_Busqueda_Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //pDialog.hide();
                        pDialog.dismiss();
                        String usuario = "";
                        String poblacion = "";
                        String calle = "";
                        String numero = "";
                        Double latitud = null;
                        Double longitud = null;
                        int velocidad = 0;

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
                                //usuario = json_array.getJSONObject(z).getString("Usuario");

                                usuario = json_array.getJSONObject(z).getString("Username");
                                poblacion = json_array.getJSONObject(z).getString("Poblacion");
                                calle = json_array.getJSONObject(z).getString("Calle");
                                numero = json_array.getJSONObject(z).getString("Numero");
                                longitud = json_array.getJSONObject(z).getDouble("Longitud");
                                latitud = json_array.getJSONObject(z).getDouble("Latitud");

                                //velocidad = (int) conversionVelocidad((int) json_array.getJSONObject(z).getDouble("Velocidad"));
                                velocidad = (int) conversionVelocidad(json_array.getJSONObject(z).getDouble("Velocidad"));
                                fechaHora = json_array.getJSONObject(z).getString("FechaHora");
                                milocalizacion = new LatLng(latitud, longitud);

                                //CASO 1--Traemos las últimas posiciones de todos
                                if (Objects.equals(patron_Busqueda_Url, Conexiones.ULTIMAS_UBICACIONES_URL_VOLLEY)) {

                                    //mMap.clear();
                                    telefonomarcador = json_array.getJSONObject(z).getString("Telefono");
                                    telefonowsp = telefonomarcador;


                                    if (usuario.equals("Antonio")) {
                                        MarkerOptions markerOptions =
                                                new MarkerOptions()
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholderotro))
                                                        //.icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.placeholderotro, "your text goes here")
                                                        .anchor(0.0f, 1.0f)
                                                        .title(usuario)
                                                        .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)

                                                        .draggable(true)
                                                        //.flat(true)
                                                        .position(milocalizacion);

                                        Marker marker = mMap.addMarker(markerOptions);
                                        //marker.isInfoWindowShown();


                                    } else if (usuario.equalsIgnoreCase("Susana")) {

                                        MarkerOptions markerOptions =
                                                new MarkerOptions()
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                                        .anchor(0.0f, 1.0f)
                                                        .title(usuario)
                                                        .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
                                                        .draggable(true)
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
                                                        .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
                                                        .draggable(true)
                                                        .position(milocalizacion)
                                                        .flat(true);


                                        Marker marker = mMap.addMarker(markerOptions);
                                        //marker.showInfoWindow();

                                    }

                                    //USUARIOS QUE NO TIENEN ICONO PROPIO
                                    else {
                                        MarkerOptions markerOptions =
                                                new MarkerOptions()
                                                        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholderbis))
                                                        .anchor(0.0f, 1.0f)
                                                        .title(usuario)
                                                        .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
                                                        .draggable(true)
                                                        .position(milocalizacion)
                                                        .flat(true);

                                        Marker marker = mMap.addMarker(markerOptions);


                                    }//Fin else usuarios SIN ICONO PROPIO

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                                }//Fin patron_Busqueda_Url Conexiones.ULTIMAS_UBICACIONES_URL_VOLLEY


                                //CASO 2-Ponemos marcadores para todas las posiciones de todos: ponemos marcadores por defecto
                                else if (metodo_Get_POST == Request.Method.GET && Objects.equals(patron_Busqueda_Url, Conexiones.TODAS_UBICACIONES_URL_VOLLEY)) {

                                    //mMap.clear();
                                    if (usuario.equals("Antonio")) {
                                        MarkerOptions markerOptions =
                                                new MarkerOptions()
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholderotro))
                                                        .anchor(0.0f, 1.0f)
                                                        .title(usuario)
                                                        .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad + " KM/H.")

                                                        //.draggable(true)
                                                        //.flat(true)
                                                        .position(milocalizacion);

                                        Marker marker = mMap.addMarker(markerOptions);
                                        // marker.showInfoWindow();

                                    } else if (usuario.equalsIgnoreCase("Susana")) {


                                        MarkerOptions markerOptions =
                                                new MarkerOptions()
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                                        .anchor(0.0f, 1.0f)
                                                        .title(usuario)
                                                        .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad + " KM/H.")
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
                                                        .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad + " KM/H.")
                                                        //.draggable(true)
                                                        .position(milocalizacion)
                                                        .flat(true);


                                        Marker marker = mMap.addMarker(markerOptions);
                                        //marker.showInfoWindow();

                                    } else {
                                        //Usuarios que no están predefinidos


                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholdermas))
                                                .anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle + " " + numero + "/-/" + fechaHora + "/-/" + velocidad + " KM/H.")
                                                .position(milocalizacion));
                                    }

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                                }//Fin patron_Busqueda_Url=patron_Busqueda_Url, Conexiones.TODAS_UBICACIONES_URL_VOLLEY

                                //CASO 3-Ponemos marcadores para todas las posiciones del usuario que se ha logado: ponemos marcadores violetas y cambiamos el snipped
                                else if (metodo_Get_POST == Request.Method.POST) {
                                    // mMap.clear();
                                    mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder))
                                            .anchor(0.0f, 1.0f)
                                            .title(usuario)
                                            .snippet(calle + " " + numero + "/-/" + fechaHora + "/Vel/" + velocidad + " KM/H.")
                                            .position(milocalizacion));

                                }

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));

                            }//fin del else de marcadores

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

    public void traerMarcadores() {

        String tag_json_obj_actual = "json_obj_req_actual";
        final String KEY_USERNAME_MARCADOR = "Usuario";
        //final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";
        final String LOGIN_URL = "http://petylde.esy.es/obtener_todas_por_usuario.php";

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
                                //patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones.php")
                                if (patron_Busqueda_Url == "http://petylde.esy.es/obtener_localizaciones.php") {
                                    telefonomarcador = json_array.getJSONObject(z).getString("Telefono");
                                    telefonowsp = telefonomarcador;

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


                                } else if (usuario.equalsIgnoreCase("Susana")) {

                                    MarkerOptions markerOptions =
                                            new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                                    .anchor(0.0f, 1.0f)
                                                    .title(usuario)
                                                    .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
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
                                    marker.showInfoWindow();


                                } else {
                                    //GESTIONAMOS LOS COLORES DE LOS MARCADORES DEPENDIENDO DEL TIPO DE LLAMADA Y DEL SERVICIO AL QUE SE LLAME...

                                    //CASO 1-Ponemos marcadores para las últimas posiciones de todos
                                    //(metodo_Get_POST == Request.Method.GET && patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones.php")
                                    if (metodo_Get_POST == Request.Method.GET && patron_Busqueda_Url == "http://petylde.esy.es/obtener_localizaciones.php") {

                                        if (z == 0) {

                                            MarkerOptions markerOptions =
                                                    new MarkerOptions()
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                                            .title(usuario)
                                                            .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
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
                                                            .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
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
                                                            .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
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
                                                            .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
                                                            .draggable(true)
                                                            .position(milocalizacion)
                                                            .flat(true);

                                            Marker marker = mMap.addMarker(markerOptions);
                                            //marker.showInfoWindow();
                                        }//Fin del if del módulo


                                        else {//Color del marcador por defecto
                                            MarkerOptions markerOptions =
                                                    new MarkerOptions()
                                                            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                            .title(usuario)
                                                            .snippet("Día: " + fechaHora + " - Teléf: " + telefonomarcador)
                                                            .draggable(true)
                                                            .position(milocalizacion)
                                                            .flat(true);

                                            Marker marker = mMap.addMarker(markerOptions);
                                        }


                                    }

                                    //CASO 2-Ponemos marcadores para todas las posiciones de todos: ponemos marcadores por defecto
                                    //else if (metodo_Get_POST == Request.Method.GET && patron_Busqueda_Url == "http://petty.hol.es/obtener_localizaciones_todas.php")
                                    else if (metodo_Get_POST == Request.Method.GET && patron_Busqueda_Url == "http://petylde.esy.es/obtener_localizaciones_todas.php") {

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
        //final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";
        final String LOGIN_URL = "http://petylde.esy.es/obtener_todas_por_usuario.php";

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
        //final String LOGIN_URL = "http://petty.hol.es/obtener_todas_por_usuario.php";
        final String LOGIN_URL = "http://petylde.esy.es/obtener_todas_por_usuario.php";

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


    private double conversionVelocidad(double speed) {
    /*Convierte la velocidad recogida a Km/h*/

        double speedConvertida = (speed / 1000) * 3600;

        return speedConvertida;
    }

    private void muestradireccion(Location location) {
    /*Devuelve velocidad,latitud, longitud y dirección a partir de lo que traiga el objeto Location*/
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

                    }

                    log("Dirección de localización:+ \n " + direccion + " " + poblacion);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void muestraProveedores() {
    /*Muestra los proveedores posible para utilizarlo después en el objeto Criteria*/
        log("Proveedores de localización: \n ");
        List<String> proveedores = manejador.getAllProviders();
        for (String proveedor : proveedores) {
            muestraProveedor(proveedor);
        }
    }

    private void muestraProveedor(String proveedor) {
        /*Lista los proveedores posibles*/
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

    private void enviarDatosAlServicioLocalizaciones() {
        //Arrancamos el servicio que mantendrá las inserciones activas

        //SI EL SERVICIO SE EJECUTA DESDE EL INICIO CON STARTCOMMAND LO LLAMARÍAMOS CON UN INTENT
        Intent interservice = new Intent(MapsActivity.this, ServicioLocalizaciones.class);

        startService(interservice);//Llama al starCommand del servicio
    }

    private void enviaDatosAlServidor() {
        /*PREPARA Y HACE LA LLAMADA PARA LA INSERCCIÓN AUTOMÁTICA DE LAS LOCALIZACIONES DEL USUARIO CONECTADO*/
        String url_Inserta_localizacion = Conexiones.INSERTAR_POSICION_URL_VOLLEY;
        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));

        Calendar c1 = GregorianCalendar.getInstance();
        //System.out.println("Fecha actual: "+c1.getTime().toLocaleString());
        //usuario="Antonio";
        //usuario="Susana";
        fechaHora2 = System.currentTimeMillis();

        //System.out.println("Fecha del sistema: " + fechaHora2);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Stringfechahora = sdf.format(fechaHora2);
        //System.out.println("Fecha del sistema: "+dateString);
        Log.v("", "Fecha del sistema: " + Stringfechahora);
        modificacion = calendarNow;


        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(url_Inserta_localizacion);   // Parámetros que recibe doInBackground

    }

    public class ObtenerWebService extends AsyncTask<String, Void, String> {
        //CONECTA CON EL WS E INSERTA LAS LOCALIZACIONES AUTOMÁTICAS DEL USUARIO CONECTADO
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

                jsonParam.put("Id_Usuario", id);
                jsonParam.put("Poblacion", poblacion);
                jsonParam.put("Calle", calle);
                jsonParam.put("Numero", numero);
                jsonParam.put("Longitud", longitud);
                jsonParam.put("Latitud", latitud);
                jsonParam.put("Velocidad", velocidad);
                jsonParam.put("FechaHora", Stringfechahora);
                jsonParam.put("Modificado", modificacion);//De momento pone 00:00:00
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

                    //String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                    //Log.d(LOGCAT, "Error insertando localización" + resultJSON);

                    //Sacamos el valor de estado
                    int resultJSON = Integer.parseInt(respuestaJSON.getString("estado"));


                    if (resultJSON == 1) {      // hay un registro que mostrar
                        devuelve = getString(R.string.localizacion_insertada);

                        //else if (resultJSON == "2")
                    } else if (resultJSON == 2) {
                        devuelve = getString(R.string.localizacion_error);
                        Log.d(LOGCAT, "Error insertando localización" + resultJSON);
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
                Log.d(LOGCAT, "Error insertando localización" + e);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return devuelve;

        }


        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        milocalizacion = new LatLng(latitud, longitud);

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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(context, R.string.gps_habilitado, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(context, R.string.gps_deshabilitado, Toast.LENGTH_SHORT).show();
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
            zoom = 10;
            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
            return true;
        }


        if (id == R.id.mapamundi_tipo_satelite) {
            zoom = 10;
            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
            return true;
        }


        if (id == R.id.mapamundi_tipo_hibrido) {
            zoom = 10;
            //Tipo terreno(calles) sin zomm
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom));
            return true;
        }


        if (id == R.id.mapamundi_tipo_normal) {
            zoom = 10;
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
            zoom = 0;
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 0));

            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom)));
            return true;
        }

        if (id == R.id.mapa_tipo_hibrido) {//En menú
            //Tipo híbrido sin zoom
            zoom = 0;
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),0));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion, 0));

            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(milocalizacion, zoom)));
            return true;
        }


        if (id == R.id.mapa_tipo_normal) {//En menú
            zoom = 0;
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

        //SE QUITA PORQUE SE EJECUTABA DOS VECES AL CAMBIAR EL SERVICO A LA ACTIVITY Y VICEVERSA
        /*if (proveedor != null) {
            manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN,
                    this);
        }*/

        //ArrancarServicio...
        if (salir == false) {
            enviarDatosAlServicioLocalizaciones();
        }

        Log.v(LOGCAT, "Activity-> onPause");
    }


    @Override
    protected void onRestart() {

        super.onRestart();

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


        //Actualizamos las modificaciones que se hayan producido en los marcadores...
        /*if (proveedor != null) {
            manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN,
                    this);
        }*/

        //Paramos el servicio
        if (salir == false) {
            stopService(new Intent(MapsActivity.this, ServicioLocalizaciones.class));
        }
        //enviaDatosAlServidor();


        Log.v(LOGCAT, "Activity-> onRestart");
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

        if (proveedor != null) {
            manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN,
                    this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alert != null) {
            alert.dismiss();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        /*Cada vez que cambian los parámetros de la localización: distancia y tiempo*/
        muestraLocaliz(location);
        muestradireccion(location);

        enviaDatosAlServidor();

        mMap.clear();
        if (!salir) {
            traerMarcadoresNew();
        }

    }

    @Override
    public void onBackPressed() {


        salidaControlada();
    }

    private void salidaControlada() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Seguro que deseas cerrar los mapas de la aplicación? Si lo haces para volver a acceder de nuevo tendrás que introducir de nuevo tu usuario y la contraseña.")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        salir = true;
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        //onResume();
                    }
                });
        alert = builder.create();
        alert.show();


    }


}