package edu.ucsf.geospatial;

import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Range;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Permutation;
import edu.ucsf.io.BiomIO;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * This code implements various geospatial statistics
 * @author jladau
 */

public class SpatialAutocorrelationTest {

	/**List of sample latitudes (grid is constructed)**/
	private double rgdLat[];
	
	/**List of sample longitudes (grid is constructed)**/
	private double rgdLon[];
	
	/**Array giving relative abundances for OTUs**/
	private double rgdAbund[][];
	
	/**Table giving weights**/
	private HashBasedTable<Integer,Integer,Double> tblWeight;
	
	/**Spatial weights matrix**/
	private SpatialWeightsMatrix spw1;
	
	/**Points**/
	private double rgdPoints[][];
	
	/**Number of MCMC iterations.**/
	private int iMCMCIterations;
	
	/**Number of MCMC chains.**/
	private int iMCMCChains;
	
	
	/**Permutations for randomization test.**/
	private Permutation<Integer>[] rgp1;
	
	/**Spatial autocorrelation object.**/
	private SpatialAutocorrelation spa1;
	
	/**BIOM object.**/
	private BiomIO bio1;
	
	/**
	 * Constructor
	*/
	public SpatialAutocorrelationTest(){
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize(){
		
		//ert1 = earth geometry object
		//dThreshold = threshold distance
		//ranDist = distances range
		//ranTime = time range
		//ranDirect = directions range
		//i1 = counter
		//rgdSum = row sums
		
		double dThreshold;
		EarthGeometry ert1;
		Range<Double> ranDist;
		Range<Double> ranTime;
		Range<Double> ranDirect;
		int i1;
		double rgdSum[];
		
		//initializing mocked data
		rgdLat = new double[]{45.,46.,47.,48.};
		rgdLon = new double[]{-110.,-109.,-108.,-107.};
		rgdPoints = new double[rgdLat.length*rgdLat.length][2];
		i1 = 0;
		for(int i=0;i<rgdLat.length;i++){
			for(int j=0;j<rgdLat.length;j++){
				rgdPoints[i1][0]=rgdLat[i];
				rgdPoints[i1][1]=rgdLon[j];
				i1++;
			}
		}
		
		rgdAbund = new double[2][rgdPoints.length];
		tblWeight = HashBasedTable.create(4,4);
		dThreshold = 150.;
		
		ert1 = new EarthGeometry();
		rgdSum = new double[rgdPoints.length];
		for(int i=0;i<rgdPoints.length;i++){
			for(int j=0;j<rgdPoints.length;j++){
				if(i!=j && ert1.orthodromicDistanceWGS84(rgdPoints[i][0], rgdPoints[i][1], rgdPoints[j][0], rgdPoints[j][1])<dThreshold){
					tblWeight.put(i, j, 1.);
					rgdSum[i]+=1.;
				}else{
					tblWeight.put(i, j, 0.);
				}
			}
			rgdAbund[0][i] = ((double) (i+1))/20.;
			rgdAbund[1][i] = (((double) (i+1)) % 4.)/4.;
		}	
		
		for(int i=0;i<rgdPoints.length;i++){
			for(int j=0;j<rgdPoints.length;j++){
				if(rgdSum[i]>0){
					tblWeight.put(i, j, tblWeight.get(i, j)/rgdSum[i]);
				}
			}
		}
		
		//loading spatial weights object from file
		ranDist = Range.closed(0.,dThreshold);
		ranTime = Range.closed(0.,12.);
		ranDirect = Range.closed(0.,360.);
		bio1 = new BiomIO("/home/jladau/Desktop/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom");
		
		try{
			spw1 = new SpatialWeightsMatrix(bio1,ranDist,ranDirect,ranTime,"binary","latitude-longitude",false);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		//loading permutation test settings
		iMCMCIterations = 100000;
		iMCMCChains = 100;
		rgp1 = new Permutation[10];
		for(int i=0;i<10;i++){
			rgp1[i]=new Permutation<Integer>(spw1.getVertexIDs());
			rgp1[i].loadRandomPermutation();	
		}
	}
	
	public void validateMoransIMCMCPValue(){
	}

	@Test
	public void calculateMoransI_MoransICalculated_CorrectValues(){
		
		//dXBar = mean observation value
		//dMoransI = moran's I value
		//dW = sum of weights
		//dNum = numerator
		//dDen = denominator
		//sTaxon = current taxon
		//dIndependentP = p-value for independent randomizations
		
		double dXBar; double dMoransI; double dW; double dNum; double dDen; double dIndependentP;
		String sTaxon;
		
		for(int k=0;k<bio1.axsObservation.getIDs().size();k++){
			
			//loading moran's I value from biom file
			sTaxon = bio1.axsObservation.getID(k);
			for(String t:bio1.axsSample.getIDs()){
				spw1.getVertex(bio1.axsSample.getIndex(t)).put("dValue", bio1.getValueByIDs(sTaxon, t));
			}
		
			spa1 = new SpatialAutocorrelation(spw1);
			spa1.calculateMoransIMCMC(rgp1, iMCMCIterations, iMCMCChains, false);
		
			//loading moran's I value from internal data set
			dXBar = ExtendedMath.mean(rgdAbund[k]);
			dW = 0.;
			dNum = 0.;
			for(Integer i:tblWeight.rowKeySet()){
				for(Integer j:tblWeight.columnKeySet()){
					if(tblWeight.get(i, j)!=0){
						dW+=tblWeight.get(i,j);
						dNum+=tblWeight.get(i,j)*(rgdAbund[k][i]-dXBar)*(rgdAbund[k][j]-dXBar);
					}
				}
			}
			dDen  = 0.;
			for(int i=0;i<rgdAbund[0].length;i++){
				dDen+=Math.pow(rgdAbund[k][i]-dXBar,2.);
			}
			dMoransI = ((double) rgdAbund[0].length)/dW*dNum/dDen;
			
			//checking that moran's i values are the same
			assertEquals(dMoransI,spa1.mrn1.dObsMoransI,0.000001);
			
			//checking that random and MCMC Moran's I values are close
			dIndependentP = spa1.calculateMoransI(1000);
			assertEquals(dIndependentP,spa1.mrn1.dPValueMoransI,0.1);
			
			//checking that same results are obtained if initial screen is not used
			spa1.calculateMoransIMCMC(rgp1, iMCMCIterations, iMCMCChains, false);
		
			//checking that moran's i values are the same
			assertEquals(dMoransI,spa1.mrn1.dObsMoransI,0.000001);
			
			//checking that random and MCMC Moran's I values are close
			if(dIndependentP<0.05){
				assertFalse(spa1.mrn1.dPValueMoransI>0.1);
			}
		}
		
	}

	@Test
	public void calculateMoranScatterPlot_ScatterPlotCalculated_SlopeCorrect(){
		
		//lst1 = scatter plot in string format
		//lstX,lstY = scatter plot in double format
		//sTaxon = current taxon
		
		ArrayList<String> lst1;
		ArrayList<Double> lstX;
		ArrayList<Double> lstY;
		String sTaxon;
		
		for(int k=0;k<bio1.axsObservation.getIDs().size();k++){
			
			//loading moran's I value from biom file
			sTaxon = bio1.axsObservation.getID(k);
			for(String t:bio1.axsSample.getIDs()){
				spw1.getVertex(bio1.axsSample.getIndex(t)).put("dValue", bio1.getValueByIDs(sTaxon, t));
			}

			spa1 = new SpatialAutocorrelation(spw1);
			lst1 = spa1.calculateMoranScatterPlot();
			lstX = new ArrayList<Double>();
			lstY = new ArrayList<Double>();
			for(int i=1;i<lst1.size();i++){
				lstX.add(Double.parseDouble(lst1.get(i).split(",")[0]));
				lstY.add(Double.parseDouble(lst1.get(i).split(",")[1]));
			}
			spa1.calculateMoransIMCMC(rgp1, iMCMCIterations, iMCMCChains, true);
			assertEquals(ExtendedMath.slope(lstX,lstY),spa1.mrn1.dObsMoransI,0.0000001);
		}
	}
}