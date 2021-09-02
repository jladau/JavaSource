package edu.ucsf.ValidationData;

import java.util.ArrayList;
import java.util.Random;
import static java.lang.Math.*;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Generates validation data for spatial and temporal grain analyses
 * @author jladau
 */
public class ValidationDataLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//lstPred = x, y, and predictor values
		//lstResp = x, y, and response values
		//lstPredCoarsegrained = coarse grained predictors
		//lstOut = output
		//dGrainSize = grain size
		//lstR = response variables
		//lstP = current predictor variables
		
		ArrayList<Double> lstR;
		ArrayList<Double> lstP;
		double dGrainSize;
		ArgumentIO arg1;
		ArrayList<Double[]> lstPred;
		ArrayList<Double[]> lstResp;
		ArrayList<Double[]> lstPredCoarseGrained;
		ArrayList<String> lstOut;
		 
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		
		//generating predictors
		lstPred = generatePredictors();
		
		//generating response variables
		dGrainSize=arg1.getValueDouble("dGrainSize");
		if(dGrainSize<0.00000001){
			dGrainSize=0.000001;
		}
		lstPredCoarseGrained = coarseGrain(lstPred, dGrainSize);
		lstResp = generateResponses(lstPredCoarseGrained, arg1.getValueDouble("dNoise"));
		
		//loading array of response variables (for calculating correlations)
		lstR = new ArrayList<Double>(lstResp.size());
		for(Double[] rgd1:lstResp){
			lstR.add(rgd1[2]);
		}
		
		//initializing output
		lstOut = new ArrayList<String>(lstResp.size()*4);
		lstOut.add("X,Y,VARIABLE,GRAIN,VALUE");
		for(Double[] rgd1:lstResp){
			lstOut.add(rgd1[0] + "," + rgd1[1] + ",response,na," + rgd1[2]);
		}
		for(double d:arg1.getValueDoubleArray("rgdGrainsToCheck")){
			
			if(d<0.00000001){
				d=0.000001;
			}
			lstPredCoarseGrained = coarseGrain(lstPred, d);
			lstP = new ArrayList<Double>(lstPredCoarseGrained.size());
			for(Double[] rgd1:lstPredCoarseGrained){
				lstOut.add(rgd1[0] + "," + rgd1[1] + ",predictor," + d + "," + rgd1[2]);
				lstP.add(rgd1[2]);
			}
			lstOut.add("na,na,pearson," + d + "," + ExtendedMath.pearson(lstR, lstP));
		}
			
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static ArrayList<Double[]> coarseGrain(ArrayList<Double[]> lstPred, double dRadius){
		
		//lstOut = output; x, y, coarse-grained valued
		//dRadius2 = radius squared
		//lst1 = list of current predictors to be averaged
		
		ArrayList<Double> lst1;
		ArrayList<Double[]> lstOut;
		double dRadius2;
		
		//initializing variables
		lstOut = new ArrayList<Double[]>(lstPred.size());
		dRadius2 = dRadius*dRadius;
		
		//looping through points for response variables
		for(double dX=0;dX<=1;dX+=0.025){
			for(double dY=0;dY<=1;dY+=0.025){
				
				//finding neighborhood mean for predictors
				lst1 = new ArrayList<Double>(500);
				for(Double[] rgd1:lstPred){
					if(pow(rgd1[0]-dX,2)+pow(rgd1[1]-dY,2)<dRadius2){
						lst1.add(rgd1[2]);
					}
				}
				
				//outputting value
				lstOut.add(new Double[]{dX, dY, ExtendedMath.mean(lst1)});
			}
		}
		
		//returning result
		return lstOut;
	}
	
	private static ArrayList<Double[]> generateResponses(ArrayList<Double[]> lstPredCoarseGrained, double dNoise){
		
		//lstResp = output; x, y, response value
		//rnd1 = random number generator
		
		ArrayList<Double[]> lstResp;
		Random rnd1;
		
		//initializing variables
		rnd1 = new Random(12345);
		lstResp = new ArrayList<Double[]>(10000);
		
		//looping through points for response variables
		for(Double rgd1[]:lstPredCoarseGrained){
	
			//outputting value
			lstResp.add(new Double[]{rgd1[0], rgd1[1], 3.*rgd1[2] + 1.5 + (dNoise*rnd1.nextDouble())-dNoise/2.});
		}
		
		//returning result
		return lstResp;
	}
	
	private static ArrayList<Double[]> generatePredictors(){
		
		//lstPred = output; x, y, predicted value
		//rnd1 = random number generator
		
		ArrayList<Double[]> lstPred;
		Random rnd1;
		
		rnd1 = new Random(1234);
		lstPred = new ArrayList<Double[]>(10000);
		for(double dX=0;dX<=1;dX+=0.025){
			for(double dY=0;dY<=1;dY+=0.025){
				lstPred.add(new Double[]{dX, dY, dX + dY + 0.4*rnd1.nextDouble()});
			}
		}
		return lstPred;
		
		
	}
	
}
