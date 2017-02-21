package adif_relevamientos.com.ar.activities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;

import adif_relevamientos.com.ar.activities.R;

//import com.github.kevinsawicki.http.HttpRequest;

import adif_relevamientos.com.ar.DatabaseHandler;
import adif_relevamientos.com.ar.connection.ApiTask;
import adif_relevamientos.com.ar.connection.Token;
import adif_relevamientos.com.ar.connection.UpdateDBRunnable;
//import adif_relevamientos.com.ar.activities.LoginActivity.LoginRunnable;
//import adif_relevamientos.com.ar.connection.UploadDBRunnable;
import adif_relevamientos.com.ar.utils.Barrier;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.formgenerator.FormActivity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OperationListActivity extends FormActivity implements
		LocationListener {

	private ListView listView;
	private OperationListActivity context = this;
	private String userId;
	private DatabaseHandler db;
	private LocationManager locationManager;
	private Barrier operationWait = new Barrier();
	private boolean someoneIsWaiting = false;
	public static final int OPTION_ACEPT = 0;
	public static final int OPTION_CANCEL = 1;
	public static final int OPTION_LOGOUT = 2;
	public static final int OPTION_UPDATE = 3;
	private static final String TAG = "OperationLisstActivity";
	protected ArrayList<String> _estados;
	private LinkedHashMap<String, String> array_list = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> _array_descripciones = new LinkedHashMap<String, String>();
	private String[] _values;
	private ArrayAdapter<String> _adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Intent myIntent = getIntent();
		userId = myIntent.getStringExtra("user");
		setTitle(R.string.title_activity_operation_list);
		onPlanilla = false;
		onActivity = true;
		db = new DatabaseHandler(getApplicationContext());
		
		/**
		 * Actualizacion por medio de hilo
		 */
		
		setContentView(R.layout.activity_on_run_list);
		Object result = new ListadoRunnable().execute();
		
	}
	
	private void mostrarListado() {
		
		setContentView(R.layout.activity_operation_list);
		configurationOfGpsService();
		
		TextView homeScreen = (TextView) findViewById(R.id.btnRealizarConsulta);
		homeScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onPlanilla = true;
				onActivity = false;
				invalidateOptionsMenu();
				setTitle(R.string.title_activity_realizando);
				generateForm(FormActivity.parseFileToString(context,
						"consulta.json"));
			}
		});
		
		listView = (ListView) findViewById(R.id.list);		
		listView.setAdapter(_adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Se habilitan los botones del menu
				invalidateOptionsMenu();
				String itemValue = (String) listView
						.getItemAtPosition(position);
				String idConsulta = null;
				try {
					idConsulta = getConsultaId(itemValue);
				} catch (GeneralSecurityException | JSONException e) {
					e.printStackTrace();
				}
				String JsonConsulta = new String();
				try {
					JsonConsulta = db.getConsultaJsonById(idConsulta);
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
				Intent myIntent = new Intent(getApplicationContext(), RevisarConsultaActivity.class);
				myIntent.putExtra("json", JsonConsulta);
				myIntent.putExtra("consultaId", idConsulta);
				myIntent.putExtra("user", userId);
				startActivity(myIntent);
				
				/*
				 * Generar Formulario con valores manuales
				switch (itemValue) {
				case "Realizar consulta":
					type = "consulta";
					generateForm(FormActivity.parseFileToString(context,
							"consulta.json"));
					break;
				default:
					// Default
				}
				*/
			}

		});
		
		//Termino Listado
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }
	
	private String getConsultaId(String asunto) throws GeneralSecurityException, JSONException {
		ArrayList<HashMap<String,String>> consultas = db.getConsultas(userId);
		int i = 0;
		while (true) {
			String jsonConsulta = consultas.get(i).get("json");
			JSONObject obj = new JSONObject(jsonConsulta);
			String asuntoConsulta = obj.getString("Asunto");
			if (asuntoConsulta.equals(asunto)) {
				String idConsulta = consultas.get(i).get("id");
				return idConsulta;
			} else {
				i += 1;
			}
			
		}
	}

	/**
	 * Gps location service
	 */
	private void configurationOfGpsService() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 2000, 1, this);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mostrarListado();
			onPlanilla = false;
			onActivity = true;
			invalidateOptionsMenu();
			setTitle(R.string.title_activity_operation_list);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		if (onPlanilla) {
			menu.add(0, OPTION_ACEPT, 0, "Aceptar");
			menu.findItem(OPTION_ACEPT).setIcon(R.drawable.ic_action_accept);
			menu.add(1, OPTION_CANCEL, 1, "Cancelar");
			menu.findItem(OPTION_CANCEL).setIcon(R.drawable.ic_action_cancel);
		}
		if (onActivity) {
			menu.add(3, OPTION_UPDATE, 2, "Actualizar consultas");
			menu.findItem(OPTION_UPDATE).setIcon(R.drawable.ic_action_exit);
			menu.add(2, OPTION_LOGOUT, 3, "Salir de sesión");
			menu.findItem(OPTION_LOGOUT).setIcon(R.drawable.ic_action_exit);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int id, MenuItem item) {
		Intent i;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		int durationserver = Toast.LENGTH_LONG;
		switch (item.getItemId()) {
		case OPTION_ACEPT:
			// Se obtiene el .json de la planilla
			// y se pasa a la siguiente etapa (tomar fotos y geoposicionamiento automático de ser necesario)
			String planilla = save();
			
			//Darlo de alta en local
			
			String planillaId;	
			planillaId = db.storeConsulta(userId, planilla, null, null, null, null);
			
			boolean manualgps = false;
			
			Intent myIntent = new Intent(getApplicationContext(),
					CameraActivity.class);
			myIntent.putExtra("Planilla", planillaId);
			myIntent.putExtra("user", userId);
			myIntent.putExtra("manualgps", manualgps);
			startActivity(myIntent);
			finish();
			
			break;

		case OPTION_CANCEL:
			mostrarListado();
			onPlanilla = false;
			onActivity = true;
			invalidateOptionsMenu();
			setTitle(R.string.title_activity_operation_list);
			
			break;
		case OPTION_LOGOUT:
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, "Si la conexión wifi se encuentra activada se sincronizarán los datos con el servidor, por favor espere.", duration);
			toast.show();
			i = new Intent(getApplicationContext(), LoginActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
			break;
		case OPTION_UPDATE:
	
			Toast toastserver;
			toastserver = Toast.makeText(context, "Sincronizando datos con el servidor, por favor espere.", durationserver);
			toastserver.show();
			if (netInfo != null && netInfo.isConnected()) {
				
				db = new DatabaseHandler(getApplicationContext());
				
				//UpdateDBRunnable updateDBRunnable = new UpdateDBRunnable();
			
				//new Thread(updateDBRunnable).start();
				
				Object result = new UpdateDBRunnable(db, this, userId).execute();
				
				/**
				someoneIsWaiting = true;
				try {
					operationWait.block();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				someoneIsWaiting = false;
				**/
			}else{ 
				toastserver = Toast.makeText(context, "Debe tener conexion a internet para subir las consultas", durationserver);
				toastserver.show();
			}
			
			// Revisar si hubo cambio, y de ser asi, volver a cargar el operationList
			break;
		}

		return super.onMenuItemSelected(id, item);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (NoSuchMethodException e) {
					Log.e(TAG, "onMenuOpened", e);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public void onLocationChanged(Location location) {

	}

	/**
	 * Called when User off Gps in phone setting.
	 */
	@Override
	public void onProviderDisabled(String provider) {
		/*
		 * Send PROVIDER DISABLED ALARM to API AlertDialog.Builder builder = new
		 * AlertDialog.Builder(this);
		 * builder.setTitle("ALARMA: GPS Desactivado");
		 * builder.setMessage("Por favor, activa el servicio de localización y GPS"
		 * ); builder.setPositiveButton("Aceptar", new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialogInterface, int i) { // Show location
		 * settings when the user acknowledges the // alert dialog Intent intent
		 * = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		 * startActivity(intent); } }); Dialog alertDialog = builder.create();
		 * alertDialog.setCanceledOnTouchOutside(false); alertDialog.show();
		 */
	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}
	
	public void release(){
		try {
			if (someoneIsWaiting) {
				operationWait.releaseAll();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class ListadoRunnable extends ApiTask {
				
		public ListadoRunnable() {
			super(null);
		}
		
		protected void onPreExecute() {    
			 
		}
		
		protected void onPostExecute(Object result) {    
			// Actualizar cantidades
			mostrarListado();
			
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected()) {
				new ActualizacionRunnable().execute();
			}
		}
		
		protected Object doInBackground(Object...objects) {
			
			ArrayList<HashMap<String, String>> consultas = null;
			try {
				consultas = db.getConsultas(userId);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < consultas.size(); i++) {
				String json_consult = consultas.get(i).get("json");
				JSONObject obj = null;
				try {
					obj = new JSONObject(json_consult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					array_list.put(obj.getString("Asunto"), obj.getString("Estado"));
					_array_descripciones.put(String.valueOf(i), obj.getString("Descripcion"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
	        }
			
			Set<String> claves = array_list.keySet();
			_estados = new ArrayList<String>();
			Set<?> set = array_list.entrySet();
		    Iterator<?> iterator = set.iterator();
			while(iterator.hasNext()) {
		         Map.Entry<?,?> mentry = (Map.Entry<?,?>)iterator.next();
		         _estados.add(mentry.getValue().toString());
		      }
			
			ArrayList<String> lista = new ArrayList<String>();
			lista.addAll(claves);
			Collections.reverse(lista);
			Collections.reverse(_estados);
			_values = lista.toArray(new String[lista.size()]);
			
			//Termino de conseguir las consultas
			
			/**
			 * Listado con valores definidos a mano
			 * String[] values = new String[] {
			 *		"Realizar consulta", };
			 */
			
			// Cuando ande probar con getContext() en vez de this, y con getActivity()
			_adapter = new Adaptador(context, _values, _array_descripciones, userId, _estados);

			return new Object();
		}
		
	}
	
	private class ActualizacionRunnable extends ApiTask {
		
		private String urlBase = ApiTask.urlBase;
		
		public ActualizacionRunnable() {
			super(null);
			
		}
		
		private Token getJSONObject(String json) {
			Gson gson = new Gson();
			return gson.fromJson(json, Token.class);
		}
		
		protected Object doInBackground(Object...objects) {
			
			try {
				
				HashMap<String, String> detalles = db.getUserById(userId);
				String username = detalles.get("name");
				String password = detalles.get("pass");
				
				String url = urlBase + "/app_dev.php/security/tokens/creates.json";
				Map<String, String> data = new HashMap<String, String>();
				data.put("_username", username);
				data.put("_password", password);
				String jResponse = HttpRequest.post(url)
						.authorization("application/x-www-form-urlencoded")
						.accept("application/json").form(data).body();
				token = getJSONObject(jResponse);
				
				url = urlBase + "/app_dev.php/api/consult/cant.json";
				JSONObject json;
				String respuesta = "";
				
				HttpURLConnection httpUrlConnection = getConnection(url, "GET");
				/**
				DataOutputStream request = new DataOutputStream(
						httpUrlConnection.getOutputStream());
				request.flush();
				**/
				int status = httpUrlConnection.getResponseCode();
				
				InputStreamReader in = new InputStreamReader((InputStream) httpUrlConnection.getContent());
				BufferedReader buff = new BufferedReader(in);
				String line = buff.readLine();
				in.close();
				respuesta = line;
				//respuesta =  Respuesta.body().replace("]", "").replace("[", "");
				//request.close();
				
				json = new JSONObject(respuesta);
				int cantConsultasServidor = Integer.parseInt(json.getString("consults"));
				int cantAsistidasServidor = Integer.parseInt(json.getString("consults_assisted"));
				
				ArrayList<HashMap<String, String>> local = db.getConsultas(userId);
				int cantConsultasLocal = local.size();
				int cantAsistidasLocal = 0;
				for (int i = 0; i < cantConsultasLocal; i++) {
					String stringJson = local.get(i).get("json");
					JSONObject jsonConsulta = new JSONObject(stringJson);
					if (jsonConsulta.has("Nombre del tecnico")) {
						cantAsistidasLocal += 1;
					}
				}
				
				if (cantConsultasLocal < cantConsultasServidor || cantAsistidasLocal < cantAsistidasServidor) {
					Object resultado = new UpdateDBRunnable(db, context, userId).execute();
				} else {
					if (cantConsultasLocal > cantConsultasServidor) {
						db.actualizarConsultas(userId, context);
					}
				}
				
			} catch (GeneralSecurityException | JSONException | IOException e) {
				e.printStackTrace();
			//} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
			return null;
		}
	}





}
