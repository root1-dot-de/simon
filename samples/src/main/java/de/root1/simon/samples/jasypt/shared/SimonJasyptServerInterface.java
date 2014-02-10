/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.jasypt.shared;

/**
 *
 * @author achristian
 */
public interface SimonJasyptServerInterface {
    
    public static final String NAME = SimonJasyptServerInterface.class.getCanonicalName();

    public String sayHelloTo(String name);
    
}
