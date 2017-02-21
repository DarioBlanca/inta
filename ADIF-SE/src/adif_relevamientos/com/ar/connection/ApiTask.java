package adif_relevamientos.com.ar.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public abstract class ApiTask extends AsyncTask<Object, Object, Object> {

	/* Necessary settings for HttpUrlConection*/
	protected String crlf = "\r\n";
	protected String twoHyphens = "--";
	protected String boundary = "*****";
	protected Token token;
	//public static final String urlBase = "http://192.168.0.10/app.php";
	public static final String urlBase = "http://inta.devp.com.ar";
	public static final String TAG = "sig.com.api.task";
	
	public ApiTask(Token token){
		this.token = token;
	}
	
	
	public HttpURLConnection getConnection(String urlConection, String method) throws IOException{
		HttpURLConnection httpUrlConnection = null;
		URL url = new URL(urlConection);
		httpUrlConnection = (HttpURLConnection) url.openConnection();
		httpUrlConnection.setUseCaches(false);
		if (method.equals("POST")) {
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");
			httpUrlConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
		} else {
			httpUrlConnection.setRequestMethod("GET");
		}
		httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
		httpUrlConnection.setRequestProperty("Cache-Control",
				"no-cache");
		//httpUrlConnection.setRequestProperty("Authorization","WSSE profile=\"UsernameToken\"");
				
		// this header is used to authenticate the request
		httpUrlConnection.setRequestProperty("X-wsse", token.getWsse());
		return httpUrlConnection;
	}
	
	protected Object doInBackground() {
		return null;
	}
	
}
