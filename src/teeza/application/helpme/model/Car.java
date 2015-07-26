package teeza.application.helpme.model;

public class Car {
	public static final String TABLE = "CAR";

    public class Column {
        public static final String CUSID = "cusid";
        public static final String CARID = "carid";
        public static final String CARPOLICY = "carpolicy";
        public static final String CARTYPE = "cartype";
        public static final String CARBAND = "carband";
        public static final String CARSERIES = "carseries";
        public static final String CARCITY = "carcity";
        public static final String CARSTATUS = "carstatus";
    }

    private String cusid,carid,carpolicy,cartype,carband,carseries,carcity,carstatus;


    // Constructor
    public Car(String cusid,String carid,String carpolicy,String cartype,String carband,String carseries,String carcity,String carstatus) {
        this.cusid = cusid;
    	this.carid = carid;
    	this.carpolicy = carpolicy;
    	this.cartype = cartype;
    	this.carband = carband;
    	this.carseries = carseries;
    	this.carcity = carcity;
    	this.carstatus =carstatus;
    }

    public Car() {

    }

    public String getcusId() {
        return cusid;
    }

    public void setcusId(String cusid) {
        this.cusid = cusid;
    }
    
    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }
    
    public String getCarpolicy() {
		return carpolicy;
	}
    public void setCarpolicy(String carpolicy) {
		this.carpolicy = carpolicy;
	}
    
    public String getCartype() {
        return cartype;
    }

    public void setCartype(String cartype) {
        this.cartype = cartype;
    }
    
    public String getcarband() {
        return carband;
    }

    public void setcarband(String carband) {
        this.carband = carband;
    }
    
    public String getcarseries() {
        return carseries;
    }

    public void setcarseries(String carseries) {
        this.carseries = carseries;
    }
    
    public String getcarcity() {
        return carcity;
    }

    public void setcarcity(String carcity) {
        this.carcity = carcity;
    }
    
    public String getcarstatus() {
        return carstatus;
    }

    public void setcarstatus(String carstatus) {
        this.carstatus = carstatus;
    }
}
