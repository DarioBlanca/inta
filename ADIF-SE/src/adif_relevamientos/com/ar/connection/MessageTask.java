package adif_relevamientos.com.ar.connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import android.util.Log;


/**
 * This task is responsible for send a message to the server.
 * 
 * 
 */
public class MessageTask extends ApiTask {

	public MessageTask(Token token) {
		super(token);
	}

	protected String doInBackground(Object... parameters) {
		try {
			HttpURLConnection httpUrlConnection = getConnection(urlBase + "/api/messages.json", "POST");				
			DataOutputStream request = new DataOutputStream(
					httpUrlConnection.getOutputStream());
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"planillaid\""
					+ crlf + crlf + (String) parameters[0].toString() + crlf);
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"latitud\""
					+ crlf + crlf + (String) parameters[1].toString() + crlf);
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"longitud\""
					+ crlf + crlf + (String) parameters[2].toString() + crlf);
			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"mensaje\""
					+ crlf + crlf + (String) parameters[3].toString() + crlf);
			request.writeBytes(crlf);
			request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
			request.flush();
			request.close();

			int status = httpUrlConnection.getResponseCode();
			httpUrlConnection.disconnect();
			return String.valueOf(status);
		} catch (IOException e) {
			return e.getMessage();
		}

	}

	protected void onPostExecute(String jResponse) {
		Log.i(TAG, "respuesta del servidor: " + jResponse);
	}

}