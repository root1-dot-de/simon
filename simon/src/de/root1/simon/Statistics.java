package de.root1.simon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Statistics {
	
	private int incomingInvocationsPerSecond = -1;
	private int outgoingInvocationsPerSecond = -1;
	
	private Map<String, Long> roundTripTimes = Collections.synchronizedMap(new HashMap<String, Long>());
	
	protected void setIncomingInvocationsPerMilli(int invocations) {
		incomingInvocationsPerSecond = invocations;
	}
	
	public int getIncomingInvocationsPerMilli() {
		return incomingInvocationsPerSecond;
	}

	protected void setOutgoingInvocationsPerMilli(int invocations) {
		outgoingInvocationsPerSecond = invocations;
	}
	
	public int getOutgoingInvocationsPerMilli(){
		return outgoingInvocationsPerSecond;
	}
	
	protected void setRtt(String key, long rtt){
		if (rtt>=0) {
			roundTripTimes.put(key, rtt);
		} else {
			roundTripTimes.remove(key);
		}
	}
	
	public Map<String, Long> getRtts(){
		return roundTripTimes;
	}

}
