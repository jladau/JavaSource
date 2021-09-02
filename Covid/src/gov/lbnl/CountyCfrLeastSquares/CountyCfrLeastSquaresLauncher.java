package gov.lbnl.CountyCfrLeastSquares;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.ArrayListMultimap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class CountyCfrLeastSquaresLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//fcn1 = objective function
		//mapObs = observed values of f_sigma
		//mapF = parameter estimates
		//lstOut = output
		//lstTimes = sorted list of times
		//mapFittedList = fitted values
		//mapFitted = fitted values
		
		ArgumentIO arg1;
		DataIO dat1;
		CfrLeastSquaresObjectiveFunction fcn1;
		HashMap<Double,Double> mapObs;
		HashMap<String,Double> mapF;
		ArrayList<String> lstOut;
		ArrayList<Double> lstTimes;
		ArrayListMultimap <Double,Double> mapFittedList;
		HashMap<Double,Double> mapFitted;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		fcn1 = new CfrLeastSquaresObjectiveFunction(dat1);
		lstTimes = fcn1.times();
		mapFittedList = ArrayListMultimap.create(dat1.iRows,10);
		//for(int i=0;i<lstTimes.size();i++){
		//	if(fcn1.leastSquares(lstTimes.get(i),lstTimes.get(i) + 154.)==true){
		//		mapFitted = fcn1.fittedValues();
		//		for(Double d:mapFitted.keySet()){
		//			mapFittedList.put(d,mapFitted.get(d));
		//		}
		//		HashMap<String,Double> mapTEMP = fcn1.parameterEstimates();
		//		for(String s:mapTEMP.keySet()){
		//			System.out.println(s + "," + mapTEMP.get(s));
		//		}
		//	}
		//}
		
		if(fcn1.leastSquares()==true){
			mapFitted = fcn1.fittedValues();
			for(Double d:mapFitted.keySet()){
				mapFittedList.put(d,mapFitted.get(d));
			}
			HashMap<String,Double> mapTEMP = fcn1.parameterEstimates();
			for(String s:mapTEMP.keySet()){
				System.out.println(s + "," + mapTEMP.get(s));
			}
		}
		
		
		
		//finding average fitted values
		mapFitted = new HashMap<Double,Double>(mapFittedList.size());
		for(Double d:mapFittedList.keySet()){
			mapFitted.put(d,ExtendedMath.mean(new ArrayList<Double>(mapFittedList.get(d))));
		}
		
		//finding observed values
		mapObs = fcn1.observedValues();
		
		//*****************************
		//for(Double d:mapFitted.keySet()){
		//	System.out.println(d + "," + mapObs.get(d) + "," + mapFitted.get(d));
		//}
		//*****************************
		
		
		
		
		/*

		//finding least squares solution
		mapObs = fcn1.observedValues();
		mapFitted = fcn1.fittedValues();
		mapF = fcn1.parameterEstimates();
		
		//outputting fits
		lstOut = new ArrayList<String>(mapFitted.size() + 1);
		lstOut.add("TIME_T,F_SIGMA_OBSERVED,F_SIGMA_FIT");
		for(Double t:mapFitted.keySet()) {
			lstOut.add(t + "," + mapObs.get(t) + "," + mapFitted.get(t));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));

		//outputting parameter estimates
		lstOut = new ArrayList<String>(mapF.size() + 1);
		lstOut.add("PARAMETER,ESTIMATE");
		for(String s:mapF.keySet()){
			lstOut.add("f_" + s + "," + mapF.get(s));
			
			//*************************
			System.out.println("f_" + s + "," + 1./mapF.get(s));
			//*************************
			
			
			
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace(".csv","-parameter-estimates.csv"));
		
		//terminating
		System.out.println("Done.");
		
		*/

	}
}
