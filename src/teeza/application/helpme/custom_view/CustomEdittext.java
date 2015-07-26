package teeza.application.helpme.custom_view;

import teeza.application.helpme.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomEdittext extends EditText {
	
	 public CustomEdittext(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        init(context, attrs);
	    }

	    public void init(Context context, AttributeSet attrs) {
	        TypedArray a = context.obtainStyledAttributes(attrs,
	                R.styleable.customfont);
	        String fontFamily = null;
	        final int n = a.getIndexCount();
	        for (int i = 0; i < n; ++i) {
	            int attr = a.getIndex(i);
	            if (attr == R.styleable.customfont_android_fontFamily) {
	                fontFamily = a.getString(attr);
	            }
	            a.recycle();
	        }
	        if (!isInEditMode()) {
	            try {
	                Typeface tf = Typeface.createFromAsset(
	                        getContext().getAssets(), fontFamily);
	                setTypeface(tf);
	            } catch (Exception e) {
	            }
	        }
	    }
}
