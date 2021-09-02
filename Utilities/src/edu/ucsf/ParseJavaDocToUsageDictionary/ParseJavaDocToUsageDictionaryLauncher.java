package edu.ucsf.ParseJavaDocToUsageDictionary;

import java.util.ArrayList;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Parses command line arguments listed in java documentation format to usage documentation format
 * @author jladau
 */

public class ParseJavaDocToUsageDictionaryLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//lst1 = data
		//arg1 = arguments
		//lstOut = output
		//rgs1 = current line in split format
		//s1 = current description
		
		ArrayList<String> lst1;
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		String rgs1[];
		
		arg1 = new ArgumentIO(rgsArgs);
		lst1 = DataIO.readFileNoDelimeter(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>();
		for(String s:lst1){
			if(s.equals("")){
				continue;
			}
			rgs1 = s.split(" ");
			lstOut.add("mapDocumentation.put(\"" + rgs1[0] + "\", new ArgumentDocumentation(");
			lstOut.add("\t\"" + rgs1[0] + "\",");
			lstOut.add("\t\"" + rgs1[1].replace("[", "").replace("]","") + "\",");
			lstOut.add("\tnew String[]{\"XXXX\"},");
			lstOut.add((rgs1[2].equals("[OPTIONAL]")) ? "\ttrue," : "\tfalse");
			lstOut.add("\t\"" + s.replace(" = ", "~").split("~")[1].replace("\"", "\\\"") + "\"));");
			lstOut.add("");
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
