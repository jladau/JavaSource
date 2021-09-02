package gov.doe.jgi.Test;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.HypergeometricDistribution;

public class TestLauncher {

	public static void main(String rgsArgs[]){
		
		//hpr1 = hypergeometric distribution
		//lst1 = list of gene read counts
		//lst2 = output
		//i1 = current variate
		
		ArrayList<Integer> lst1;
		ArrayList<Integer> lst2;
		int iPopulationSize;
		int iNumberOfSuccesses; 
		int iSampleSize;
		HypergeometricDistribution hpr1;
		int i1;
		
		lst1 = new ArrayList<Integer>(1000);
		iPopulationSize=0;
		for(int i=1000;i>0;i--){
			lst1.add(i*1000);
			iPopulationSize+=i*1000;
		}
		System.out.println("Population size: " + iPopulationSize);
		iSampleSize=(int) (0.01*iPopulationSize);
		System.out.println("Sample size: " + iSampleSize);
		lst2 = new ArrayList<Integer>(lst1.size());
		for(int i=0;i<lst1.size();i++){
			iNumberOfSuccesses = lst1.get(i); 
			if(iSampleSize>0){
				hpr1 = new HypergeometricDistribution(iPopulationSize, iNumberOfSuccesses, iSampleSize);
				i1 = hpr1.sample();
			}else{
				i1 = 0;
			}
			lst2.add(i1);
			iSampleSize-=i1;
			iPopulationSize-=iNumberOfSuccesses;
		}
		
		/*
		iPopulationSize = 10000000;
		iNumberOfSuccesses = 500000; 
		iSampleSize = 1000000;
		hpr1 = new HypergeometricDistribution(iPopulationSize, iNumberOfSuccesses, iSampleSize);
		System.out.println(hpr1.sample());
		*/
		
		System.out.println("Done.");
		
		
		
	}
	
	
}
