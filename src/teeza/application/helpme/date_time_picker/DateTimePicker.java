package teeza.application.helpme.date_time_picker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.Date;

import teeza.application.helpme.R;

public class DateTimePicker extends DialogFragment {
	public static final String TAG_FRAG_DATE_TIME = "fragDateTime";
	private static final String KEY_DIALOG_TITLE = "dialogTitle";
	private static final String KEY_INIT_DATE = "initDate";
	private static final String TAG_DATE = "date";
	private static final String TAG_TIME = "time";
	private Context mContext;
	private ButtonClickListener mButtonClickListener;
	private OnDateTimeSetListener mOnDateTimeSetListener;
	private Bundle mArgument;
	private DatePicker mDatePicker;
	private TimePicker mTimePicker;
	private NumberPicker dayPicker, monthPicker, yearPicker;
	private NumberPicker hourPicker, minPicker, amPmPicker;

	// DialogFragment constructor must be empty
	public DateTimePicker() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		mButtonClickListener = new ButtonClickListener();
	}

	/**
	 * 
	 * @param dialogTitle
	 *            Title of the DateTimePicker DialogFragment
	 * @param initDate
	 *            Initial Date and Time set to the Date and Time Picker
	 * @return Instance of the DateTimePicker DialogFragment
	 */
	public static DateTimePicker newInstance(CharSequence dialogTitle,
			Date initDate) {
		// Create a new instance of DateTimePicker
		DateTimePicker mDateTimePicker = new DateTimePicker();
		// Setup the constructor parameters as arguments
		Bundle mBundle = new Bundle();
		mBundle.putCharSequence(KEY_DIALOG_TITLE, dialogTitle);
		mBundle.putSerializable(KEY_INIT_DATE, initDate);
		mDateTimePicker.setArguments(mBundle);
		// Return instance with arguments
		return mDateTimePicker;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Retrieve Argument passed to the constructor
		mArgument = getArguments();
		// Use an AlertDialog Builder to initially create the Dialog
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
		// Setup the Dialog
//		mBuilder.setTitle(Html.fromHtml("<font color='#e30053'>"+ mArgument.getCharSequence(KEY_DIALOG_TITLE)+"</font>"));
		mBuilder.setNegativeButton(android.R.string.no, mButtonClickListener);
		mBuilder.setPositiveButton(android.R.string.yes, mButtonClickListener);
		// Create the Alert Dialog
		AlertDialog mDialog = mBuilder.create();
		// Set the View to the Dialog
		mDialog.setView(createDateTimeView(mDialog.getLayoutInflater()));
		// Return the Dialog created
		return mDialog;
	}

	/**
	 * Inflates the XML Layout and setups the tabs
	 * 
	 * @param layoutInflater
	 *            Layout inflater from the Dialog
	 * @return Returns a view that will be set to the Dialog
	 */
	private View createDateTimeView(LayoutInflater layoutInflater) {
		// Inflate the XML Layout using the inflater from the created Dialog
		View mView = layoutInflater.inflate(R.layout.date_time_picker, null);
		// Extract the TabHost
		TabHost mTabHost = (TabHost) mView.findViewById(R.id.tab_host);
		mTabHost.setup();
		
		// Create Date Tab and add to TabHost
		TabHost.TabSpec mDateTab = mTabHost.newTabSpec(TAG_DATE);
		mDateTab.setIndicator("DATE");
		mDateTab.setContent(R.id.date_content);
		mTabHost.addTab(mDateTab);
		// Create Time Tab and add to TabHost
		TabHost.TabSpec mTimeTab = mTabHost.newTabSpec(TAG_TIME);
		mTimeTab.setIndicator("TIME");
		mTimeTab.setContent(R.id.time_content);
		mTabHost.addTab(mTimeTab);
		
		TabWidget widget = mTabHost.getTabWidget();
		for(int i = 0; i < widget.getChildCount(); i++) {
		    View v = widget.getChildAt(i);

		    // Look for the title view to ensure this is an indicator and not a divider.
		    TextView tv = (TextView)v.findViewById(android.R.id.title);
		    if(tv == null) {
		        continue;
		    }
		    v.setBackgroundResource(R.drawable.tab_selector);
		}
		
		// Retrieve Date from Arguments sent to the Dialog
		DateTime mDateTime = new DateTime(
				(Date) mArgument.getSerializable(KEY_INIT_DATE));
		// Initialize Date and Time Pickers
		mDatePicker = (DatePicker) mView.findViewById(R.id.date_picker);
		mTimePicker = (TimePicker) mView.findViewById(R.id.time_picker);
		mDatePicker.init(mDateTime.getYear(), mDateTime.getMonthOfYear(),
				mDateTime.getDayOfMonth(), null);
		mTimePicker.setCurrentHour(mDateTime.getHourOfDay());
		mTimePicker.setCurrentMinute(mDateTime.getMinuteOfHour());
		prepareDateTimePicker();
		// Return created view
		return mView;
	}
	
	private void prepareDateTimePicker() {
        try {
            Field datePickerFields[] = mDatePicker.getClass().getDeclaredFields();
            for (Field field : datePickerFields) {
                if ("mSpinners".equals(field.getName())) {
                    field.setAccessible(true);
                    Object spinnersObj = new Object();
                    spinnersObj = field.get(mDatePicker);
                    LinearLayout mSpinners = (LinearLayout) spinnersObj;
                    monthPicker = (NumberPicker) mSpinners.getChildAt(0);
                    dayPicker = (NumberPicker) mSpinners.getChildAt(1);
                    yearPicker = (NumberPicker) mSpinners.getChildAt(2);
                    setDividerColor(monthPicker);
                    setDividerColor(dayPicker);
                    setDividerColor(yearPicker);
                    break;
                }
            }
            
            Field timePickerFields[] = mTimePicker.getClass().getDeclaredFields();
            for (Field field : timePickerFields) {
                if ("mSpinners".equals(field.getName())) {
                    field.setAccessible(true);
                    Object spinnersObj = new Object();
                    spinnersObj = field.get(mTimePicker);
                    LinearLayout mSpinners = (LinearLayout) spinnersObj;
                    hourPicker = (NumberPicker) mSpinners.getChildAt(0);
                    minPicker = (NumberPicker) mSpinners.getChildAt(1);
                    amPmPicker = (NumberPicker) mSpinners.getChildAt(2);
                    setDividerColor(hourPicker);
                    setDividerColor(minPicker);
                    setDividerColor(amPmPicker);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	 private void setDividerColor(NumberPicker picker) {
	        Field[] numberPickerFields = NumberPicker.class.getDeclaredFields();
	        for (Field field : numberPickerFields) {
	            if (field.getName().equals("mSelectionDivider")) {
	                field.setAccessible(true);
					try {
	                    field.set(picker, getResources().getDrawable(R.drawable.custom_divider));
	                } catch (IllegalArgumentException e) {
	                    Log.v(TAG_DATE, "Illegal Argument Exception");
	                    e.printStackTrace();
	                } catch (Resources.NotFoundException e) {
	                    Log.v(TAG_DATE, "Resources NotFound");
	                    e.printStackTrace();
	                } catch (IllegalAccessException e) {
	                    Log.v(TAG_DATE, "Illegal Access Exception");
	                    e.printStackTrace();
	                }
	                break;
	            }
	        }
	    }


	/**
	 * Sets the OnDateTimeSetListener interface
	 * 
	 * @param onDateTimeSetListener
	 *            Interface that is used to send the Date and Time to the
	 *            calling object
	 */
	public void setOnDateTimeSetListener(
			OnDateTimeSetListener onDateTimeSetListener) {
		mOnDateTimeSetListener = onDateTimeSetListener;
	}

	private class ButtonClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialogInterface, int result) {
			// Determine if the user selected Ok
			if (DialogInterface.BUTTON_POSITIVE == result) {
				DateTime mDateTime = new DateTime(mDatePicker.getYear(),
						mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),
						mTimePicker.getCurrentHour(),
						mTimePicker.getCurrentMinute());
				mOnDateTimeSetListener.DateTimeSet(mDateTime.getDate());
			}
		}
	}

	/**
	 * Interface for sending the Date and Time to the calling object
	 */
	public interface OnDateTimeSetListener {
		public void DateTimeSet(Date date);
	}
}
