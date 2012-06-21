/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.custominvoketimeout;

import java.lang.reflect.Method;

/**
 *
 * @author achristian
 */
public class Test {

    public static void main(final String[] args) {
        Method[] methods = A.class.getMethods();
        for (int idx = 0; idx < methods.length; idx++) {
            System.out.println(methods[idx] + " declared by "
                    + methods[idx].getDeclaringClass());
        }
        
        Method[] methodsB = B.class.getMethods();
        for (int idx = 0; idx < methodsB.length; idx++) {
            System.out.println(methodsB[idx] + " declared by "
                    + methodsB[idx].getDeclaringClass());
        }
        
        Method[] methodsC = C.class.getMethods();
        for (int idx = 0; idx < methodsC.length; idx++) {
            System.out.println(methodsC[idx] + " declared by "
                    + methodsC[idx].getDeclaringClass());
        }
        
        System.out.println(methods[1].equals(methodsC[0]));
    }
}
