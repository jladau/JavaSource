package edu.ucsf.TwoDimensionalHistogram;

import java.util.ArrayList;
import edu.ucsf.base.ExtendedMath;

public class TwoDimensionalHistogram{

	/**List of x values**/
	private ArrayList<Double> lstX;
	
	/**List of y values**/
	private ArrayList<Double> lstY;
	
	/**List of frequencies**/
	private ArrayList<Integer> lstFrequency;
	
	public TwoDimensionalHistogram(int iExpectedObservations) {
		lstX = new ArrayList<Double>(iExpectedObservations);
		lstY = new ArrayList<Double>(iExpectedObservations);	
	}
	
	public void add(double dX, double dY) {
		lstX.add(dX);
		lstY.add(dY);
	}
	
	public void loadHistogram(double dThreshold) {
	
		//dXRange = x range
		//dYRange = y range
		//rgd1 = min and max for x (0), min and max for y (1)
		//lstXS = list of x values scaled
		//lstYS = list of y values scaled
		//i1 = current count
		//lstOut = output
		//dThreshold2 = distance threshold squared
		//d1 = current squared distance
		
		double d1;
		int i1;
		double dXRange;
		double dYRange;
		double rgd1[][];
		ArrayList<Double> lstXS;
		ArrayList<Double> lstYS;
		double dThreshold2;
		
		//loading step sizes
		dThreshold2 = dThreshold*dThreshold;
		rgd1 = new double[2][2];
		rgd1[0][0] = ExtendedMath.minimum(lstX);
		rgd1[0][1] = ExtendedMath.maximum(lstX);
		rgd1[1][0] = ExtendedMath.minimum(lstY);
		rgd1[1][1] = ExtendedMath.maximum(lstY);
		dXRange = rgd1[0][1]-rgd1[0][0];
		dYRange = rgd1[1][1]-rgd1[1][0];
		if(dXRange==0){
			dXRange=1;
		}
		if(dYRange==0) {
			dYRange=1;
		}
		
		//rescaling values
		lstXS = new ArrayList<Double>(lstX.size());
		lstYS = new ArrayList<Double>(lstY.size());
		for(int i=0;i<lstX.size();i++){
			lstXS.add((lstX.get(i)-rgd1[0][0])/dXRange);
			lstYS.add((lstY.get(i)-rgd1[1][0])/dYRange);
		}
		
		//finding frequencies
		lstFrequency = new ArrayList<Integer>(lstX.size());
		for(int i=0;i<lstXS.size();i++){
			i1 = 0;
			for(int j=0;j<lstXS.size();j++){
				if(j!=i){
					d1 = distanceSquared(lstXS.get(i),lstYS.get(i),lstXS.get(j),lstYS.get(j));
					if(d1<=dThreshold2){
						i1++;
					}
				}
			}
			lstFrequency.add(i1);
		}	
	}

	public ArrayList<String> print(String sCategory){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(lstFrequency.size());
		for(int i=0;i<lstX.size();i++) {
			if(!sCategory.equals("-9999")){
				lstOut.add(sCategory + lstX.get(i) + "," + lstY.get(i) + "," + lstFrequency.get(i));
			}else{
				lstOut.add(lstX.get(i) + "," + lstY.get(i) + "," + lstFrequency.get(i));
			}
		}
		return lstOut;
	}
	
	private double distanceSquared(double dX1, double dY1, double dX2, double dY2){
		return Math.pow(dX2-dX1,2)+Math.pow(dY2-dY1,2);
	}
}