package de.root1.simon.test.utils;

import de.root1.simon.Simon;
import static org.junit.Assert.*;

import org.junit.Test;

import de.root1.simon.utils.Utils;

public class UtilsTestCase {

    @Test
    public void testFindAllInterfaces() {
        Class<?>[] interfases = Utils.findAllRemoteInterfaces(InhertedClass.class);
        assertNotNull(interfases);
        assertEquals(2, interfases.length);
    }

    @Test
    public void testIsValidRemote() {

        MarkerInterface mc = new MarkerClass();
        MarkerInterface markedAsRemote = (MarkerInterface) Simon.markAsRemote(mc);

        assertTrue("A SimonRemoteMarker instance normally is a valid simon remote", Utils.isValidRemote(markedAsRemote));
        assertFalse("A SimonRemoteMarker instance normally is a valid simon remote", Utils.isValidRemote(this));

        assertTrue("A class that implements an interface that inherits SimonRemte IF must be a valid simon remote", Utils.isValidRemote(new BasisClass()));

    }
}
