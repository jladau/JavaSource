package gov.doe.jgi.FormatVCardDoro;

import java.util.ArrayList;

public class ContactEntry{
	
	public String sFN;
	public String sN;
	public ArrayList<String> lstNumbers;
	
	public ContactEntry(){
		sFN = null;
		sN = null;
		lstNumbers = new ArrayList<String>(5);
	}
	
	public void addFN(String sFN){
		this.sFN = sFN;
	}
	
	public void addN(String sN){
		this.sN = sN;
	}
	
	public void addNumber(String sNumber){
		lstNumbers.add(sNumber);
	}
	
	public ArrayList<String> printContact(){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		if(lstNumbers.size()==0){
			System.out.println("No phone numbers for " + sFN.replace("FN:", "") + ".");
			return null;
		}
		lst1 = new ArrayList<String>();
		lst1.add("BEGIN:VCARD");
		lst1.add("VERSION:3.0");
		lst1.add(sN.replaceFirst("N:", "n:"));
		lst1.add(sFN);
		lst1.add("CATEGORY:DEVICE,KAICONTACT");
		for(String s:lstNumbers){
			lst1.add("TEL;TYPE=CELL:" + s);
		}
		lst1.add("END:VCARD");
		return lst1;
	}
}	