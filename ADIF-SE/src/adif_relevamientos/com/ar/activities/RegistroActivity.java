package adif_relevamientos.com.ar.activities;


import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import adif_relevamientos.com.ar.DatabaseHandler;
import adif_relevamientos.com.ar.connection.ApiRunnable;
import adif_relevamientos.com.ar.connection.ApiTask;
import adif_relevamientos.com.ar.utils.Barrier;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import adif_relevamientos.com.ar.activities.R;

public class RegistroActivity extends Activity{
	public static final String TAG = "adif.com.registro";
	private RegistroActivity context = this;
	private Barrier registroWait = new Barrier();
	private boolean someoneIsWaiting = false;
	private boolean ok = false;
	private String username;
	private String pass;
	private String email;
	private String passrepetida;
	private String phone;
	private DatabaseHandler db;
	private String codigo_error;
	private String url;
	private int codigo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		android.os.Process
		.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
		db = new DatabaseHandler(getApplicationContext());

		// Listening to registro
		TextView homeScreen = (TextView) findViewById(R.id.btnRegistro);
		homeScreen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				username = ((EditText) findViewById(R.id.usuario)).getText()
						.toString();
				pass = ((EditText) findViewById(R.id.pass)).getText()
						.toString();
				email = ((EditText) findViewById(R.id.email)).getText()
						.toString();
				phone = "2215555555";
				passrepetida = ((EditText) findViewById(R.id.passrepetida)).getText()
						.toString();
				String userID = "";

				if (pass.equals(passrepetida)) {
					
					RegisterRunnable tokenTask = (new RegisterRunnable());
					new Thread(tokenTask).start();
		 			someoneIsWaiting = true;
					try {
						registroWait.block();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					someoneIsWaiting = false;
					if (ok) {
						HashMap<String, String> user = new HashMap<String, String>();
						try {
							user = db.getUserDetails(username);
						} catch (GeneralSecurityException e) {
							e.printStackTrace();
						}
						Intent i = new Intent(getApplicationContext(),
								OperationListActivity.class);
						i.putExtra("user", user.get(DatabaseHandler.KEY_ID)); 
						startActivity(i);
						finish();
					} else {
						int duration = Toast.LENGTH_LONG;
						Toast toast = Toast.makeText(context, String.valueOf(codigo), duration);
						toast.show();
					}

				}else{
					int duration = Toast.LENGTH_LONG;
					String mensaje = "Las contraseñas no coiciden";
					Toast toast = Toast.makeText(context, mensaje, duration);
					toast.show();
				}
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent(getApplicationContext(),
					LoginActivity.class);
			startActivity(i);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * This task is responsible for establishing the connection to the server or
	 * local db to login into the adif system.
	 */
	

	private class RegisterRunnable extends ApiRunnable {

		public RegisterRunnable() {
			super(null);
		}

		private String urlBase = ApiTask.urlBase;
		
		@Override
		public void run() {
			try {
				/*
				 * Try to connect with the server to
				 * register new user
				 */
				String url = urlBase + "/app_dev.php/security/users/registers.json";
				Map<String, String> data = new HashMap<String, String>();
				data.put("_username", username);
				data.put("_password", pass);
				data.put("_email", email);
				data.put("_telephone", phone);
				HttpRequest post = HttpRequest.post(url).accept("application/json").form(data);
						//.authorization("application/x-www-form-urlencoded")
				codigo = post.code();
			} catch (HttpRequestException exception) {
			
			}
			try {
				responseVerifying(codigo);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
		
		@SuppressWarnings("deprecation")
		private void responseVerifying(int codigo) throws GeneralSecurityException {
			String mensaje;
			if (codigo == 200) {
				db.addUser(username, pass, email, phone);
				mensaje = "Bienvenido " + username + " a la aplicación del INTA";
				ok = true;
			} else {
				if (codigo == 300) {
					mensaje = "Usuario ya existe";
				} else {
					if (codigo == 400) {
						mensaje = "Alguno de los datos no es valido";
					} else {
						mensaje = "El servidor no esta funcionando, hay que intentarlo mas tarde";
					}
				}
				/*
				AlertDialog alertDialog = new AlertDialog.Builder(context).create();
				alertDialog.setTitle("No se pudo logear en el sistema");
				alertDialog
						.setMessage(mensaje);
				alertDialog.setButton("Listo",
						new DialogInterface.OnClickListener() {
							public void onClick(
									final DialogInterface dialog,
									final int which) {
							}
						});
				// Set the Icon for the Dialog
				alertDialog.setIcon(R.drawable.ic_launcher);
				alertDialog.show();
				*/
		    }
			codigo_error = mensaje;
			release();
		}	


	
		public void release(){
			try {
				if (someoneIsWaiting) {
					registroWait.releaseAll();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
