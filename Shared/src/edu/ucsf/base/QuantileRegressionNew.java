package edu.ucsf.base;

import edu.ucsf.base.QuantileRegressionDataNew.FormattedData;

public class QuantileRegressionNew{

	
	/**Slope estimate**/
	private double dSlope;
	
	/**Correlation estimate**/
	private double dR;
	
	public QuantileRegressionNew(
			QuantileRegressionDataNew qrd1, 
			String sPredictor){
					
		//d1 = current value
		//dSxy = sum of x*y
		//dSy = sum of y
		//dSy2 = sum of y^2
		//dXBar = mean value of x
		//dYBar = mean value of y
		//frm1 = formatted data
		
		FormattedData frm1;
		double d1;
		double dSxy;
		double dSy;
		double dSy2;
		
		frm1 = qrd1.getData(sPredictor);
		dSxy = 0;
		dSy = 0;
		dSy2 = 0;
		for(int i=0;i<frm1.size();i++){
			d1 = frm1.y(i);
			dSxy+=d1*frm1.x(i);
			dSy+=d1;
			dSy2+=d1*d1;
		}
		dSlope = (frm1.n()*dSxy-frm1.sumX()*dSy)/(frm1.n()*frm1.sumX2()-frm1.sumX()*frm1.sumX());
		dR = (frm1.n()*dSxy-frm1.sumX()*dSy)/(Math.sqrt(frm1.n()*frm1.sumX2()-frm1.sumX()*frm1.sumX())*Math.sqrt(frm1.n()*dSy2-dSy*dSy));
	}
		
	public double slope() {
		return dSlope;
	}
	
	public double correlation() {
		return dR;
	}
}