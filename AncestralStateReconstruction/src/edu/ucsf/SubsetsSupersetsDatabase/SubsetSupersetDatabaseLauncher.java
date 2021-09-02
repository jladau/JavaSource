package edu.ucsf.SubsetsSupersetsDatabase;

import java.util.HashSet;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class SubsetSupersetDatabaseLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datRelation = binary relation
		//datShifts = shifts database
		//mapSuper = map from environment IDs to superset environments
		//mapSub = map from environment IDs to subset environments
		//tblSub = rows are edge id, simulation combinations; columns are environments; values are subset counts
		//tblSuper = rows are edge id, simulation combinations; columns are environments; values are superset counts
		//i1 = current subset count
		//i2 = current superset count
		//set1 = set of edge id, simulation combinations
		//s1 = current edge id, simulation combination
		//sEnv = current environment
		
		ArgumentIO arg1;
		DataIO datRelation;
		DataIO datShifts;
		HashMultimap<String,String> mapSuper;
		HashMultimap<String,String> mapSub;
		HashBasedTable<String,String,Integer> tblSub;
		HashBasedTable<String,String,Integer> tblSuper;
		HashSet<String> set1;
		String s1;
		String sEnv;
		int i1;
		int i2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datRelation = new DataIO(arg1.getValueString("sBinaryRelationPath"));
		datShifts = new DataIO(arg1.getValueString("sShiftsDatabasePath"));
		
		//loading superset and subset maps
		mapSuper = HashMultimap.create();
		mapSub = HashMultimap.create();
		for(int i=1;i<datRelation.iRows;i++){
			mapSuper.put(datRelation.getString(i, "SUBSET"), datRelation.getString(i, "SUPERSET"));
			mapSub.put(datRelation.getString(i, "SUPERSET"), datRelation.getString(i, "SUBSET"));
		}
		
		//loading table of values
		set1 = new HashSet<String>(datShifts.iRows);
		for(int i=1;i<datShifts.iRows;i++){
			s1 = datShifts.getString(i, "EDGE_ID") + "," + datShifts.getString(i, "SIMULATION_ID");
			set1.add(s1);
		}
		
		//loading tables of counts
		tblSub = HashBasedTable.create(set1.size(),datRelation.iRows);
		tblSuper = HashBasedTable.create(set1.size(),datRelation.iRows);
		for(int i=1;i<datShifts.iRows;i++){
			if(datShifts.getString(i, "EVOLUTION").equals("loss") || datShifts.getString(i, "EVOLUTION").equals("retain")){
				s1 = datShifts.getString(i, "EDGE_ID") + "," + datShifts.getString(i, "SIMULATION_ID");
				for(String s:mapSub.get(datShifts.getString(i, "ENVIRONMENT"))){
					if(tblSuper.contains(s1, s)){
						i2 = tblSuper.get(s1, s);
					}else{
						i2 = 0;
					}
					i2++;
					tblSuper.put(s1, s, i2);
				}
				for(String s:mapSuper.get(datShifts.getString(i, "ENVIRONMENT"))){
					if(tblSub.contains(s1, s)){
						i1 = tblSub.get(s1, s);
					}else{
						i1 = 0;
					}
					i1++;
					tblSub.put(s1, s, i1);
				}
			}
		}
		
		//appending output
		datShifts.appendToLastColumn(0,"SUBSET_COUNT,SUPERSET_COUNT,SUBSET_OCCURRENCE,SUBSET_OCCURRENCE_ALL,SUPERSET_OCCURRENCE,SUPERSET_OCCURRENCE_ALL");
		for(int i=1;i<datShifts.iRows;i++){
			s1 = datShifts.getString(i, "EDGE_ID") + "," + datShifts.getString(i, "SIMULATION_ID");
			sEnv = datShifts.getString(i, "ENVIRONMENT");
			if(tblSuper.contains(s1, sEnv)){
				i2 = tblSuper.get(s1, sEnv);
			}else{
				i2 = 0;
			}
			if(tblSub.contains(s1, sEnv)){
				i1 = tblSub.get(s1, sEnv);
			}else{
				i1 = 0;
			}
			datShifts.appendToLastColumn(i,
					i1 + "," +
					i2 + "," +
					(i1 > 0 ? 1 : 0) + "," +
					(i1 == mapSub.get(sEnv).size() && i1>0 ? 1 : 0) + "," +
					(i2 > 0 ? 1 : 0) + "," +
					(i2 == mapSuper.get(sEnv).size() && i2>0 ? 1 : 0)	
			);
		}
		
		//writing output
		DataIO.writeToFile(datShifts.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
