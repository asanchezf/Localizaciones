package volley;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by Susana on 11/04/2016.
 */
public class MyJSonRequestImmediate extends JsonObjectRequest {

    //private Map<String, String> headers = new HashMap<String, String>();
    private Request.Priority inmediatePriority = Request.Priority.IMMEDIATE;

    public MyJSonRequestImmediate(int method,
                                  String url,
                                  JSONObject jsonRequest,
                                  Response.Listener<JSONObject> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        // TODO Auto-generated constructor stub
    }

    public MyJSonRequestImmediate(String url,
                                  JSONObject jsonRequest,
                                  Response.Listener<JSONObject> listener,
                                  Response.ErrorListener errorListener) {
        super(url,jsonRequest,listener,errorListener);
    }


    //NUEVO
    public MyJSonRequestImmediate(int method,
                                  String url,
                                  Response.Listener<JSONObject> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);

    }



/* @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }*/

    /*public void setHeader(String title, String content) {
        headers.put(title, content);
    }*/

    public Request.Priority getInmediatePriority() {
        return inmediatePriority;
    }

    public void setInmediatePriority(Request.Priority inmediatePriority) {
        this.inmediatePriority = inmediatePriority;
    }




}
