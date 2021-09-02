package edu.ucsf.LogisticRegressionSimulation;

import java.util.Random;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class LogisticRegressionSimulationLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//dBPred = coefficient for predecessors
		//dBNotPred = coefficient for not predecessors
		//dBInter = intercept
		//rnd1 = random number generator
		
		ArgumentIO arg1;
		DataIO dat1;
		double dBPred;
		double dBNotPred;
		double dBInter;
		Random rnd1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		dBPred = 1.5;
		dBNotPred = 0;
		dBInter = -9;
		rnd1 = new Random(1234);
		
		//loading results
		dat1.appendToLastColumn(0, "EVOLUTION_LOGISTIC_SIM");
		for(int i=1;i<dat1.iRows;i++){
			dat1.appendToLastColumn(i, logisticVariate(
					dat1.getDouble(i, "PREDECESSOR_COUNT"),
					dat1.getDouble(i, "NOTPREDECESSOR_COUNT"), 
					dBPred, 
					dBNotPred, 
					dBInter, 
					rnd1));
			//dat1.appendToLastColumn(i, polynomialVariate(dat1.getDouble(i, "PREDECESSOR_COUNT"),rnd1));
		}
		
		//outputting results
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static int polynomialVariate(double dPred, Random rnd1){
		
		//d1 = probability
		
		double d1;
		
		d1 = Math.pow(dPred/12,5);
		if(rnd1.nextDouble()<d1){
			return 1;
		}else{
			return 0;
		}
	}
	
	private static int logisticVariate(double dPred, double dNotPred, double dBPred, double dBNotPred, double dBInter, Random rnd1){
		
		//d1 = probability
		
		double d1;
		
		//d1 = 1./(1. + Math.exp(-(dBInter + dBPred*dPred + dBNotPred*dNotPred + rnd1.nextDouble())));
		d1 = 1./(1. + Math.exp(-(dBInter + dBPred*dPred + dBNotPred*dNotPred)));
		if(rnd1.nextDouble()<d1){
			return 1;
		}else{
			return 0;
		}
	}	
}