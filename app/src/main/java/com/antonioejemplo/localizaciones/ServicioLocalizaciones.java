package com.antonioejemplo.localizaciones;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

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

public class ServicioLocalizaciones extends Service implements LocationListener {
    //private static final long TIEMPO_MIN = 60 * 1000; //Se mide en milisegundos 60000 msg ==> 1 minuto
    private static final long TIEMPO_MIN = 900 * 1000; //Un minuto son 6000 milisegundos ==> 15 minutos.900000 milisegundos.
    private static final long DISTANCIA_MIN = 100; // 100 metros
    private static final String[] A = {"n/d", "preciso", "impreciso"};
    private static final String[] P = {"n/d", "bajo", "medio", "alto"};
    private static final String[] E = {"fuera de servicio",
            "temporalmente no disponible ", "disponible"};



    private static String LOGCATSERVICIO;
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
    int id;
    String usuarioMapas;
    String email;
    String android_id;
    String telefono;
    String telefonowsp;
    Calendar fechaHora;

    Calendar modificacion;
    long fechaHora2;
    String Stringfechahora;
    ObtenerWebService obtenerWebService;



    Location localizacion;
    GoogleMap mapServicio;
    private float zoom = 10;

    public ServicioLocalizaciones() {
        super();

        //Variables estáticas
        id = MapsActivity.id;
        poblacion = MapsActivity.poblacion;
        calle = MapsActivity.calle;
        numero = MapsActivity.numero;
        longitud = MapsActivity.longitud;
        latitud = MapsActivity.latitud;
        velocidad = MapsActivity.velocidad;
        Stringfechahora = MapsActivity.Stringfechahora;
        modificacion = MapsActivity.modificacion;

        Log.v(LOGCATSERVICIO, "ServicioLocalizaciones-> Constructor sin parámetros");
    }

    public ServicioLocalizaciones(Context contextoMaps, GoogleMap map) {
        super();

        this.mapServicio = map;
        this.context = contextoMaps;

        //Variables estáticas
        id = MapsActivity.id;
        poblacion = MapsActivity.poblacion;
        calle = MapsActivity.calle;
        numero = MapsActivity.numero;
        longitud = MapsActivity.longitud;
        latitud = MapsActivity.latitud;
        velocidad = MapsActivity.velocidad;
        Stringfechahora = MapsActivity.Stringfechahora;
        modificacion = MapsActivity.modificacion;

        Log.v(LOGCATSERVICIO, "ServicioLocalizaciones-> Constructor");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Servicio creado!", Toast.LENGTH_SHORT).show();

        //manejador es el LocationManager
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

        muestraProveedores();

        Criteria criterio = new Criteria();
        criterio.setCostAllowed(false);
        criterio.setAltitudeRequired(false);
        criterio.setAccuracy(Criteria.ACCURACY_FINE);
        proveedor = manejador.getBestProvider(criterio, true);
        Log.v(LOGCATSERVICIO, "Mejor proveedor: " + proveedor + "\n");
        Log.v(LOGCATSERVICIO, "Comenzamos con la última localización conocida:");

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
        localizacion = manejador.getLastKnownLocation(proveedor);
        manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN,
                this);
        muestraLocaliz(localizacion);
        muestradireccion(localizacion);


        Log.v(LOGCATSERVICIO, "ServicioLocalizaciones-> OnCreate y cambiaLocalizacion");

        //obtenerWebService=new ObtenerWebService();
        // enviaDatosAlServidor();

    }

    private void cambiaLocalizacion() {
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


        manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);


        muestraLocaliz(localizacion);
        muestradireccion(localizacion);

        Log.v(LOGCATSERVICIO, "ServicioLocalizaciones-> requestLocationUpdates en cambiaLocalizacion");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);


        Log.v(LOGCATSERVICIO, "ServicioLocalizaciones-> OnStarCommand");

        return START_STICKY;

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Servicio destruído!", Toast.LENGTH_SHORT).show();
        //obtenerWebService.cancel(true);
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

        Log.v(LOGCATSERVICIO, "ServicioLocalizaciones-> OnDestroy");

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

    private void muestraProveedores() {
    /*Muestra los proveedores posible para utilizarlo después en el objeto Criteria*/
        log("Proveedores de localización: \n ");
        List<String> proveedores = manejador.getAllProviders();
        for (String proveedor : proveedores) {
            muestraProveedor(proveedor);
        }
    }

    private void muestraLocaliz(Location localizacion) {
        if (localizacion == null)
            log("Localización desconocida\n");
        else
            log(localizacion.toString() + "\n");
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


    // Métodos para mostrar información
    private void log(String cadena) {
        //salida.append(cadena + "\n");
    }

    private void enviaDatosAlServidor() {

        //PREPARA Y HACE LA LLAMADA PARA LA INSERCCIÓN AUTOMÁTICA DE LAS LOCALIZACIONES DEL USUARIO CONECTADO
        String url_Inserta_localizacion = Conexiones.INSERTAR_POSICION_URL_VOLLEY;

        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));

        Calendar c1 = GregorianCalendar.getInstance();
        fechaHora2 = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Stringfechahora = sdf.format(fechaHora2);
        //System.out.println("Fecha del sistema: "+dateString);
        Log.v("", "Fecha del sistema: " + Stringfechahora);
        modificacion = calendarNow;

        ObtenerWebService hiloconexion = new ObtenerWebService();
        hiloconexion.execute(url_Inserta_localizacion);   // Parámetros que recibe doInBackground
    }


    public class ObtenerWebService extends AsyncTask<String, Void, String> {

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
                //jsonParam.put("Usuario", usuarioMapas);

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

                    //String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                    //Log.d(LOGCATSERVICIO, "Error insertando localización" + resultJSON);

                    //Sacamos el valor de estado
                    int resultJSON = Integer.parseInt(respuestaJSON.getString("estado"));


                    if (resultJSON == 1) {      // hay un registro que mostrar
                        devuelve = getString(R.string.localizacion_insertada_desdeServicio);

                        //else if (resultJSON == "2")
                    } else if (resultJSON == 2) {
                        devuelve = getString(R.string.localizacion_error);
                        Log.d(LOGCATSERVICIO, "Error insertando localización" + resultJSON);
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
                Log.d(LOGCATSERVICIO, "Error insertando localización" + e);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return devuelve;

        }


        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(LOGCATSERVICIO, "ServicioLocalizaciones-> onLocationChanged");
        //cambiaLocalizacion();
        muestraLocaliz(location);
        muestradireccion(location);
        enviaDatosAlServidor();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

    }


}
