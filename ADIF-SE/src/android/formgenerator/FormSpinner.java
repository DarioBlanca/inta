package android.formgenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Edited by Mauro Figari
 *
 */
public class FormSpinner extends FormWidget
{
	protected JSONObject _options;
	protected TextView _label;
	protected Spinner _spinner;
	protected Map<String, String> _propmap;
	private Boolean _multiselect = false;
	protected ArrayAdapter<String> _adapter;
	
	public FormSpinner( Context context, String property, JSONObject options, Boolean multiselect ) 
	{
		super( context, property );
		_multiselect  = multiselect;
		_options = options;
		
		_label = new TextView( context );
		_label.setText( getDisplayText() );
		_label.setLayoutParams( FormActivity.defaultLayoutParams );
		
		if (_multiselect){
			_spinner = new MultiSpinner( context );
			_spinner.setLayoutParams( FormActivity.defaultLayoutParams );
		}else{
			_spinner = new Spinner( context );
			_spinner.setLayoutParams( FormActivity.defaultLayoutParams );
			_adapter = new ArrayAdapter<String>( context, android.R.layout.simple_spinner_item );
			_adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
			_spinner.setAdapter( _adapter );
		}
		String p;
		String name;
		JSONArray propertyNames = options.names();
		
		_propmap = new HashMap<String, String>();
		_spinner.setSelection( 0 );
		LinkedList<String> items = new LinkedList<String>();
		if (!_multiselect){
			_adapter.add("");
			_propmap.put("", "");
		}
		try{
			for( int i = 0; i < options.length(); i++ ) 
			{
				name =  propertyNames.getString(i);
				p = options.getString( name );
				
				if (_multiselect){
					items.add( p );
				}else{
					_adapter.add( p );
				}
				_propmap.put( p, name );
			}
			if (_multiselect){
				((MultiSpinner)_spinner).setItems(items);
			}
		} catch( JSONException e){
			
		}
		
		_layout.addView( _label );
		_layout.addView( _spinner );
	}
	
	@Override
	public String getValue() {
		if (_multiselect){
			LinkedList<String> values =(LinkedList<String>)((MultiSpinner)_spinner).getSelectedStrings();
			String value="";
			boolean foundOne = true;
			for (String item : values) {
				if (!foundOne) {
					value=value+","+_propmap.get(item);
				}else{
					foundOne = false;
					value= _propmap.get(item);
				}
			}
			return value;
		}else{
			return _propmap.get( _adapter.getItem( _spinner.getSelectedItemPosition() ) );
		}
	}
	
	@Override
	public void setValue(String value)
	{
		try{
			String name;
			JSONArray names = _options.names();
			for( int i = 0; i < names.length(); i++ )
			{
				name = names.getString(i);
				
				if( name.equals(value) )
				{
				  
					String item = _options.getString(name);
					if (_multiselect){
						((MultiSpinner)_spinner).setSelection(item);
					}else{
						_spinner.setSelection( _adapter.getPosition(item) );
					}
				}
			}
		} catch( JSONException e ){
			Log.i("Lykaion", e.getMessage() );
		}
	}
	
	@Override 
	public void setToggleHandler( FormActivity.FormWidgetToggleHandler handler )
	{
		super.setToggleHandler(handler);
		_spinner.setOnItemSelectedListener( new SelectionHandler( this ) );
	}
	
	class SelectionHandler implements AdapterView.OnItemSelectedListener 
	{
		protected FormWidget _widget;
		
		public SelectionHandler( FormWidget widget ){
			_widget = widget;
		}
		
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if( _handler != null ){
				_handler.toggle( _widget );
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
		
	}
}
