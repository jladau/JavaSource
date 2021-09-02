package gov.lbnl.SimulateData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SimulateDataServer{

	/**Number of iterations**/
	public int iIterations;
	
	/**Random number generator**/
	public Random rnd1;
	
	/**Slope of bound**/
	public double dSlope;
	
	/**Intercept of bound**/
	public double dIntercept;
	
	/**X values**/
	public HashMap<String,Double> mapX;
	
	/**Initial map of Y values**/
	public HashMap<String,Double> mapY0;
	
	/**Y values**/
	public HashMap<String,Double> mapY;
	
	/**Number of values**/
	public double dRows;
	
	/**Ordered list of samples**/
	public ArrayList<String> lstSamples;
	
	public SimulateDataServer(int iIterations, double dBoundIntercept, double dBoundSlope, HashMap<String,Double> mapX, HashMap<String,Double> mapY){
	
		this.iIterations = iIterations;
		rnd1 = new Random();
		this.dSlope = dBoundSlope;
		this.dIntercept = dBoundIntercept;
		this.mapX = mapX;
		this.mapY0 = new HashMap<String,Double>(mapY);
		this.mapY = mapY;
		this.dRows = (double) mapX.size();
		lstSamples = new ArrayList<String>(mapX.keySet());
	}
}