package uk.ac.soton.ldanalytics.replay.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import uk.ac.soton.ldanalytics.replay.transform.RdfTransformer;

import com.hp.hpl.jena.rdf.model.Model;

public class Producer implements Configurable {
	
	String timeFormat = null;
	String outputFile = null;
	int speed = 1;
	
	List<String> colHeadings = new ArrayList<String>();
	BufferedReader br = null;
	String currentLine = null;
	long currentTimeStamp = 0;
	Map<String,String> data = new HashMap<String,String>();
	String replayColHeading = null;
	
	RdfTransformer rdfTransformer = null;

	public void loadJsonConfig(JSONObject jsonSource) {
		String formatFile = jsonSource.getString("format_file");
		String sourceFile = jsonSource.getString("source_file");
		String transformFile = jsonSource.getString("transform_file");
		String startTime = jsonSource.getString("start_time");
		outputFile = jsonSource.getString("output_file");
		speed = jsonSource.getInt("speed");
		timeFormat = jsonSource.getString("time_format");
		replayColHeading = jsonSource.getString("replay_col_heading");
		
		loadFormat(formatFile);
		loadSource(sourceFile);
		currentTimeStamp = parseTime(startTime);
		rdfTransformer = new RdfTransformer(transformFile);
	}
	
	public String getOutputFileName() {
		return outputFile;
	}
	
	private long parseTime(String time) {
		long timeStamp = 0;
		if(timeFormat.equals("unix")) {
			timeStamp = Long.parseLong(time) * 1000L;
		}
		return timeStamp;
	}
	
	public void close() {
		if(br!=null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadSource(String sourceFile) {
		try {
			br = new BufferedReader(new FileReader(sourceFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean next() {
		Boolean isNotNull = true;
		try {
			currentLine = br.readLine();
			if(currentLine!=null) {
				String[] parts = currentLine.split(",");
				for(int i=0;i<parts.length;i++) {
					data.put(colHeadings.get(i), parts[i]);
				}
			} else {
				isNotNull = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isNotNull;
	}
	
	public long getIntervalToNext() {
		long newTimeStamp = parseTime(data.get(replayColHeading));
		long interval = newTimeStamp - currentTimeStamp;
		currentTimeStamp = newTimeStamp;
		return interval/speed;
	}
	
	public void transform() {
		rdfTransformer.transform(data);
	}
	
	public Model getModel() {
		return rdfTransformer.outputModel();
	}
	
	public void getTurtle(PrintStream ps) {
		rdfTransformer.outputTurtle(ps);
	}
	
	public void closeSource() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFormat(String formatFile) {
		String format = null;
		//load format
		try {
			format = FileUtils.readFileToString(new File(formatFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(format!=null) {
			String[] parts = format.split(",");
			for(int i=0;i<parts.length;i++) {
				colHeadings.add(parts[i]);
			}
		}
	}

}
