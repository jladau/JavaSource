package gov.lbnl.FindInvariantIntervals;

import java.util.ArrayList;
import java.util.Collections;
import com.google.common.collect.ArrayListMultimap;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class FindInvariantIntervalsLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data (sorted by region id and time)
		//d1 = period length
		//i1 = minimum number of valid observations
		//map1 = map from region ids to lists of values
		//map2 = map from region ids to lists of time points
		//lst1 = current list of all values
		//lst2 = current list of all time points
		//lst3 = current sublist of values
		//lst4 = current sublist of time points
		//lst5 = list of coefficients of variation
		//map3 = coefficients of variation to means
		//dMean = mean
		//dStDev = standard deviation
		//lst6 = current list of means
		//lstOut = output
		//dMaximumValue = maximum value
		
		double dMaximumValue;
		ArgumentIO arg1;
		DataIO dat1;
		double d1;
		double dMean;
		double dStDev;
		int i1;
		ArrayListMultimap<String,Double> map1;
		ArrayListMultimap<String,Double> map2;
		ArrayListMultimap<Double,Double> map3;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		ArrayList<Double> lst4;
		ArrayList<Double> lst5;
		ArrayList<Double> lst6;
		ArrayList<String> lstOut;
		
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		d1 = arg1.getValueDouble("dPeriodLength");
		i1 = arg1.getValueInt("iMinimumValidObservations");
		lstOut = new ArrayList<String>();
		lstOut.add("REGION_ID,COEFFICIENT_ESTIMATE");
		dMaximumValue = arg1.getValueDouble("dMaximumValue");
		
		//loading data
		map1 = ArrayListMultimap.create(300,300);
		map2 = ArrayListMultimap.create(300,300);
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getDouble(i,"COEFFICIENT_ESTIMATE")<dMaximumValue){
				map1.put(dat1.getString(i,"REGION_ID"),dat1.getDouble(i,"COEFFICIENT_ESTIMATE"));
				map2.put(dat1.getString(i,"REGION_ID"),dat1.getDouble(i,"TIME"));
			}
		}
		
		//finding coefficients of variation
		for(String s:map1.keySet()){
			lst1 = new ArrayList<Double>(map1.get(s));
			lst2 = new ArrayList<Double>(map2.get(s));
			lst5 = new ArrayList<Double>(lst1.size());
			map3 = ArrayListMultimap.create();
			for(int i=0;i<lst1.size()-i1+1;i++) {
				lst3 = new ArrayList<Double>(lst1.subList(i,i+i1-1));
				lst4 = new ArrayList<Double>(lst2.subList(i,i+i1-1));
				if(lst4.get(lst4.size()-1)-lst4.get(0)<=d1){
					dMean = ExtendedMath.mean(lst3);
					dStDev = ExtendedMath.standardDeviationP(lst3);
					map3.put(dStDev/dMean, dMean);
					if(!lst5.contains(dStDev/dMean)){	
						lst5.add(dStDev/dMean);
					}
				}
			}
			Collections.sort(lst5);
			lst6 = new ArrayList<Double>(100);
			for(int i=0;i<lst5.size();i++){
				for(double d:map3.get(lst5.get(i))){
					lst6.add(d);
					if(lst6.size()>4){
						break;
					}
				}
				if(lst6.size()>4){
					break;
				}
			}
			if(lst6.size()>0){
				lstOut.add(s + "," + ExtendedMath.mean(lst6));
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}