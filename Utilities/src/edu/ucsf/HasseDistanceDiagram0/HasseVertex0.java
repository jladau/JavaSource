package edu.ucsf.HasseDistanceDiagram0;

public class HasseVertex0 {

	private double dX;
	private double dY;
	private String sMoveDirection;
	private double dMoveStep;
	private String sID;
	
	private double dTheta;
	private double dThetaStep;
	private HasseVertex0 hvt1;
	private HasseVertex0 hvt2;
	private double dTargetDistance1;
	private Double dTargetDistance2;
	
	public HasseVertex0(HasseVertex0 hvt1, HasseVertex0 hvt2, double dTargetDistance1, double dTargetDistance2, String sMoveDirection, String sID){
		
		this.sMoveDirection = sMoveDirection;
		this.sID = sID;
		this.hvt1 = hvt1;
		this.hvt2 = hvt2;
		this.dTargetDistance1 = dTargetDistance1;
		this.dTargetDistance2 = dTargetDistance2;
		
		//finding initial value of theta
		dTheta = Math.atan2(hvt1.y()-hvt2.y(), hvt1.x()-hvt2.x());
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
	
	public HasseVertex0(double dX, double dY, String sMoveDirection, double dBound, String sID){
		
		this.dX = dX;
		this.dY = dY;
		this.sMoveDirection = sMoveDirection;
		this.sID = sID;
		
		if(sMoveDirection.equals("left")){
			dMoveStep = 0.5*(dBound-dX);
			dX+=dMoveStep;
		}else if(sMoveDirection.equals("right")){
			dMoveStep = 0.5*(dBound-dX);
			dX+=dMoveStep;
		}else if(sMoveDirection.equals("up")){
			dMoveStep = 0.5*(dBound-dY);
			dY+=dMoveStep;
		}else if(sMoveDirection.equals("down")){
			dMoveStep = 0.5*(dBound-dY);
			dY+=dMoveStep;
		}else if(sMoveDirection.equals("none")){
			dMoveStep = 0.;
		}
		dMoveStep=dMoveStep/2.;
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
	
	public static double distance(HasseVertex0 hvt1, HasseVertex0 hvt2){
		return Math.sqrt((hvt1.x()-hvt2.x())*(hvt1.x()-hvt2.x()) + (hvt1.y()-hvt2.y())*(hvt1.y()-hvt2.y()));
	}
	
	private double distance(HasseVertex0 hvt1, double dX, double dY){
		return Math.sqrt((this.x()+dX-hvt1.x())*(this.x()+dX-hvt1.x()) + (this.y()+dY-hvt1.y())*(this.y()+dY-hvt1.y()));
	}
	
	private double testMove(HasseVertex0 hvt2, String sSign){
		if(sMoveDirection.equals("left") || sMoveDirection.equals("right")){
			if(sSign.equals("positive")){	
				return distance(hvt2, dMoveStep, 0.);
			}else if(sSign.equals("negative")){
				return distance(hvt2, -dMoveStep, 0.);
			}
		}else if(sMoveDirection.equals("up") || sMoveDirection.equals("down")){
			if(sSign.equals("positive")){
				return distance(hvt2, 0., dMoveStep);
			}else if(sSign.equals("negative")){
				return distance(hvt2, 0., -dMoveStep);
			}
		}
		return Double.NaN;
	}
	
	private void move(String sSign){
		if(sMoveDirection.equals("left") || sMoveDirection.equals("right")){
			if(sSign.equals("positive")){	
				this.dX+=dMoveStep;
			}else if(sSign.equals("negative")){
				this.dX-=dMoveStep;
			}
		}else if(sMoveDirection.equals("up") || sMoveDirection.equals("down")){
			if(sSign.equals("positive")){
				this.dY+=dMoveStep;
			}else if(sSign.equals("negative")){
				this.dY-=dMoveStep;
			}
		}
		dMoveStep=dMoveStep/2.;
	}
	
	public void move(HasseVertex0 hvt1, double dTargetDistance, double dThreshold, int iMaxIterations){
		
		//d1 = current distance
		//d2 = current negative distance
		//d3 = current positive distance
		//i1 = counter
		
		double d1;
		double d2;
		double d3;
		int i1;
		
		d1 = distance(hvt1, 0, 0);
		i1 = 0;
		while(error(d1,dTargetDistance)>dThreshold && i1<iMaxIterations){
			d2 = testMove(hvt1, "negative");
			d3 = testMove(hvt1, "positive");
			if(error(d2,dTargetDistance) < error(d3,dTargetDistance)){
				move("negative");
				d1 = d2;
			}else{
				move("positive");
				d1 = d3;
			}
			i1++;
		}
	}
	
	public void move(double dThreshold, int iMaxIterations){
		
		//d1 = current distance
		//d2 = current negative distance
		//d3 = current positive distance
		//i1 = counter
		
		double d1;
		double d2;
		double d3;
		int i1;
		
		this.dX = hvt1.x() + Math.cos(dTheta)*dTargetDistance1;
		this.dY = hvt1.y() + Math.sin(dTheta)*dTargetDistance1;
		d1 = distance(hvt2, 0, 0);
		i1 = 0;
		while(error(d1,dTargetDistance2)>dThreshold && i1<iMaxIterations){
			d2 = testMoveRadial("negative");
			d3 = testMoveRadial("positive");
			if(error(d2,dTargetDistance2) < error(d3,dTargetDistance2)){
				moveRadial("negative");
				d1 = d2;
			}else{
				moveRadial("positive");
				d1 = d3;
			}
			i1++;
		}	
	}
	
	private void moveRadial(String sDirection){
		
		if(sDirection.equals("positive")){
			dTheta += dThetaStep;
		}else{
			dTheta -= dThetaStep;
		}
		dThetaStep=dThetaStep/2.;
	}
	
	private double testMoveRadial(String sDirection){
		
		//d1 = current test value of theta
		//d2 = test x-coordinate
		//d3 = test y-coordinate
		
		double d1;
		double d2;
		double d3;
		
		if(sDirection.equals("positive")){
			d1 = dTheta+dThetaStep;
		}else{
			d1 = dTheta-dThetaStep;
		}
		d2 = hvt1.x() + Math.cos(d1)*dTargetDistance1;
		d3 = hvt1.y() + Math.sin(d1)*dTargetDistance1; 
		return Math.sqrt((d2-hvt2.x())*(d2-hvt2.x()) + (d3-hvt2.y())*(d3-hvt2.y()));
	}
	
	
	private double error(double dDistance, double dTargetDistance){
		return Math.abs(dDistance - dTargetDistance);
	}
}