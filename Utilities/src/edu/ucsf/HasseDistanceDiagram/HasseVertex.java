package edu.ucsf.HasseDistanceDiagram;

public class HasseVertex {

	/**First reference vertex**/
	private HasseVertex hvt1;
	
	/**ID**/
	private String sID;
	
	/**Length**/
	private double dLength;

	/**Angle**/
	private double dTheta;
	
	/**Current x-coordinate**/
	private double dX =  Double.NaN;
	
	/**Current y-coordinate**/
	private double dY = Double.NaN;
	
	public HasseVertex(HasseVertex hvt1, double dLength, double dTheta, String sID){
		
		this.sID = sID;
		this.hvt1 = hvt1;
		this.dLength = dLength;
		this.dTheta = Math.PI*dTheta;
		dX = hvt1.x() + Math.cos(this.dTheta)*dLength;
		dY = hvt1.y() + Math.sin(this.dTheta)*dLength;
	}
	
	public HasseVertex(double dX, double dY, String sID){
		this.sID = sID;
		this.dX = dX;
		this.dY = dY;
	}
		
	public String toString(){
		return sID + "," + this.x() + "," + this.y();
	}
	
	public String id(){
		return sID;
	}
	
	public double x(){
		return dX;
	}
	
	public double y(){
		return dY;
	}
	
	public static double distance(HasseVertex hvt1, HasseVertex hvt2){
		return Math.sqrt((hvt1.x()-hvt2.x())*(hvt1.x()-hvt2.x()) + (hvt1.y()-hvt2.y())*(hvt1.y()-hvt2.y()));
	}
}