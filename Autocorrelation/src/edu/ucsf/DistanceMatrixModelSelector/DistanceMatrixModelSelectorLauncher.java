package edu.ucsf.DistanceMatrixModelSelector;

import java.util.ArrayList;

import edu.ucsf.base.DistanceMatrixModelSelection;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Selects best model using cross validation for a distance matrix
 * @author jladau
 */

public class DistanceMatrixModelSelectorLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//dms1 = distance matrix model selection object
		//lst1 = output
		
		ArgumentIO arg1;
		DataIO dat1;
		DistanceMatrixModelSelection dms1;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//running analysis
		dms1 = new DistanceMatrixModelSelection(
				dat1, 
				arg1.getValueString("sVertexField1"), 
				arg1.getValueString("sVertexField2"), 
				arg1.getValueStringArray("rgsCandidatePredictors"), 
				arg1.getValueString("sResponse"),
				arg1.getValueString("sPriorityPredictor"));
		
		//outputting results
		lstOut = dms1.print();
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}