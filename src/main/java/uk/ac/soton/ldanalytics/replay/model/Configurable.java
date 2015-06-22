package uk.ac.soton.ldanalytics.replay.model;

import org.json.JSONObject;

public interface Configurable {
	public void loadJsonConfig(JSONObject jsonSource);
}
