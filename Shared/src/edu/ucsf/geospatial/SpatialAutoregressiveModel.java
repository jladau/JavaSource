package edu.ucsf.geospatial;

import java.util.HashMap;
import com.google.common.collect.ArrayTable;
import static edu.ucsf.base.ExtendedMath.*;
import edu.ucsf.base.Graph;

/**
 * Generates variates from a spatial autoregressive model: Y=rho*W*Y+epsilon
 * @author jladau
 *
 */

public class SpatialAutoregressiveModel{

	/**Spatial weights matrix in table form**/
	private ArrayTable<Graph.GraphVertex, Graph.GraphVertex, Double> tblW;
	
	/**Inverse of 1-rho*W; used for calculating variates**/
	private double rgdInv[][];
	
	/**Error variance**/
	private double dErrorVariance;
	
	/**Number of samples**/
	private int iSamples;
	
	public SpatialAutoregressiveModel(ArrayTable<Graph.GraphVertex, Graph.GraphVertex, Double> tblW, double dCorrelation, double dErrorVariance){
		this.tblW = tblW;
		initialize(dCorrelation, dErrorVariance);
	}
	
	public SpatialAutoregressiveModel(SpatialWeightsMatrix spw1, double dCorrelation, double dErrorVariance){
		tblW = spw1.getTable();
		initialize(dCorrelation, dErrorVariance);
	}
	
	private void initialize(double dCorrelation, double dErrorVariance){
		
		//rgd1 = matrix to be inverted
		
		double rgd1[][];
		
		this.dErrorVariance = dErrorVariance;
		iSamples=tblW.rowKeySet().size();
		rgd1 = toPrimitive(tblW.toArray(Double.class));
		rgd1 = matrixScalarProduct(rgd1,dCorrelation);
		try{
			rgd1 = matrixDifference(matrixIdentity(iSamples),rgd1);
		}catch (Exception e) {
			e.printStackTrace();
		}
		rgdInv = matrixInverse(rgd1);
	}
	
	/**
	 * Generates a list of relative abundances according to SAR model
	 * @return Returns a HashMap between the sample IDs and the simulated relative abundance at a location.
	 */
	public HashMap<String,Double> generateRelativeAbundances(){
		
		//rgd1 = vector of normal variates
		//rgd2 = raw output
		//mapOut = output
		
		HashMap<String,Double> mapOut;
		double rgd1[];
		double rgd2[] = null;
		
		rgd1 = normalRandomVector(0,dErrorVariance,iSamples);
		try {
			rgd2 = matrixProduct(rgdInv,rgd1);
		}catch(Exception e) {
			e.printStackTrace();
		}
		mapOut=new HashMap<String,Double>();
		for(int i=0;i<tblW.rowKeyList().size();i++){
			mapOut.put(tblW.rowKeyList().get(i).sName, rgd2[i]);
		}
		return mapOut;
	}
}
