package teeza.application.helpme.model;

public class User {

	public static final String TABLE = "User";

	public class Column {
		public static final String CUSID = "cusid";
		public static final String USERNAME = "username";
		public static final String PASSWORD = "password";
		public static final String STATUSLOGIN = "stat";
		public static final String STATUSSEND = "statsend";
		public static final String NAME = "name";
		public static final String PHONE = "phone";
		public static final String ADDRESS = "address";
		public static final String PIN = "pin";
	}

	private String cusid;
	private String username;
	private String password;
	private String stat;
	private String statsend;
	private String name;
	private String phone;
	private String address;
	private String pin;

	// Constructor
	public User(String cusid, String username, String password, String stat,
			String statsend, String name, String phone, String address,
			String pin) {
		this.cusid = cusid;
		this.username = username;
		this.password = password;
		this.stat = stat;
		this.statsend = statsend;
		this.name = name;
		this.phone = phone;
		this.address = address;
		this.pin = pin;
	}

	public User() {

	}

	public String getcusId() {
		return cusid;
	}

	public void setcusId(String cusid) {
		this.cusid = cusid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStatus() {
		return stat;
	}

	public void setStatus(String stat) {
		this.stat = stat;
	}

	public String getStatussend() {
		return statsend;
	}

	public void setStatussend(String statsend) {
		this.statsend = statsend;
	}

	public String getphone() {
		return phone;
	}

	public void setname(String name) {
		this.name = name;
	}

	public String getname() {
		return name;
	}

	public void setphone(String phone) {
		this.phone = phone;
	}

	public String getaddress() {
		return address;
	}

	public void setaddress(String address) {
		this.address = address;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
}
