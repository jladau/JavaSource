package edu.ucsf.geospatial;

import java.util.ArrayList;

import static edu.ucsf.base.ExtendedMath.*;
import static java.lang.Math.*;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.Permutation;
import edu.ucsf.base.Graph.GraphEdge;
import edu.ucsf.base.Graph.GraphVertex;


/**
 * This code implements various geospatial statistics
 * @author jladau
 */

public class SpatialAutocorrelation {

	//spw1 = spatial weights matrix
	//mapEdgeProperties(sProperty) = returns list of edge property values for current graph
	//mapVertexProperties(sProperty) = returns list of vertex property values for current graph
	//mrn1 = moran's i
	
	public MoransI mrn1;
	public SpatialWeightsMatrix spw1;
	//private HashMap<String,ArrayList<Double>> mapEdgeProperties;
	//private HashMap<String,ArrayList<Double>> mapVertexProperties;
	
	/**
	 * Constructor
	*/
	public SpatialAutocorrelation(SpatialWeightsMatrix spw1){
		
		//saving graph
		this.spw1 = spw1;
			
		//initializing moran's i object
		mrn1 = new MoransI();
		
		//loading list of standardized variables
		this.loadStandardizedVariables();
	}

	/**
	 * Validates mcmc p-value by performing independent randomizations
	 * @param iIndependentIterations number of independent iterations to use
	 * @return P-value calculated by independent randomizations
	 */
	@SuppressWarnings("unchecked")
	public double calculateMoransI(int iIndependentIterations){
		
		//mrnOriginal = original moran's i value
		//rgp1 = random permutations
		//dOut = output
		
		MoransI mrnOriginal;
		Permutation<Integer> rgp1[];
		double dOut;
		
		//copying moran's i value
		mrnOriginal=mrn1.clone();
		
		//loading permutations
		rgp1 = new Permutation[iIndependentIterations];
		for(int i=0;i<iIndependentIterations;i++){
			rgp1[i]=new Permutation<Integer>(spw1.getVertexIDs());
			rgp1[i].loadRandomPermutation();	
		}
		
		//finding independent p-value
		calculateMoransIPValue(rgp1);
		dOut=mrn1.dPValueMoransI;
		
		//terminating
		mrn1=mrnOriginal.clone();
		return dOut;
	}

