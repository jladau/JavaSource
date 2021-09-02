package gov.doe.jgi.EstimatorPerformance;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;

public class MetagenomeRarefactionSimulator {

	/**Total number of reads**/
	public double dN; 
	
	/**Number of reads in subsample**/
	public double dNTilde;
	
	/**Read depth necessary for assembly**/
	public double dC;
	
	/**Length of gene**/
	public double dL_i; 
	
	/**Probability of sampling gene**/
	public double dRho_i;
	
	/**Normal generator**/
	private NormalDistribution nrm1;
	
	/**MLE estimator**/
	private MetagenomeRarefactionMLE mle1;
	
	/**Step estimator**/
	private MetagenomeRarefactionStepEstimator spe1;
	
	public MetagenomeRarefactionSimulator(double dN, double dNTilde, double dC, double dL_i, double dRho_i){
		this.dN = dN;
		this.dNTilde = dNTilde;
		this.dC = dC;
		this.dL_i = dL_i;
		this.dRho_i = dRho_i;
		nrm1 = new NormalDistribution();
		mle1 = new MetagenomeRarefactionMLE(dC, dN, dL_i, dNTilde);
		spe1 = new MetagenomeRarefactionStepEstimator(dC, dN, dL_i, dNTilde);
	}
	
	public double probabilityOfAssembly(){
		return 1. - nrm1.cumulativeProbability((dC*dL_i-dNTilde*dRho_i)/(Math.sqrt(dNTilde*dRho_i*(1.-dRho_i))));
	}
	
	public ArrayList<Double> estimatesMLE(int iEstimates){
		
		//lst1 = output
		//dX_i = current normal deviate
		//dMu = mean
		//dSigma = standard deviation
		
		double dX_i;
		double dMu;
		double dSigma;
		ArrayList<Double> lst1;
		
		lst1 = new ArrayList<Double>(iEstimates);
		dMu = dN*dRho_i;
		dSigma = Math.sqrt(dN*dRho_i*(1.-dRho_i));
		for(int i=0;i<iEstimates;i++){
			dX_i = nrm1.sample()*dSigma+dMu;
			lst1.add(mle1.estimate(dX_i, nrm1));
		}
		return lst1;
	}
	
	public ArrayList<Double> estimatesStep(int iEstimates){
		
		//lst1 = output
		//dX_i = current normal deviate
		//dMu = mean
		//dSigma = standard deviation
		
		double dX_i;
		double dMu;
		double dSigma;
		ArrayList<Double> lst1;
		
		lst1 = new ArrayList<Double>(iEstimates);
		dMu = dN*dRho_i;
		dSigma = Math.sqrt(dN*dRho_i*(1.-dRho_i));
		for(int i=0;i<iEstimates;i++){
			dX_i = nrm1.sample()*dSigma+dMu;
			lst1.add(spe1.estimate(dX_i));
		}
		return lst1;
	}
}
