package com.antonioejemplo.localizaciones;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.util.List;
import java.util.Locale;
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
    double velocidad;
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

    private JsonObjectRequest myjsonObjectRequest;

    LatLng milocalizacion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    private void traerMarcadoresWebService() {
        //PETICIÓN PARA TRAER LOS MARCADORES DE TODOS LOS USUARIOS DEL WEBSERVICES:

        String tag_json_obj_actual = "json_obj_req_actual";

        //String ciudad = txtciudad.getText().toString();

        String patronUrl = "http://petty.hol.es/obtener_localizaciones.php";
        String uri = String.format(patronUrl);


        Log.v(LOGTAG, "Ha llegado a immediateRequestTiempoActual. Uri: " + uri);

        myjsonObjectRequest = new MyJSonRequestImmediate(//Prioridad
                Request.Method.GET,
                uri,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response2) {

                        String usuario = "";
                        String poblacion = "";
                        String calle = "";
                        String numero = "";
                        Double latitud = null;
                        Double longitud = null;
                        Double velocidad = null;

                        String fechaHora = "";

                        try {

                            for (int i = 0; i < response2.length(); i++) {
                                //JSONObject json_estado = response2.getJSONObject("estado");
                                String resultJSON = response2.getString("estado");

                                JSONArray json_array = response2.getJSONArray("alumnos");
                                for (int z = 0; z < json_array.length(); z++) {
                                    usuario = json_array.getJSONObject(z).getString("Usuario");
                                    poblacion = json_array.getJSONObject(z).getString("Poblacion");
                                    calle = json_array.getJSONObject(z).getString("Calle");
                                    numero = json_array.getJSONObject(z).getString("Numero");
                                    longitud = json_array.getJSONObject(z).getDouble("Longitud");
                                    latitud = json_array.getJSONObject(z).getDouble("Latitud");

                                    //Da error de conversión de datos. Probar...

                                    //velocidad = json_array.getJSONObject(z).getDouble("Velocidad");
                                    fechaHora = json_array.getJSONObject(z).getString("FechaHora");


                                    milocalizacion=new LatLng(latitud,longitud);

                                    if(usuario.equalsIgnoreCase("Antonio")){

                                                mMap.addMarker(new MarkerOptions()
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situar))
                                                        .anchor(0.0f, 1.0f)
                                                        .title(usuario)
                                                        .snippet(calle+" "+numero+"-->"+fechaHora)
                                                        .position(milocalizacion));

                                    }

                                    //mMap.addMarker(new MarkerOptions().position(milocalizacion).title(usuario+" está en "+direccion+" "+calle+" "+numero));

                                    else if (usuario.equalsIgnoreCase("Susana")){
                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_situacion))
                                                .anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle+" "+numero+"-->"+fechaHora)
                                                .position(milocalizacion));
                                    }

                                    else if (usuario.equalsIgnoreCase("Dario")){
                                        mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ruta))
                                                .anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle+" "+numero+"-->"+fechaHora)
                                                .position(milocalizacion));
                                    }

                                    else  {
                                        mMap.addMarker(new MarkerOptions()
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))//Icono por defecto
                                                //.anchor(0.0f, 1.0f)
                                                .title(usuario)
                                                .snippet(calle+" "+numero+"-->"+fechaHora)
                                                .position(milocalizacion));
                                    }

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion,10));

                                }


                            }

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
                        Toast.makeText(context, "Se ha producido un error conectando al Servidor", Toast.LENGTH_SHORT).show();

                    }
                }
        ) ;

        // Añadir petición a la cola
        AppController.getInstance().addToRequestQueue(myjsonObjectRequest, tag_json_obj_actual);
        /////////////////////////////////////////
   /*     milocalizacion=new LatLng(latitud,longitud);
        mMap.addMarker(new MarkerOptions().position(milocalizacion).title(usuario+" está en "+direccion+" "+calle+" "+numero));
 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milocalizacion,15));*/
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


    private void muestradireccion(Location location) {

        this.context = getApplicationContext();
        //location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        Geocoder geo;

        //Lo pasamos a miembros de la clase
        /*double longitud;
        double latitud;
        double velocidad;
        double altitud;
        String direccion;
        String calle;
        String poblacion;
        String numero;
        String velocidad_dir;*/

        if (location != null) {
            //Devolvemos los datos
            latitud = location.getLatitude();
            longitud = location.getLongitude();
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

                        velocidad_dir = Float.toString(location.getSpeed());


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

                jsonParam.put("Velocidad", velocidad_dir);

                //jsonParam.put("Velocidad", velocidad);
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
        traerMarcadoresWebService();

    }


    @Override//Cada vez que cambian los parámetros de la localización...
    public void onLocationChanged(Location location) {
        muestraLocaliz(location);
        muestradireccion(location);
        enviaDatosAlServidor();
        traerMarcadoresWebService();

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
