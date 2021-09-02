package edu.ucsf.HasseDistanceDiagram;

import java.util.ArrayList;

public class HasseEdge {

	/**First vertex**/
	private HasseVertex hvt1;
	
	/**Second vertex**/
	private HasseVertex hvt2;
	
	/**Desired length**/
	private double dPathLength;
	
	/**Distance between points**/
	private double dEdgeLength;
	
	/**List of points in path**/
	private ArrayList<Double[]> lstPoints;
	
	/**ID**/
	private String sID;
	
	public HasseEdge(HasseVertex hvt1, HasseVertex hvt2, double dPathLength, String sID) throws Exception{
		this.hvt1 = hvt1;
		this.hvt2 = hvt2;
		this.dPathLength = dPathLength;
		dEdgeLength = HasseVertex.distance(hvt1, hvt2);
		this.sID = sID;
	}
	
	public ArrayList<Double[]> path() throws Exception{
	
		if(lstPoints==null){
			if(Math.abs(dEdgeLength-dPathLength)<0.0001){
				lstPoints = new ArrayList<Double[]>(1000);
				for(double t=0;t<1;t+=0.01){
					lstPoints.add(new Double[]{hvt1.x()*(1.-t) + t*hvt2.x(), hvt1.y()*(1.-t) + t*hvt2.y()});
				}
				lstPoints.add(new Double[]{hvt2.x(),hvt2.y()});
			}else{
				if(dEdgeLength>dPathLength){
					throw new Exception("Path length cannot be shorter than distance between points for edge " + sID + ".");
				}else if(dEdgeLength<dPathLength && dPathLength<=dEdgeLength*Math.PI/2){
					lstPoints = circularArc();
				}else{
					throw new Exception("Edge " + sID + " length cannot exceed pi*distance.");
				}
			}	
		}
		return lstPoints;
	}
	
	public String toString(){
		return hvt1.id() + "-->" + hvt2.id() + "," + this.dEdgeLength + "," + this.dPathLength;
	}
	
	public double edgeLength(){
		return dEdgeLength;
	}
	
	public double pathLength(){
		return dPathLength;
	}
	
	public void updatePathLength(double dNewPathLength){
		this.dPathLength = dNewPathLength;
	}
	
	private ArrayList<Double[]> circularArc() throws Exception{
		
		//rgdRTheta = radius and angle
		//rgdCenter = center
		//rgd1 = 0-offset coordinates for vertex 1
		//rgd2 = 0-offset coordinates for vertex 2
		//dTheta1 = angle for first vertex
		//dTheta2 = angle for second vertex
		//lstOut = output
		//dR = radius
		
		double dR;
		double rgd1[];
		double rgd2[];
		double rgdRTheta[];
		double rgdCenter[];
		double dTheta1;
		double dTheta2;
		ArrayList<Double[]> lstOut;
		
		rgdRTheta = this.findAngleRadius();
		dR = rgdRTheta[0];
		rgdCenter = this.findCircleCenter(dR);
		
		//**********************
		//Checking values
		if(Math.abs(dR*rgdRTheta[1]-dPathLength)>0.001){
			System.out.println(dR*rgdRTheta[1]);
		}
		if(Math.abs(Math.sqrt((hvt1.x()-rgdCenter[0])*(hvt1.x()-rgdCenter[0]) + (hvt1.y()-rgdCenter[1])*(hvt1.y()-rgdCenter[1]))-dR)>0.001){
			System.out.println(Math.sqrt((hvt1.x()-rgdCenter[0])*(hvt1.x()-rgdCenter[0]) + (hvt1.y()-rgdCenter[1])*(hvt1.y()-rgdCenter[1])));
		}
		//**********************
		
		rgd1 = new double[]{hvt1.x()-rgdCenter[0], hvt1.y()-rgdCenter[1]};
		rgd2 = new double[]{hvt2.x()-rgdCenter[0], hvt2.y()-rgdCenter[1]};
		dTheta1 = Math.atan2(rgd1[1], rgd1[0]);
		lstOut = new ArrayList<Double[]>(1000);
		if(dTheta1<0){
			dTheta1 = 2.*Math.PI+dTheta1;
		}
		dTheta2 = Math.atan2(rgd2[1], rgd2[0]);
		if(dTheta2<0){
			dTheta2 = 2.*Math.PI+dTheta2;
		}
		if(dTheta1<dTheta2){
			if(dTheta2-dTheta1<=Math.PI){
				for(double d=dTheta1;d<dTheta2;d+=0.01){
					lstOut.add(new Double[]{dR*Math.cos(d)+rgdCenter[0], dR*Math.sin(d)+rgdCenter[1]});
				}
			}else{
				dTheta2=dTheta2-2.*Math.PI;
				for(double d=dTheta2;d<dTheta1;d+=0.01){
					lstOut.add(new Double[]{dR*Math.cos(d)+rgdCenter[0], dR*Math.sin(d)+rgdCenter[1]});
				}
			}
		}else{
			if(dTheta1-dTheta2<=Math.PI){
				for(double d=dTheta2;d<dTheta1;d+=0.01){
					lstOut.add(new Double[]{dR*Math.cos(d)+rgdCenter[0], dR*Math.sin(d)+rgdCenter[1]});
				}
			}else{
				dTheta1 = dTheta1-2.*Math.PI;
				for(double d=dTheta1;d<dTheta2;d+=0.01){
					lstOut.add(new Double[]{dR*Math.cos(d)+rgdCenter[0], dR*Math.sin(d)+rgdCenter[1]});
				}
			}
		}
		
		//*******************************
		//System.out.println("");
		//System.out.println(hvt1.x() + "," + hvt1.y());
		//System.out.println(hvt2.x() + "," + hvt2.y());
		//System.out.println("");
		//for(Double[] rgd:lstOut){
		//	System.out.println(rgd[0] + "," + rgd[1]);
		//}
		//System.out.println("");
		//*******************************
		
		
		return lstOut;
	}
	
