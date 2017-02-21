package adif_relevamientos.com.ar.connection;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * WSSE token, for example:
 * "WSSE":"UsernameToken Username=\"***\", PasswordDigest=\"***\", Nonce=\"**\", Created=\"2014-05-30T20:16:52+00:00\""
 * 
 *
 * @author mauro
 *
 */
public class Token implements Serializable {
	private static final long serialVersionUID = 1L;
	@SerializedName("WSSE")
	private String wsse = null;

	public String getWsse() {
		this.wsse = wsse.replace("\u00f1", "Ã±");
		this.wsse = wsse.replace("ñ", "Ã±");
		return wsse;
	}

	public void setWsse(String wsse) {
		
		this.wsse = wsse.replace("\\","");
		this.wsse = wsse.replace("\u00f1", "Ã±");
		this.wsse = wsse.replace("ñ", "Ã±");
	}
	
	@Override
	public String toString(){
		return wsse;	
	}
	
}
