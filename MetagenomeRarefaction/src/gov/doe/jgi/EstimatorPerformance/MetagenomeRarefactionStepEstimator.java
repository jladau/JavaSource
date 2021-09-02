package gov.doe.jgi.EstimatorPerformance;

/**
 * Step function estimator for probability of a gene being assembled.
 * @author jladau
 */

public class MetagenomeRarefactionStepEstimator {

	/**Read depth necessary for assembly**/
	public double dC;
	
	/**Total number of reads**/
	public double dN; 
	
	/**Length of gene**/
	public double dL_i; 
	
	/**Number of reads in subsample**/
	public double dNTilde;

	public MetagenomeRarefactionStepEstimator(double dC, double dN, double dL_i, double dNTilde){
		this.dC = dC;
		this.dN = dN;
		this.dL_i = dL_i;
		this.dNTilde = dNTilde;
	}
	
	public double estimate(double dX_i){
		
		if(dX_i/dN < dC*dL_i/dNTilde){
			return 0;
		}else{
			return 1;
		}
	}
}
