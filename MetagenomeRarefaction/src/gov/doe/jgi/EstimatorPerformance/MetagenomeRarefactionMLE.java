package gov.doe.jgi.EstimatorPerformance;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Maximum likelihood estimator for probability of a gene being assembled.
 * @author jladau
 */


public class MetagenomeRarefactionMLE {

	/**Read depth necessary for assembly**/
	public double dC;
	
	/**Total number of reads**/
	public double dN; 
	
	/**Length of gene**/
	public double dL_i; 
	
	/**Number of reads in subsample**/
	public double dNTilde;
	
	public MetagenomeRarefactionMLE(double dC, double dN, double dL_i, double dNTilde){
		this.dC = dC;
		this.dN = dN;
		this.dL_i = dL_i;
		this.dNTilde = dNTilde;
	}
	
	public double estimate(double dX_i, NormalDistribution nrm1){
		return 1. - nrm1.cumulativeProbability((dN*dC*dL_i-dNTilde*dX_i)/(Math.sqrt(dNTilde*dX_i*(dN-dX_i))));
	}
}
