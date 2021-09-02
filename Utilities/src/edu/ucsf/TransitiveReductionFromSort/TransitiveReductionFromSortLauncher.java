package edu.ucsf.TransitiveReductionFromSort;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TransitiveReductionFromSortLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//lst1 = output in string builder format
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		ArrayList<StringBuilder> lst1;
		
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows);
		lst1 = new ArrayList<StringBuilder>(dat1.iRows);
		lst1.add(new StringBuilder());
		lst1.get(0).append("object");
		
		//loading transitive reduction
		for(int i=1;i<dat1.iRows;i++){
			lst1.get(0).append("," + dat1.getString(i, 0));
			lst1.add(new StringBuilder());
			lst1.get(i).append(dat1.getString(i, 0));
			for(int j=0;j<i-2;j++){
				lst1.get(i).append(",0");
			}
			if(i>1){
				lst1.get(i).append(",1");
			}
			for(int j=i;j<dat1.iRows;j++){
				lst1.get(i).append(",0");
			}
		}
		for(int i=0;i<lst1.size();i++){
			lstOut.add(lst1.get(i).toString());
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
		
		
		
		
		
		
		
		
		
	}
	
}
