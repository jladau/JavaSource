package edu.ucsf.base;

import java.util.ArrayList;

public class QuantileRegression{

	
	/**Slope estimate**/
	private double dSlope;
	
	/**Correlation estimate**/
	private double dR;
		
	public QuantileRegression(QuantileRegressionData qrd1, double dPercent){
		
		//rgd1 = slope and correlation
		
		double rgd1[];
		
		rgd1 = calculateStatistics(qrd1,dPercent);
		dSlope = rgd1[0];
		dR = rgd1[1];
	}
		
	private double[] calculateStatistics(QuantileRegressionData qrd1, double dPercent){
			
		//lstY = current Y vector (windowed)
		//lstX = current X vector (windowed)
		//d1 = current value
		//dSxy = sum of x*y
		//dSy = sum of y
		//dSy2 = sum of y^2
		//dXBar = mean value of x
		//dYBar = mean value of y

		double d1;
		double dSxy;
		double dSy;
		double dSy2;
		ArrayList<Double> lstY;
		ArrayList<Double> lstX;
		
		lstY = qrd1.windowedY(dPercent);
		lstX = qrd1.windowedX();
		dSxy = 0;
		dSy = 0;
		dSy2 = 0;
		for(int i=0;i<lstY.size();i++){
			
			d1 = lstY.get(i);
			dSxy+=d1*lstX.get(i);
			dSy+=d1;
			dSy2+=d1*d1;
		}
		return new double[]{
				(qrd1.n()*dSxy-qrd1.sumX()*dSy)/(qrd1.n()*qrd1.sumX2()-qrd1.sumX()*qrd1.sumX()),
				(qrd1.n()*dSxy-qrd1.sumX()*dSy)/(Math.sqrt(qrd1.n()*qrd1.sumX2()-qrd1.sumX()*qrd1.sumX())*Math.sqrt(qrd1.n()*dSy2-dSy*dSy))};
	}
	
	
	public double slope() {
		return dSlope;
	}
	
	public double correlation() {
		return dR;
	}
}