package uk.ac.soton.ldanalytics.replay.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Producers implements Configurable {
	
	List<Producer> producersList = new ArrayList<Producer>();

	public void loadJsonConfig(JSONObject jsonSource) {
		if(jsonSource!=null) {
			JSONArray producers = jsonSource.getJSONArray("producers");
			for(int i=0;i<producers.length();i++) {
				Producer producer = new Producer();
				producer.loadJsonConfig(producers.getJSONObject(i));
				producersList.add(producer);
			}
		}
	}
	
	public List<Producer> getProducers() {
		return producersList;
	}

}
