package edu.ucsf.AllRastersInputMESS;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code generates 
 * @author jladau
 *
 */

public class AllRastersInputMESSLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//lstOut = output
		//sbl1 = output
		
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		arg1 = new ArgumentIO(rgsArgs);
		lstOut = new ArrayList<String>();
		lstOut.add("" +
				"BOOTSTRAP_ID," +
				"MODEL_ID," +
				"CLIMATOLOGY," +
				"NUMBER_PREDICTORS," +
				"CV_R2," +
				"PRESS," +
				"R2," +
				"R2_ADJUSTED," +
				"RESPONSE_VAR," +
				"RESPONSE_VAR_TRANSFORM," +
				"PREDICTORS," +
				"MODEL," +
				"TRAINING_DATA_DATE," +
				"TRAINING_DATA_VERT," +
				"CLIMATOLOGY_MIDPOINT," +
				"CLIMATOLOGY_STARTPOINT," +
				"CLIMATOLOGY_ENDPOINT," +
				"CLIMATOLOGY_LENGTH," +
				"BEST_MODEL");
		sbl1 = new StringBuilder();
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append(arg1.getValueString("sTrainingClimatology") + ",");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("ResponseVar0,");
		sbl1.append("logit,");
		sbl1.append("na,");
		sbl1.append(generateModel(arg1.getValueStringArray("rgsPredictors")));
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na,");
		sbl1.append("na");
		lstOut.add(sbl1.toString());
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static String generateModel(String[] rgsPredictors){
		
		//sbl1 = output
		
		StringBuilder sbl1;
		
		sbl1 = new StringBuilder();
		sbl1.append("(Intercept):0");
		for(int i=0;i<rgsPredictors.length;i++){
			sbl1.append(";" + rgsPredictors[i] + ":0");
		}
		return sbl1.toString();
	}
}