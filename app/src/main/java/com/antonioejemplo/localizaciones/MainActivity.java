package com.antonioejemplo.localizaciones;

import android.Manifest;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LocationListener {


    private static final long TIEMPO_MIN = 10 * 1000; // 10 segundos
    private static final long DISTANCIA_MIN = 5; // 5 metros
    private static final String[] A = {"n/d", "preciso", "impreciso"};
    private static final String[] P = {"n/d", "bajo", "medio", "alto"};
    private static final String[] E = {"fuera de servicio",
            "temporalmente no disponible ", "disponible"};

    private static String LOGCAT;
    private LocationManager manejador;
    private String proveedor;
    private TextView salida;
    private Context context;

    private Button verMapa;


    double longitud;
    double latitud;
    double velocidad;
    double altitud;
    String direccion;
    String calle;
    String poblacion;
    String numero;
    String velocidad_dir;
    String usuario;
    Calendar fechaHora;
    Calendar modificacion;
    long fechaHora2;
    String Stringfechahora;

    // IP de mi Url
    String IP = "http://petty.hol.es/";
    // Rutas de los Web Services
    String GET = IP + "insertar_localizacion.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //salida = (TextView) findViewById(R.id.salida);
        verMapa=(Button)findViewById(R.id.btnvermapa);
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.v(LOGCAT, "Proveedores de localización: \n ");

        //LISTA TODOS LOS PROVEEDORES EXISTENTES EN EL TERMINAL
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location localizacion = manejador.getLastKnownLocation(proveedor);

        muestraLocaliz(localizacion);
        muestradireccion(localizacion);

        enviaDatosAlServidor( );


        verMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });


    }//fin Oncreate





    private void enviaDatosAlServidor() {

        String INSERT="http://petty.hol.es/insertar_localizacion.php";
        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));

        Calendar c1 = GregorianCalendar.getInstance();
        //System.out.println("Fecha actual: "+c1.getTime().toLocaleString());
        usuario="Antonio";
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
                jsonParam.put("Usuario", usuario);
                jsonParam.put("Poblacion", poblacion);
                jsonParam.put("Calle", calle);
                jsonParam.put("Numero", numero);
                jsonParam.put("Longitud", longitud);
                jsonParam.put("Latitud", latitud);
                jsonParam.put("Velocidad", velocidad_dir);
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



    // Métodos del ciclo de vida de la actividad
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



    /**METODO DE LA INTERFAZ LOCATIONlISTENERSE LLAMA CUANDO CAMBIA LA LOCALIZACIÓN
     * Cuando cambia la localización
     */
    @Override
    public void onLocationChanged(Location location) {
        log("Nueva localización: ");
        muestraLocaliz(location);
        muestradireccion(location);
        enviaDatosAlServidor();
    }

    /**METODO DE LA INTERFAZ LOCATIONlISTENER SE LLAMA CUANDO CAMBIA EL ESTADO DEL PROVEEDOR
     * Se le llama cuando cambia el estado de proveedor. Este método es llamado cuando
     * Un proveedor no es capaz de buscar una ubicación o si el proveedor tiene poco
     * Estén disponibles transcurrido un período de no disponibilidad.
     *
     * @param Proveedor el nombre del proveedor de ubicación asociada con este
     * Actualización.
     * @param Estado {@ link LocationProvider #} si el OUT_OF_SERVICE
     * Proveedor está fuera de servicio, y esto no se espera que cambie en el
     *                 futuro cercano; {@ Link LocationProvider #} si TEMPORARILY_UNAVAILABLE
     * El proveedor no está disponible temporalmente, pero se espera que esté disponible
     * En breve; y {@ link LocationProvider # DISPONIBLE} si el
     * Proveedor está disponible actualmente.
     * @param De extras opcionales un paquete que contendrá específica proveedor
     * Variables de estado.
     * <P />
     * <P> Un número de pares clave / valor común para el paquete de extras se enumeran
     * A continuación. Los proveedores que utilicen cualquiera de las teclas en esta lista que hay
     * Proporcionar el valor correspondiente, como se describe a continuación.
     * <P />
     * <Ul>
     * <Li> satélites - el número de satélites utilizados para derivar la solución
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("Cambia estado proveedor: " + proveedor + ", estado="
                + E[Math.max(0, status)] + ", extras=" + extras + "\n");
    }

    /**METODO DE LA INTERFAZ LOCATIONlISTENER SE LLAMA CUANDO EL PROVEEDOR LLAMADO ESTÁ HABILITADO
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {
        log("Proveedor habilitado: " + proveedor + "\n");
    }

    /**METODO DE LA INTERFAZ LOCATIONlISTENER SE LLAMA CUANDO EL PROVEEDOR LLAMADO ESTÁ DESHABILITADO
     *
     Se llama cuando el proveedor está deshabilitado por el usuario . Si requestLocationUpdates
     * Se llama en un proveedor ya desactivado, este método se llama
     * Inmediatamente.
     *
     * @param Proveedor el nombre del proveedor de ubicación asociada con este
     * Actualización.
     */
    @Override
    public void onProviderDisabled(String provider) {
        log("Proveedor deshabilitado: " + proveedor + "\n");
    }


}

