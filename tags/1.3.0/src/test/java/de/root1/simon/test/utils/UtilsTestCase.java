package de.root1.simon.test.utils;

import de.root1.simon.Simon;
import static org.junit.Assert.*;

import org.junit.Test;

import de.root1.simon.utils.Utils;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsTestCase {
    
    private final Logger logger = LoggerFactory.getLogger(UtilsTestCase.class);

    @Test
    public void testFindAllInterfaces() {
        Class<?>[] interfaces = Utils.findAllRemoteInterfaces(InhertedClass.class);
        assertNotNull(interfaces);
        assertEquals(1, interfaces.length);

        Class<?>[] annotatedInterfaces = Utils.findAllRemoteInterfaces(InterfaceWithRemoteannotationImpl.class);
        assertNotNull("Class that implements a SimonRemote annotated interface, must be findable with 'findAllRemoteInterfaces()'", annotatedInterfaces);
        assertEquals("Number of found interfaces do not meet the expectation", 1, annotatedInterfaces.length);
        assertEquals("Class that implements a SimonRemote annotated interface, must be findable with 'findAllRemoteInterfaces()'", InterfaceWithRemoteannotation.class, annotatedInterfaces[0]);

        Class<?>[] annotatedInterfaces2 = Utils.findAllRemoteInterfaces(InterfaceWithRemoteannotationImpl2.class);
        assertNotNull("Class that implements a extended SimonRemote annotated interface, must be findable with 'findAllRemoteInterfaces()'", annotatedInterfaces2);
        assertEquals("Number of found interfaces do not meet the expectation 2", 2, annotatedInterfaces2.length);
        List<Class<?>> asList = Arrays.asList(annotatedInterfaces2);
        
        logger.info("Found Interfaces: "+Arrays.toString(annotatedInterfaces2));
        
        assertTrue("ExtendedInterfaceWithRemoteannotation that implements a extended SimonRemote annotated interface, must be findable with 'findAllRemoteInterfaces()'", asList.contains(ExtendedInterfaceWithRemoteannotation.class));
        assertTrue("InterfaceWithRemoteannotation that implements a extended SimonRemote annotated interface, must be findable with 'findAllRemoteInterfaces()'", asList.contains(InterfaceWithRemoteannotation.class));
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
