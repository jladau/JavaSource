package gov.lbnl.SimulateCaseFatalityRates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class SimulateCaseFatalityRatesLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = mortality database
		//lst1 = list of unique counties
		//mapBaseline = map from county ids to baseline reciprocal case fatality rates
		//mapTimeOffset = map from time point so time offsets
		//rnd1 = random number generator
		//mapOffset = addition term for given time point
		//lst2 = list of time points
		//dSlope = time-offset slope
		//rgdNorm = array of normal variates
		//d1 = current case fatality rate
		//d2 = case fatality rate without error
		//iRandomSeed = random seed
		//i1 = counter
		//sModel = model to use
		//d3 = current counter in double format
		//d4 = current baseline value
		//d5 = decrement factor
		
		double d5;
		double d3;
		double d4;
		int i1;
		int iRandomSeed;
		double d2;
		double d1;
		double dSlope;
		ArrayList<String> lst1;
		ArrayList<Double> lst2;
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<String,Double> mapBaseline;
		HashMap<Double,Double> mapTimeOffset;
		Random rnd1;
		double rgdNorm[];
		String sModel;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		dSlope = arg1.getValueDouble("dSlope");
		iRandomSeed = arg1.getValueInt("iRandomSeed");
		sModel = arg1.getValueString("sModel");
		
		//loading list of counties and list of time points
		lst1 = new ArrayList<String>(5000);
		lst2 = new ArrayList<Double>(365);
		for(int i=1;i<dat1.iRows;i++){
			if(!lst1.contains(dat1.getString(i,"COUNTY_FIPS"))){
				lst1.add(dat1.getString(i,"COUNTY_FIPS"));
			}
			if(!lst2.contains(dat1.getDouble(i,"TIME"))){
				lst2.add(dat1.getDouble(i,"TIME"));
			}
		}
		
		//loading baseline reciprocal case fatality rates
		Collections.shuffle(lst1,new Random(1234));
		mapBaseline = new HashMap<String,Double>(lst1.size());
		d4 = 0.05;
		d5 = Math.exp(Math.log(d4/0.001)/((double) lst1.size()));
		for(int i=0;i<lst1.size();i++){
			//mapBaseline.put(lst1.get(i),i + 74.);
			//mapBaseline.put(lst1.get(i),10*i + 74.);
			mapBaseline.put(lst1.get(i),1./d4);
			d4 = d4/d5;
			//mapBaseline.put(lst1.get(i),i + 20.);
		}
		
		//loading daily offset values
		Collections.sort(lst2);
		mapTimeOffset = new HashMap<Double,Double>(lst2.size());
		for(int i=0;i<lst2.size();i++){
			d3 = ((double) i);
			if(sModel.equals("linear")){
				mapTimeOffset.put(lst2.get(i), d3*dSlope/10.);
			}else if(sModel.equals("step")) {
				if(i<lst2.size()/2){
					mapTimeOffset.put(lst2.get(i), 0.);
				}else {
					mapTimeOffset.put(lst2.get(i), dSlope);
				}
			}else if(sModel.equals("quadratic")){
				mapTimeOffset.put(lst2.get(i), dSlope/250.*(-2.*(d3-30.)+0.11*(d3-30.)*(d3-30.)));
			}else if(sModel.equals("superlinear")){
				mapTimeOffset.put(lst2.get(i), 1./((300.-dSlope*3.)/d3-1.));
			}
		}
		
		//loading random offsets
		rgdNorm = ExtendedMath.normalRandomVector(0.,arg1.getValueDouble("dErrorVariance"),dat1.iRows,iRandomSeed);
		
		//appending case fatality rates
		dat1.appendToLastColumn(0,"CASE_FATALITY_RATE");
		i1  = 0;
		for(int i=1;i<dat1.iRows;i++){
			d1 = mapBaseline.get(dat1.getString(i,"COUNTY_FIPS"));
			if(sModel.equals("superlinear")){
				d1 += mapBaseline.get(dat1.getString(i,"COUNTY_FIPS"))*mapTimeOffset.get(dat1.getDouble(i,"TIME"));
				if(d1<=0){
					d1 = 10000000;
				}
			}else{
				d1 += mapTimeOffset.get(dat1.getDouble(i,"TIME"));
				if(d1<1){
					d1 = 1;
				}
			}
			d1 += rgdNorm[i];
			if(d1<=1){
				d2 = d1 - rgdNorm[i];
				do{
					d1 = d2;
					d1 += ExtendedMath.normalRandomVector(0.,arg1.getValueDouble("dErrorVariance"),1,iRandomSeed + i1*7+13)[0];
					i1++;
				}while(d1<=1);
			}
			dat1.appendToLastColumn(i, 1./d1);
		}
		
		//outputting results
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");		
	}
}