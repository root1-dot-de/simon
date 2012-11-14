/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon.test.phantomref;

import de.root1.simon.Lookup;
import de.root1.simon.LookupTableMBean;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestPhantomRef {

    private final Logger logger = LoggerFactory.getLogger(TestPhantomRef.class);
    
    public TestPhantomRef() {
    }

    //@BeforeClass
    //public static void setUpClass() throws Exception {
    //}

    //@AfterClass
    //public static void tearDownClass() throws Exception {
    //}

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPhantomRefRelease() {


        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry();
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("localhost");

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");
            roiRemote.setCallback(new ClientCallbackImpl());
            
            logger.info("1 ------------------------------------------------------");
            
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectInstance> queryMBeans = mbs.queryMBeans(null, null);
            
            LookupTableMBean ltmbean = null;
            long sessionId=-1;
            String refId=null;
            for (ObjectInstance objectInstance : queryMBeans) {
                if (objectInstance.getObjectName().getDomain().equals("de.root1.simon") &&
                        objectInstance.getObjectName().getKeyProperty("subType").equals(LookupTableMBean.MBEAN_SUBTYPE_CLIENT)) {
                    System.out.println("Found it: "+objectInstance);
                    
                    ltmbean = (LookupTableMBean) MBeanServerInvocationHandler.newProxyInstance(mbs, objectInstance.getObjectName(), LookupTableMBean.class, false);
                    break;
                }
            }
            if (ltmbean!=null) {
                assertTrue("There must be one session with a ref: "+ltmbean.getNumberOfRemoteRefSessions(), ltmbean.getNumberOfRemoteRefSessions()==1);
                
                sessionId = ltmbean.getRemoteRefSessions()[0];
                
                assertTrue("There must be one ref on this session("+sessionId+")", ltmbean.getRefIdsForSession(sessionId).length==1);
                
                refId = ltmbean.getRefIdsForSession(sessionId)[0];
                assertTrue("Refcount must be 1 after setting callback", ltmbean.getRemoteRefCount(sessionId, refId)==1);
            }
            
            Thread.sleep(2000);
            
            logger.info("2 ------------------------------------------------------");
            
            // kill casllback reference to give GC the chance to cleanup
            roiRemote.setCallback(null);
            
            logger.info("3 ------------------------------------------------------");
            
            // ensure GC is running at least once
            for (int i=0;i<10;i++){
                System.gc();
                Thread.sleep(500);
            }
            
            assertTrue("There must not be any remote ref after clearing the callback", ltmbean.getNumberOfRemoteRefSessions()==0);

            logger.info("4 ------------------------------------------------------");
            
            lookup.release(roiRemote);
            
            r.unbind("roi");
            r.stop();
            
            // TODO how to very it worked?! Add JMX interface?

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

    }
    
    @Test
    public void testPhantomRefReleaseServerCallback() {

        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry();
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("127.0.0.1");

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");
            for(int i=0;i<1;i++) {
                ServerCallback serverCallback = roiRemote.getServerCallback();
                serverCallback.sayHelloToServer();
                System.gc();
            }

            Thread.sleep(1000);
            
            logger.info("1 ------------------------------------------------------");
//            
//            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//            Set<ObjectInstance> queryMBeans = mbs.queryMBeans(null, null);
//            
//            LookupTableMBean ltmbean = null;
//            long sessionId=-1;
//            String refId=null;
//            for (ObjectInstance objectInstance : queryMBeans) {
//                if (objectInstance.getObjectName().getDomain().equals("de.root1.simon") &&
//                        objectInstance.getObjectName().getKeyProperty("isServer").equals("false")) {
//                    System.out.println("Found it: "+objectInstance);
//                    
//                    ltmbean = (LookupTableMBean) MBeanServerInvocationHandler.newProxyInstance(mbs, objectInstance.getObjectName(), LookupTableMBean.class, false);
//                    break;
//                }
//            }
//            if (ltmbean!=null) {
//                assertTrue("There must be one session with a ref: "+ltmbean.getNumberOfRemoteRefSessions(), ltmbean.getNumberOfRemoteRefSessions()==1);
//                
//                sessionId = ltmbean.getRemoteRefSessions()[0];
//                
//                assertTrue("There must be one ref on this session("+sessionId+")", ltmbean.getRefIdsForSession(sessionId).length==1);
//                
//                refId = ltmbean.getRefIdsForSession(sessionId)[0];
//                assertTrue("Refcount must be 1 after setting callback", ltmbean.getRemoteRefCount(sessionId, refId)==1);
//            }
            
            Thread.sleep(2000);
            
            lookup.release(roiRemote);
            
            r.unbind("roi");
            r.stop();
            
            // TODO how to very it worked?! Add JMX interface?

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }

    }
    
}