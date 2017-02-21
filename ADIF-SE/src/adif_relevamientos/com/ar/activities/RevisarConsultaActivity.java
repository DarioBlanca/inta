package adif_relevamientos.com.ar.activities;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import adif_relevamientos.com.ar.activities.R;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
//import com.github.kevinsawicki.http.HttpRequest;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;

import adif_relevamientos.com.ar.DatabaseHandler;
import adif_relevamientos.com.ar.connection.ApiTask;
//import adif_relevamientos.com.ar.activities.LoginActivity.LoginRunnable;
//import adif_relevamientos.com.ar.connection.UploadDBRunnable;
import adif_relevamientos.com.ar.utils.Barrier;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.formgenerator.FormActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RevisarConsultaActivity extends FormActivity {

	private RevisarConsultaActivity context = this;
	private String consultaId;
	private String json;
	private DatabaseHandler db;
	private Barrier operationWait = new Barrier();
	private boolean someoneIsWaiting = false;
	public static final int OPTION_ACEPT = 0;
	public static final int OPTION_CANCEL = 1;
	public static final int OPTION_LOGOUT = 2;
	public static final int OPTION_UPLOAD = 3;
	protected LinearLayout _container;
	protected ScrollView _realcontainer;
	public static final LayoutParams defaultLayoutParams = new LinearLayout.LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

	@Deprecated
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Intent myIntent = getIntent();
		consultaId = myIntent.getStringExtra("consultaId");
		json = myIntent.getStringExtra("json");
		setTitle(R.string.title_activity_revisar_consulta);
		db = new DatabaseHandler(getApplicationContext());
		
		setContentView(R.layout.activity_on_run_list);
		Object result = new MostrarConsultaRunnable().execute();
	}
	
	public void mostrar() {
		setContentView(_realcontainer);
	}

	public TextView titulo_estilo(TextView titulo)	{
		int color = getResources().getColor(R.color.eldesiempre);
		titulo.setBackgroundColor(color);
		titulo.setTextColor(Color.WHITE);
		titulo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
		titulo.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_medium));
		titulo.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		return titulo;
	}
	
	public TextView contenido_estilo(TextView contenido) {
		contenido.setTextColor(Color.BLACK);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    llp.setMargins(20,10,20,50); // llp.setMargins(left, top, right, bottom);
	    contenido.setLayoutParams(llp);
		return contenido;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
	
	private class MostrarConsultaRunnable extends ApiTask {
		
		public MostrarConsultaRunnable() {
			super(null);
		}
		
		protected void onPreExecute() {    
			 
		}
		
		protected void onPostExecute(Object result) {    
			// Pone en vista todo
			mostrar();
		}
		
		protected Object doInBackground(Object...objects) {
			
			_realcontainer = new ScrollView(context);
			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			_realcontainer.setLayoutParams(llp);
			
			_container = new LinearLayout(context);
			_container.setOrientation(LinearLayout.VERTICAL);
			_container.setLayoutParams(FormActivity.defaultLayoutParams);
			
			//Datos del Json
			try {
				JSONObject formulario = new JSONObject(json);
				Iterator<?> propiedades = formulario.keys();
				int i = 0;
				while ( propiedades.hasNext() ) {
					String clave = (String) propiedades.next();
					String valor = (String) formulario.getString(clave);
					TextView texto = new TextView(context);
					texto.setText(clave);
					texto = titulo_estilo(texto);
					TextView textoContenido = new TextView(context);
					textoContenido.setText(valor);
					textoContenido = contenido_estilo(textoContenido);
					_container.addView(texto);
					_container.addView(textoContenido);
				}			
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//Galeria
			
			LayoutParams parametros = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			parametros.setMargins(0, 50, 0, 0);
			final HorizontalScrollView horizontal = new HorizontalScrollView(context);
			horizontal.setLayoutParams(parametros);
			horizontal.setHorizontalScrollBarEnabled(false);
			
			
			LinearLayout llimagenes = new LinearLayout(context);
			llimagenes.setOrientation(LinearLayout.HORIZONTAL);
			llimagenes.setLayoutParams(defaultLayoutParams);
			
			String id = consultaId;
			HashMap<Integer, String> images = db.getImagesFromConsultaId(id);		
			
			for (int i = 0; i < images.size(); i++) {
				if (images.get(i) != null) {
					ImageView img = new ImageView(context);
					File imgFile = new  File(images.get(i));
					Bitmap source = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imgFile.getAbsolutePath()), 500, 500);
					
					//source = rotate(source, 90, null);
					img.setImageBitmap(source);
					LinearLayout.LayoutParams imageparams = new LinearLayout.LayoutParams(400,400);
					img.setLayoutParams(imageparams);
					img.setAdjustViewBounds(true);
					//img.setRotation(rotacion);
					llimagenes.addView(img);
				} else {
					
				}
			};
			horizontal.addView(llimagenes);
			_container.addView(horizontal);
				
			_realcontainer.addView(_container);

			return new Object();
		}
		
		public Bitmap rotate(Bitmap b, int degrees, Matrix m) {
	        if (degrees != 0 && b != null) {
	          if (m == null) {
	            m = new Matrix();
	          }
	            m.setRotate(degrees,
	                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
	            try {
	                Bitmap b2 = Bitmap.createBitmap(
	                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
	                if (b != b2) {
	                    b.recycle();
	                    b = b2;
	                }
	            } catch (OutOfMemoryError ex) {
	                // We have no memory to rotate. Return the original bitmap.
	                Log.e(TAG, "Got oom exception ", ex);
	            }
	        }
	        return b;
	    }
		
	}
}
