package uk.ac.soton.ldanalytics.replay;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import uk.ac.soton.ldanalytics.replay.model.Producer;
import uk.ac.soton.ldanalytics.replay.model.Producers;

public class Replay {

	public static void main(String[] args) {
		Producers producers = new Producers();
		
		//load config
		try {
			producers.loadJsonConfig(new JSONObject(FileUtils.readFileToString(new File("config/config.json"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<AtomicInteger> locks = new ArrayList<AtomicInteger>();
		for(Producer producer:producers.getProducers()) {
			AtomicInteger lock = new AtomicInteger(1);
			locks.add(lock);
			Runnable producerThread = new ProducerThread(producer,lock);
			new Thread(producerThread).start();
		}
		
		//scan for input to break
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
		
			while(true) {
				System.out.println("Enter 'exit' to end:");
		        String s = br.readLine();
		        if(s.equals("exit")) {
		        	for(AtomicInteger lock:locks) {
		        		lock.set(0);
		        	}
		        	break;
		        }
			}
			
			System.out.println("Exiting...");
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

}
