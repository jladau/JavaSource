package edu.ucsf.Trees.NewickToTaxonStrings;

import java.util.ArrayList;
import java.util.HashSet;
import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class NewickToTaxonStringsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//lst1 = data
		//rgs1 = current line
		//s1 = current string
		//i1 = current depth
		//i2 = current node index
		//lstOut = output
		//nod1 = current node
		//nod2 = current candidate node
		//sbl1 = current taxon string
		//setInternal = set of internal nodes
		//setLeaves = set of leaves
		//nod1 = current node being iterated across
		//b1 = flag for whether node was added
		//ncl1 = node collection
		
		OrderedNodeCollection ncl1;
		boolean b1;
		StringBuilder sbl1;
		ArgumentIO arg1;
		ArrayList<String> lst1;
		String rgs1[];
		String s1;
		int i1;
		int i2;
		ArrayList<String> lstOut;
		Node nod1 = null;
		HashSet<Node> setLeaves;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		lst1 = DataIO.readFileNoDelimeter(arg1.getValueString("sDataPath"));
		i1 = 0;
		i2 = 0;
		setLeaves = new HashSet<Node>();
		ncl1 = new OrderedNodeCollection();
		
		//looping through lines of file
		for(int i=0;i<lst1.size();i++){
			
			//loading current line in split format
			s1 = lst1.get(i).replace(",", ",+,").replace(";", "");
			rgs1 = s1.replace("(", "(,").replace(")",",)").split(",");
			
			//looping through elements of line
			for(int j=0;j<rgs1.length;j++){
				
				s1 = rgs1[j].replace("'", "");
				
				//increase depth by 1
				if(s1.equals("(")){
					i1++;
					
				//decrease depth by 1	
				}else if(s1.equals(")")){
					i1--;
				}else{
					
					//decrease depth by 1, new internal node
					if(s1.startsWith(")")){
						i1--;
						i2++;
						b1 = ncl1.add(new Node(s1.substring(1),i2,i1,false));
						
					//clade	
					}else{
					
						//placeholder internal node
						if(s1.equals("+")){
							i2++;
							b1 = ncl1.add(new Node(s1,i2,i1,false));
							
						//start new leaf			
						}else{
							i2++;
							b1 = setLeaves.add(new Node(s1,i2,i1,true));
							if(b1==false){
								System.out.println(new Node(s1,i2,i1,true));
							}
						}
					}
				}
			}
		}
		
		//looping through leaves
		ncl1.loadLeastDepthFunction();
		lstOut = new ArrayList<String>(setLeaves.size()+1);
		lstOut.add("OTU,SPECIES,GENUS,FAMILY,ORDER,CLASS,PHYLUM,KINGDOM");
		for(Node nodLeaf:setLeaves){
			sbl1 = new StringBuilder();
			nod1 = nodLeaf;
			do{
				if(!nod1.id().equals("+")){
					if(sbl1.length()>0){
						sbl1.append(";");
					}
					sbl1.append(nod1.id());
				}
				nod1 = ncl1.nextNode(nod1);
				
				//**********************
				//System.out.println(nod1);
				//**********************
				
				
			}while(nod1!=null);
			lstOut.add(formatTaxonomy(sbl1.toString()));	
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}

	private static String formatTaxonomy(String s1){
		
		//rgs1 = output
		//rgs2 = input
		
		String rgs2[];
		String rgs1[];
		
		rgs2 = s1.split(";");
		rgs1 = new String[]{"","s__","g__","f__","o__","c__","p__","k__"};
		for(String s:rgs2){
			if(s.startsWith("s__")){
				rgs1[1]=s;
			}else if(s.startsWith("g__")){
				rgs1[2]=s;
			}else if(s.startsWith("f__")){
				rgs1[3]=s;
			}else if(s.startsWith("o__")){
				rgs1[4]=s;
			}else if(s.startsWith("c__")){
				rgs1[5]=s;
			}else if(s.startsWith("p__")){
				rgs1[6]=s;
			}else if(s.startsWith("k__")){
				rgs1[7]=s;
			}else{
				rgs1[0]=s;
			}
		}
		
		return Joiner.on(",").join(rgs1);
	}
	
	/*
	public static void main0(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//lst1 = data
		//rgs1 = current line
		//s1 = current string
		//i1 = current depth
		//i2 = current internal node array index to start with
		//i3 = current internal node index
		//i4 = leaf counter
		//lstOut = output
		//lstNodes = set of nodes
		//nod1 = current node
		//nod2 = current candidate node
		//sbl1 = current taxon string
		//lstInternal = list of indices of nodes that are internal
		//lstLeaves = list of indices of nodes that are leaves
		//b1 = flag for whether internal node index has been updated
		
		boolean b1;
		StringBuilder sbl1;
		ArrayList<Node> lstNodes;
		ArgumentIO arg1;
		ArrayList<String> lst1;
		String rgs1[];
		String s1;
		int i1;
		int i2;
		int i3;
		int i4;
		ArrayList<String> lstOut;
		Node nod1;
		Node nod2;
		ArrayList<Integer> lstInternal;
		ArrayList<Integer> lstLeaves;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		lst1 = DataIO.readFileNoDelimeter(arg1.getValueString("sDataPath"));
		i1 = 0;
		i2 = 0;
		i4 = 0;
		lstNodes = new ArrayList<Node>(10000);
		lstInternal = new ArrayList<Integer>(lstNodes.size());
		lstLeaves = new ArrayList<Integer>(lstNodes.size());
		
		//looping through lines of file
		for(int i=0;i<lst1.size();i++){
			
			//loading current line in split format
			s1 = lst1.get(i).replace(",", ",+,").replace(";", "");
			rgs1 = s1.replace("(", "(,").replace(")",",)").split(",");
			
			//looping through elements of line
			for(int j=0;j<rgs1.length;j++){
				
				s1 = rgs1[j];
				
				//increase depth by 1
				if(s1.equals("(")){
					i1++;
					
				//decrease depth by 1	
				}else if(s1.equals(")")){
					i1--;
				}else{
					
					//decrease depth by 1, new internal node
					if(s1.startsWith(")")){
						i1--;
						lstNodes.add(new Node(s1.substring(1),i1,false));
						lstInternal.add(lstNodes.size()-1);
						
					//clade	
					}else{
					
						//placeholder internal node
						if(s1.equals("+")){
							lstNodes.add(new Node(s1,i1,false));
							lstInternal.add(lstNodes.size()-1);
							
						//start new leaf			
						}else{
							lstNodes.add(new Node(s1,i1,true));
							lstLeaves.add(lstNodes.size()-1);
						}
					}
				}
			}
		}
		
		//**********************************
		for(int i:lstInternal){
			System.out.println(lstNodes.get(i).depth());
		}
		//**********************************
		
		//looping through leaves
		lstOut = new ArrayList<String>(lstLeaves.size());
		for(int i:lstLeaves){
			
			i4++;
			System.out.println("Analyzing leaf " + i4 + " of " + lstLeaves.size() + "...");
		
			nod1 = lstNodes.get(i);
			i1 = nod1.depth();
			sbl1 = new StringBuilder();
			sbl1.append(nod1.id());
			b1 = false;
			for(int k=i2;k<lstInternal.size();k++){
				i3 = lstInternal.get(k);
				if(i3>i){
					nod2 = lstNodes.get(i3);
					if(!nod2.isLeaf() && nod2.depth()<i1){
						if(!nod2.id().equals("+")){
							sbl1.append(";" + nod2.id());
						}
						i1 = nod2.depth();
					}
					if(b1==false){
						i2=k;
						b1=true;
					}
				}
			}
			lstOut.add(sbl1.toString());
			//*********************************
			//System.out.println(sbl1.toString());
			//*********************************
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	*/
}
