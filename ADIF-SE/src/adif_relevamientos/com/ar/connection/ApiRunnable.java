package adif_relevamientos.com.ar.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public abstract class ApiRunnable implements Runnable {

	/* Necessary settings for HttpUrlConection */
	protected String crlf = "\r\n";
	protected String twoHyphens = "--";
	protected String boundary = "*****";
	protected Token token;
	// For local Test // public static final String urlBase =
	// "http://192.168.0.10/app_dev.php";
	public static final String urlBase = "http://inta.devp.com.ar";
	public static final String TAG = "sig.com.api.task";

	public ApiRunnable(Token token) {
		this.token = token;	
	}

	public HttpURLConnection getConnection(String urlConection)
			throws IOException {
		HttpURLConnection httpUrlConnection = null;
		URL url = new URL(urlConection);
		httpUrlConnection = (HttpURLConnection) url.openConnection();
		httpUrlConnection.setUseCaches(false);
		httpUrlConnection.setDoOutput(true);

		httpUrlConnection.setRequestMethod("POST");
		httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
		httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
		httpUrlConnection.setRequestProperty("Content-Type",
				"multipart/form-data;boundary=" + boundary);
		httpUrlConnection.setRequestProperty("Authorization",
				"WSSE profile=\"UsernameToken\"");
		// this header is used to authenticate the request
		httpUrlConnection.setRequestProperty("X-wsse", token.getWsse());

		return httpUrlConnection;
	}

}
