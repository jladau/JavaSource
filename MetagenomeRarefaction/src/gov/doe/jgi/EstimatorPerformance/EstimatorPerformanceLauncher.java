package gov.doe.jgi.EstimatorPerformance;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class EstimatorPerformanceLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments object
		//mrs1 = metagenome rarefaction simulator object
		//lst1 = current list of mle estimates
		//lst2 = current list of step estimates
		//iEstimates = number of estimates per value of rho
		//dPrAssembly = current probability of assembly
		//lstOut = output
		
		double dPrAssembly;
		ArgumentIO arg1;
		MetagenomeRarefactionSimulator mrs1;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		ArrayList<String> lstOut;
		double dN;
		double dNTilde; 
		double dCL_i; 
		int iEstimates;
		
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		//iEstimates = 1000;
		dN=arg1.getValueDouble("dN");
		dNTilde=arg1.getValueDouble("dNTilde"); 
		dCL_i=arg1.getValueDouble("dCLi");
		iEstimates=arg1.getValueInt("iEstimates");
		lstOut = new ArrayList<String>(iEstimates*100);
		lstOut.add("RHO,PR_ASSEMBLY,MLE,STEP");
		
		//looping through values of rho
		for(double dRho=0.01;dRho<1;dRho+=0.005){
			
			mrs1 = new MetagenomeRarefactionSimulator(dN, dNTilde, dCL_i, 1., dRho);
			//mrs1 = new MetagenomeRarefactionSimulator(1000000, 5000, 10, 400, dRho);
			dPrAssembly = mrs1.probabilityOfAssembly();
			lst1 = mrs1.estimatesMLE(iEstimates);
			lst2 = mrs1.estimatesStep(iEstimates);
			for(int i=0;i<lst1.size();i++){
				lstOut.add(dRho + "," + dPrAssembly + "," + lst1.get(i) + "," + lst2.get(i));
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
	
}
