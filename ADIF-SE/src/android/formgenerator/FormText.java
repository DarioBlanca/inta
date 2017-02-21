package android.formgenerator;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

public class FormText extends FormWidget
{
	protected TextView _label;
	
	public FormText( Context context, String property )
	{
		super( context, property );	
		_label = new TextView(context);
		_label.setText(getDisplayText());
		_label.setTextColor(Color.argb(255, 0, 145, 208));
		_label.setTextSize(20);
		_layout.addView(_label);
	}
	
	@Override
	public String getValue(){
		return (String) _label.getText();
	}
	
	/**
	 * returns the un-modified name of the property this widget represents
	 */
	public String getPropertyName(){
		return "label";
	}
	
	@Override
	public void setValue( String value ) {
	}
	
	@Override 
	public void setHint( String value ){
		_label.setHint( value );
	}
}