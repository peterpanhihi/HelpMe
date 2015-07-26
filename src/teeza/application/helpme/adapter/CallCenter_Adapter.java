package teeza.application.helpme.adapter;

import java.util.List;

import teeza.application.helpme.R;
import teeza.application.helpme.model.PhoneNumber;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallCenter_Adapter extends BaseAdapter {
	private Activity context;
	private List<PhoneNumber> numbers;
	
	private LayoutInflater mLayoutInflater = null;

	public CallCenter_Adapter(Activity context, List<PhoneNumber> numbers) {
		this.context = context;
		this.numbers = numbers;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return numbers.size();
	}

	@Override
	public Object getItem(int pos) {
		return numbers.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		CallListViewHolder viewHolder;
		if(convertView == null) {
			LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.calllistholder, null);
			v.setBackgroundColor(position % 2 == 0 ? Color.WHITE : Color.parseColor("#EDEDED"));
			viewHolder = new CallListViewHolder(v);
			v.setTag(viewHolder);
		} else {
			viewHolder = (CallListViewHolder) v.getTag();
		}
		
		viewHolder.title.setText(numbers.get(position).getTitle());
		viewHolder.phone.setText(numbers.get(position).getNumber());
		return v;
	}
}

class CallListViewHolder {
	public TextView title;
	public TextView phone;
	
	public CallListViewHolder(View base) {
		title = (TextView) base.findViewById(R.id.title);
		phone = (TextView) base.findViewById(R.id.phone);
	}
}
