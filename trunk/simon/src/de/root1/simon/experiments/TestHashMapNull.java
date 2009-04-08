package de.root1.simon.experiments;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.WeakHashMap;

public class TestHashMapNull {
	
	public static void main(String[] args) {
		HashMap<String, Long> methodHashs = new HashMap<String, Long>();
		
		methodHashs.put("haha", 1l);
		
		long l = methodHashs.get("huhu");
		
		Long o = methodHashs.get("huhu");
		
		if (o!=null)
			System.out.println(o.longValue());
		
		o = methodHashs.get("haha");
		
		if (o!=null)
			System.out.println(o.longValue());
	}

}
