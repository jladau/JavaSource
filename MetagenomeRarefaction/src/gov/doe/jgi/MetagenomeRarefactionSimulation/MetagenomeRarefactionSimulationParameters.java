package gov.doe.jgi.MetagenomeRarefactionSimulation;

import java.util.ArrayList;

public class MetagenomeRarefactionSimulationParameters {

	/**Value of rho**/
	public double dRho;
	
	/**Value of nu**/
	public double dNu;
	
	/**Values for which CDF has been computed**/
	public ArrayList<Integer> lstX;
	
	/**Values of CDF**/
	public ArrayList<Double> lstCDF;
	
	public MetagenomeRarefactionSimulationParameters(){
	}
	
	
}
