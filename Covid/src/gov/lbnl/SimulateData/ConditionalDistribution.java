package gov.lbnl.SimulateData;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class ConditionalDistribution{
	
	/**Simulate data server**/
	public SimulateDataServer sds1;
	
	public ConditionalDistribution(SimulateDataServer sds1) {
		this.sds1 = sds1;
		for(int i=0;i<sds1.iIterations;i++) {
			next();
		}
	}
	
	private void next(){
		
		//i1 = first index being swapped
		//i2 = second index being swapped
		//df0 = joint likelihood of initial arrangement
		//df1 = joint likelihood of proposed arrangement
		//bAccept = flag for whether to accept swap
		//d1 = first value being swapped
		//d2 = second value being swapped
		//s1 = first sample
		//s2 = second sample
		
		String s1;
		String s2;
		int i1;
		int i2;
		double df0;
		double df1;
		boolean bAccept;
		double d1;
		double d2;
		
		i1 = (int) Math.floor(sds1.rnd1.nextDouble()*sds1.dRows);
		do {
			i2 = (int) Math.floor(sds1.rnd1.nextDouble()*sds1.dRows);
		}while(i2==i1);
		s1 = sds1.lstSamples.get(i1);
		s2 = sds1.lstSamples.get(i2);
		
		df0 = density(sds1.mapX.get(s1),sds1.mapY.get(s1))*density(sds1.mapX.get(s2),sds1.mapY.get(s2));
		df1 = density(sds1.mapX.get(s1),sds1.mapY.get(s2))*density(sds1.mapX.get(s2),sds1.mapY.get(s1));
		bAccept = false;
		if(df0==0 && df1>0) {
			bAccept = true;
		}else if(df0>0) {
			if(sds1.rnd1.nextDouble()<df1/df0){
				bAccept = true;
			}
		}
		
		if(bAccept == true) {
			d1 = sds1.mapY.get(s1);
			d2 = sds1.mapY.get(s2);
			sds1.mapY.put(s1,d2);
			sds1.mapY.put(s2,d1);
		}
	}
	
	private double density(double dX, double dY) {
	
		//d1 = value of bound
		
		double d1;
		
		d1 = sds1.dIntercept + dX*sds1.dSlope;
		if(dY>d1) {
			return 0;
		}else {
			return 1/d1;
		}
	}
	
	public ArrayList<String> print(){
		
		//lst1 = output
		//pct1 = percentile object
		//lst2 = list of values less than current bound
		//lst3 = list of sorted y-values
		//rgd1 = values less than current bound (for percentile)
		//d1 = current bound
		//dPercentile = current percentile
		
		Percentile pct1;
		ArrayList<String> lst1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		double rgd1[];
		double d1;
		double dPercentile;
		
		lst1 = new ArrayList<String>(sds1.mapX.size() + 1);
		lst1.add("SAMPLE_ID,BETA_DIVERSITY,BOUND,RESPONSE_OBSERVED,RESPONSE_SIMULATED,POPULATION_75_QUANTILE");
		pct1 = new Percentile(75.);
		lst3 = new ArrayList<Double>(sds1.mapY.values());
		Collections.sort(lst3);
		for(String s:sds1.mapX.keySet()){
			lst2 = new ArrayList<Double>(lst3.size());
			d1 = sds1.mapX.get(s)*sds1.dSlope + sds1.dIntercept;
			for(int k=0;k<lst3.size();k++){
				if(lst3.get(k)<=d1) {
					lst2.add(lst3.get(k));
				}else {
					break;
				}
			}
			rgd1 = new double[lst2.size()];
			for(int k=0;k<lst2.size();k++) {
				rgd1[k] = lst2.get(k);
			}
			dPercentile = pct1.evaluate(rgd1);
			lst1.add(s + "," + sds1.mapX.get(s) + "," + d1 + "," + sds1.mapY0.get(s) + "," + sds1.mapY.get(s) + "," + dPercentile);
		}
		return lst1;
	}
}