	private double[] findCircleCenter(double dRadius){
		
		//dA = a parameter
		//dB = b parameter
		//dV = value of v
		//dU = value of u
		
		double dA;
		double dB;
		double dV;
		double dU;
		
		dA = (hvt2.x()-hvt1.x())/2.;
		dB = (hvt2.y()-hvt1.y())/2.;
		dU = dB/dEdgeLength*Math.sqrt(4.*dRadius*dRadius-dEdgeLength*dEdgeLength);
		dV = -dA/dEdgeLength*Math.sqrt(4.*dRadius*dRadius-dEdgeLength*dEdgeLength);
		return new double[]{
			dU + (hvt1.x() + hvt2.x())/2.,	
			dV + (hvt1.y() + hvt2.y())/2.	
		};
	}
	
	private double radiusAngleFunction(double dX){
		return 2*dPathLength*Math.sin(dX/2.)-dEdgeLength*dX;
	}
	
	
	private double[] findAngleRadius() throws Exception{
		
		//rgd1 = current start point, midpoint, and end point
		//rgd2 = function evaluated at points of rgd1
		//i1 = counter
		
		double rgd1[];
		double rgd2[];
		int i1;
		
		rgd1 = new double[]{0.000001,Math.PI/2.,Math.PI};
		rgd2 = new double[3];
		for(int i=0;i<3;i++){
			rgd2[i] = radiusAngleFunction(rgd1[i]);
		}
		i1 = 0;
		do{
			if(Math.signum(rgd2[0])!=Math.signum(rgd2[1]) && Math.signum(rgd2[1])==Math.signum(rgd2[2])){
				rgd1 = new double[]{rgd1[0],(rgd1[0]+rgd1[1])*0.5,rgd1[1]};
			}else if(Math.signum(rgd2[0])==Math.signum(rgd2[1]) && Math.signum(rgd2[1])!=Math.signum(rgd2[2])){
				rgd1 = new double[]{rgd1[1],(rgd1[1]+rgd1[2])*0.5,rgd1[2]};
			}else{
				throw new Exception("Error: multiple roots found.");
			}
			for(int i=0;i<3;i++){
				rgd2[i] = radiusAngleFunction(rgd1[i]);
			}
			i1++;
		}while(i1<101 && Math.abs(rgd2[1])>0.0000001);
		if(Math.abs(rgd2[1])>0.0000001){
			System.out.println("Warning: tolerance not met.");
		}
		
		return new double[]{dPathLength/rgd1[1], rgd1[1]};
	}
	
	private double[] findAngleRadius0(){
		
		//dTheta = current value of theta
		//dStep = current step size
		//d2 = current trial value 1
		//d3 = current trial value 2
		//d4 = current function value
		//i1 = iterations count
		
		double d2;
		double d3;
		double d4;
		double dTheta;
		double dStep;
		int i1;
		
		dTheta = Math.PI/2.;
		dStep = Math.PI/4.;
		i1 = 0;
		do{
			d2 = 2.*dPathLength*Math.sin((dTheta+dStep)/2.)-(dTheta+dStep)*dEdgeLength;
			d3 = 2.*dPathLength*Math.sin((dTheta-dStep)/2.)-(dTheta-dStep)*dEdgeLength;
			if(Math.abs(d2)<Math.abs(d3)){
				dTheta+=dStep;
				d4 = Math.abs(d2);
			}else{
				dTheta-=dStep;
				d4 = Math.abs(d3);
			}
			dStep=dStep/2.;
			i1++;
		}while(d4>0.00001 && i1<101);
		if(d4>0.00001){
			System.out.println("Warning: tolerance not met (error = " + d4 + ") for edge " + sID + ".");
			System.out.println(dPathLength + "," + dEdgeLength + "," + dTheta);
			System.out.println("HERE");
		}
		
		
		return new double[]{dPathLength/dTheta, dTheta};
	}
}