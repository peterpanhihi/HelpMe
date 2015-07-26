package teeza.application.helpme.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Camera {
	final String PREF_NAME = "IMAGE_ID";
	private int width[] = new int[] { 500, 510, 1500 }, height[] = new int[] {
			500, 900, 1200 };
	private int size_watermark[] = new int[] { 60, 90, 190 },
			size_number[] = new int[] { 20, 30, 50 };
	private int width_watermark[] = new int[] { 130, 110, 410 },
			height_watermark[] = new int[] { 250, 460, 660 };
	private int width_number[] = new int[] { 370, 370, 1170 },
			height_number[] = new int[] { 470, 870, 1170 };
	private File file;
	private SharedPreferences sp;
	private ExifInterface exif;
	private Bitmap bitmap;
	private Uri uri;
	private Context context;
	private double lat, lng;

	public Camera(Context context) {
		this.context = context;
	}

	public void setUp(String id) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String fileName = "_" + id + "_" + sdf.format(new Date()) + ".jpg";
		File myDirectory = new File(Environment.getExternalStorageDirectory()
				+ "/DCIM/HelpMe");
		myDirectory.mkdirs();
		file = new File(myDirectory, fileName);
		uri = Uri.fromFile(file);
	}

	public void result(Intent data, double lat, double lng) {
		this.lat = lat;
		this.lng = lng;

		if (data != null) {
			Bundle extras = data.getExtras();
			if (extras.containsKey("data")) {
				bitmap = (Bitmap) extras.get("data");
			} else {
				bitmap = getBitmapFromUri();
			}
		} else {
			bitmap = getBitmapFromUri();
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);
			String[] s = uri.toString().split("///");
			exif = new ExifInterface("/" + s[1]);

			sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
			int image_id = sp.getInt("image_id", 0);
			int ww, hh;

			ww = width[image_id];
			hh = height[image_id];

			if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
					.equalsIgnoreCase("6")) {
				bitmap = rotate(bitmap, 90, ww, hh);
			} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
					.equalsIgnoreCase("8")) {
				bitmap = rotate(bitmap, 270, ww, hh);
			} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
					.equalsIgnoreCase("3")) {
				bitmap = rotate(bitmap, 180, ww, hh);
			} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
					.equalsIgnoreCase("0")) {
				bitmap = rotate(bitmap, 90, ww, hh);
			}

			Canvas newCanvas = new Canvas(bitmap);

			newCanvas.drawBitmap(bitmap, 0, 0, null);

			String captionString = "Ref " + sp.getString("active", "");

			if (captionString != null) {

				Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
				paintText.setColor(Color.RED);
				paintText.setTextSize(size_number[image_id]);
				paintText.setStyle(Style.FILL_AND_STROKE);
				paintText.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

				// draw text to the Canvas center
				Resources resources = context.getResources();
				float scale = resources.getDisplayMetrics().density;
				Rect bounds = new Rect();
				paintText.getTextBounds(captionString, 0,
						captionString.length(), bounds);

				int x = width_number[image_id];
				int y = height_number[image_id];
				newCanvas.drawText(captionString, x, y, paintText);

				// draw some rotated text
				// get text width and height
				// set desired drawing location
				Paint paint = new Paint();
				paint.setStyle(Paint.Style.FILL);
				Canvas canvas = new Canvas(bitmap);
				x = width_watermark[image_id];
				y = height_watermark[image_id];
				paint.setColor(Color.GRAY);
				paint.setTextSize(size_watermark[image_id]);
				String str2rotate = "Help Me!";

				// draw bounding rect before rotating text
				Rect rect = new Rect();
				paint.getTextBounds(str2rotate, 0, str2rotate.length(), rect);
				canvas.translate(x, y);
				paint.setStyle(Paint.Style.FILL);

				canvas.translate(-x, -y);
				// rotate the canvas on center of the text to draw
				canvas.rotate(-45, x + rect.exactCenterX(),
						y + rect.exactCenterY());
				// draw the rotated text
				canvas.drawText(str2rotate, x, y, paint);
			}
			boolean bo = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

			fos.close();
			Log.d("Info", bo + "");
			
			setLocation();

		} catch (FileNotFoundException e) {
			Log.d("Info", "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d("TAG", "Error accessing file: " + e.getMessage());
		}
	}

	public Bitmap getBitmapFromUri() {
		context.getContentResolver().notifyChange(uri, null);
		ContentResolver cr = context.getContentResolver();
		Bitmap bitmap;
		try {
			bitmap = android.provider.MediaStore.Images.Media
					.getBitmap(cr, uri);

			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setLocation() {
		String dms = Location.convert(Math.abs(lat), Location.FORMAT_SECONDS);
		String[] splits = dms.split(":");
		String[] secnds = (splits[2]).split("\\.");
		String seconds;
		if (secnds.length == 0) {
			seconds = splits[2];
		} else {
			seconds = secnds[0];
		}
		String latitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds
				+ "/1";
		exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr);
		exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat > 0 ? "N"
				: "S");

		dms = Location.convert(lng, Location.FORMAT_SECONDS);
		splits = dms.split(":");
		secnds = (splits[2]).split("\\.");

		if (secnds.length == 0) {
			seconds = splits[2];
		} else {
			seconds = secnds[0];
		}
		String longitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds
				+ "/1";
		exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr);
		exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
				Math.abs(lng) > 0 ? "E" : "W");

		try {
			exif.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap rotate(Bitmap bitmap, int degree, int newHeight,
			int newWidth) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		float scaleWidth = ((float) newWidth) / w;
		float scaleHeight = ((float) newHeight) / h;

		// createa matrix for the manipulation
		Matrix mtx = new Matrix();
		// resize the bit map
		mtx.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		mtx.postRotate(degree);

		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	public Uri getUri() {
		return uri;
	}
	
	public File getFile() {
		return file;
	}
}
