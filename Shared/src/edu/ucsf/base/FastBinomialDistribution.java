package edu.ucsf.base;

import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

public class FastBinomialDistribution {

	/**Number of trials**/
	private int n;
	
	/**Probability of success**/
	private double p;
	
	/**Random number generator**/
	private Random rnd1;
	
	/**Flag for whether 1-p is being used**/
	private boolean bGreaterThanHalf;
	
	/**Normal approximation, if appropriate**/
	NormalDistribution nrm1;
	
	public FastBinomialDistribution(int iN, double dP, int iRandomSeed){
		
		//dN = n in double format
		
		double dN;
		
		this.n = iN;
		if(dP<0.5){	
			this.p = dP;
			bGreaterThanHalf=false;
		}else{
			this.p = 1-dP;
			bGreaterThanHalf=true;
		}
		rnd1 = new Random(iRandomSeed);
		dN = (double) iN;
		if(dN*p>9 && dN*(1.-p)>9){
			nrm1 = new NormalDistribution(dN*p, Math.sqrt(dN*p*(1.-p)));
			nrm1.reseedRandomGenerator(rnd1.nextLong());
		}else{
			nrm1 = null;			
		}
	}
	
	public FastBinomialDistribution(int iN, double dP, Random rnd1){
		
		//dN = n in double format
		
		double dN;
		
		this.n = iN;
		if(dP<0.5){	
			this.p = dP;
			bGreaterThanHalf=false;
		}else{
			this.p = 1-dP;
			bGreaterThanHalf=true;
		}
		this.rnd1 = new Random(rnd1.nextInt());
		dN = (double) iN;
		if(dN*p>9 && dN*(1.-p)>9){
			nrm1 = new NormalDistribution(dN*p, Math.sqrt(dN*p*(1.-p)));
			nrm1.reseedRandomGenerator(rnd1.nextLong());
		}else{
			nrm1 = null;			
		}
	}
	
	public boolean hasNormalApproximation(){
		if(nrm1==null){
			return false;
		}else{
			return true;
		}
	}
	
	public double minimumValue(){
		if(hasNormalApproximation()){
			return nrm1.getMean()-3.*nrm1.getStandardDeviation();
		}else{
			return Double.NaN;
		}
	}
	
	public int sample(){
		
		double U;
		//double c;
		double logc;
		int i;
		double logpr;
		double F;
		
		if(p==0 && bGreaterThanHalf==false){
			return 0;
		}
		if(p==0 && bGreaterThanHalf==true){
			return n;
		}
		
		if(nrm1!=null){
			i = (int) Math.floor(nrm1.sample());
			if(i<0){
				i=0;
			}
			if(i>n){
				i=n;
			}
		}else{
			
			U = rnd1.nextDouble();
			//c = p/(1-p);
			logc = Math.log(p/(1-p));
			i = 0;
			logpr = n*Math.log(1-p);
			//pr = Math.pow(1-p,(double) n); 
			F = Math.exp(logpr);
			//F = pr;
			if(U<F){
				if(bGreaterThanHalf==false){	
					return i;
				}else{
					return n-i;
				}
			}
			do{
				logpr = logpr + logc + Math.log(((double) (n-i))/((double) (i+1)));
				//pr = pr*c*((double) (n-i))/((double) (i+1));
				F += Math.exp(logpr);
				i++;
			}while(U>=F);
		}
			
		if(bGreaterThanHalf==false){	
			return i;
		}else{
			return n-i;
		}
	}
}