	/**
	 * Calculate monovariate Moran's I statistic
	 */
	public void calculateMoransIMCMC(Permutation<Integer>[] rgp1, int iMCMCIterations, int iMCMCChains, boolean bInitialPValueScreen){
		
		//dValue1 = current first value
		//dValue2 = current second value
		//dWeight = current weight
		//dWeightReversed = current reverse edge weight
		//rgsS = S_0 to S_2 (see arcgis formula)
		//dA, dB, dC, dD = values from arcgis formula
		//edgReversed = current edge (reversed)
		//mapW(iVertexID) = returns partial sum for given vertex id
		//mapW2(iVertexID) = returns transposed partial sum
		
		double dValue1; double dValue2; double dWeightReversed; double dWeight; double dA; double dB; double dC; double dD; double rgdS[];
		GraphEdge edgReversed;
		HashMap_AdditiveDouble<Integer> mapW; HashMap_AdditiveDouble<Integer> mapW2;
		
		//loading values
		mrn1.dMeanValue=mean(spw1.getVertexProperties("dValue"));
		mrn1.dN = spw1.getVertices().size();
		mrn1.dSumSquares=sumOfPowersMeanCentered(spw1.getVertexProperties("dValue"),2.);

		//loading other values
		mrn1.dSXY=0.;
		rgdS = new double[3];
		mapW = new HashMap_AdditiveDouble<Integer>();
		mapW2 = new HashMap_AdditiveDouble<Integer>();
		for(GraphEdge edg1:spw1.getEdges()){
			
			//loading reverse edge
			edgReversed = spw1.getEdge(edg1.vtxEnd.iID, edg1.vtxStart.iID);
			
			//loading values
			dValue1 = edg1.vtxStart.getDouble("dValue");
			dValue2 = edg1.vtxEnd.getDouble("dValue");
			
			//loading weights
			dWeight = spw1.getWeight(edg1);
			if(edgReversed!=null){
				dWeightReversed = spw1.getWeight(edgReversed);
			}else{
				dWeightReversed = 0.;
			}
			
			//updating values
			rgdS[0]+=dWeight;
			rgdS[1]+=pow(dWeight + dWeightReversed, 2.);
			mapW.putSum(edg1.vtxEnd.iID, dWeight);
			mapW2.putSum(edg1.vtxEnd.iID, dWeightReversed);
			mrn1.dSXY+=dWeight*(dValue1-mrn1.dMeanValue)*(dValue2-mrn1.dMeanValue);
		}
		for(int i:mapW.keySet()){
			rgdS[2]+=pow(mapW.get(i)+mapW2.get(i),2.);
		}
	
		//updating s variables
		rgdS[1]=0.5*rgdS[1];
		
		//loading other variables
		mrn1.dS0=rgdS[0];
		
		//loading variables with letters
		dD=mrn1.dN*sumOfPowers(spw1.getVertexProperties("dValue"),4.)/pow(mrn1.dSumSquares, 2.);
		dC=(mrn1.dN-1.)*(mrn1.dN-2.)*(mrn1.dN-3.)*pow(rgdS[0],2.);
		dB=dD*(mrn1.dN*(mrn1.dN-1.)*rgdS[1]-2*mrn1.dN*rgdS[2]+6.*pow(rgdS[0],2.));
		dA=mrn1.dN*((pow(mrn1.dN,2.)-3.*mrn1.dN+3.)*rgdS[1]-mrn1.dN*rgdS[2]+3*pow(rgdS[0],2.));
		mrn1.dEMoransI=-1./(mrn1.dN-1.);
		mrn1.dVarMoransI=(dA-dB)/dC-pow(mrn1.dEMoransI,2.);
		mrn1.dObsMoransI=mrn1.dN*mrn1.dSXY/(mrn1.dS0*mrn1.dSumSquares);
		
		//checking if mcmc should be used
		if(iMCMCIterations!=-9999){
		
			//screening moran's i p-value using independent randomizations
			if(bInitialPValueScreen){	
				screenMoransIPValue(rgp1);
			}else{
				mrn1.dPValueMoransI=-9999;
			}
				
			//checking if potentially significant result found and refining p-value if so using mcmc
			if(mrn1.dPValueMoransI==-9999){
				calculateMoransIPValueMCMC(iMCMCIterations, iMCMCChains);		
			}
		}else{
			
			//calculating p value (no randomization)
			this.calculateMoransIPValue(rgp1);
		}
		
		
		if(mrn1.dVarMoransI>0){
			mrn1.dZMoransI=(mrn1.dObsMoransI-mrn1.dEMoransI)/sqrt(mrn1.dVarMoransI);
		}else{
			mrn1.dZMoransI=-9999;
		}
	}

	/**
	 * Calculates Moran scatter plot
	 * @return Moran scatter plot
	 */
	public ArrayList<String> calculateMoranScatterPlot(){
		
		//lstOut = output
		//dTotalWeight = total weight
		//dN = total number of observations
		//mapY(iVertexID) = returns y value for given vertex
		
		double dTotalWeight; double dN;
		ArrayList<String> lstOut;
		HashMap_AdditiveDouble<Integer> mapY;
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("Value,SpatiallyLaggedValue");
		
		//initializing map of y values
		mapY = new HashMap_AdditiveDouble<Integer>();
		
		//loading total weight
		dTotalWeight=sum(spw1.getWeights());
		
		//loading number of observations
		dN=(double) spw1.order();
		
		//looping through edges
		for(GraphEdge edg1:spw1.getEdges()){
			mapY.putSum(edg1.vtxStart.iID, spw1.getWeight(edg1)*edg1.vtxEnd.getDouble("dValueNormalized"));
		}
		
		//loading output
		for(int k:mapY.keySet()){
			lstOut.add(spw1.getVertex(k).getDouble("dValueNormalized")+","+mapY.get(k)*(dN/dTotalWeight));
		}
		
		//returning result
		return lstOut;
	}

	/**
	 * Loads means and standard deviations for each category
	 */
	private void loadStandardizedVariables(){
		
		//lst1 = list of values
		//dMean = current mean
		//dStDev = current standard deviation
		
		ArrayList<Double> lst1;
		double dMean; double dStDev;
		
		//loading map with values
		lst1 = new ArrayList<Double>();
		for(GraphVertex vtx1:spw1.getVertices()){
			lst1.add(vtx1.getDouble("dValue"));
		}
	
		//loading map of summary statistics
		dMean = mean(lst1);
		dStDev = standardDeviationP(lst1);
		
		//loading z scores and centered values
		for(GraphVertex vtx1:spw1.getVertices()){
			vtx1.put("dValueNormalized", (vtx1.getDouble("dValue")-dMean)/dStDev);
			vtx1.put("dValueMeanCentered", vtx1.getDouble("dValue")-dMean);
		}
	}

