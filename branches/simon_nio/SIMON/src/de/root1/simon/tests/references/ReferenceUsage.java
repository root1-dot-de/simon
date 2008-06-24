package de.root1.simon.tests.references;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

class ReferenceUsage {
	
	public static void main(String args[]) {
		
		hold();
		release();
		
	}

	public static void hold() {
		System.out.println("Example of incorrectly holding a strong reference");
		
		//Create an object
		MyObject obj = new MyObject();
		System.out.println("object is " + obj);

		//Create a reference queue
		ReferenceQueue rq = new ReferenceQueue();

		//Create a weakReference to obj and associate our reference queue
		WeakReference wr = new WeakReference(obj, rq);

		System.out.println("The weak reference is " + wr);

		//Check to see if it's on the ref queue yet
		System.out.println("Polling the reference queue returns " + rq.poll());
		System.out.println("Getting the referent from the weak reference returns " + wr.get());

		System.out.println("Calling GC");
		obj = null;
		System.gc();
		System.out.println("Polling the reference queue returns " + rq.poll());
		System.out.println("Getting the referent from the weak reference returns " + wr.get());
	}

	public static void release() {
		System.out.println("");
		System.out.println("Example of correctly releasing a strong reference");
		
		//Create an object
		MyObject obj = new MyObject();
		System.out.println("object is " + obj);

		//Create a reference queue
		ReferenceQueue rq = new ReferenceQueue();

		//Create a weakReference to obj and associate our reference queue
		WeakReference wr = new WeakReference(obj, rq);

		System.out.println("The weak reference is " + wr);

		//Check to see if it's on the ref queue yet
		System.out.println("Polling the reference queue returns " + rq.poll());
		System.out.println("Getting the referent from the weak reference returns " + wr.get());

		System.out.println("Set the obj reference to null and call GC");
		obj = null;
		System.gc();
		System.out.println("Polling the reference queue returns " + rq.poll());
		System.out.println("Getting the referent from the weak reference returns " + wr.get());
	}
}
