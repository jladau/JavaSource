package edu.ucsf.TransitiveReductionToPartialOrder;

import java.util.ArrayList;
import edu.ucsf.base.OrderedPair;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TransitiveReductionToPartialOrder {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lst1 = output
		//b1 = flag for whether additional transitive pairs added
		//lst2 = set of ordered pairs
		//orp1 = first element of ordered pair
		//orp2 = second element of ordered pair
		//orp3 = ordered pair being added
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lst1;
		boolean b1;
		ArrayList<OrderedPair<String>> lst2;
		OrderedPair<String> orp1;
		OrderedPair<String> orp2;
		OrderedPair<String> orp3;
				
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lst1 = new ArrayList<String>(dat1.iRows*dat1.iRows);
		lst1.add("OBJECT_1,OBJECT_2");
		lst2 = new ArrayList<OrderedPair<String>>(dat1.iRows*dat1.iRows);
		
		//initializing set of ordered pairs
		for(int i=1;i<dat1.iRows;i++){
			for(int j=1;j<dat1.iCols;j++){
				if(dat1.getInteger(i, j)==1){
					lst2.add(new OrderedPair<String>(dat1.getString(i,0), dat1.getString(0,j)));
				}
			}
		}
		
		//checking for pairs to add
		do{
			b1=false;
			for(int i=1;i<lst2.size();i++){
				orp1 = lst2.get(i);
				for(int k=0;k<i;k++){
					orp2 = lst2.get(k);
					if(orp1.o2.equals(orp2.o1)){
						orp3=new OrderedPair<String>(orp1.o1,orp2.o2);
						if(!lst2.contains(orp3)){
							lst2.add(orp3);
							b1=true;
						}
					}
					if(orp2.o2.equals(orp1.o1)){
						orp3=new OrderedPair<String>(orp2.o1,orp1.o2);
						if(!lst2.contains(orp3)){
							lst2.add(orp3);
							b1=true;
						}
					}
					
				}
			}
			
		}while(b1==true);
		
		//writing output
		for(OrderedPair<String> orp:lst2){
			lst1.add(orp.toString());
		}
		DataIO.writeToFile(lst1, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}