	/**
	 * Screens p-value to check if mcmc analysis is needed
	 */
	private void screenMoransIPValue(Permutation<Integer> rgp1[]){
		
		//rgdCutoffs = element i returns the number of greater values such that the probability of significant result is less than 10^-8
		
		double rgdCutoffs[];
		
		//loading cutoffs
		rgdCutoffs = new double[101];
		for(int i=0;i<=6;i++){
			rgdCutoffs[i]=Double.POSITIVE_INFINITY;
		}
		for(int i=7;i<=8;i++){
			rgdCutoffs[i]=7;
		}
		for(int i=9;i<=11;i++){
			rgdCutoffs[i]=8;
		}
		for(int i=12;i<=15;i++){
			rgdCutoffs[i]=9;
		}
		for(int i=16;i<=19;i++){
			rgdCutoffs[i]=10;
		}
		for(int i=20;i<=24;i++){
			rgdCutoffs[i]=11;
		}
		for(int i=25;i<=30;i++){
			rgdCutoffs[i]=12;
		}
		for(int i=31;i<=36;i++){
			rgdCutoffs[i]=13;
		}
		for(int i=37;i<=42;i++){
			rgdCutoffs[i]=14;
		}
		for(int i=43;i<=49;i++){
			rgdCutoffs[i]=15;
		}
		for(int i=50;i<=56;i++){
			rgdCutoffs[i]=16;
		}
		for(int i=57;i<=63;i++){
			rgdCutoffs[i]=17;
		}
		for(int i=64;i<=71;i++){
			rgdCutoffs[i]=18;
		}
		for(int i=72;i<=79;i++){
			rgdCutoffs[i]=19;
		}
		for(int i=80;i<=87;i++){
			rgdCutoffs[i]=20;
		}
		for(int i=88;i<=96;i++){
			rgdCutoffs[i]=21;
		}
		for(int i=97;i<=100;i++){
			rgdCutoffs[i]=22;
		}
		
		mrn1.dPValueMoransI=0.;
		for(int i=0;i<rgp1.length;i++){
			
			//checking randomization
			if(calculateRandomizedMoransI(rgp1[i], mrn1, false).dObsMoransI>=mrn1.dObsMoransI){
				mrn1.dPValueMoransI++;
			}
			
			//checking if probability of significant result is less than 10^-8
			if(mrn1.dPValueMoransI>=rgdCutoffs[i+1]){
				mrn1.dPValueMoransI = mrn1.dPValueMoransI/((double) (i+1));		
				mrn1.iPValueIterations=i+1;
				return;
			}
		}
		
		//non-significant result not found
		mrn1.dPValueMoransI=-9999;
	}
	
	/**
	 * Calculates p-value for moran's I by independent simulation
	 */
	private void calculateMoransIPValue(Permutation<Integer> rgp1[]){
		
		mrn1.dPValueMoransI=0.;
		for(int i=0;i<rgp1.length;i++){
			
			//checking randomization
			if(calculateRandomizedMoransI(rgp1[i], mrn1, false).dObsMoransI>=mrn1.dObsMoransI){
				mrn1.dPValueMoransI++;
			}
		}
		
		//outputting result
		mrn1.dPValueMoransI=mrn1.dPValueMoransI/((double) rgp1.length);
		mrn1.iPValueIterations=rgp1.length;
	}

