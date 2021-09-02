package edu.ucsf.ShiftDatabase;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class ShiftDatabaseLauncher {

	public static void main(String rgsArgs[]){
		
		//datEdges = edges
		//datOccur = occurrences
		//arg1 = arguments
		//set1 = set of environment,simulation combinations
		//s1 = current environment,simulation combination
		//tbl1 = rows represent environment,simulation combinations; columns vertex ids; values are occurrences or absences
		//iParent = parent value
		//iChild = child value
		//sParent = current parent
		//sChild = current child
		//lstOut = output
		//lstEdgeOut = edge information output
		//sChange = change value
		
		ArrayList<String> lstOut;
		ArrayList<String> lstEdgeOut;
		String sParent;
		String sChild;
		int iParent;
		int iChild;
		HashSet<String> set1;
		String s1;
		DataIO datEdges;
		DataIO datOccur;
		ArgumentIO arg1;
		HashBasedTable<String,String,Integer> tbl1;
		String sChange;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datEdges = new DataIO(arg1.getValueString("sEdgesPath"));
		datOccur = new DataIO(arg1.getValueString("sOccurrencesPath"));		
		
		//loading table of values
		set1 = new HashSet<String>(datOccur.iRows);
		for(int i=1;i<datOccur.iRows;i++){
			s1 = datOccur.getString(i, "ENVIRONMENT") + "," + datOccur.getString(i, "SIMULATION_ID");
			set1.add(s1);
		}
		tbl1 = HashBasedTable.create(set1.size(),datEdges.iRows);
		for(int i=1;i<datOccur.iRows;i++){
			s1 = datOccur.getString(i, "ENVIRONMENT") + "," + datOccur.getString(i, "SIMULATION_ID");
			tbl1.put(s1, datOccur.getString(i, "NODE_ID"), datOccur.getInteger(i, "NODE_VALUE"));
		}
		
		//loading change values
		lstOut = new ArrayList<String>(datEdges.iRows*set1.size()+1);
		lstOut.add("EDGE_ID,ENVIRONMENT,SIMULATION_ID,EVOLUTION");
		lstEdgeOut = new ArrayList<String>(datEdges.iRows+1);
		lstEdgeOut.add("EDGE_ID,VERTEX_PARENT,VERTEX_CHILD");
		for(int i=1;i<datEdges.iRows;i++){
			sParent = datEdges.getString(i, "VERTEX_PARENT");
			sChild = datEdges.getString(i, "VERTEX_CHILD");
			lstEdgeOut.add(i + "," + sParent + "," + sChild);
			for(String s:set1){
				iParent = tbl1.get(s, sParent);
				iChild = tbl1.get(s, sChild);
				sChange = null;
				if(iParent==0 && iChild==0){
					sChange="absent";
				}else if(iParent==0 && iChild==1){
					sChange="gain";
				}else if(iParent==1 && iChild==0){
					sChange="loss";
				}else if(iParent==1 && iChild==1){
					sChange="retain";
				}
				lstOut.add(i + "," + s + "," + sChange);
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(lstEdgeOut, arg1.getValueString("sOutputPath").replace(".csv", "-edge-properties.csv"));
		System.out.println("Done.");	
	}
}
