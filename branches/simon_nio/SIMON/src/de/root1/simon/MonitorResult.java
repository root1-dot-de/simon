package de.root1.simon;

public class MonitorResult extends Object {
	
	private boolean waiting = false;
	
	public void waitForResult() throws InterruptedException{
		waiting = true;
		synchronized (this) {
			wait();
		}
	}
	
	public void wakeUp(){
		while (!waiting) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		synchronized (this) {
			notify();
		}
	}

}
