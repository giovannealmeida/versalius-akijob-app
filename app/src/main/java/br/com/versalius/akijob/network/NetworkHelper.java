package br.com.versalius.akijob.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.com.versalius.akijob.utils.EncryptHelper;

/**
 * Created by Giovanne on 28/06/2016.
 */
public class NetworkHelper {
    private static final String TAG = NetworkHelper.class.getSimpleName();

    private static NetworkHelper instance;
    private static Context context;
    private RequestQueue requestQueue;

    private final String DOMINIO = "http://devakijob.versalius.com.br/api"; // Remoto
//    private final String DOMINIO = "http://192.168.1.106/akijob/api"; // Repo

    private final String LOGIN = "/login";
    private final String FETCH_FORMS = "/fetchforms";
    private final String UPDATE_FORMS = "/updateforms";
    private final String SEND_SURVEY = "/survey";
    private final String CITY = "/city_controller";

    private NetworkHelper(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Pegar o contexto da aplicação garante que a requestQueue vai ser singleton e só
            // morre quando a aplicação parar
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    //Retorna uma instância estática de NetworkHelper
    public static synchronized NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context);
        }
        return instance;
    }

    public void doLogin(String cpf, String password, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("cpf", cpf);
        params.put("password", EncryptHelper.SHA1(password));
        execute(Request.Method.POST, params, TAG, DOMINIO + LOGIN, callback);
    }

    public void fetchForms(long userId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));

        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + FETCH_FORMS,
                callback);
    }

    public void getCities(long stateId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("state_id", String.valueOf(stateId));

        execute(Request.Method.GET,
                null, //GET não precisa de parâmetro no corpo
                TAG,
                buildGetURL(DOMINIO + CITY, params),
                callback);
    }

    private String buildGetURL(String url, HashMap<String, String> params) {
        url += "?";
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            url += pair.getKey() + "=" + pair.getValue();
            it.remove(); // avoids a ConcurrentModificationException
            if(it.hasNext()){
                url += "&";
            }
        }
        return url;
    }

    public void updateForms(String formsIds, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("forms_ids", formsIds);
        Log.i("UPDATE FORMS", params.toString());

        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + UPDATE_FORMS,
                callback);
    }

    /**
     * Recebe as questões respondidas em JSON e envia para o servidor
     *
     * @param response String JSON com as respostas
     * @param callback Callback da requisição Volley
     */
    public void sendSurvey(String response, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("data", response);
        Log.i("SendSurvey", "String enviada: " + params.toString());
        execute(Request.Method.POST, params, TAG, DOMINIO + SEND_SURVEY, callback);
    }

    private void execute(int method, final HashMap params, String tag, String url, final ResponseCallback callback) {
        final CustomRequest request = new CustomRequest(
                method,
                url,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("onResponse - LOG", "response: " + response);
                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("onResponse - LOG", "response: " + error.getMessage());
                        if (callback != null) {
                            callback.onFail(error);
                        }
                    }
                });

        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
