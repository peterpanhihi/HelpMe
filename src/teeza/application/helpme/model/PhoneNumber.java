package teeza.application.helpme.model;
/**
 * Phone number for call center.
 * @author PAN
 *
 */
public class PhoneNumber {
	private String title;
	private String number;

	public PhoneNumber(String title, String number) {
		this.title = title;
		this.number = number;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

}
