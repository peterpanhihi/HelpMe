package teeza.application.helpme.model;

import android.graphics.Bitmap;

public class ImageItem {
	private boolean selection;
	private int id;
	private Bitmap img;

	public boolean isSelection() {
		return selection;
	}

	public void setSelection(boolean selection) {
		this.selection = selection;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
	}

}
