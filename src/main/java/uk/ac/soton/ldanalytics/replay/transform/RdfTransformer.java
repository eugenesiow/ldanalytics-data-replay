package uk.ac.soton.ldanalytics.replay.transform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.json.JSONArray;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class RdfTransformer {
	
	private Map<String,String> uuidMap = new HashMap<String,String>();
	private List<String> statements = new ArrayList<String>();
	private Model model = null;

	public RdfTransformer(String transformFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(transformFile));
			
			String line = "";
			while((line=br.readLine())!=null) {
				line = line.trim();
				if(line.startsWith("var:")) {
					String[] varParts = line.split(":");
					if(varParts.length>2) {
						if(varParts[2].equals("UUID")) {
							uuidMap.put(varParts[1], null);
						}
					}
				} else {
					statements.add(line);
				}
			}
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void transform(Map<String,String> data) {
		initUUID();
		model = ModelFactory.createDefaultModel();
		for(String statement:statements) {
			String[] stParts = statement.split("\\s+");
			if(stParts.length>2) {
				model.add(processPart(stParts[0],data).asResource(), processPart(stParts[1],data).as(Property.class), processPart(stParts[2],data));
			}
		}
		
	}
	
	public void outputTurtle(PrintStream ps) {
		RDFDataMgr.write(ps, model, Lang.TURTLE) ;
	}
	
	public Model outputModel() {
		return model;
	}

	private RDFNode processPart(String part, Map<String,String> data) {
		part = part.trim();
		String type = null;
		//check if uri
		if(part.startsWith("<") && part.endsWith(">")) {
			type = "uri";
		} //check if literal
		else if(part.startsWith("\"") && part.endsWith("\"")) {
			type = "literal";
		}
		//remove the uri or literal indicators, brackets or quotes
		part = part.substring(1,part.length()-1);
		//has a var ref
		if(part.contains("{")&&part.contains("}")) {
			Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(part);
			while(m.find()) {
				if(uuidMap.containsKey(m.group(1))) {
					part = part.replace("{"+m.group(1)+"}", uuidMap.get(m.group(1)));
				}			   
			}
		}
		//has a data ref
		if(part.contains("(") && part.contains(")")) {
			Matcher m = Pattern.compile("\\((.*?)\\)").matcher(part);
			while(m.find()) {
				String subPart = m.group(1);
				String properties = null;
				if(subPart.contains("[") && subPart.contains("]")) {
					properties = subPart.substring(subPart.indexOf("["),subPart.indexOf("]")+1);
					subPart = subPart.substring(0,subPart.indexOf("["));
				}
			    part = part.replace("("+m.group(1)+")", formatPart(data.get(subPart),properties));
			    
			    //check special data types for literals
			    if(properties!=null) {
			    	JSONArray props = new JSONArray(properties);
			    	if(props.length()>2) {
			    		if(props.getString(0).equals("time") && props.getString(2).equals("xsdDateTime")) {
			    			type = "literal,xsdDateTime";
			    		}
			    	}
			    }
			}
		}
		RDFNode node = null;
		if(type!=null) {
			if(type.equals("literal")) {
				node = model.createLiteral(part);
			} else if(type.equals("uri")) {
				node = model.createResource(part);
			} else if(type.equals("literal,xsdDateTime")) {
				node = model.createTypedLiteral(part, XSDDatatype.XSDdateTime);
			}
		}
		
		return node;
	}

	private String formatPart(String part, String properties) {
		if(properties!=null) {
			JSONArray props = new JSONArray(properties);
			if(props.length()>0) {
				String function = props.getString(0);
				if(function.equals("time")) {
					String from = props.getString(1);
					
					String to = props.getString(2);
					
					Date currentDate = null;
					
					if(from.equals("unix")) {
						currentDate = new Date(Long.parseLong(part)*1000);
					}
					if(to.equals("xsdDateTime")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						part = sdf.format(currentDate);
					};
				}
			}
		}
		return part;
	}

	private void initUUID() {
		for(Entry<String,String> uuid:uuidMap.entrySet()) {
			uuid.setValue(UUID.randomUUID().toString());
		}
	}

}
