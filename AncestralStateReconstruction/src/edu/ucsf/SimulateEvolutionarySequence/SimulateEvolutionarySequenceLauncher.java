package edu.ucsf.SimulateEvolutionarySequence;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.base.Graph.GraphEdge;
import edu.ucsf.base.Tree;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Simulates sequence of evolution on tree where gains and losses are more or less likely dependent on nestedness order
 * @author jladau
 */

public class SimulateEvolutionarySequenceLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//datTree = tree, with headers VERTEX_PARENT and VERTEX_CHILD
		//datNestedness = partial order of environments
		//datProbability = gives probability of gain or loss change factor for each environment contingent on number of predecessors present or successors absent (headers ENVIRONMENT, CONDITIONING_FUNCTION, CONDITIONING_FACTOR, PR_GAIN, PR_LOSS)
		//tre1 = tree
		//lstEdges = list of edges in traversable order
		//map1 = map from environment names to environment objects
		//map2 = map of current states
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO datTree;
		DataIO datNestedness;
		DataIO datProbability;
		Tree tre1;
		ArrayList<GraphEdge> lstEdges;
		HashMap<String,SimulatedEnvironment> map1;
		HashMap<String,String> map2;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datTree = new DataIO(arg1.getValueString("sTreePath"));
		datNestedness = new DataIO(arg1.getValueString("sNestednessPath"));
		datProbability = new DataIO(arg1.getValueString("sProbabilityPath"));
		lstOut = new ArrayList<String>(datProbability.iRows*datTree.iRows);
		
		//loading tree
		tre1 = new Tree(datTree);
		lstEdges = tre1.orderedEdgeList();

		//initializing simulated environments
		map1 = new HashMap<String,SimulatedEnvironment>(datProbability.iRows);
		for(int i=1;i<datProbability.iRows;i++){
			map1.put(datProbability.getString(i, "ENVIRONMENT"), new SimulatedEnvironment(
					datProbability.getString(i, "ENVIRONMENT"),
					datProbability.getDouble(i, "PR_GAIN_INITIAL"),
					datProbability.getDouble(i, "LOG_ODDS_GAIN"),
					datProbability.getDouble(i, "LOG_ODDS_LOSS"),
					datProbability.getDouble(i, "LOG_ODDS_RATIO_GAIN_SUPERSET"),
					datProbability.getDouble(i, "LOG_ODDS_RATIO_LOSS_SUBSET"),
					arg1.getValueInt("iRandomSeed")*i+43));
		}
		
		//loading predecessors and successors
		for(int i=1;i<datNestedness.iRows;i++){
			if(map1.containsKey(datNestedness.getString(i, "SUBSET")) && map1.containsKey(datNestedness.getString(i, "SUPERSET"))){
				map1.get(datNestedness.getString(i, "SUBSET")).addSuperset(datNestedness.getString(i, "SUPERSET"));
				map1.get(datNestedness.getString(i, "SUPERSET")).addSubset(datNestedness.getString(i, "SUBSET"));
			}
		}
		
		//loading initial state
		for(String s:map1.keySet()){
			tre1.putProperty(tre1.root().iID, s, map1.get(s).simulateInitialState());
		}
		
		//looping through edges and loading simulated environments 
		for(int i=0;i<lstEdges.size();i++){
			map2 = lstEdges.get(i).vtxStart.getProperties();
			for(String s:map1.keySet()){
				tre1.putProperty(lstEdges.get(i).vtxEnd.iID, s, map1.get(s).simulateNextState(map2));
			}
		}
		
		//outputting results
		lstOut.add("ENVIRONMENT,NODE_ID,NODE_VALUE");
		for(Integer i:tre1.getVertexIDs()){	
			map2 = tre1.getVertex(i).getProperties();
			for(String s:map2.keySet()){
				if(map2.get(s).equals("present")){
					lstOut.add(s + "," + tre1.getVertex(i).sName + ",1");
				}else if(map2.get(s).equals("absent")){
					lstOut.add(s + "," + tre1.getVertex(i).sName + ",0");
				}
			}
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
