package volley;


import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;


public class MyJsonRequest extends JsonObjectRequest {

    //private Map<String, String> headers = new HashMap<String, String>();
    private Priority inmediatePriority = Priority.HIGH;

    public MyJsonRequest(int method,
                         String url,
                         JSONObject jsonRequest,
                         Listener<JSONObject> listener,
                         ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        // TODO Auto-generated constructor stub
    }

    public MyJsonRequest(String url,
                         JSONObject jsonRequest,
                         Listener<JSONObject> listener,
                         ErrorListener errorListener) {
        super(url,jsonRequest,listener,errorListener);
    }


    //NUEVO
    public MyJsonRequest(int method,
                         String url,
                         Listener<JSONObject> listener,
                         ErrorListener errorListener) {
        super(method, url, listener, errorListener);

    }



/* @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }*/

    /*public void setHeader(String title, String content) {
        headers.put(title, content);
    }*/

    public Priority getInmediatePriority() {
        return inmediatePriority;
    }

    public void setInmediatePriority(Priority inmediatePriority) {
        this.inmediatePriority = inmediatePriority;
    }
}
