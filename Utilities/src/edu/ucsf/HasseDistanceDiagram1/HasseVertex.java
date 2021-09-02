package edu.ucsf.HasseDistanceDiagram1;

public class HasseVertex {

	/**First reference vertex**/
	private HasseVertex hvt1;
	
	/**ID**/
	private String sID;
	
	/**Y-difference**/
	private double dYDifference;
	
	/**X-difference**/
	private double dXDifference;

	/**Current x-coordinate**/
	private double dX =  Double.NaN;
	
	/**Current y-coordinate**/
	private double dY = Double.NaN;
	
	public HasseVertex(HasseVertex hvt1, double dVerticalDistance, double dTheta, String sID){
		
		//d1 = actual angle
		
		double d1;
		
		d1 = Math.PI*dTheta;
		
		this.sID = sID;
		this.hvt1 = hvt1;
		if(Math.sin(d1)<0){
			dYDifference=-dVerticalDistance;
		}else{
			dYDifference=dVerticalDistance;
		}
		if(Math.cos(d1)<0){
			dXDifference=-dVerticalDistance/(Math.abs(Math.tan(d1)));
		}else{
			dXDifference=dVerticalDistance/(Math.abs(Math.tan(d1)));
		}
	}
	
	public HasseVertex(double dX, double dY, String sID){
		this.sID = sID;
		this.dX = dX;
		this.dY = dY;
	}
		
	public String id(){
		return sID;
	}
	
	public double x(){
		if(Double.isNaN(dX)){
			return hvt1.x() + dXDifference;
		}else{
			return dX;
		}
	}
	
	public double y(){
		if(Double.isNaN(dY)){
			return hvt1.y() + dYDifference;
		}else{
			return dY;
		}
	}
	
	public static double distance(HasseVertex hvt1, HasseVertex hvt2){
		return Math.sqrt((hvt1.x()-hvt2.x())*(hvt1.x()-hvt2.x()) + (hvt1.y()-hvt2.y())*(hvt1.y()-hvt2.y()));
	}
}