	/**
	 * calculates p-value for moran's I by simulation (mcmc)
	 * @return P-value for moran's I by simulation
	 */
	private void calculateMoransIPValueMCMC(int iMCMCIterations, int iMCMCChains){
		
		//mrnRandom = current randomized moran's I
		//lstSwap = current pair of elements that were swapped
		//prmCurrent = current permutation
		//prmPrevious = previous permutation
		//dChains = number of chains to consider
		//dCount = count for current chain
		//dPValue = pvalue for current chain
		//prm1 = current initial permutation
		
		MoransI mrnRandom;
		ArrayList<Integer> lstSwap;
		double dChains; double dCount; double dPValue;
		Permutation<Integer> prm1;
		
		//loading number of chains
		dChains=(double) iMCMCChains;
		
		//initializing list of pvalues
		mrn1.lstChainPValues=new ArrayList<Double>((int) dChains);
		
		//looping through chains
		for(int j=0;j<dChains;j++){	
		
			//initializing count and pvalue
			dCount=0;
			dPValue=0;
			
			//loading permutation
			prm1 = new Permutation<Integer>(spw1.getVertexIDs());
			prm1.loadRandomPermutation();
		
			//initializing randomized moran's I
			mrnRandom = this.calculateRandomizedMoransI(prm1, mrn1, true);
		
			//looping through mcmc iterations
			for(int i=0;i<((double) iMCMCIterations)/dChains;i++){
			
				//swapping elements of permutations
				lstSwap = prm1.swapRandomElements();
				
				//updating morans I value
				nextMCMCRandomizedMoransI(prm1,mrnRandom,lstSwap);
				
				//checking if greater than observed value
				if(mrnRandom.dSXY>=mrn1.dSXY){		
					dPValue++;
				}
				
				//updating count
				dCount++;
			}
			
			//saving result
			mrn1.lstChainPValues.add(dPValue/dCount);
		}
	
		//outputting results
		mrn1.dPValueMoransI=mean(mrn1.lstChainPValues);
		mrn1.iPValueIterations=iMCMCIterations;
	}

	private void nextMCMCRandomizedMoransI(Permutation<Integer> prm1, MoransI mrnRandom, ArrayList<Integer> lstSwap){
		
		//dValue1 = current first value (mean centered)
		//dValue2 = current second value (mean centered)
		//dCrossProduct = cross product (difference)
		//iVertexCurrent = current vertex id
		//iVertexOther = other vertex id
		
		double dValue1; double dValue2; double dCrossProduct;
		int iVertexCurrent; int iVertexOther;
		
		//looping through pairs of edges
		for(int k=0;k<2;k++){
		
			//loading vertex ids
			iVertexCurrent = lstSwap.get(k);
			if(k==0){
				iVertexOther=lstSwap.get(1);
			}else{
				iVertexOther=lstSwap.get(0);
			}
			
			//looping through edges
			for(GraphEdge edg1:spw1.getEdges(lstSwap.get(k))){
				
				//checking if edge spans both vertices
				if(edg1.vtxStart.iID==iVertexCurrent && edg1.vtxEnd.iID==iVertexOther){
					continue;
				}
				if(edg1.vtxStart.iID==iVertexOther && edg1.vtxEnd.iID==iVertexCurrent){
					continue;
				}
				
				//subtracting previous cross product
				mrnRandom.dSXY-=edg1.getDouble("dCrossProduct");
				
				//loading current cross product
				dValue1=spw1.getVertex(prm1.getImage(edg1.vtxStart.iID)).getDouble("dValueMeanCentered");
				dValue2=spw1.getVertex(prm1.getImage(edg1.vtxEnd.iID)).getDouble("dValueMeanCentered");
				dCrossProduct=spw1.getWeight(edg1)*dValue1*dValue2;
				
				//updating
				edg1.put("dCrossProduct", dCrossProduct);
				mrnRandom.dSXY+=dCrossProduct;
			}
		}
		
		//loading variables with letters
		//Using R (ape package) here. Formula from ArcGIS is commented
		//dD=ElementaryMathOperations.calculateSumFourthPowers(lstValues)/Math.pow(ElementaryMathOperations.calculateSumSquares(lstValues), 2.);
		//NOTE: not updating observed value of random moran's i because this is unnecessary for comparison purposes
		//***************************
		//mrnRandom.dObsMoransI = mrnRandom.dN*mrnRandom.dSXY/(mrnRandom.dS0*mrnRandom.dSumSquares);
		//***************************
		
		//******************************
		//MoransI mrnTEMP = this.calculateRandomizedMoransI(prm1, mrnRandom, false);
		//System.out.println(mrnRandom.dObsMoransI + "," + mrnTEMP.dObsMoransI);
		//mrnRandom.dObsMoransI = mrnRandom.dN*mrnRandom.dSXY/(mrnRandom.dS0*mrnRandom.dSumSquares);
		//System.out.println(mrnRandom.dObsMoransI);
		//******************************
		
	}
	
