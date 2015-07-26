package teeza.application.helpme.adapter;

import java.io.File;
import java.util.ArrayList;

import teeza.application.helpme.R;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.ImageItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private final static int VIEW_IMAGE = 3;
	private ApplicationStatus appStatus;
	private Activity activity;
	private LayoutInflater mInflater;
	private ArrayList<ImageItem> images;
	private long lastId;

	public ImageAdapter(Activity activity, ArrayList<ImageItem> images) {
		appStatus = ApplicationStatus.getInstance();
		this.activity = activity;
		this.images = images;
		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void initialize() {
		images.clear();
		final String[] columns = { MediaStore.Images.Media._ID };
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		Cursor imagecursor = activity.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
				null, orderBy + " DESC LIMIT 21");
		if (imagecursor != null) {
			int image_column_index = imagecursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			for (imagecursor.moveToFirst(); !imagecursor.isAfterLast(); imagecursor
					.moveToNext()) {
				int id = imagecursor.getInt(image_column_index);
				ImageItem imageItem = new ImageItem();
				imageItem.setId(id);
				lastId = id;
				imageItem.setImg(MediaStore.Images.Thumbnails.getThumbnail(
						activity.getApplicationContext().getContentResolver(),
						id, MediaStore.Images.Thumbnails.MICRO_KIND, null));
				images.add(imageItem);
			}
			imagecursor.close();
		}
		notifyDataSetChanged();
	}

	public void checkForNewImages() {
		images.clear();
		final String[] columns = { MediaStore.Images.Thumbnails._ID };
		final String orderBy = MediaStore.Images.Media.DATE_ADDED;
		Cursor imagecursor = activity.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
				MediaStore.Images.Media._ID + " > " + lastId, null,
				orderBy + " DESC LIMIT 21");
		int image_column_index = imagecursor
				.getColumnIndex(MediaStore.Images.Media._ID);
		int count = imagecursor.getCount();
		for (int i = 0; i < count; i++) {
			imagecursor.moveToPosition(i);
			int id = imagecursor.getInt(image_column_index);
			ImageItem imageItem = new ImageItem();
			imageItem.setId(id);
			lastId = id;
			imageItem.setImg(MediaStore.Images.Thumbnails.getThumbnail(activity
					.getApplicationContext().getContentResolver(), id,
					MediaStore.Images.Thumbnails.MICRO_KIND, null));
			images.add(imageItem);
		}
		imagecursor.close();
		notifyDataSetChanged();
	}

	public int getCount() {
		return images.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.galleryitem, null);
			holder.imageview = (ImageView) convertView
					.findViewById(R.id.thumbImage);
			holder.checkbox = (CheckBox) convertView
					.findViewById(R.id.itemCheckBox);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ImageItem item = images.get(position);
		holder.checkbox.setId(position);
		holder.imageview.setId(position);
		holder.checkbox.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				int id = cb.getId();
				if (images.get(id).isSelection()) {
					cb.setChecked(false);
					images.get(id).setSelection(false);
				} else {
					cb.setChecked(true);
					images.get(id).setSelection(true);
				}
			}
		});

		holder.imageview.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				int id = v.getId();
				ImageItem item = images.get(id);
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				final String[] columns = { MediaStore.Images.Media.DATA };
				Cursor imagecursor = activity.getContentResolver().query(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
						MediaStore.Images.Media._ID + " = " + item.getId(),
						null, MediaStore.Images.Media._ID);
				if (imagecursor != null && imagecursor.getCount() > 0) {
					imagecursor.moveToPosition(0);
					String path = imagecursor.getString(imagecursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
					File file = new File(path);
					imagecursor.close();

					intent.setDataAndType(Uri.fromFile(file), "image/*");

					if (!appStatus.isOpenImage()) {
						activity.startActivityForResult(intent, VIEW_IMAGE);
						appStatus.setOpenImage(true);
					}

				}
			}
		});
		holder.imageview.setImageBitmap(item.getImg());
		holder.checkbox.setChecked(item.isSelection());
		return convertView;
	}
}

class ViewHolder {
	ImageView imageview;
	CheckBox checkbox;
}
