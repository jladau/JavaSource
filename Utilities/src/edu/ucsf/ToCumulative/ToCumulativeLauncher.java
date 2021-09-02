package edu.ucsf.ToCumulative;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class ToCumulativeLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lst1 = output
		//d1 = current cumulant
		//s1 = header
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lst1;
		double d1;
		String s1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		s1 = arg1.getValueString("sValueHeader");
		lst1 = new ArrayList<String>(dat1.iRows);
		
		//outputting results
		d1 = 0;
		lst1.add(s1 + "_CUMULATIVE");
		for(int i=1;i<dat1.iRows;i++){
			d1+=dat1.getDouble(i,s1);
			lst1.add(Double.toString(d1));
		}
		
		//terminating
		DataIO.writeToFile(lst1,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}
