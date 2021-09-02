package edu.ucsf.TransitiveReduction;

import java.util.ArrayList;

import com.google.common.collect.TreeBasedTable;
import edu.ucsf.base.BinaryRelation;
import edu.ucsf.base.OrderedPair;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TransitiveReductionLauncher {

	public static void main(String rgsArgs[]){
		
		//dThreshold = minimum standardized effect size
		//arg1 = arguments
		//dat1 = data
		//bin1 = binary relation
		//tbl1 = transitive reduction in adjacency matrix format
		//sbl1 = output line
		//lst1 = current output line
		
		BinaryRelation<String> bin1;
		ArgumentIO arg1;
		DataIO dat1;
		double dThreshold;
		TreeBasedTable<String,String,Integer> tbl1;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		dThreshold = arg1.getValueDouble("dSESThreshold");
		
		//loading binary relation
		bin1 = new BinaryRelation<String>();
		for(int i=1;i<dat1.iRows;i++){
			
			//checking standardized effect size 
			if(dat1.getDouble(i, "SES")>dThreshold){
	
				//adding pair if large effect
				bin1.addOrderedPair(dat1.getString(i, "VERTEX_1"), dat1.getString(i, "VERTEX_2"));
			}
		}
		
		//checking for strict order
		System.out.println("----------------------------------");
		System.out.println("Strict order: " + bin1.isStrictOrdering());
		
		//outputting transitivity violations
		for(ArrayList<OrderedPair<String>> lst1:bin1.listTransitivityViolations()){
			System.out.println(lst1.get(0) + ";" +lst1.get(1));
		}
		System.out.println("----------------------------------");
		
		//outputting transitive reduction
		tbl1 = bin1.findTransitiveReduction().toAdjacencyMatrix();
		lstOut = new ArrayList<String>();
		sbl1 = new StringBuilder();
		sbl1.append("object");
		for(String t:tbl1.columnKeySet()){
			sbl1.append("," + t);
		}
		lstOut.add(sbl1.toString());
		for(String s:tbl1.rowKeySet()){
			sbl1 = new StringBuilder();
			sbl1.append(s);
			for(String t:tbl1.columnKeySet()){
				if(arg1.getValueBoolean("bInvert")==false){
					sbl1.append("," + tbl1.get(s, t));
				}else{
					sbl1.append("," + tbl1.get(t, s));
				}
			}
			lstOut.add(sbl1.toString());
		}
		
		//for(OrderedPair<String> orp1:bin1.findTransitiveReduction().getOrderedPairs()){
		//	System.out.println(orp1.o1 + "," + orp1.o2);
		//}
		
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}