package gov.doe.jgi.FormatVCardDoro;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class FormatVCardDoroLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sFN = current FN
		//sN = current N
		//map1 = map from FN entries to contacts
		//s1 = current string
		//sFN = current FN
		//lstOut = output
		//lst1 = current contact
		
		HashMap<String,ContactEntry> map1;
		DataIO dat1;
		ArgumentIO arg1;
		String s1;
		String sFN = null;
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		map1 = new HashMap<String,ContactEntry>(1000);
		lstOut = new ArrayList<String>(dat1.iRows);
		
		for(int i=0;i<dat1.iRows;i++){
			s1 = dat1.getString(i, 0);
			if(s1.startsWith("FN:")){
				sFN=s1;
				map1.put(sFN, new ContactEntry());
				map1.get(sFN).addFN(sFN);
			}
			if(s1.startsWith("N:")){
				map1.get(sFN).addN(s1);
			}
			if(s1.contains("TEL:") || s1.contains("TEL;")){
				map1.get(sFN).addNumber(s1.split("\\+")[1].replace("+", ""));
			}
		}
		
		//printing output
		for(String s:map1.keySet()){
			lst1 = map1.get(s).printContact();
			if(lst1!=null){
				lstOut.addAll(map1.get(s).printContact());
			}
			lstOut.add("");
		}
		lstOut.remove(lstOut.size()-1);
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
		
	}
	
	
}
