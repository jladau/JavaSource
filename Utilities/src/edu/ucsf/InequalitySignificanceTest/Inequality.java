package edu.ucsf.InequalitySignificanceTest;

import java.util.ArrayList;

public class Inequality{

	/**X start value**/
	private double dXStart;
	
	/**Y start value**/
	private double dYStart;
	
	/**X end value**/
	private double dXEnd;
	
	/**Y end value**/
	private double dYEnd;
	
	/**Observed statistic**/
	private double dObs=Double.NaN;
	
	/**Number of null statistics**/
	private double dNNull;
	
	/**Null sum**/
	private double dNullSum;
	
	/**Null sum squared**/
	private double dNullSum2;
	
	/**Null count greater than observed value**/
	private double dNullGTE;
	
	/**Null count less than observed value**/
	private double dNullLTE;
	
	public Inequality(double dXStart, double dYStart, double dXEnd, double dYEnd) {
		this.dXStart = dXStart;
		this.dYStart = dYStart;
		this.dXEnd = dXEnd;
		this.dYEnd = dYEnd;
		
		dNNull=0;
		dNullSum=0;
		dNullSum2=0;
		dNullGTE=0;
		dNullLTE=0;
	}
	
	public void countPoints(ArrayList<Double> lstX, ArrayList<Double> lstY, String sType){
		
		//i1 = output
		
		int i1;
		
		i1 = 0;
		for(int i=0;i<lstX.size();i++){
			if(isLeft(lstX.get(i),lstY.get(i))){
				i1++;
			}
		}
		
		if(sType.equals("observed")){
			dObs = i1;
		}else if(sType.equals("null")){
			this.dNNull++;
			this.dNullSum+=i1;
			this.dNullSum2+=(i1*i1);
			if(i1>=dObs){
				dNullGTE++;
			}
			if(i1<=dObs){
				dNullLTE++;
			}
		}
	}

	public double angle(){
		return Math.atan2(dYEnd-dYStart,dXEnd-dXStart);
	}
	
	public double xStart(){
		return dXStart;
	}
	
	public double xEnd(){
		return dXEnd;
	}
	
	public double dProbabilityGTE(){
		return this.dNullGTE/this.dNNull;
	}
	
	public double dProbabilityLTE(){
		return this.dNullLTE/this.dNNull;
	}
	
	public double yStart(){
		return dYStart;
	}
	
	public double yEnd(){
		return dYEnd;
	}
	
	public double slope(){
		return (dYEnd-dYStart)/(dXEnd-dXStart);
	}
	
	public ArrayList<Double[]> lineValues(int iValues){
		
		//lst1 = output
		//d1 = values in double format
		
		ArrayList<Double[]> lst1;
		double d1;
		
		lst1 = new ArrayList<Double[]>(iValues);
		d1 = (double) iValues;
		for(double t=0;t<1;t+=1./d1){
			lst1.add(new Double[]{dXStart + (dXEnd-dXStart)*t, dYStart + (dYEnd-dYStart)*t});
		}
		return lst1;
	}
	
	public double xIntercept(){
		
		//dM = slope
		
		double dM;
		
		dM = (dYEnd-dYStart)/(dXEnd-dXStart);
		return dXEnd - dYEnd/dM;
	}
	
	public double observed() {
		return dObs;
	}
	
	public double ses(){
		
		//dNullMean = null mean
		//dNullStDev = null standard deviation
		
		double dNullMean;
		double dNullStDev;
		
		dNullMean=dNullSum/dNNull;
		dNullStDev = Math.sqrt((dNNull*dNullSum2 - dNullSum*dNullSum)/(dNNull*(dNNull-1.)));
		return (dObs - dNullMean)/dNullStDev;
	}
	
	private boolean isLeft(double dXTest, double dYTest){
	
		if((dXEnd-dXStart)*(dYTest-dYStart)-(dYEnd-dYStart)*(dXTest-dXStart)>0){
			return true;
		}else{
			return false;
		}
	}	
}