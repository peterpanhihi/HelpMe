package teeza.application.helpme.model;

public class ArrayMarker {

	private String name;
	private String phone;
	private String pic;
	private String address;
	private double lati, longi;

	public ArrayMarker() {
	}

	public ArrayMarker(String name, String phone, String pic, String address,
			double lati, double longi) {

		this.name = name;
		this.phone = phone;
		this.pic = pic;
		this.address = address;
		this.lati = lati;
		this.longi = longi;

	}

	public String getname() {
		return name;
	}

	public void setname(String name) {
		this.name = name;
	}

	public String getphone() {
		return phone;
	}

	public void setphone(String phone) {
		this.phone = phone;
	}

	public String getpic() {
		return pic;
	}

	public void setpic(String pic) {
		this.pic = pic;
	}

	public String getaddress() {
		return address;
	}

	public void setaddress(String address) {
		this.address = address;
	}

	public double getlati() {
		return lati;
	}

	public void setlati(Double lati) {
		this.lati = lati;
	}

	public double getlongi() {
		return longi;
	}

	public void setlongi(Double longi) {
		this.longi = longi;
	}
}
