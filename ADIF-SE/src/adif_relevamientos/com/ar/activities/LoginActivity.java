package adif_relevamientos.com.ar.activities;


import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import adif_relevamientos.com.ar.DatabaseHandler;
import adif_relevamientos.com.ar.connection.ApiRunnable;
import adif_relevamientos.com.ar.connection.ApiTask;
import adif_relevamientos.com.ar.connection.ConsultaRunnable;
import adif_relevamientos.com.ar.connection.UpdateDBRunnable;
import adif_relevamientos.com.ar.utils.Barrier;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import adif_relevamientos.com.ar.activities.R;

public class LoginActivity extends Activity{
	public static final String TAG = "adif.com.login";
	private LoginActivity context = this;
	private Barrier loginWait = new Barrier();
	private boolean login = false;
	private boolean someoneIsWaiting = false;
	private String username;
	private String pass;
	private DatabaseHandler db;
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		android.os.Process
		.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
		db = new DatabaseHandler(getApplicationContext());
		progress = (ProgressBar) findViewById(R.id.progreso);
		progress.setVisibility(View.GONE);
		// Listening to login
		
		TextView homeScreen = (TextView) findViewById(R.id.btnLogin);
		homeScreen.requestFocus();
		homeScreen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				username = ((EditText) findViewById(R.id.usuario)).getText()
						.toString();
				pass = ((EditText) findViewById(R.id.pass)).getText()
						.toString();
				
				Object result = new LoginRunnable(context, username, pass).execute();
			
			}
		});
				
		TextView homeScreenRegistro = (TextView) findViewById(R.id.btnRegistro);
		homeScreenRegistro.requestFocus();
		homeScreenRegistro.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						RegistroActivity.class); 
				
				startActivity(i);
				finish();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * This task is responsible for establishing the connection to the server or
	 * local db to login into the adif system.
	 */
	

	private class LoginRunnable extends ApiTask {
		private String _username;
		private String _pass;
		private String urlBase = ApiTask.urlBase;
		
		public LoginRunnable(Context contexto, String username, String pass) {
			super(null);
			_username = username;
			_pass = pass;			
		}
		
		protected void onPreExecute() {    
			progress.setVisibility(View.VISIBLE);  
		}
		
		protected Object doInBackground(Object... params) {
			Object resultado = null;
			try {
				loguearse();
				if (login) {
					// Switching to operation screen
					HashMap<String, String> user = new HashMap<String, String>();
					try {
						user = db.getUserDetails(_username);
					} catch (GeneralSecurityException e) {
						e.printStackTrace();
					}
					Intent i = new Intent(getApplicationContext(),
							OperationListActivity.class);
					i.putExtra("user", user.get(DatabaseHandler.KEY_ID)); 
					startActivity(i);
					finish();
				}
			} catch (Exception e) {

			}
			return resultado;
		}
		
		public void loguearse() {
			String response = "connectionError";
			try {
				// Primero se fija si el usuario ya se encuentra en la bd local 
				//Map<String, String> user = new HashMap<String, String>();
				HashMap<String, String> user = (HashMap<String, String>) db.getUserDetails(_username);
				if (user != null) {
					if (DatabaseHandler.encrypto(_pass).equals(
							user.get(DatabaseHandler.KEY_PASS))) {
						response = "ok";
					}
				} else {
					/*
					 * User can not be found , try to connect with the server to
					 * verify the identity
					 */
					String url = urlBase + "/app_dev.php/security/tokens/creates.json";
					Map<String, String> data = new HashMap<String, String>();
					data.put("_username", _username);
					data.put("_password", DatabaseHandler.encrypto(_pass));
					HttpRequest post = HttpRequest.post(url).accept("application/json").form(data);
							//.authorization("application/x-www-form-urlencoded")
							
					int x = post.code();		
					if (x == 200) {
						response = "new_user_ok";
					}
				}
			} catch (HttpRequestException exception) {

			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
			responseVerifying(response);
		}
		
		@SuppressWarnings("deprecation")
		private void responseVerifying(String response) {
			if (response.equals("connectionError")) {
				((LoginActivity) context).runOnUiThread(new Runnable() {
					public void run() {
						int duration = Toast.LENGTH_LONG;
						Toast toast = Toast.makeText(context, "El usuario o contraseña es incorrecto, intente nuevamente", duration);
						toast.show();
						progress.setVisibility(View.GONE);
					}
				});
			} else {
				login = true;
				try {
					if (response == "new_user_ok") {
						/* The user is not in the local bd */
						db.addUser(username, pass, null, null);
					}
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
			}
		}
		
	//Fin LoginTask
	}
}
