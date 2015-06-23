package uk.ac.soton.ldanalytics.replay.transform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RdfTransformer {
	
	private Map<String,String> uuidMap = new HashMap<String,String>();
	private List<String> statements = new ArrayList<String>();

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
		for(String statement:statements) {
			String[] stParts = statement.split("\\s+");
			if(stParts.length>2) {
				System.out.println(processPart(stParts[0]));
			}
		}
	}

	private String processPart(String part) {
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
			    System.out.println(m.group(1));
			}
		}
		//has a data ref
		if(part.contains("(") && part.contains("")) {
			Matcher m = Pattern.compile("\\((.*?)\\)").matcher(part);
			while(m.find()) {
			    System.out.println(m.group(1));
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
