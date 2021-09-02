package edu.ucsf.HasseDistanceDiagram0;

public class HasseVertex {

	/**First reference vertex**/
	private HasseVertex hvt1;
	
	/**Second reference vertex**/
	private HasseVertex hvt2;
	
	/**Radius from first vertex**/
	private double dRadius1;
	
	/**Radius from second vertex**/
	private double dRadius2;
	
	/**ID**/
	private String sID;
	
	/**Current angle**/
	private double dTheta;
	
	/**Current angular step**/
	private double dThetaStep;

	/**Current x-coordinate**/
	private double dX =  Double.NaN;
	
	/**Current y-coordinate**/
	private double dY = Double.NaN;
	
	public HasseVertex(HasseVertex hvt1, HasseVertex hvt2, double dRadius1, double dRadius2, String sMoveDirection, String sID){
		
		this.sID = sID;
		this.hvt1 = hvt1;
		this.hvt2 = hvt2;
		this.dRadius1 = dRadius1;
		this.dRadius2 = dRadius2;
		
		//finding initial value of theta
		dTheta = Math.atan2(hvt2.y()-hvt1.y(), hvt2.x()-hvt1.x());
		if(dTheta<0){
			dTheta = 2*Math.PI+dTheta;
		}
		if(sMoveDirection.equals("clockwise")){
			dThetaStep = -Math.PI/2.;
		}else if(sMoveDirection.equals("counterclockwise")){
			dThetaStep = Math.PI/2.;
		}else{
			dThetaStep = Double.NaN;
		}
		dTheta+=dThetaStep;
		dThetaStep=dThetaStep/2.;
	}
	
	public HasseVertex(HasseVertex hvt1, double dRadius1, double dTheta, String sID){
		this.sID = sID;
		this.hvt1 = hvt1;
		this.dRadius1 = dRadius1;
		this.dTheta=dTheta*Math.PI;
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
			return hvt1.x() + Math.cos(dTheta)*dRadius1;
		}else{
			return dX;
		}
	}
	
	public double y(){
		if(Double.isNaN(dY)){
			return hvt1.y() + Math.sin(dTheta)*dRadius1;
		}else{
			return dY;
		}
	}
	
	public static double distance(HasseVertex hvt1, HasseVertex hvt2){
		return Math.sqrt((hvt1.x()-hvt2.x())*(hvt1.x()-hvt2.x()) + (hvt1.y()-hvt2.y())*(hvt1.y()-hvt2.y()));
	}
	
	private double distance(double dTheta){
		
		//d2 = test x-coordinate
		//d3 = test y-coordinate
		
		double d2;
		double d3;
		
		d2 = hvt1.x() + Math.cos(dTheta)*dRadius1;
		d3 = hvt1.y() + Math.sin(dTheta)*dRadius1; 
		return Math.sqrt((d2-hvt2.x())*(d2-hvt2.x()) + (d3-hvt2.y())*(d3-hvt2.y()));
	}
	
	public void move(double dThreshold, int iMaxIterations) throws Exception{
		
		//d1 = current distance
		//d2 = current negative distance
		//d3 = current positive distance
		//i1 = counter
		
		double d1;
		double d2;
		double d3;
		int i1;
		
		d1 = distance(this,hvt2);
		i1 = 0;
		while(error(d1,dRadius2)>dThreshold && i1<iMaxIterations){
			d2 = testMoveRadial("negative");
			d3 = testMoveRadial("positive");
			if(error(d2,dRadius2) < error(d3,dRadius2)){
				moveRadial("negative");
				d1 = d2;
			}else{
				moveRadial("positive");
				d1 = d3;
			}
			i1++;
		}
		if(i1==iMaxIterations){
			throw new Exception("Error: maximum number of iterations reached for vertex " + this.id() + ".");
		}
	}
	
	private void moveRadial(String sDirection){
		
		if(sDirection.equals("positive")){
			dTheta += dThetaStep;
		}else if(sDirection.equals("negative")){
			dTheta -= dThetaStep;
		}
		dThetaStep=dThetaStep/2.;
	}
	
	private double testMoveRadial(String sDirection){
		
		//d1 = current test value of theta
		
		double d1;
		
		if(sDirection.equals("positive")){
			d1 = dTheta+dThetaStep;
		}else if(sDirection.equals("negative")){
			d1 = dTheta-dThetaStep;
		}else{
			d1 = Double.NaN;
		}
		return distance(d1);
	}
	
	
	private double error(double dDistance, double dTargetDistance){
		return Math.abs(dDistance - dTargetDistance);
	}
}