package teeza.application.helpme.model;


public class DataShow  
{
	private String title;
    private String detail;
 
    public DataShow(String title,String detail) {
        this.title = title;
        this.detail = detail;
    }
 
    //���觤�ҡ�Ѻ� setText �ͧ TextView
    public String getTitle(){
        return this.title;
    }
 
    //���觤�ҡ�Ѻ� setText �ͧ TextView
    public String getDeteil(){
        return this.detail;
    }

}
