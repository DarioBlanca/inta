package adif_relevamientos.com.ar;

import java.io.File;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Date;

import adif_relevamientos.com.ar.connection.ConsultaRunnable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author mauro
 *
 */

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "inta";

	// Login and Object table name
	private static final String TABLE_LOGIN = "login";
	private static final String TABLE_CONSULT = "consulta";

	// Login and Object table Columns names
	public static final String KEY_ID = "id";
	// Login table Columns names
	public static final String KEY_NAME = "name";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_PASS = "pass";
	// public static final String KEY_LAST_LOGIN = "";
	// Object table Columns names
	public static final String KEY_JSON = "json";
	public static final String KEY_IMAGE1 = "image1";
	public static final String KEY_IMAGE2 = "image2";
	public static final String KEY_IMAGE3 = "image3";
	public static final String KEY_LATITUD = "latitud";
	public static final String KEY_LONGITUD = "longitud";
	public static final String KEY_DATE = "date";
	public static final String KEY_USER_ID = "userId";
	
	

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME
				+ " TEXT," + KEY_PASS + " TEXT," + KEY_EMAIL + " TEXT," + KEY_PHONE + " TEXT)";
		String CREATE_CONSULT_TABLE = "CREATE TABLE " + TABLE_CONSULT + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_ID
				+ " TEXT," + KEY_JSON + " TEXT," + KEY_IMAGE1 + " TEXT,"
				+ KEY_IMAGE2 + " TEXT," + KEY_IMAGE3 + " TEXT," + KEY_LATITUD + " TEXT," + KEY_LONGITUD + " TEXT," + KEY_DATE + " TEXT)";
		db.execSQL(CREATE_LOGIN_TABLE);
		db.execSQL(CREATE_CONSULT_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// oh siii
	}

	/**
	 * Storing user details in database
	 * 
	 * @throws GeneralSecurityException
	 * */
	
	public String addUser(String name, String pass, String email, String phone)
			throws GeneralSecurityException {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name.toLowerCase());
		values.put(KEY_PASS, encrypto(pass));
		values.put(KEY_EMAIL, email);
		values.put(KEY_PHONE, phone);
		long userID = db.insert(TABLE_LOGIN, null, values);
		db.close();
		return String.valueOf(userID);
	}
	/**
	 * Re create database Delete all tables and create them again
	 * */
	public void resetTables() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LOGIN, null, null);
		db.delete(TABLE_CONSULT, null, null);
		db.close();
	}
	
	public void DeleteConsultas(String userId) {
		SQLiteDatabase db = this.getWritableDatabase();
		//String consulta = "DELETE * FROM " + TABLE_CONSULT + " WHERE " + KEY_USER_ID + "=?";
		db.execSQL("delete from "+ TABLE_CONSULT);
		db.close();
	}
	
	
	
	/**
	 * MD5 Encryption
	 * 
	 * @param pass
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static String encrypto(String pass) throws GeneralSecurityException {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			byte[] array = md.digest(pass.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;

	}
	
	public HashMap<String, String> getConsulta(String id) {
		HashMap<String, String> consulta = new HashMap<String, String>();
		String query = "SELECT * FROM " + TABLE_CONSULT + " WHERE " + KEY_ID + " = ?";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query, new String[] { id });
		boolean ok = cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			consulta.put(KEY_ID, cursor.getString(cursor.getColumnIndex(KEY_ID)));
			consulta.put(KEY_JSON, cursor.getString(cursor.getColumnIndex(KEY_JSON)));
			consulta.put(KEY_IMAGE1, cursor.getString(cursor.getColumnIndex(KEY_IMAGE1)));
			consulta.put(KEY_LATITUD, cursor.getString(cursor.getColumnIndex(KEY_LATITUD)));
			consulta.put(KEY_LONGITUD, cursor.getString(cursor.getColumnIndex(KEY_LONGITUD)));
			consulta.put(KEY_USER_ID, cursor.getString(cursor.getColumnIndex(KEY_USER_ID)));
		} else {
			consulta = null;
		}
		cursor.close();
		db.close();
		return consulta;
	}
	
	public boolean saveLocalizedImages(String planillaId, String image1,
			String image2, String image3, String latitud, String longitud) {
		/*
		String selectQuery = "SELECT  * FROM " + TABLE_CONSULT + " WHERE "
				+ KEY_ID + "= ?";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, new String[] { planillaId });
		cursor.moveToFirst();
		String userId = cursor.getString(1);
		String json= cursor.getString(2);
		String type = cursor.getString(3);
		//deletePlanilla(planillaId);
		cursor.close();
		db.close();
		*/
		SQLiteDatabase dbw = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		//contentValues.put(KEY_USER_ID, userId); // User
		//contentValues.put(KEY_JSON, json); //  Form
		//contentValues.put(KEY_TYPE, type); // Form Type
		if (image1 != null){
			contentValues.put(KEY_IMAGE1, image1); // Image 1
		}else{
			contentValues.putNull(KEY_IMAGE1); // Image 1
		}
		if (image2 != null) {
			contentValues.put(KEY_IMAGE2, image2); // Image 2
		}else{
			contentValues.putNull(KEY_IMAGE2); // Image 2
		}
		if (image3 != null){
			contentValues.put(KEY_IMAGE3, image3); // Image 3
		}else{
			contentValues.putNull(KEY_IMAGE3); // Image 3
		}
		contentValues.put(KEY_LATITUD, latitud); // Latitud
		contentValues.put(KEY_LONGITUD, longitud); // Longitud		
		String[] args = new String[]{planillaId};
		// Latitud y longitud
		dbw.update(TABLE_CONSULT, contentValues, "id=?", args);
		dbw.close();

		return true;
	}
	
	public HashMap<String, String> getUserDetails(String name)
			throws GeneralSecurityException {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_LOGIN + " WHERE "
				+ KEY_NAME + "= ?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, new String[] { name.toLowerCase() });
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put(KEY_ID, cursor.getString(cursor.getColumnIndex(KEY_ID)));
			user.put(KEY_NAME, cursor.getString(cursor.getColumnIndex(KEY_NAME)));
			user.put(KEY_PASS, cursor.getString(cursor.getColumnIndex(KEY_PASS)));
		} else {
			user = null;
		}
		cursor.close();
		db.close();
		return user;
	}
	
	//@throws GeneralSecurityException
	public HashMap<String, String> getUserById(String id)
			throws GeneralSecurityException {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_LOGIN + " WHERE "
				+ KEY_ID + "= ?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, new String[] { id });
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put(KEY_ID, cursor.getString(cursor.getColumnIndex(KEY_ID)));
			user.put(KEY_NAME, cursor.getString(cursor.getColumnIndex(KEY_NAME)));
			user.put(KEY_PASS, cursor.getString(cursor.getColumnIndex(KEY_PASS)));
		} else {
			user = null;
		}
		cursor.close();
		db.close();
		// return user
		return user;
	}
	
	public String getConsultaJsonById(String id)
			throws GeneralSecurityException {
		String consulta = new String();
		String selectQuery = "SELECT " + KEY_JSON + " FROM " + TABLE_CONSULT + " WHERE "
				+ KEY_ID + "= ?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, new String[] { id });
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			consulta = (String) cursor.getString(0);
		} else {
			consulta = null;
		}
		cursor.close();
		db.close();
		// return user
		return consulta;
	}
	
	public ArrayList<HashMap<String,String>> getConsultas(String id)
			throws GeneralSecurityException {
		
		ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT * FROM " + TABLE_CONSULT + " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_DATE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, new String[] { id });
		
		// Move to first row
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			HashMap<String, String> consulta = new HashMap<String, String>();
			consulta.put(KEY_ID, cursor.getString(cursor.getColumnIndex(KEY_ID)));
			consulta.put(KEY_USER_ID, cursor.getString(cursor.getColumnIndex(KEY_USER_ID)));
			consulta.put(KEY_JSON, cursor.getString(cursor.getColumnIndex(KEY_JSON)));
			//consulta.put(KEY_LATITUD, cursor.getString(cursor.getColumnIndex(KEY_LATITUD)));
			//consulta.put(KEY_LONGITUD, cursor.getString(cursor.getColumnIndex(KEY_LONGITUD)));
			array_list.add(consulta);
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		// return users list
		return array_list;
	}
	
	//@param planilla
	
	public String storeConsulta(String userID, String form, String consultaID, String latitud, String longitud, String fecha) {
		//String query = "INSERT INTO " + TABLE_CONSULT + "(" + ;
		String id;
		String date = fecha;
		if (consultaID == null) {
			SQLiteDatabase dbLeible = this.getReadableDatabase();
			String query = "SELECT * FROM " + TABLE_CONSULT;
			
			Cursor cursor = dbLeible.rawQuery(query, null);		
			Integer cantidad = cursor.getCount();
			dbLeible.close();
			
			cantidad = (cantidad * (-1)) - 1;
			id = cantidad.toString();
			cursor.close();
		} else {
			id = consultaID;
		}
		if (!(date != null)) {
			date = new Date().toString();
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, id);
		values.put(KEY_USER_ID, userID);
		values.put(KEY_JSON, form); // Form
		values.put(KEY_DATE, date);
		// Inserting Row
		long planillaId = db.insert(TABLE_CONSULT, null, values);
		// Closing database connection
		db.close();
		// Por ahora se guarda sin imagenes
		if (planillaId > 0) {
			saveLocalizedImages(id, null, null, null, latitud, longitud);
		}
		return String.valueOf(planillaId);
	}
	
	
	//Gato
	public HashMap<Integer, String> getImagesFromConsultaId(String consultaId) {
		String query = "SELECT * FROM " + TABLE_CONSULT + " WHERE " + KEY_ID + "= ?"; 
		SQLiteDatabase db = this.getReadableDatabase();
		HashMap<Integer,String> imagenes = new HashMap<Integer, String>();
		Cursor cursor = db.rawQuery(query, new String[] { consultaId });
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			//Que son, string? las rutas
			//Y hay que ver si hay nulas y eso
			imagenes.put(0, cursor.getString(cursor.getColumnIndex(KEY_IMAGE1)));
			imagenes.put(1, cursor.getString(cursor.getColumnIndex(KEY_IMAGE2)));
			imagenes.put(2, cursor.getString(cursor.getColumnIndex(KEY_IMAGE3)));
		} else {
			imagenes = null;
		}
		cursor.close();
		db.close();
		
		return imagenes;
	}
	
	public boolean updateConsulta(String storedID, String newID) {
		try {
			String query = "UPDATE " + TABLE_CONSULT + " SET " + KEY_ID + "=" + newID + " WHERE " + KEY_ID + "=" + storedID;
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(query);
			db.close();
			return true;
		} catch (Exception e){
			return false;
		}
		
	}
	
	public boolean updateConsultaJson(String json, String id) {
		try {
			json = "'" + json + "'";
			String query = "UPDATE " + TABLE_CONSULT + " SET " + KEY_JSON + "=" + json + " WHERE " + KEY_ID + "=" + id;
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(query);
			db.close();
			return true;
			
		} catch (Exception e){
			return false;
		}
		
	}

	public boolean hasImages(String consultaID) {
		String query = "SELECT * FROM " + TABLE_CONSULT + " WHERE " + KEY_ID + "=? and " + KEY_IMAGE1 + " <> null" ;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, new String[] {consultaID});
		if (cursor.getCount() > 0) {
			return true;
		}
		cursor.close();
		db.close();
		return false;
	}
	
	public LinkedHashMap<Integer, String> getImagesFromConsultas(String userID) {
		String query = "SELECT " + KEY_IMAGE1 + " FROM " + TABLE_CONSULT + " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_DATE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query, new String[] { userID });
		cursor.moveToFirst();
		LinkedHashMap<Integer, String> resultado = new LinkedHashMap<Integer, String>();
		Integer i = 0;
		while (cursor.isAfterLast() == false) {
			String ruta = cursor.getString(cursor.getColumnIndex(KEY_IMAGE1));
			resultado.put(i, ruta);
			i += 1;
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return resultado;
	}

	public void actualizarConsultas(String userId, Context context) {
		String query = "SELECT * FROM " + TABLE_CONSULT + " WHERE " + KEY_USER_ID + "=? AND " + KEY_ID + "< 0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query, new String[] { userId });
		cursor.moveToFirst();
		int cantidad = cursor.getCount();
		while (cursor.isAfterLast() == false) {
			HashMap<String, String> consulta = new HashMap<String, String>();
			consulta.put("consultaID", cursor.getString(cursor.getColumnIndex(KEY_ID)));
			consulta.put("userID", cursor.getString(cursor.getColumnIndex(KEY_USER_ID)));
			consulta.put(KEY_JSON, cursor.getString(cursor.getColumnIndex(KEY_JSON)));
			consulta.put(KEY_LATITUD, cursor.getString(cursor.getColumnIndex(KEY_LATITUD)));
			consulta.put(KEY_LONGITUD, cursor.getString(cursor.getColumnIndex(KEY_LONGITUD)));
			consulta.put(KEY_IMAGE1, cursor.getString(cursor.getColumnIndex(KEY_IMAGE1)));
			consulta.put(KEY_DATE, cursor.getString(cursor.getColumnIndex(KEY_DATE)));
			new ConsultaRunnable(this, context, consulta).execute();
			cursor.moveToNext();
		}
	}
	
	public void deletePlanilla(String idPlanilla) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CONSULT, KEY_ID + " = ?", new String[] { idPlanilla });
		db.close();
	}
}