package edu.ucsf.InequalitySignificanceTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class InequalitySignificanceTestLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sX = x header
		//sY = y header
		//lstX = list of x values
		//lstY = list of y values
		//lstYShuffle = y shuffled values
		//rgdXRange = range of x values
		//rgdYRange = range of y values
		//lstOut = output
		//iSteps = number of steps
		//iIterations = number of iterations
		//lstInequalities = list of inequalities
		//dXStep = x step size
		//dYStep = y step size
		//lstPoints = list of candidate start/end points
		//mapYShuffle = map from indices to Y shuffled values
		//lst1 = lst of line values
		//dSES = current ses
		//rgi1 = indices of minimum and maximum standardized effect sizes
		//rgd1 = values of minimum and maximum standardized effect sizes
		//dGTE = greater than or equal to probability
		//dLTE = less than or equal to probability
		
		double dGTE;
		double dLTE;
		double dSES;
		ArrayList<Double[]> lst1;
		double dXStep;
		double dYStep;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		String sX;
		String sY;
		ArrayList<Double> lstX;
		ArrayList<Double> lstY;
		ArrayList<Double> lstYShuffle;
		double rgdXRange[];
		double rgdYRange[];
		ArrayList<Double[]> lstPoints;
		int iSteps;
		int iIterations;
		ArrayList<Inequality> lstInequalities;
		HashMap<Integer,ArrayList<Double>> mapYShuffle;
		int[] rgi1;
		double[] rgd1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sX = arg1.getValueString("sXHeader");
		sY = arg1.getValueString("sYHeader");
		lstX = dat1.getDoubleColumn(sX);
		lstY = dat1.getDoubleColumn(sY);
		iIterations = arg1.getValueInt("iIterations");
		
		//****************************
		//Collections.shuffle(lstY);
		//****************************
		
		//loading ranges
		rgdXRange = new double[]{ExtendedMath.minimum(lstX), ExtendedMath.maximum(lstX)};
		rgdYRange = new double[]{ExtendedMath.minimum(lstY), ExtendedMath.maximum(lstY)};
		
		//loading possible start/end points
		iSteps = 10;
		dXStep = (rgdXRange[1]-rgdXRange[0])/((double) iSteps);
		dYStep = (rgdYRange[1]-rgdYRange[0])/((double) iSteps);
		lstPoints = new ArrayList<Double[]>(4*iSteps);
		for(double dX=rgdXRange[0];dX<=rgdXRange[1];dX+=dXStep){
			lstPoints.add(new Double[]{dX,rgdYRange[0]});
			lstPoints.add(new Double[]{dX,rgdYRange[1]});
		}
		for(double dY=rgdYRange[0];dY<=rgdYRange[1];dY+=dYStep){
			lstPoints.add(new Double[]{rgdXRange[0],dY});
			lstPoints.add(new Double[]{rgdXRange[1],dY});
		}
		
		//loading inequalities
		lstInequalities = new ArrayList<Inequality>(lstPoints.size()*lstPoints.size());
		for(Double[] rgdStart:lstPoints) {
			for(Double[] rgdEnd:lstPoints) {
				if(rgdStart[0]!=rgdEnd[0] && rgdStart[1]!=rgdEnd[1]){
					lstInequalities.add(new Inequality(rgdStart[0],rgdStart[1],rgdEnd[0],rgdEnd[1]));
				}
			}	
		}
		
		//loading shuffled values
		mapYShuffle = new HashMap<Integer,ArrayList<Double>>(iIterations);
		lstYShuffle = new ArrayList<Double>(lstY);
		for(int i=0;i<iIterations;i++){
			Collections.shuffle(lstYShuffle);
			mapYShuffle.put(i,new ArrayList<Double>(lstYShuffle));
		}
		
		//Finding standardized effect sizes
		for(int i=0;i<lstInequalities.size();i++) {
			
			System.out.println("Analyzing inequality " + i + " of " + lstInequalities.size() + "...");
			
			//loading statistics
			lstInequalities.get(i).countPoints(lstX,lstY,"observed");
			for(Integer j:mapYShuffle.keySet()){
				lstInequalities.get(i).countPoints(lstX,mapYShuffle.get(j),"null");
			}
		}
		
		//Finding inequality with greatest and least standardized effect size
		rgd1 = new double[]{Double.MAX_VALUE,-Double.MAX_VALUE};
		rgi1 = new int[2];
		for(int i=0;i<lstInequalities.size();i++) {
			if(!Double.isNaN(lstInequalities.get(i).ses())){
				dSES = lstInequalities.get(i).ses();
				if(dSES<rgd1[0]) {
					rgd1[0]=dSES;
					rgi1[0]=i;
				}
				if(dSES>rgd1[1]) {
					rgd1[1]=dSES;
					rgi1[1]=i;
				}	
			}
		}
		
		/*
		//Finding inequality with smallest gte and lte p-values
		rgd1 = new double[]{Double.MAX_VALUE,Double.MAX_VALUE};
		rgi1 = new int[2];
		for(int i=0;i<lstInequalities.size();i++) {
			if(!Double.isNaN(lstInequalities.get(i).ses())){
				dGTE = lstInequalities.get(i).dProbabilityGTE();
				dLTE = lstInequalities.get(i).dProbabilityLTE();
				if(dLTE<rgd1[0]) {
					rgd1[0]=dLTE;
					rgi1[0]=i;
				}
				if(dGTE<rgd1[1]) {
					rgd1[1]=dGTE;
					rgi1[1]=i;
				}	
			}
		}
		*/
		
		//Outputting inequalities
		lstOut = new ArrayList<String>(lstInequalities.size()+1);
		lstOut.add("X,"
				+ "Y,"
				+ "SES,"
				+ "PROBABILITY_GTE,"
				+ "PROBABILITY_LTE");
		for(int i=0;i<=1;i++){
			lst1 = lstInequalities.get(rgi1[i]).lineValues(50);
			dSES = lstInequalities.get(rgi1[i]).ses();
			dGTE = lstInequalities.get(rgi1[i]).dProbabilityGTE();
			dLTE = lstInequalities.get(rgi1[i]).dProbabilityLTE();
			for(int k=0;k<lst1.size();k++){
				lstOut.add(lst1.get(k)[0] + "," + lst1.get(k)[1] + "," + dSES + "," + dGTE + "," + dLTE);
			}
		}		
				
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}