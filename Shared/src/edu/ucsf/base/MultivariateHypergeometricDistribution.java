package edu.ucsf.base;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.HypergeometricDistribution;

public class MultivariateHypergeometricDistribution {

	/**List of categories**/
	private ArrayList<Integer> lstCategories;
	
	/**Population size**/
	private int iPopulationSize;
	
	public MultivariateHypergeometricDistribution(ArrayList<Integer> lstCategories){
		this.lstCategories = lstCategories;
		this.iPopulationSize=0;
		for(int i:lstCategories){
			iPopulationSize+=i;
		}
	}
	
	public ArrayList<Integer> sample(int iSampleSize){
		
		//hpr1 = hypergeometric distribution
		//lst2 = output
		//iNumberOfSuccesses = number of successes
		//i1 = current variate
		//i2 = current sample size
		//i3 = current population size
		
		ArrayList<Integer> lst2;
		HypergeometricDistribution hpr1;
		int i1;
		int i2;
		int i3;
		int iNumberOfSuccesses;
		
		//*********************
		//if(iSampleSize==iPopulationSize){
		//	System.out.println("HERE");
		//	return lstCategories;
		//}
		//*********************
		
		lst2 = new ArrayList<Integer>(lstCategories.size());
		i2 = iSampleSize;
		i3 = iPopulationSize;
		for(int i=0;i<lstCategories.size();i++){
			iNumberOfSuccesses = lstCategories.get(i); 
			if(i2>0){
				hpr1 = new HypergeometricDistribution(i3, iNumberOfSuccesses, i2);
				i1 = hpr1.sample();
			}else{
				i1 = 0;
			}
			lst2.add(i1);
			i2-=i1;
			i3-=iNumberOfSuccesses;
		}
		return lst2;
	}
	
	public ArrayList<Double> mean(int iSampleSize){
		
		//lst2 = output
		//iNumberOfSuccesses = number of successes
		
		ArrayList<Double> lst2;
		int iNumberOfSuccesses;
		
		lst2 = new ArrayList<Double>(lstCategories.size());
		for(int i=0;i<lstCategories.size();i++){
			iNumberOfSuccesses = lstCategories.get(i);
			lst2.add(((double) iSampleSize)*((double) iNumberOfSuccesses)/((double) iPopulationSize));
		}
		return lst2;
	}
}
