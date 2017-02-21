package adif_relevamientos.com.ar.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import adif_relevamientos.com.ar.activities.R;
import adif_relevamientos.com.ar.DatabaseHandler;
import adif_relevamientos.com.ar.connection.ConsultaRunnable;
import adif_relevamientos.com.ar.utils.Barrier;
import adif_relevamientos.com.ar.utils.Preview;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.formgenerator.FormActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class CameraActivity extends Activity implements LocationListener {

	private static final String TAG = "CameraActivity";
	private static final String APLICACION = "Relevamientos";
	private LocationManager locationManager;
	private Camera myCamera;
	private Preview preview;
	private WakeLock mWakeLock;
	private boolean nomorephoto = false;
	public static final int OPTION_SAVE = 0;
	public static final int OPTION_TAKEPICTURE = 1;
	private static final int OPTION_TAKEPICTUREFROMGALERY = 2;
	public static final int OPTION_CANCEL = 3;
	public static final int OPTION_LOGOUT = 4;
	private static final int RESULT_LOAD_IMAGE = 99999;
	private int countImages = 0;
	private boolean someoneIsWaiting = false;
	private Barrier cameraWait = new Barrier();
	private DatabaseHandler db;
	private String planillaId;
	private String userId;
	private String image1;
	private String image2;
	private String image3;
	private boolean manualgps = false;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (myCamera != null) {
			preview.getHolder().removeCallback(preview);
			myCamera.release();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (myCamera != null) {
			preview.getHolder().removeCallback(preview);
			myCamera.release();
		}
	}

	@SuppressLint("Wakelock")
	@Override
	public void onDestroy() {
		this.mWakeLock.release();
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		db = new DatabaseHandler(getApplicationContext());
		Intent myIntent = getIntent();
		planillaId = myIntent.getStringExtra("Planilla");
		userId = myIntent.getStringExtra("user");
		manualgps = myIntent.getBooleanExtra("manualgps", false);
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"My Tag");
		this.mWakeLock.acquire();
		configurationOfGpsService();
		configurationOfCamera();
		
		ImageView fotografiar = (ImageView) findViewById(R.id.foto);
		fotografiar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				myCamera.takePicture(myShutterCallback, myPictureCallback_RAW,
						myPictureCallback_JPG);
			}
		});
		
		ImageView elegirFoto = (ImageView) findViewById(R.id.galeria);
		elegirFoto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(intent, RESULT_LOAD_IMAGE);
			}
		});

	}

	/**
	 * Gps location service
	 */
	private void configurationOfGpsService() {
		if (!manualgps) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				int duration = Toast.LENGTH_LONG;   
				Toast toast = Toast.makeText(this, "GPS automático encendido, obteniendo coordenadas",
						duration);
				toast.show();
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 2000, 1, this);
			}
		}
	}

	/**
	 * Configuration of Camera
	 */
	private void configurationOfCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			myCamera = Camera.open();
			myCamera.setDisplayOrientation(90);
			
			Parameters params=myCamera.getParameters();
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			params.setRotation(90);
			List<Size> sizes = params.getSupportedPictureSizes();
			Camera.Size size = sizes.get(0);
			for(int i=0;i<sizes.size();i++)
			{
			    if(sizes.get(i).width > size.width)
			        size = sizes.get(i);
			}
			params.setPictureSize(size.width, size.height);
			myCamera.setParameters(params);
			// Create our Preview view and set it as the content of our
			// activity.
			
			preview = new Preview(this, myCamera);
			FrameLayout fPreview = (FrameLayout) findViewById(R.id.preview);
			fPreview.addView(preview);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("No se detectó una cámara en este dispositivo");
			builder.setMessage("Por favor, debes utilizar un dispositivo con camara");
			builder.setPositiveButton("Aceptar",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface,
								int i) {
						}
					});
			Dialog alertDialog = builder.create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
		}
	}

	/**
	 * Called exceeded 10-20 meters of last location
	 */
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();

		getMenuInflater().inflate(R.menu.camera, menu);
        return true;
	}

	@Override
	public boolean onMenuItemSelected(int id, MenuItem item) {
		Intent i;
		String latitud = "";
		String longitud = "";
		switch (item.getItemId()) {
		case R.id.listo:
			if (!manualgps) {
				if (locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					Location location = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (location != null) {
						latitud = String.valueOf(location.getLatitude());
						longitud = String.valueOf(location.getLongitude());
					}
				}
			}			
			db.saveLocalizedImages(planillaId, image1, image2, image3, latitud, longitud);
						
			//ALTA AL SERVIDOR
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			int durationserver = Toast.LENGTH_LONG;
			String resultText = "";
			
			if (netInfo != null && netInfo.isConnected()) {
				try {
					HashMap<String, String> argumentos = new HashMap<String, String>();
					argumentos.put("userID", userId);
					argumentos.put("consultaID", planillaId);
					argumentos.put("imagen", image1);
					argumentos.put("latitud", latitud);
					argumentos.put("longitud", longitud);
					Object result = new ConsultaRunnable(db,this.getApplicationContext(), argumentos).execute();
					resultText = "Consulta realizada con exito";
				} catch (Exception e) {
					resultText = "La consulta sera enviada una vez que tenga conexion a internet";
				}
			}
			/**
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(getApplicationContext(), resultText, duration);
			toast.show();
			**/
			i = new Intent(getApplicationContext(), OperationListActivity.class);
			i.putExtra("user", userId);
			startActivity(i);
			// Darlo de alta en el servidor
			finish();
			
			break;
		/**	
		case OPTION_TAKEPICTURE:
			myCamera.takePicture(myShutterCallback, myPictureCallback_RAW,
					myPictureCallback_JPG);
			break;
		case OPTION_TAKEPICTUREFROMGALERY:
			Intent intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

			startActivityForResult(intent, RESULT_LOAD_IMAGE);
			break;
		case OPTION_CANCEL:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					CameraActivity.this);
			alertDialog
					.setMessage("¿Desea eliminar los datos guardados hasta el momento?");
			alertDialog.setTitle("Cancelar carga de datos");
			alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
			alertDialog.setCancelable(false);
			alertDialog.setPositiveButton("Sí",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							db.deletePlanilla(planillaId);
							Intent i = new Intent(getApplicationContext(),
									OperationListActivity.class);
							i.putExtra("user", userId);
							startActivity(i);
							finish();
						}
					});
			alertDialog.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// código java si se ha pulsado no
						}
					});
			alertDialog.show();

			break;
		**/
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

	ShutterCallback myShutterCallback = new ShutterCallback() {
		public void onShutter() {

		}
	};

	PictureCallback myPictureCallback_RAW = new PictureCallback() {
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			
		}
	};

	PictureCallback myPictureCallback_JPG = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			countImages++;
			File directory = getDirectoryForPictures();
			SimpleDateFormat sdfDate = new SimpleDateFormat(
					"yyyy_MM_dd_HH_mm_ss");
			Date now = new Date();
			String date = sdfDate.format(now);
			File image = new File(directory, date + ".jpg");
			FileOutputStream fos = null;
			try {
				/**
				Bitmap  bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				bitmap = rotate(bitmap, 90, null);
				
				int bytes = bitmap.getByteCount();

				ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
				bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

				byte[] imageBytes = buffer.array();
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] imageBytes = stream.toByteArray();
				**/
				fos = new FileOutputStream(image);
				fos.write(data);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (countImages == 1) {
				// image1 = data;
				image1 = image.getAbsolutePath();
			}
			if (countImages == 2) {
				image2 = image.getAbsolutePath();
			}
			if (countImages == 3) {
				image3 = image.getAbsolutePath();
				nomorephoto = true;
				invalidateOptionsMenu();

			}
			myCamera.startPreview();
		}
	};

	/**
	 * Make picture directory
	 */
	private File getDirectoryForPictures() {
		File _directory = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				APLICACION);
		if (!_directory.exists()) {
			_directory.mkdir();
		}
		return _directory;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			countImages++;
			switch (countImages) {
			case 1:
				image1 = picturePath;
				break;
			case 2:
				image2 = picturePath;
				break;
			case 3:
				image3 = picturePath;
				nomorephoto = true;
				invalidateOptionsMenu();
				break;
			default:
			}
			configurationOfGpsService();
			configurationOfCamera();
			myCamera.startPreview();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					CameraActivity.this);
			alertDialog
					.setMessage("¿Desea eliminar los datos guardados hasta el momento?");
			alertDialog.setTitle("Cancelar carga de datos");
			alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
			alertDialog.setCancelable(false);
			alertDialog.setPositiveButton("Sí",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							db.deletePlanilla(planillaId);
							Intent i = new Intent(getApplicationContext(),
									OperationListActivity.class);
							i.putExtra("user", userId);
							startActivity(i);
							finish();
						}
					});
			alertDialog.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// código java si se ha pulsado no
						}
					});
			alertDialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void release(){
		try {
			if (someoneIsWaiting) {
				cameraWait.releaseAll();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}