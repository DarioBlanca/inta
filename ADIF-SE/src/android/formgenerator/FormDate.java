package android.formgenerator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class FormDate extends FormWidget {
	protected TextView _label;
	protected EditText _input;
	private Context context;

	public FormDate(final Context context, String property) {
		super(context, property);
		this.context = context;
		_label = new TextView(context);
		_label.setText(getDisplayText());
		_label.setLayoutParams(FormActivity.defaultLayoutParams);

		_input = new EditText(context);
		_input.setLayoutParams(FormActivity.defaultLayoutParams);
		_input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		_input.setKeyListener(null);
		_input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callPicker();
			}
		});
		_layout.addView(_label);
		_layout.addView(_input);
	}

	private void callPicker() {
		// TODO Auto-generated method stub
		// To show current date in the datepicker
		Calendar mcurrentDate = Calendar.getInstance();
		int mYear = mcurrentDate.get(Calendar.YEAR);
		int mMonth = mcurrentDate.get(Calendar.MONTH);
		int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog mDatePicker = new DatePickerDialog(context,
				new OnDateSetListener() {
					public void onDateSet(DatePicker datepicker,
							int selectedyear, int selectedmonth, int selectedday) {
						// 2014-10-07 09:39:35
						SimpleDateFormat sDate = new SimpleDateFormat(
								"HH:mm:ss");
						Date now = new Date();
						String strDate = sDate.format(now);
						_input.setText(new StringBuilder()
								// Month is 0 based, so you have to add
								// 1
								.append(selectedyear).append("-")
								.append(selectedmonth + 1).append("-")
								.append(selectedday).append(" ")
								.append(strDate));

					}
				}, mYear, mMonth, mDay);
		mDatePicker.setTitle("Seleccione una fecha");
		mDatePicker.show();
	}

	@Override
	public String getValue() {
		return _input.getText().toString();
	}

	@Override
	public void setValue(String value) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String date = sdfDate.format(now);
		_input.setText(date);
	}

	@Override
	public void setHint(String value) {
		_input.setHint(value);
	}
}
