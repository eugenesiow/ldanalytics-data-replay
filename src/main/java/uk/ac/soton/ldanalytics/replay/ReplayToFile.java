package uk.ac.soton.ldanalytics.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import uk.ac.soton.ldanalytics.replay.model.Producer;
import uk.ac.soton.ldanalytics.replay.model.Producers;

public class ReplayToFile {
	public static void main(String[] args) {
		Producers producers = new Producers();
		
		//load config
		try {
			producers.loadJsonConfig(new JSONObject(FileUtils.readFileToString(new File("config/config-windows-sensors.json"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(Producer producer:producers.getProducers()) {
			PrintStream ps = null;
			long start = System.currentTimeMillis();
			try {
				ps = new PrintStream(new File(producer.getOutputFileName()));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			while(producer.next()) {
				producer.transform();
				producer.getTurtle(ps);
				ps.flush();
			}
			ps.close();
			long time = System.currentTimeMillis() - start;
			System.out.println("File "+producer.getOutputFileName()+" generated in " + time + "ms.");
		}
	}
}
