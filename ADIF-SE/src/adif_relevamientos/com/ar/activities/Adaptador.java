package adif_relevamientos.com.ar.activities;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import adif_relevamientos.com.ar.activities.R;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import adif_relevamientos.com.ar.DatabaseHandler;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


/**
 * Clase adaptador
 **/
public class Adaptador extends ArrayAdapter<String> {
	private static final String TAG = null;
	private String[] values;
	private Context context;
	private HashMap<String, String> descripciones = new LinkedHashMap<String, String>();
	private DatabaseHandler db;
	private String userID;
	private LinkedHashMap<Integer, Bitmap> imagenes;
	private ArrayList<String> estados;
	
	public Adaptador(Context aContext, String[] values, LinkedHashMap<String, String> array_descripciones, String user, ArrayList<String> estados) {
		super(aContext, 0, values);
		this.values = values;
		this.context = aContext;
		this.estados = estados;
		Integer x = 0;
		for( int i = array_descripciones.size() -1; i >= 0 ; i --){
		    descripciones.put(String.valueOf(x), array_descripciones.get(String.valueOf(i)));
			x += 1;
		}
		this.userID = user;
		db = new DatabaseHandler(context);
		this.imagenes = getImagenes();
	}

	@SuppressLint("ViewHolder")
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.activity_revisar_consulta, null);
		TextView asunto = (TextView) rowView.findViewById(R.id.asunto);
		asunto.setText(values[position]);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.imagen);
		
		if (imagenes.get(Integer.valueOf(position)) != null) {
			Bitmap source = imagenes.get(Integer.valueOf(position));
			imageView.setImageBitmap(source);
		} else {
			imageView.setImageResource(R.drawable.imagen);
		}
		
		if ((estados.get(position).equals("Consulta atendida"))) {
			ImageView asistido = (ImageView) rowView.findViewById(R.id.asistido);
			asistido.setVisibility(View.VISIBLE);
		}	
		
		Integer num = (Integer) position;
		TextView descripcion = (TextView) rowView.findViewById(R.id.descripcion);
		CharSequence texto = this.descripciones.get(String.valueOf(num));
		descripcion.setText(texto);
		
		return rowView;
	}
	
	public static Bitmap rotate(Bitmap b, int degrees, Matrix m) {
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
	
	public String getConsultaId(String asunto) throws GeneralSecurityException, JSONException {
		ArrayList<HashMap<String,String>> consultas = this.db.getConsultas(this.userID);
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
	
	public LinkedHashMap<Integer, Bitmap> getImagenes() {
		LinkedHashMap<Integer, Bitmap> resultadoConImagenes = new LinkedHashMap<Integer, Bitmap>();
		LinkedHashMap<Integer, String> rutas = db.getImagesFromConsultas(this.userID);
		Iterator<Map.Entry<Integer, String>> it = rutas.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry<Integer, String> e = (Map.Entry<Integer, String>)it.next();
			if (e.getValue() != null) {
				File imgFile = new File(e.getValue());
				Bitmap source = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imgFile.getAbsolutePath()), 300, 300); 
				//source = rotate(source, 90, null);
				resultadoConImagenes.put(e.getKey(), source);
			} else {
				resultadoConImagenes.put(e.getKey(), null);
			}
		}
		//Hay que invertir el orden!
		LinkedHashMap<Integer, Bitmap> revertido = new LinkedHashMap<Integer, Bitmap>();
		Integer x = 0;
		for( int i = resultadoConImagenes.size() -1; i >= 0 ; i --){
		    revertido.put(x, resultadoConImagenes.get(i));
			x += 1;
		}
		return revertido;
	}
}

