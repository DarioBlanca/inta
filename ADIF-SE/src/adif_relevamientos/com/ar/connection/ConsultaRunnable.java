package adif_relevamientos.com.ar.connection;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import adif_relevamientos.com.ar.DatabaseHandler;
import adif_relevamientos.com.ar.activities.OperationListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.Base64;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.google.gson.Gson;


/**
 * This thread is responsible for establishing the connection to the server for
 * update a user password in local db and upload the forms to the server
 * 
 * @author mauro
 */
public class ConsultaRunnable extends ApiTask  {

	private String urlBase = ApiTask.urlBase;
	private DatabaseHandler db;
	private Context context;
	private HashMap<String, String> argumentos;

	public ConsultaRunnable(DatabaseHandler db, Context context, HashMap<String, String> argumentos) {
		super(null);
		this.db = db;
		this.context = context;
		this.argumentos = argumentos;
	}

	private Token getJSONObject(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, Token.class);
	}

	@Override
	protected Object doInBackground(Object... params) {
		Object resultado = null;
		try {
			resultado = this.subirConsulta();
		} catch (Exception e) {

		}
		return resultado;
	}
	
	/**
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws JSONException
	 */
	private Object subirConsulta() throws IOException, GeneralSecurityException,
			JSONException {
		
		HashMap<String, String> detalles = db.getUserById(argumentos.get("userID"));
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
		
		url = urlBase + "/app_dev.php/api/consults.json";
		// 		Datos qe sea que le tenga que mandar
		String jsonText;
		HashMap<String, String> consulta = argumentos;
		if (consulta.get("json") != null) {
			jsonText = consulta.get("json");
		} else {
			jsonText = this.db.getConsultaJsonById(consulta.get("consultaID"));
		}
		JSONObject json = new JSONObject(jsonText);
		String asunto = json.getString("Asunto");
		String descripcion = json.getString("Descripcion");
		String latitud = consulta.get("latitud");
		String longitud = consulta.get("longitud");
		// 					Serializando la imagen
		String imagen;
			
		HttpURLConnection httpUrlConnection;

		httpUrlConnection = getConnection(url, "POST");
	
		DataOutputStream request = new DataOutputStream(
				httpUrlConnection.getOutputStream());
		if (latitud == null) {
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"_latitude\""
					+ crlf + crlf + "0" + crlf);
		} else {
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"_latitude\""
					+ crlf + crlf + latitud + crlf);
		}
		
		if (longitud == null) {
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"_longitude\""
					+ crlf + crlf + "0" + crlf);
		} else {
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"_longitude\""
					+ crlf + crlf + longitud + crlf);
		}
		
		request.writeBytes(twoHyphens + boundary + crlf);
		request.writeBytes("Content-Disposition: form-data; name=\"_subject\""
				+ crlf + crlf + asunto + crlf);
		request.writeBytes(twoHyphens + boundary + crlf);
		request.writeBytes("Content-Disposition: form-data; name=\"_description\""
				+ crlf + crlf + descripcion + crlf);
		request.writeBytes(twoHyphens + boundary + crlf);
		
		if (consulta.get("date") == null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date(); //2016/11/16 12:08:43
			request.writeBytes("Content-Disposition: form-data; name=\"_date\""
					+ crlf + crlf + (dateFormat.format(date)).toString() + crlf);
			request.writeBytes(twoHyphens + boundary + crlf);
		} else {
			request.writeBytes("Content-Disposition: form-data; name=\"_date\""
					+ crlf + crlf + consulta.get("date").toString() + crlf);
			request.writeBytes(twoHyphens + boundary + crlf);
		}
			
		if (consulta.get("imagen") != null) {
			attach(request,consulta.get("imagen"), 0);
		}	
		
		request.writeBytes(crlf);
		request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
		request.flush();
		
		int status = httpUrlConnection.getResponseCode();
		if (status == 200) {
			InputStreamReader in = new InputStreamReader((InputStream) httpUrlConnection.getContent());
			BufferedReader buff = new BufferedReader(in);
			String line = buff.readLine();
			in.close();
			JSONObject jsonRespuesta = new JSONObject(line);	
			String nuevoID = jsonRespuesta.getString("consult_id");
			httpUrlConnection.disconnect();
			db.updateConsulta(consulta.get("consultaID"), nuevoID);
		}
		request.close();
		return new Object();
	}	
	
	/**
	 * Agrega una imagen al request
	 * 
	 * @param request
	 * @param image
	 * @param i
	 *            número de imagen en caso de ser mas de una
	 * @throws IOException
	 */
	private void attach(DataOutputStream request, String imagePath, int i)
			throws IOException {
		if (imagePath != null) {
			File file = new File(imagePath);
			int size = (int) file.length();
			byte[] image = new byte[size];
			BufferedInputStream buf = new BufferedInputStream(
					new FileInputStream(file));
			buf.read(image, 0, image.length);
			buf.close();
			if (image != null) {
				String attachmentName = file.getName().replaceAll(".jpg", "");
				String attachmentFileName = file.getName();
				Bitmap bitmap = ThumbnailUtils.extractThumbnail((BitmapFactory.decodeFile(imagePath)), 500, 500);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				request.writeBytes("Content-Disposition: form-data; name=\""
						+ "_image" + "\";filename=\""
						+ attachmentFileName + "\"" + crlf);
				request.writeBytes(crlf);
				request.write(bos.toByteArray());
				request.writeBytes(crlf);
				request.writeBytes(twoHyphens + boundary + crlf);
			}
		}
	}
}