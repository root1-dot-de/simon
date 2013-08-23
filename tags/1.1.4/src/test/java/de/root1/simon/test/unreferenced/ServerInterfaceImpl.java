/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.unreferenced;

// mark this class as a remote class and export all methods known in ServerInterface

import de.root1.simon.annotation.SimonRemote;
import java.util.ArrayList;
import java.util.List;

@SimonRemote(value = {ServerInterface.class})
public class ServerInterfaceImpl implements ServerInterface {

    private static final long serialVersionUID = 1L;

    // this is where all user sessions are stored
    private static List<SessionInterface> userSessions = new ArrayList<SessionInterface>();

    @Override
    public SessionInterface login(String user, ClientCallbackInterface clientCallback) {
        System.out.println("login. user="+user);
        clientCallback.callback("Login is in progress ...");
        System.out.flush();
        Session session = new Session(user, this);
        userSessions.add(session);
        clientCallback.callback("Session is created ... Now "+userSessions.size()+" users are online ...");
        System.out.println("Session created for user "+user+". Now "+userSessions.size()+" users are online ...");
        return session;
    }

    // if a session get's unreferenced, the session is removed from the list
    void removeUserSession(Session userSession) {
        userSessions.remove(userSession);
        System.out.println("Removed user "+userSession.getUsername()+" from sessionlist. "+userSessions.size()+" user are online.");
    }
    
    public static int getSessionCount() {
        return userSessions.size();
    }

}
