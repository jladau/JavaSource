package edu.ucsf.BasicSimulation;

import java.util.ArrayList;
import java.util.Random;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class BasicSimulationLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dP1 = probability of gain if predecessor is absent
		//dP2 = probability of gain in predecessor is present
		//iTaxa = total number of taxa
		//iSteps = number of time steps
		//rnd1 = random number generator
		//lstOut = output
		//iSimulations = number of simulations to run
		
		ArgumentIO arg1;
		double dP1;
		double dP2;
		int iTaxa;
		int iSteps;
		Random rnd1;
		ArrayList<String> lstOut;
		int iSimulations;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dP1 = arg1.getValueDouble("dProbabilityOccurNoPredecessor");
		dP2 = arg1.getValueDouble("dProbabilityOccurPredecessor");
		iTaxa = arg1.getValueInt("iTaxa");
		iSimulations = arg1.getValueInt("iSimulations");
		iSteps = arg1.getValueInt("iMaximumSteps");
		rnd1 = new Random(System.currentTimeMillis());
		lstOut = new ArrayList<String>(iSteps*iTaxa*iSimulations+1);
		lstOut.add("SIMULATION_ID,TIME_STEP,TAXON,NUMBER_PREDECESSORS,NUMBER_SUCCESSORS,EVENT");

		//looping through simulations
		for(int i=1;i<=iSimulations;i++){
			lstOut.addAll(runSimulation(iSteps, iTaxa, dP1, dP2, rnd1, i));
		}

		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static ArrayList<String> runSimulation(int iSteps, int iTaxa, double dP1, double dP2, Random rnd1, int iSimulationID){

		//dP1 = probability of gain if predecessor is absent
		//dP2 = probability of gain in predecessor is present
		//iTaxa = total number of taxa
		//rgi1 = previous vector of occurrences
		//rgi2 = current vector of occurrences
		//iSteps = number of time steps
		//rgiPredecessors = number of predecessors
		//rgiSuccessors = number of successors
		//rgsChange = change in current step
		//rnd1 = random number generator
		//i1 = current count
		//lst1 = current output list
		//lstOut = output
		
		int rgi1[];
		int rgi2[];
		int rgiPredecessors[];
		int rgiSuccessors[];
		int i1;
		ArrayList<String> lst1;
		String[] rgsChange;
		ArrayList<String> lstOut;
		
		
		//initializing variables
		rgi1 = new int[iTaxa];
		rgiPredecessors = new int[iTaxa];
		rgiSuccessors = new int[iTaxa];
		lstOut = new ArrayList<String>(iSteps*iTaxa);
		
		//looping through time steps
		for(int i=0;i<iSteps;i++){
			
			//generating current vector of occurrences
			rgi2 = new int[iTaxa];
			rgsChange = new String[iTaxa];
			for(int j=0;j<iTaxa;j++){
				
				//*********************************
				//if(rgiPredecessors[1]>0 && rgi1[2]==0){
				//	System.out.println("HERE1");
				//}
				//if(rgiPredecessors[1]==0 && rgi1[2]>0){
				//	System.out.println("HERE2");
				//}
				//*********************************				
				
				
				if(rgi1[j]==0 && rgiPredecessors[j]>0){
					if(rnd1.nextDouble()<dP2){
						rgi2[j]=1;
						rgsChange[j]="gain";
					}else{
						rgsChange[j]="absent";
					}
				}else if(rgi1[j]==0 && rgiPredecessors[j]==0){
					if(rnd1.nextDouble()<dP1){
						rgi2[j]=1;
						rgsChange[j]="gain";
					}else{
						rgsChange[j]="absent";
					}
				}else if(rgi1[j]==1){
					rgi2[j]=1;
					rgsChange[j]="retain";
				}
			}
			
			//***************************
			//if(rgiPredecessors[1]==1 & rgiSuccessors[1]==0){
			//	if(rgsChange[1].equals("gain")){
			//		System.out.println(1);
			//	}
			//	if(rgsChange[1].equals("absent")){
			//		System.out.println(0);
			//	}
			//}
			
			
			
			//if(rgi1[2]!=rgiPredecessors[1]){
			//	System.out.println("OINK");
			//}
			
			
			//if(rgi1[2]==1 && rgi1[0]==0){
			//	if(rgsChange[1].equals("gain")){
			//		System.out.println(1);
			//	}else if(rgsChange[1].equals("absent")){
			//		System.out.println(0);
			//	}
			//}
			//***************************
			
			//outputting results
			for(int j=0;j<iTaxa;j++){
				lstOut.add(iSimulationID + "," + i + "," + j + "," + rgiPredecessors[j] + "," + rgiSuccessors[j] + "," + rgsChange[j]);
			}
		
			//updating predecessor counts
			rgiPredecessors = new int[iTaxa];
			i1 = 0;
			for(int j=(iTaxa-1);j>0;j--){
				i1+=rgi2[j];
				rgiPredecessors[j-1]=i1;
			}
			
			//updating successor counts
			rgiSuccessors = new int[iTaxa];
			i1 = 0;
			for(int j=0;j<iTaxa-1;j++){
				i1+=rgi2[j];
				rgiSuccessors[j+1]=i1;
			}
			
			/*
			lst1 = new ArrayList<String>(iTaxa);
			for(int j=0;j<rgi2.length;j++){
				lst1.add(Integer.toString(rgi2[j]));
			}
			System.out.println(Joiner.on(",").join(lst1));
			lst1 = new ArrayList<String>(iTaxa);
			for(int j=0;j<rgi2.length;j++){
				lst1.add(rgsChange[j]);
			}
			System.out.println(Joiner.on(",").join(lst1));
			lst1 = new ArrayList<String>(iTaxa);
			for(int j=0;j<rgi2.length;j++){
				lst1.add(Integer.toString(rgiPredecessors[j]));
			}
			System.out.println(Joiner.on(",").join(lst1));
			lst1 = new ArrayList<String>(iTaxa);
			for(int j=0;j<rgi2.length;j++){
				lst1.add(Integer.toString(rgiSuccessors[j]));
			}
			System.out.println(Joiner.on(",").join(lst1));
			System.out.println("");
			*/
			
			//exiting if done
			i1=0;
			for(int k:rgi2){
				i1+=k;
			}
			
			if(i1==iTaxa){
				break;
			}
			
			//updating lists
			rgi1 = rgi2;
		}
		
		return lstOut;
	}
}