	/**
	 * Calculates moran's I with data randomized (used for calculating permutation test based p-values)
	 * @param prm1 Permutation of vertices to use
	 * @param dSumSquares Sum of squares to use
	 * @param dS0 S_0 (see arcgis formula)
	 * @return Moran's I value
	 */
	private MoransI calculateRandomizedMoransI(Permutation<Integer> prm1, MoransI mrn1, boolean bSaveCrossProducts){
		
		//mrnOut = output
		//dValue1 = current first value
		//dValue2 = current second value
		//dWeight = current weight
		//dW2 = current transposed partial sum
		//dCrossProduct = current cross product
		
		double dValue2; double dWeight; double dValue1; double dCrossProduct;
		MoransI mrnOut;
		
		//initializing output
		mrnOut = mrn1.clone();
		
		//looping through edges
		mrnOut.dSXY=0.;
		
		for(GraphEdge edg1:spw1.getEdges()){
			
			//loading values
			dWeight = spw1.getWeight(edg1);
			
			//loading values for vertices
			dValue1=spw1.getVertex(prm1.getImage(edg1.vtxStart.iID)).getDouble("dValue");
			dValue2=spw1.getVertex(prm1.getImage(edg1.vtxEnd.iID)).getDouble("dValue");
			
			//updating values
			if(bSaveCrossProducts==true){
				dCrossProduct=dWeight*(dValue1-mrn1.dMeanValue)*(dValue2-mrn1.dMeanValue);
				edg1.put("dCrossProduct", dCrossProduct);
				mrnOut.dSXY+=dCrossProduct;
			}else{
				mrnOut.dSXY+=dWeight*(dValue1-mrn1.dMeanValue)*(dValue2-mrn1.dMeanValue);
			}
		}
		
		//loading variables with letters
		//Using R (ape package) here. Formula from ArcGIS is commented
		//dD=ElementaryMathOperations.calculateSumFourthPowers(lstValues)/Math.pow(ElementaryMathOperations.calculateSumSquares(lstValues), 2.);
		mrnOut.dObsMoransI = mrn1.dN*mrnOut.dSXY/(mrn1.dS0*mrn1.dSumSquares);
		
		return mrnOut;
	}

	/**
	 * Moran's I inner class
	 * @author jladau
	 *
	 */
	public class MoransI{
		
		//dObsMoransI = observed value of moran's i that was calculated
		//dEMoransI = expected value of moran's i
		//dVarMoransI = variance of moran's i
		//dZMoransI = z value for moran's I
		//dPValueMoransI = p-value for moran's I from simulation
		//dSumSquares = sum of squares
		//dMeanValue = mean value
		//dS0 = value of s_0
		//dN = total number of observations
		//dSXY = sum of pairs of observations (weighted)
		//lstChainPValues = list of p values for each mcmc chain
		//lstChainCounts = list of counts for each mcmc chain
		//iPValueIterations = number of iterations for p-value
		
		private double dSXY;
		private double dN;
		private double dSumSquares;
		private double dMeanValue;
		private double dS0;
		public double dObsMoransI;
		public double dEMoransI;
		public double dVarMoransI;
		public double dZMoransI;
		public double dPValueMoransI;
		public ArrayList<Double> lstChainPValues=null;
		public int iPValueIterations;
		
		//TODO write unit test
		public MoransI clone(){
			
			//mrn1 = output
			
			MoransI mrn1;
			
			mrn1 = new MoransI();
			
			mrn1.dSXY = this.dSXY;
			mrn1.dN=this.dN;
			mrn1.dSumSquares=this.dSumSquares;
			mrn1.dMeanValue=this.dMeanValue;
			mrn1.dS0=this.dS0;
			mrn1.dObsMoransI=this.dObsMoransI;
			mrn1.dEMoransI=this.dEMoransI;
			mrn1.dVarMoransI=this.dVarMoransI;
			mrn1.dZMoransI=this.dZMoransI;
			mrn1.dPValueMoransI=this.dPValueMoransI;
			mrn1.iPValueIterations=this.iPValueIterations;
			if(this.lstChainPValues!=null){
				mrn1.lstChainPValues = new ArrayList<Double>(this.lstChainPValues.size());
				for(int i=0;i<this.lstChainPValues.size();i++){
					mrn1.lstChainPValues.add(this.lstChainPValues.get(i));
				}
			}
			return mrn1;
		}
	}
}