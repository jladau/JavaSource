package edu.ucsf.InequalitySignificanceTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Inequality0{

	/**X start**/
	private double dX0;
	
	/**X end**/
	private double dX1;
	
	/**Y start**/
	private double dY0;
	
	/**Y end**/
	private double dY1;
	
	/**Direction to count points in (above line or below line)**/
	private String sDirection;
	
	/**Observed statistic value**/
	private int iT;
	
	/**Fraction null values greater than or equal to observed value**/
	private double dNullFractionGTE;
	
	/**Mean of null values**/
	private double dNullMean;
	
	/**Standard deviation of null values**/
	private double dNullStDev;
	
	/**Standardized effect size**/
	private double dSES;
	
	public Inequality0(double dX0, double dY0, double dX1, double dY1, String sDirection){
		this.dX0 = dX0;
		this.dY0 = dY0;
		this.dX1 = dX1;
		this.dY1 = dY1;
		this.sDirection = sDirection;
	}
	
	public Inequality0(Inequality0 inq1){
		
		//rgd1 = start and end points
		
		double rgd1[];
		
		this.dNullFractionGTE = inq1.significance();
		this.dSES = inq1.ses();
		rgd1 = inq1.line();
		this.dX0 = rgd1[0];
		this.dY0 = rgd1[1];
		this.dX1 = rgd1[2];
		this.dY1 = rgd1[3];
		this.iT = inq1.testStatistic();
	}
	
	public String toString(){
		return dX0 + "," + dY0 + "," + dX1 + "," + dY1;
	}
	
	public double significance(){
		return dNullFractionGTE;
	}
	
	public double ses(){
		return dSES;
	}
	
	public double[] line(){
		return new double[] {dX0, dY0, dX1, dY1};
	}
	
	public int testStatistic(){
		return iT;
	}
		
	public void nullDistribution(ArrayList<Double> lstX, ArrayList<Double> lstY, int iIterations, ResponseShuffler shf1){
		
		//i1 = current null value
		//dSum = sum of null values
		//dSum2 = sum of null values squared
		//dN = count in double format
		//lstYShuffle = y values shuffled
		
		int i1;
		double dSum;
		double dSum2;
		double dN;
		ArrayList<Double> lstYShuffle;
		
		iT = countPoints(lstX,lstY);
		dNullFractionGTE = 0.;
		dSum = 0;
		dSum2 = 0;
		lstYShuffle = new ArrayList<Double>(lstY);
		for(int i=0;i<iIterations;i++){
			
			//****************************
			Collections.shuffle(lstYShuffle);
			i1 = countPoints(lstX,lstYShuffle);
			//i1 = countPoints(lstX,shf1.nextShuffle());
			//****************************
			
			if(i1>=iT){
				dNullFractionGTE++;
			}
			dSum+=i1;
			dSum2+=(i1*i1);
		}
		dN = ((double) iIterations);
		dNullFractionGTE=dNullFractionGTE/dN;
		dNullMean=dSum/dN;
		dNullStDev = Math.sqrt((dN*dSum2 - dSum*dSum)/(dN*(dN-1.)));
		dSES = ((double) iT - dNullMean)/dNullStDev;
	}
	
	/*
	public static Inequality maximizeSES(ArrayList<Double> lstX, ArrayList<Double> lstY, int iIterations, double[] rgdXRange, double[] rgdYRange, String sDirection){
		
		//inqMaxSES = inequality with maximum ses
		//inq1 = current candidate inequality
		//inqPlus = current candidate inequality + step
		//inqMinus = current candidate inequality - step
		//dXStart = current x start
		//dYStart = current y start
		//dXEnd = current x end
		//dYEnd = current y end
		//dStep = current step size
		//dXRange = x range
		//dYrange = y range
		
		double dStep;
		Inequality inq1;
		Inequality inqPlus;
		Inequality inqMinus;
		Inequality inqMaxSES;
		double dXStart;
		double dYStart;
		double dXEnd;
		double dYEnd;
		double dXRange;
		double dYRange;
		
		//initializing
		dXStart = rgdXRange[0];
		dYStart = rgdYRange[0];
		dXEnd = rgdXRange[1];
		dYEnd = rgdYRange[1];
		dXRange = rgdXRange[1]-rgdXRange[0];
		dYRange = rgdYRange[1]-rgdYRange[0];
		inqMaxSES = new Inequality(dXStart,dYStart,dXEnd,dYEnd,sDirection);
		inqMaxSES.nullDistribution(lstX,lstY,iIterations);
		
		//updating left
		dStep = dYRange/2.;
		inq1 = new Inequality(dXStart,dYStart + dStep,dXEnd,dYEnd,sDirection);
		inq1.nullDistribution(lstX,lstY,iIterations);
		
		iLines = lstStart.size()*lstEnd.size();
		iCounter = 0;
		for(Double[] rgdStart:lstStart){
			for(Double[] rgdEnd:lstEnd){
				iCounter++;
				if(iCounter % 100 == 0){	
					System.out.println("Analyzing line " + iCounter + " of " + iLines + "...");
				}
				if(rgdStart[1]<rgdEnd[1]){
					inq1 = new Inequality(rgdStart[0],rgdStart[1],rgdEnd[0],rgdEnd[1],sDirection);
					inq1.nullDistribution(lstX,lstY,iIterations);
					if(!Double.isNaN(inq1.ses()) && !Double.isInfinite(inq1.ses())){
						if(inqMaxSES==null || inqMaxSES.ses()<inq1.ses()){
							inqMaxSES = new Inequality(inq1);
						}
					}
				}
			}
		}
		return inqMaxSES;
	}
	
	*/

	public static Inequality0 maximizeSESSerial(
			ArrayList<Double> lstX, 
			ArrayList<Double> lstY, 
			int iIterations, 
			ArrayList<Double[]> lstStart, 
			ArrayList<Double[]> lstEnd, 
			String sDirection,
			ResponseShuffler shf1){
		
		//inqMaxSES = inequality with maximum ses
		//inq1 = current candidate inequality
		//rgdStart = starting coordinates
		//rgdEnd = ending coordinates
		
		Double rgdStart[];
		Double rgdEnd[];
		Inequality0 inq1;
		Inequality0 inqMaxSES;
		
		//looping through pairs of start and end points and testing
		inqMaxSES = null;
		for(int i=0;i<lstStart.size();i++){
			rgdStart = lstStart.get(i);
			rgdEnd = lstEnd.get(i);			
			if(rgdStart[1]<rgdEnd[1]){
				inq1 = new Inequality0(rgdStart[0],rgdStart[1],rgdEnd[0],rgdEnd[1],sDirection);
				inq1.nullDistribution(lstX,lstY,iIterations, shf1);
				if(!Double.isNaN(inq1.ses()) && !Double.isInfinite(inq1.ses())){
					if(inqMaxSES==null || inqMaxSES.ses()<inq1.ses()){
						inqMaxSES = new Inequality0(inq1);
					}
				}
			}
		}
		return inqMaxSES;
	}
	
	public static Inequality0 maximizeSES(
			ArrayList<Double> lstX, 
			ArrayList<Double> lstY, 
			int iIterations, 
			ArrayList<Double[]> lstStart, 
			ArrayList<Double[]> lstEnd, 
			String sDirection,
			ResponseShuffler shf1){
		
		//inqMaxSES = inequality with maximum ses
		//iCounter = counter
		//inq1 = current candidate inequality
				
		Inequality0 inq1;
		Inequality0 inqMaxSES;
		int iCounter;
		
		//looping through pairs of start and end points and testing
		inqMaxSES = null;
		iCounter = 0;
		for(Double[] rgdStart:lstStart){
			for(Double[] rgdEnd:lstEnd){
				iCounter++;
				if(iCounter % 100 == 0){	
					//System.out.println("Analyzing line " + iCounter + " of " + iLines + "...");
				}
				if(rgdStart[1]<rgdEnd[1]){
					inq1 = new Inequality0(rgdStart[0],rgdStart[1],rgdEnd[0],rgdEnd[1],sDirection);
					inq1.nullDistribution(lstX,lstY,iIterations,shf1);
					if(!Double.isNaN(inq1.ses()) && !Double.isInfinite(inq1.ses())){
						if(inqMaxSES==null || inqMaxSES.ses()<inq1.ses()){
							inqMaxSES = new Inequality0(inq1);
						}
					}
				}
			}
		}
		return inqMaxSES;
	}
	
	private int countPoints(
			ArrayList<Double> lstX, 
			ArrayList<Double> lstY){
		
		//i1 = output
		//bAbove = indicator for whether current point is above the line
		
		int i1;
		boolean bAbove;
		
		i1 = 0;
		for(int i=0;i<lstX.size();i++){
			bAbove = isAbove(lstX.get(i), lstY.get(i));	
			if(bAbove==true && sDirection.equals("above")){
				i1++;
			}else if(bAbove==false && sDirection.equals("below")){
				i1++;
			}
		}
		return i1;
	}
	
	public void setSignificance(double dSignificance){
		dNullFractionGTE = dSignificance;
	}
	
	private boolean isAbove(double dXTest, double dYTest){
		
		//bLeft = boolean for whether point is to the left of the line
		
		boolean bLeft;
		
		if((dX1-dX0)*(dYTest-dY0)-(dY1-dY0)*(dXTest-dX0)>0){
			bLeft = true;
		}else{
			bLeft = false;
		}
		
		if(dY0>dY1){
			if(bLeft==true){
				return false;
			}else{
				return true;
			}
		}else if(dY0<dY1){
			if(bLeft==true){
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
}
