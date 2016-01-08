

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;

public class ParseFiles {
	
	Map<String, List<String>> implementsCollection =  new HashMap<String,List<String>>();
	Map<String, List<String>> extendsCollection =  new HashMap<String,List<String>>();
	Map<String, List<String>> methodsCollection =  new HashMap<String,List<String>>();
	Map<String, List<String>> fieldsCollection =  new HashMap<String,List<String>>();
	
	List<String> classCollection = new ArrayList<>();
	List<String> interfaceCollection = new ArrayList<>();
	List<String> checkGetSet = new ArrayList<>();
	List<String> variablesCollection = new ArrayList<String>();	
	Set<String> potentialFieldsCollection = new HashSet<>();
	Set<String> dependancyList = new HashSet<>();
	Map<String,List<String>> potentialDependancyList = new HashMap<String,List<String>>();
	
	List<String> implementing ;
	List<String> extending ;
	List<String> methodsList ;
	List<String> fields ;
	List<String> dependantClass;
	
	 boolean getFlag=false,setFlag=false;   	 

	public void extractElements(File file) throws IOException, ParseException{
		
		FileInputStream in = new FileInputStream(file);
		CompilationUnit cu ;
		
		try{ 	cu = JavaParser.parse(in);	} 
		finally{	in.close();  	}		
		
		try{			
			for (TypeDeclaration type : cu.getTypes()) {
				
				implementing = new ArrayList<String>();
			    extending = new ArrayList<String>();
				methodsList = new ArrayList<String>();
				fields = new ArrayList<String>();	
				dependantClass = new ArrayList<String>();
				
				if (type instanceof ClassOrInterfaceDeclaration) {
		             ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) type;
		             if(cid.isInterface()){
		            	 interfaceCollection.add(cid.getName());
		             }else{
		            	 classCollection.add(cid.getName());
		             }
		             if(cid.getImplements()!=null){
		            	 for (ClassOrInterfaceType cit : cid.getImplements() ){
		            		 implementing.add(cit.getName());
		            	//	 System.out.println(cit.getName());
		            	 }
		             }
		             
		             if(cid.getExtends()!=null){
		            	 for(ClassOrInterfaceType cit : cid.getExtends()){
		            		 extending.add(cit.getName());
		            	//	 System.out.println(cit.getName());
		            	 }
		             }
		             
		             if (cid.getMembers() != null) {
		                 for (BodyDeclaration bd : cid.getMembers()) {
		                	 if(bd instanceof ConstructorDeclaration){
		                		 String modifier="",constructorParams="";
		                		 boolean flag=false;
		                		 ConstructorDeclaration m = (ConstructorDeclaration) bd;
		         
		                    switch(m.getModifiers()){
		                    case 0 : modifier="~"; break;
		                    case 1 : modifier="+"; break;
		                    case 2 : modifier ="-"; break;
		                    case 4 : modifier ="#"; break;
		                    }
		                    List<Parameter> parameters = m.getParameters();
		                    Iterator itr = parameters.iterator();
		                    while(itr.hasNext()){
		                    	StringTokenizer stoken = new StringTokenizer(itr.next().toString());
		                    	String temp1=stoken.nextToken(),temp2=stoken.nextToken();
		                    	
		                    	if(flag){ constructorParams+="," +temp2 +" : "+temp1; continue;}
		                    	else{ constructorParams = temp2 +" : "+temp1;}
		                    	flag = true;
		                    	dependantClass.add(temp1+" "+temp2);
		                    }
	                    	methodsList.add(modifier+" "+m.getName()+"("+constructorParams+")" );	
		                	 }

		               if (bd instanceof MethodDeclaration) {
		                         MethodDeclaration m = (MethodDeclaration) bd;
		                         List<Parameter> methods = m.getParameters();
		                       for(int q=0;q<methods.size();q++){
		                    	   String[] sdk =methods.get(q).toString().split(" ");
		                   	   dependantClass.add(sdk[0]+" "+sdk[1]);
		                       }
		                         if ((ModifierSet.isPublic(m.getModifiers()))) {
		                       	   	 String methodParams="";
		                        	 Boolean flag = false;
		                        	 List<Parameter> parameters = m.getParameters();
		 		                    Iterator itr = parameters.iterator();
		 		                    while(itr.hasNext()){
		 		                    	StringTokenizer stoken = new StringTokenizer(itr.next().toString());
		 		                    	String temp1=stoken.nextToken(),temp2=stoken.nextToken();
		 		                    	if(flag){ methodParams+="," +temp2 +" : "+temp1; continue;}
		 		                    	else{ methodParams = temp2 +" : "+temp1;}
		 		                    	flag = true;
		 		                    }
		 		                
		 		                    if(m.getName().toString().startsWith("get")){
		 		                    	getFlag=true;
		 		                  	
		 		                    }
		 		                    if(m.getName().toString().startsWith("set")){
		 		                    	setFlag=true;
		 		            
		 		                    }
		 		                    
		                        	 methodsList.add("+ "+m.getName()+"("+methodParams+")"+" : "+m.getType());
		                         }
		                     }
		                     if (bd instanceof FieldDeclaration) {
		                         FieldDeclaration m = (FieldDeclaration) bd;
		                         String modifier="";
		                       
		                         for(VariableDeclarator v : m.getVariables()){
		                        	 variablesCollection.add(cid.getName() +" "+ m.getType()+" "+ v.getId().getName());
		                         }
		                       
		                         if (ModifierSet.isPrivate(m.getModifiers()) || ModifierSet.isPublic(m.getModifiers())) {
		                             for (VariableDeclarator v : m.getVariables()) {
		                        
		                            	 switch(m.getModifiers()){
		                            	 case 0 : modifier="~"; break;
		     		                    case 1 : modifier="+"; break;
		     		                    case 2 : modifier="-";break;
		     		                    case 4 : modifier ="#"; break;
		                            	 }
				       

		                             fields.add(modifier+"  "+v.getId().getName()+" : "+m.getType());
		                             }
		                         }
		                     }
		                  }
		             }
		             if (getFlag && setFlag){
		            	 checkGetSet.add(cid.getName());
		            	 getFlag=false; setFlag=false; 
		   
		             }
	            	 implementsCollection.put(cid.getName(), implementing);
		             extendsCollection.put(cid.getName(), extending);
		             methodsCollection.put(cid.getName(), methodsList);
		             fieldsCollection.put(cid.getName(), fields);
		             potentialDependancyList.put(cid.getName(), dependantClass);
				}
			 }
		}
		catch(Exception e)	{		}
	}
	
	public void diaplayData(){
		System.out.println("Classes : "+classCollection);
		System.out.println("Interfaces : "+interfaceCollection);
		System.out.println("Implements : "+implementsCollection.toString());
		System.out.println("Extends : "+extendsCollection.toString());
		System.out.println("Methods : "+methodsCollection.toString());
		System.out.println("Fields : "+fieldsCollection.toString());
		System.out.println("variables : "+variablesCollection.toString());
		System.out.println("Potential Dependancy : "+potentialDependancyList.toString());
		}
	
	public void outputToFile() throws IOException{
		
		File outputFile = new File(System.getProperty("user.dir"),"output.txt");
		String string = this.buildLogic();
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"))) {	 
		   writer.write("@startuml\nskinparam classAttributeIconSize 0\n");
		   writer.write(string);
		   writer.write("@enduml");
		 
			}
	}
	
	public String buildLogic(){
		String write = "";
		for(int i=0;i<classCollection.size();i++){
			String methods = this.getMethods(classCollection.get(i));
			String fields = this.getFields(classCollection.get(i));
			write+="class "+classCollection.get(i)+" {\n";
			write+=fields+"\n";
			write+=methods+"\n";
			write+="}\n";			
		}
		for(int i=0;i<interfaceCollection.size();i++){
			String methods = this.getMethods(interfaceCollection.get(i));
			write+="interface "+interfaceCollection.get(i)+" {\n";
			write+=methods+"\n";
			write+="}\n";			
		}
		
		for(Map.Entry<String, List<String>> entry : extendsCollection.entrySet()){
			if((entry.getValue().isEmpty())){
				continue;
			}
			String value = entry.getValue().toString();
			write+=value.substring(1, value.length()-1)+"<|-- "+entry.getKey()+"\n";
		}
		
		for(Map.Entry<String, List<String>> entry : implementsCollection.entrySet()){
			if((entry.getValue().isEmpty())){
				continue;
			}
			if(entry.getValue().size()>1){
			//	System.out.println(entry.getValue());
				for(int i=0;i<entry.getValue().size();i++){
					//		System.out.println("Values : "+entry.getValue().get(i));
					write+=entry.getValue().get(i)+"<|.."+entry.getKey()+"\n";
				}
			continue;}
			String value = entry.getValue().toString();
		
			write+=value.substring(1, value.length()-1)+"<|.. "+entry.getKey()+"\n";
		}
		
		write+= this.findMultiplicity();
		write+= this.findDependancy();
		return write;
	}
	
	public String getMethods(String classname){
		String write = "";
		for(Map.Entry<String, List<String>> entry : methodsCollection.entrySet()){
			if(entry.getKey().equals(classname)){
			if(entry.getValue().isEmpty()){
				continue;
			}
			if(entry.getValue().size()>1){
				
				for(int j=0;j<entry.getValue().size();j++){
			
					write+=entry.getValue().get(j)+"\n";
				}
			continue;}
	
			write+=entry.getValue().toString().substring(1, entry.getValue().toString().length()-1)+"\n";
				}
		}
	
		return write;
	}
	
	public String getFields(String classname){
		String write = "";
		for(Map.Entry<String, List<String>> entry : fieldsCollection.entrySet()){
			if(entry.getKey().equals(classname)){
				if(!(entry.getValue().isEmpty())){
		
					if(entry.getValue().size()>1){
						
						for(int j=0;j<entry.getValue().size();j++){
					
							write+=entry.getValue().get(j)+"\n";
						}
					continue;}
					write+=entry.getValue().toString().substring(1, entry.getValue().toString().length()-1)+"\n";

				}
			}
		}
		return write;
	}
	
	
	public void findPublicGetSet(){
		for(int i=0;i<checkGetSet.size();i++){
			String classname = checkGetSet.get(i);
			for(Map.Entry<String, List<String>> entry: methodsCollection.entrySet()){
				if(entry.getKey().equals(classname)){
					List<String> mList = entry.getValue();
					for (int x=0;x<mList.size();x++){	
						if(mList.get(x).contains("get")){
							String std = mList.get(x).substring(5,mList.get(x).indexOf("("));
							potentialFieldsCollection.add(std.toLowerCase());
						}
						if(mList.get(x).contains("set")){
							String std = mList.get(x).substring(5,mList.get(x).indexOf("("));
							potentialFieldsCollection.add(std.toLowerCase());
						}
					}	
					for(Map.Entry<String, List<String>> entrylist : fieldsCollection.entrySet()){
						if(entrylist.getKey().equals(classname)){
							for(int y=0;y<entrylist.getValue().size();y++){
								String temp =entrylist.getValue().get(y).substring(3, entrylist.getValue().get(y).indexOf(" :")).toLowerCase();
								if(potentialFieldsCollection.contains(temp)){
								  String tempp=  entrylist.getValue().get(y).replace("-", "+");
								  entrylist.getValue().remove(y); 
								  entrylist.getValue().add(tempp);
								   
								}
							}
						}
					}
				}
				
			}
		}
	}
	
	public String findMultiplicity(){
		String string="";
		List<String[]> checkList = new ArrayList<String[]>();
		
		for(int i=0;i<variablesCollection.size();i++){
			String str = variablesCollection.get(i);
			String[] splits = str.split(" ");
			String array[] = new String[4];

	
			if(classCollection.contains(splits[1]) || interfaceCollection.contains(splits[1])){
		
			
				array[0] = splits[0]; array[1] = splits[1]; array[2] = "0"; array[3] = "1";
				checkList.add(array);
			}
			
			if(splits[1].startsWith("Collection")){

				if(classCollection.contains(splits[1].substring(splits[1].indexOf("<")+1, splits[1].indexOf(">"))) || interfaceCollection.contains(splits[1].substring(splits[1].indexOf("<")+1, splits[1].indexOf(">")))){
				array[0] = splits[0]; array[1] = splits[1].substring(splits[1].indexOf("<")+1, splits[1].indexOf(">")); array[2] = "0"; array[3] = "*";
					checkList.add(array);
				}
			}
		}
		
		for(int p=0;p<checkList.size();p++){
				System.out.println("\n");
					for(int q=p+1;q<checkList.size();q++){
						if(checkList.get(p)[0].equals(checkList.get(q)[1]) && checkList.get(p)[1].equals(checkList.get(q)[0])){
							checkList.get(p)[2] = checkList.get(q)[3];
							checkList.remove(q);
						}
				}
			}
		
		System.out.println("CHeckLIst : ");
		for(int p=0;p< checkList.size();p++){
			System.out.println("\n");
			string+=checkList.get(p)[0]+" "+" \""+checkList.get(p)[2]+"\" - \""+checkList.get(p)[3]+"\" "+checkList.get(p)[1]+"\n";
		}
		return string;
	}
	
	public String findDependancy(){
		String string="";
		Boolean flag=false;
		Map<String,String> tempMap = new HashMap<String,String>();
			
		for(Map.Entry<String, List<String>> entry : potentialDependancyList.entrySet()){
			if(!(entry.getValue().isEmpty())){
			
				
				for(int itr =0;itr<entry.getValue().size();itr++){
					Boolean mulFlag=false;
							for(Map.Entry<String, List<String>> check : fieldsCollection.entrySet()){
								if(check.getKey().equals(entry.getKey())){
									if(!(check.getValue().isEmpty())){
										Iterator iterator = check.getValue().iterator();
									for(int in =0; in<check.getValue().size();in++){
										String temp = check.getValue().get(in);
										String[] compareWith = temp.split(" ");
											if(entry.getValue().get(itr).equals((compareWith)[4]+" "+compareWith[2])){
												flag=true;
											}
										}
									}
								}
							}
							if(flag){
							}else
							{	
								writeDependancy(entry.getKey(),entry.getValue().get(itr).split(" ")[0]);
							}  
					}		
				}
			}
		Iterator jj =dependancyList.iterator();
		while(jj.hasNext()){
			String [] aray = jj.next().toString().split(" ");
			string+=aray[0] + " ..> "+aray[1]+"\n";
		}
		return string;
	}

	public void writeDependancy(String key, String string) {
			if(classCollection.contains(string) || interfaceCollection.contains(string)){
				dependancyList.add(key+" "+string);
			}
	}
	
	
}
