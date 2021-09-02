package gov.lbnl.SimulateData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class UnconditionalDistribution{

	/**Simulate data server**/
	public SimulateDataServer sds1;
	
	public UnconditionalDistribution(SimulateDataServer sds1) {
		this.sds1 = sds1;
		simulateData();
	}
	
	private void simulateData(){

		sds1.mapY = new HashMap<String,Double>(sds1.mapX.size());
		for(String s:sds1.mapX.keySet()) {
			sds1.mapY.put(s,randomVariate(sds1.mapX.get(s)));
		}
		
	}
	
	private double randomVariate(double dX) {
	
		//d1 = value of bound
		
		double d1;
		
		d1 = sds1.dIntercept + dX*sds1.dSlope;
		return sds1.rnd1.nextDouble()*d1;
		
		
	}
	
	public ArrayList<String> print(){
		
		//lst1 = output
		//pct1 = percentile object
		//lst2 = list of values less than current bound
		//lst3 = list of sorted y-values
		//rgd1 = values less than current bound (for percentile)
		//d1 = current bound
		
		ArrayList<String> lst1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		double rgd1[];
		double d1;
		double dPercentile;
		
		lst1 = new ArrayList<String>(sds1.mapX.size() + 1);
		lst1.add("SAMPLE_ID,BETA_DIVERSITY,BOUND,RESPONSE_OBSERVED,RESPONSE_SIMULATED,POPULATION_75_QUANTILE");
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
			dPercentile = 0.75*d1;
			lst1.add(s + "," + sds1.mapX.get(s) + "," + d1 + "," + sds1.mapY0.get(s) + "," + sds1.mapY.get(s) + "," + dPercentile);
		}
		return lst1;
	}
}
