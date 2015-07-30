package teeza.application.helpme.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class QueueItem {
	private int id;
	private String path;
	private String description = "รายละเอียด";
	private long media_id;
	private int uploaded;
	private Bitmap bitmap;
	private Drawable draw;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getMedia_id() {
		return media_id;
	}

	public void setMedia_id(long media_id) {
		this.media_id = media_id;
	}

	public int getUploaded() {
		return uploaded;
	}

	public void setUploaded(int uploaded) {
		this.uploaded = uploaded;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Drawable getDraw() {
		return draw;
	}

	public void setDraw(Drawable draw) {
		this.draw = draw;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
