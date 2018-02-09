package com.antonioejemplo.localizaciones;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 *
 * SE UTILIZÓ PARA CREAR UNA CUARTA PESTAÑA.ACTUALMENTE NO SE USA.
 */
public class AdaptadorRecyclerView3 extends RecyclerView.Adapter<AdaptadorRecyclerView3.ContactosViewHolder> implements View.OnClickListener {

    //private ArrayList<Usuarios> items;//ArrayList de contactos
    //private OnItemClickListener escucha;
    private final Context contexto;
    ArrayList<Usuarios> listaUsuarios = null;

    Usuarios usuarios;
    private RequestQueue requestQueue;//Cola de peticiones de Volley. se encarga de gestionar automáticamente el envió de las peticiones, la administración de los hilos, la creación de la caché y la publicación de resultados en la UI.
    JsonObjectRequest jsArrayRequest;//Tipo de petición Volley utilizada...
    private View.OnClickListener listener;

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

    }


    /*  interface OnItemClickListener {
          public void onClick(RecyclerView.ViewHolder holder, int idPromocion, View v);
      }
  */
    //CLASE INTERNA CON VIEWHOLDER. CONTIENE EL MANEJADOR DE EVENTOS
    public class ContactosViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //Campos a mostrar en la celda
        TextView titulo;
        TextView subtitulo;
        TextView descripcion;
        TextView telefono;
        ImageView categoria;
        TextView txtObservaciones;
        Button contactar;

        ImageView ubicacion;
        TextView txtUbicacion;
        //Button ruta;
        TextView txtRuta;
        ImageView ruta;

        public ContactosViewHolder(View v) {
            super(v);

            titulo = (TextView) v.findViewById(R.id.text1);
            subtitulo = (TextView) v.findViewById(R.id.text2);
            descripcion = (TextView) v.findViewById(R.id.text3);
            categoria = (ImageView) v.findViewById(R.id.category);
            telefono = (TextView) v.findViewById(R.id.text4);


            ubicacion = (ImageView) v.findViewById(R.id.posicionamiento);
            txtUbicacion = (TextView) v.findViewById(R.id.txtubicacion);
            ruta = (ImageView) v.findViewById(R.id.imgruta);
            txtRuta = (TextView) v.findViewById(R.id.txtruta);

            //v.setOnClickListener(this);

            //categoria.setOnClickListener(this);


            /*txtUbicacion.setOnClickListener(this);
            txtRuta.setOnClickListener(this);
            ruta.setOnClickListener(this);
*/

        }


        private int obtenerIdContacto(int posicion) {

            //return (int)items.get(posicion).getId();
            return (int) listaUsuarios.get(posicion).getId();

            //return (int)contactos.get_id();
            //return items.getInt(contactos.get_id());

        /*    if (items != null) {



                if (items.moveToPosition(posicion)) {
                    return items.getInt(ConsultaAlquileres.ID_ALQUILER);
                } else {
                    return -1;
                }
            } else {
                return -1;
            }*/

        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (listener != null)

                listener.onClick(v);
        }
    }

    //CONSTRUCTOR DEL ADAPTADOR
    public AdaptadorRecyclerView3(ArrayList<Usuarios> datos, Context contexto) {


        String URL_BASE = "";

        this.listaUsuarios = datos;
        //this.escucha = escucha;
        this.contexto = contexto;

        requestQueue = Volley.newRequestQueue(contexto);

        // Nueva petición JSONObject

        jsArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_BASE,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listaUsuarios = parseJson(response);
                        notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d(TAG, "Error Respuesta en JSON llenado registros sin imagen: " + error.getMessage());

                    }
                }
        );
        // Añadir petición a la cola
        requestQueue.add(jsArrayRequest);

    }


    private ArrayList<Usuarios> parseJson(JSONObject respuestaJSON) {
        // Variables locales
        int id;
        int id_Servidor;
        //int id_Android = 0;
        String nombre;
        //String apellidos;
        //String direccion;
        String telefono;
        String email;
        //int idCategoria;
        //String observaciones;
        //String propietario;
        //String rutaimagen;
        Usuarios usuarios = null;

        //listaClientesJson=new ArrayList<Clientes>();
        listaUsuarios = new ArrayList<Usuarios>();
        //JSONArray jsonArray= null;

        try {


            //JSONObject respuestaJSON = new JSONObject(resultado.toString());//Creo un JSONObject a partir del StringBuilder pasado a cadena

            //Accedemos al vector de resultados


            //String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON. el primero que se encuentra...


            int resultJSON = Integer.parseInt(respuestaJSON.getString("estado"));
            //Log.v(LOGTAG, "Valor de estado: " + resultJSON);


            if (resultJSON == 1) {      // hay clientes a mostrar


                JSONArray arrayrespJson = respuestaJSON.getJSONArray("alumnos");


                for (int i = 0; i < arrayrespJson.length(); i++) {


                    JSONObject objetoJSon = arrayrespJson.getJSONObject(i);

                    id_Servidor = objetoJSon.getInt("Id");

                    //id_Android = objetoJSon.getInt("androidID");//Cuando se dió de alta desde la página web no está informado...
                    if (objetoJSon.has("AndroidID")) {//Se comprueba previamente si está informado el campo androidID
                        Log.i("AndroidID ", objetoJSon.getString("AndroidID"));
                        //id_Android = objetoJSon.getInt("AndroidID");

                    } else {
                        Log.i("Project Number ", "androidID no tiene valor en el JSON");
                        //id_Android = 0;
                    }


                    nombre = objetoJSon.getString("Username");
                    //apellidos = objetoJSon.getString("Apellidos");
                    //direccion = objetoJSon.getString("Direccion");
                    telefono = objetoJSon.getString("Telefono");
                    email = objetoJSon.getString("Email");
                    /*idCategoria = objetoJSon.getInt("Id_Categoria");
                    observaciones = objetoJSon.getString("Observaciones");
                    propietario = objetoJSon.getString("Owner");
                    rutaimagen=objetoJSon.getString("Rutaimagen");*/


                    usuarios = new Usuarios(id_Servidor, nombre, telefono, email);

                    //Obtenermos un ArrayList de clientes desde la BB.DD. de MySql

                    //listaClientesJson.add(clientes);
                    listaUsuarios.add(usuarios);

                            /*    if(clientes.getRutaimagen().equals("noimagen")){
                                    bitmap= BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.image1);
                                }
                                else{

                                    URL urlimagen=new URL("http://petty.hol.es/img"+objetoJSon.getString("Rutaimagen"));
                                    HttpURLConnection conimagen=(HttpURLConnection)urlimagen.openConnection();
                                    conimagen.connect();
                                    bitmap=BitmapFactory.decodeStream(conimagen.getInputStream());
                                }*/


                }//Fin bucle if estado==1


            }//Fin JSon

            //publishProgress(z);//Actualizamos la barra de progreso
            //}//Fin For barra de progreso


        } catch (JSONException e) {

            e.printStackTrace();
            //e.getMessage();
            Log.e("SERVICIO PHP", "ERROR LEYENDO JSON", e);
        }


        //return listaClientesJson;
        return listaUsuarios;
    }


    @Override
    public AdaptadorRecyclerView3.ContactosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fila_recyclerview2, parent, false);

        v.setOnClickListener(this);
        return new ContactosViewHolder(v);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(AdaptadorRecyclerView3.ContactosViewHolder holder, int position) {


        //contactos=items.get(position);
        usuarios = listaUsuarios.get(position);


        holder.titulo.setText(listaUsuarios.get(position).getNombre());
        holder.subtitulo.setText(listaUsuarios.get(position).getEmail());
        holder.telefono.setText(listaUsuarios.get(position).getTelefono());


        //Evento scroll en una textView
        // holder.txtObservaciones.setMovementMethod(new ScrollingMovementMethod());

        holder.categoria.setImageResource(R.drawable.brujula_litle);
        holder.ubicacion.setImageResource(R.drawable.icono_situar);

        //holder.descripcion.setText("CATEGORIA");

        //holder.categoria.setOnClickListener(holder);

    }


    @Override
    public int getItemCount() {


        //return items.size();
        return listaUsuarios != null ? listaUsuarios.size() : 0;
        //return items =0 ? 0:  items.size();
    }


}
