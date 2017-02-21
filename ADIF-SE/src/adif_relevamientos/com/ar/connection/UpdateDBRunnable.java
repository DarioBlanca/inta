package adif_relevamientos.com.ar.connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import adif_relevamientos.com.ar.DatabaseHandler;
import adif_relevamientos.com.ar.activities.LoginActivity;
import adif_relevamientos.com.ar.activities.OperationListActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.formgenerator.FormText;
import android.os.Handler;
import android.os.Looper;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * This thread is responsible for establishing the connection to the server for
 * update a user password in local db and upload the forms to the server
 * 
 * @author mauro
 */
public class UpdateDBRunnable extends ApiTask {

	private String urlBase = ApiTask.urlBase;
	private DatabaseHandler db;
	private Context context;
	private String user;

	public UpdateDBRunnable(DatabaseHandler db, OperationListActivity context, String userId) {
		super(null);
		this.db = db;
		this.context = context;
		this.user = userId;
	}
	
	public UpdateDBRunnable(DatabaseHandler db, LoginActivity context, String userId) {
		super(null);
		this.db = db;
		this.context = context;
		this.user = userId;
	}

	private Token getJSONObject(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, Token.class);
	}
	
	protected void onPostExecute() {
		
		Intent myIntent = new Intent(context, OperationListActivity.class);
		myIntent.putExtra("user", user);
	    context.startActivity(myIntent);
	    ((Activity) context).finish();
	}

	@Override
	protected Object doInBackground(Object... params) {
		Object resultado = null;
		try {
			resultado = this.update();
			onPostExecute();
		} catch (Exception e) {
			//Holi
		}
		return resultado;
	}
	
	/**
	 * Updates the local databases
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws JSONException
	 */
	//@Override
	private Object update() throws IOException, GeneralSecurityException,
			JSONException {		
		/* personal user token is obtained to update the local bd */
		HashMap<String, String> detalles = db.getUserById(user);
		String username = detalles.get("name");
		String password = detalles.get("pass");
		
		String url = urlBase + "/app_dev.php/security/tokens/creates.json";
		Map<String, String> data = new HashMap<String, String>();
		data.put("_username", username);
		data.put("_password", password);
		HttpRequest jResponse = HttpRequest.post(url)
				.authorization("application/x-www-form-urlencoded")
				.accept("application/json").form(data);
				
		String tokenRespuesta = jResponse.body();
		int codigo = jResponse.code();
		token = getJSONObject(tokenRespuesta);
		
		url = urlBase + "/app_dev.php/api/consult/list.json";
		
		HttpURLConnection httpUrlConnection;

		httpUrlConnection = getConnection(url, "GET");
		
		int status = httpUrlConnection.getResponseCode();
		InputStream ini = new BufferedInputStream(httpUrlConnection.getInputStream());
		InputStreamReader in = new InputStreamReader(ini);
		//InputStream inputStream = (InputStream) httpUrlConnection.getContent();
		//InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buff = new BufferedReader(in);
		String line = buff.readLine();
		in.close();
		String primero = line;
		//String[] respuesta =  jResponse.body().replace("]", "").replace("[", "").split(",");
		JSONArray listaJson = new JSONArray(primero);
		ArrayList<HashMap<String,String>> consultas = db.getConsultas(user); 
		
		for (int i = 0; i < listaJson.length(); i ++) {
			String stringJson = listaJson.getString(i);
			JSONObject jsonrespuesta = new JSONObject(stringJson);
			HashMap<String, String> consultaLocal = db.getConsulta(jsonrespuesta.getString("id_consult"));
			if (consultaLocal != null) {
				JSONObject jsonconsulta = new JSONObject(consultaLocal.get("json"));
				String assisted = jsonrespuesta.getString("assisted");
				if ( assisted == "1" ) {	
					if (!(jsonconsulta.has("Especialidad"))) {
				        jsonconsulta.put("Estado", "Consulta atendida");
				        jsonconsulta.put("Nombre del tecnico", jsonrespuesta.get("technical_name").toString());
				        jsonconsulta.put("Especialidad", jsonrespuesta.get("technical_specialty").toString());
				        jsonconsulta.put("Email", jsonrespuesta.get("technical_email").toString());
				        jsonconsulta.put("Telefono", jsonrespuesta.get("technical_telephone").toString());
				        db.updateConsultaJson(jsonconsulta.toString(), jsonrespuesta.getString("id_consult"));
					} 
				}
			} else {
				// Dar de alta consulta
				/**
				 * Pedir consulta especifica
				 */
				url = urlBase + "/app_dev.php/security/tokens/creates.json";
				data = new HashMap<String, String>();
				data.put("_username", username);
				data.put("_password", password);
				jResponse = HttpRequest.post(url)
						.authorization("application/x-www-form-urlencoded")
						.accept("application/json").form(data);
						
				tokenRespuesta = jResponse.body();
				codigo = jResponse.code();
				token = getJSONObject(tokenRespuesta);
				
				url = urlBase + "/app_dev.php/api/consults/" + jsonrespuesta.get("id_consult").toString() + ".json";
				
				httpUrlConnection = getConnection(url, "GET");
				status = httpUrlConnection.getResponseCode();
				
				if (status == 200) {
					ini = new BufferedInputStream(httpUrlConnection.getInputStream());
					in = new InputStreamReader(ini);
					buff = new BufferedReader(in);
					line = buff.readLine();
					in.close();
					String respuesta = line;
					String consulta =  respuesta.replace("]", "").replace("[", "");
					// Dar de alta consulta
					JSONObject jsonconsulta = new JSONObject(consulta);
					JSONObject jsonPaGuardar = new JSONObject();
					JSONArray hola = jsonconsulta.names();
					jsonPaGuardar.put("Asunto", jsonconsulta.get("subject"));
					jsonPaGuardar.put("Descripcion", jsonconsulta.get("description"));
					String assisted = jsonconsulta.get("assisted").toString();
					if ( assisted == "0") {
						jsonPaGuardar.put("Estado", "Su Consulta Sera Derivada A Un Tecnico");
					} else {
						jsonPaGuardar.put("Estado", "Consulta atendida");
						jsonPaGuardar.put("Nombre del tecnico", jsonrespuesta.get("technical_name").toString());
				        jsonPaGuardar.put("Especialidad", jsonrespuesta.get("technical_specialty").toString());
				        jsonPaGuardar.put("Email", jsonrespuesta.get("technical_email").toString());
				        jsonPaGuardar.put("Telefono", jsonrespuesta.get("technical_telephone").toString());
					}
					String json = jsonPaGuardar.toString();
					String consultaID = jsonconsulta.get("id").toString();
					String fecha = jsonconsulta.get("consult_date").toString();
					String resultado = db.storeConsulta(user, json, consultaID, null, null, fecha);
				}
			}
		}
		
		return new Object();
	}
}