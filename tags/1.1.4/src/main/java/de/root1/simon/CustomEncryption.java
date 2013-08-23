/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

/**
 *
 * @author achristian
 */
public interface CustomEncryption {
    
    public byte[] encrypt(byte[] data);
    public byte[] decrypt(byte[] data);
    
}
