package de.root1.simon.tests;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import de.root1.simon.Statics;
import de.root1.simon.utils.Utils;

public class WrapValueTest {
	
//	public static void main(String[] args) throws IOException {
//		
////		Statics.DEBUG_MODE = true;
//		ByteBuffer bb = ByteBuffer.allocate(1);
//		
//		Method[] declaredMethods = String.class.getDeclaredMethods();
//		int i = 0;
//		// searching method
//		for (Method method : declaredMethods) {
//			if (method.toString().equalsIgnoreCase("public int java.lang.String.indexOf(java.lang.String,int)")) break;
//			i++;
//		}
//		Method m = declaredMethods[i];
//
//		Class<?>[] parameterTypes = m.getParameterTypes();
//		for (Class<?> class1 : parameterTypes) {
//			System.out.println(class1);
//		}
//		
//		Object[] arg = new Object[2];
//		
//		StringBuffer sb = new StringBuffer();
//		
//		for(i = 0; i<1280000;i++){
//			
//			sb.append("a");
//			
//		}
//		arg[0] = new String(sb.toString());
//		
//		System.out.println("Length="+((String)arg[0]).length());
//		arg[1] = new Integer(2);
//		long start = System.currentTimeMillis();
//		for (i = 0; i < parameterTypes.length; i++) {
//            bb = Utils.wrapValue(parameterTypes[i], arg[i], bb);
//        }
//		System.out.println("duration="+(System.currentTimeMillis()-start)+"ms");
//		
//		start = System.currentTimeMillis();
//		for (i = 0; i < parameterTypes.length; i++) {
//            bb = Utils.wrapValue(parameterTypes[i], arg[i], bb);
//        }
//		System.out.println("duration="+(System.currentTimeMillis()-start)+"ms");
//		
//		start = System.currentTimeMillis();
//		for (i = 0; i < parameterTypes.length; i++) {
//            bb = Utils.wrapValue(parameterTypes[i], arg[i], bb);
//        }
//		System.out.println("duration="+(System.currentTimeMillis()-start)+"ms");
//		start = System.currentTimeMillis();
//		for (i = 0; i < parameterTypes.length; i++) {
//            bb = Utils.wrapValue(parameterTypes[i], arg[i], bb);
//        }
//		System.out.println("duration="+(System.currentTimeMillis()-start)+"ms");
//		start = System.currentTimeMillis();
//		for (i = 0; i < parameterTypes.length; i++) {
//            bb = Utils.wrapValue(parameterTypes[i], arg[i], bb);
//        }
//		System.out.println("duration="+(System.currentTimeMillis()-start)+"ms");
//		
//		
//		System.out.println(bb.capacity());
//		System.out.println(bb.position());
//		
//		
//	}

}
