package teeza.application.helpme.adapter;

import java.io.File;
import java.util.ArrayList;

import teeza.application.helpme.R;
import teeza.application.helpme.model.QueueItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class QueueAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater mInflater;
	public ArrayList<QueueItem> queueItems = new ArrayList<QueueItem>();

	public QueueAdapter(Activity activity) {
		this.activity = activity;
		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void initialize(String ids) {
		String[] arrIds = ids.split(",");
		for (String item : arrIds) {
			QueueItem queueItem = new QueueItem();
			queueItem.setMedia_id(Long.parseLong(item));

			String[] columns = { MediaStore.Images.Media.DATA };
			String orderBy = MediaStore.Images.Media._ID;
			Cursor imagecursor = null;
			imagecursor = activity.getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					columns,
					MediaStore.Images.Media._ID + " = "
							+ queueItem.getMedia_id() + "", null, orderBy);
			for (imagecursor.moveToFirst(); !imagecursor.isAfterLast(); imagecursor
					.moveToNext()) {
				int dataColumnIndex = imagecursor
						.getColumnIndex(MediaStore.Images.Media.DATA);
				queueItem.setPath(imagecursor.getString(dataColumnIndex));
			}
			imagecursor.close();

			queueItems.add(queueItem);
		}
		notifyDataSetChanged();
	}

	public void addcapture(File f) {
		QueueItem queueItem = new QueueItem();
		Bitmap original = BitmapFactory.decodeFile(f.getAbsolutePath());
		queueItem.setBitmap(Bitmap
				.createScaledBitmap(original, 120, 120, false));
		queueItem.setPath(f.getPath());
		queueItems.add(queueItem);
	}

	public int getCount() {
		return queueItems.size();
	}

	public int getUploadCount() {
		int cnt = 0;
		for (QueueItem item : queueItems) {
			if (item.getUploaded() != 1)
				cnt++;
		}
		return cnt;
	}

	public QueueItem getItem(int position) {
		return queueItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.uploadqueueitem, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.imageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				queueItems.remove(position);
				notifyDataSetChanged();
			}
		});

		holder.description.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LayoutInflater li = LayoutInflater.from(activity);
				View promptsView = li.inflate(R.layout.prompt_description, null);

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
				alertDialogBuilder.setView(promptsView);

				final EditText userInput = (EditText) promptsView
						.findViewById(R.id.editTextDialogUserInput);

				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										String txt = userInput.getText().toString();
										if(!txt.equals("")) {
											holder.description.setText(userInput.getText());
										}
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();

			}
		});

		final QueueItem item = getItem(position);
		if (item.getBitmap() != null) {
			holder.imageview.setImageBitmap(item.getBitmap());
		} else {
			final String[] columns = { MediaStore.Images.Media.DATA };
			final String orderBy = MediaStore.Images.Media._ID;
			Cursor imagecursor = activity.getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					columns,
					MediaStore.Images.Media._ID + " = " + item.getMedia_id()
							+ "", null, orderBy);
			for (imagecursor.moveToFirst(); !imagecursor.isAfterLast(); imagecursor
					.moveToNext()) {
				holder.imageview.setImageBitmap(MediaStore.Images.Thumbnails
						.getThumbnail(activity.getContentResolver(),
								item.getMedia_id(),
								MediaStore.Images.Thumbnails.MICRO_KIND, null));
			}
			imagecursor.close();
		}
		return convertView;

	}

	class ViewHolder {
		public ImageView imageview;
		public ImageView imageButton;
		public TextView description;

		public ViewHolder(View v) {
			imageview = (ImageView) v.findViewById(R.id.QueueItemThumbnail);
			imageButton = (ImageView) v.findViewById(R.id.imageButton1);
			description = (TextView) v.findViewById(R.id.description);
		}
	}
}