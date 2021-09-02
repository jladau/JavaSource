package edu.ucsf.SimulateEvolutionarySequence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class SimulatedEnvironment {

	/**Intercept for logistic model, subsets**/
	public double dBInterceptSubset;
	
	/**Intercept for logistic model, supersets**/
	public double dBInterceptSuperset;
	
	/**Coefficient predecessors in logistic model**/
	public double dBSuperset;
	
	/**Coefficient for successors in logisitic model**/
	public double dBSubset;
	
	/**Initial probability of gain for root node*/
	public double dPrGainInitial;
	
	/**Name**/
	public String sName;
	
	/**Set of predecessors in nestedness order**/
	public HashSet<String> setSupersets;
	
	/**Set of successors in nestedness order**/
	public HashSet<String> setSubsets;
	
	/**Random number generator**/
	private Random rnd1;
	
	public SimulatedEnvironment(
			String sName,
			double dPrGainInitial,
			double dBInterceptSuperset,
			double dBInterceptSubset,
			double dBSuperset,
			double dBSubset,
			int iRandomSeed){
		
		this.sName=sName;
		this.dPrGainInitial=dPrGainInitial;
		this.dBInterceptSubset = dBInterceptSubset;
		this.dBInterceptSuperset = dBInterceptSuperset;
		this.dBSuperset = dBSuperset;
		this.dBSubset = -dBSubset;
		this.setSupersets=new HashSet<String>(100); 
		this.setSubsets=new HashSet<String>(100);
		rnd1 = new Random(iRandomSeed);
		for(int i=0;i<25;i++){
			rnd1.nextDouble();
		}
	}
	
	public void addSubset(String sSubset){
		setSubsets.add(sSubset);
	}
	
	public void addSuperset(String sSuperset){
		setSupersets.add(sSuperset);
	}
	
	public String simulateInitialState(){
		if(rnd1.nextDouble()<dPrGainInitial){
			return "present";
		}else{
			return "absent";
		}
	}
	
	public String simulateNextState(HashMap<String,String> mapCurrentStates) throws Exception{
		
		//s1 = current state for this environment
		//d2 = number of predecessors or successors present
		//d1 = current probability cut off
		
		String s1;
		double d2;
		double d1 = 0;
		
		s1 = mapCurrentStates.get(sName);
		d2 = 0;
		if(s1.equals("absent")){
			for(String s:mapCurrentStates.keySet()){
				if(mapCurrentStates.get(s).equals("present") && setSupersets.contains(s)){
					d2++;
				}
			}
			if(d2==setSupersets.size()){
				d2=1;
			}else{
				d2=0;
			}
			d1 = 1./(1.+Math.exp(-(dBInterceptSuperset + d2*dBSuperset)));
			if(rnd1.nextDouble()<d1){
				return "present";
			}else{
				return "absent";
			}
		}else if(s1.equals("present")){
			for(String s:mapCurrentStates.keySet()){
				if(mapCurrentStates.get(s).equals("present") && setSubsets.contains(s)){
					d2++;
				}
			}
			if(d2>0){
				d2=1;
			}else{
				d2=0;
			}
			d1 = 1./(1.+Math.exp(-(dBInterceptSubset + d2*dBSubset)));
			if(rnd1.nextDouble()<d1){
				
				//************************************
				//if(sName.equals("L") && Math.abs(d2)<0.0000001){
				//	System.out.println("1");
				//}
				//************************************
				
				return "present";
			}else{
				
				//************************************
				//if(sName.equals("L") && Math.abs(d2)<0.0000001){	
				//	System.out.println("0");
				//}
				//************************************
				
				
				return "absent";
			}
		}else{
			throw new Exception("Error: Current environment state not initialized.");
		}
	}
}