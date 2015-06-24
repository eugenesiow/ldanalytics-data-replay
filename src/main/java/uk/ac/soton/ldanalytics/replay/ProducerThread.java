package uk.ac.soton.ldanalytics.replay;

import java.util.concurrent.atomic.AtomicInteger;

import uk.ac.soton.ldanalytics.replay.model.Producer;

public class ProducerThread implements Runnable {
	private Producer producer = null;
	private AtomicInteger lock = null;
	
	public ProducerThread(Producer producer, AtomicInteger lock) {
		this.producer = producer;
		this.lock = lock;
	}
	
	public void run() {
		while(lock.get()==1) {
			if(producer.next()) {
				try {
					//sleeps till the time to produce this record
					Thread.sleep(producer.getIntervalToNext());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				producer.transform();
			} else {
				//if producer is finished with the source file it signals to shutdown
				lock.set(0);
			}
		}
		producer.close();
		System.out.println("End Generator Thread");
	}
	
}
