package de.root1.simon.codec.messages;
/**
 * Provides Simon protocol constants.
 *
 * @author ACHR
 */
public class SimonMessageConstants {
	
    public static final int TYPE_LEN = 2;
    public static final int SEQUENCE_LEN = 4;
    public static final int HEADER_LEN = TYPE_LEN + SEQUENCE_LEN;
    
    // ---------------------

	public static final int MSG_LOOKUP 				= 0;
	public static final int MSG_LOOKUP_RETURN 		= 1;
	
	public static final int MSG_INVOKE 				= 2;
	public static final int MSG_INVOKE_RETURN 		= 3;

	public static final int MSG_TOSTRING 			= 4;
	public static final int MSG_TOSTRING_RETURN 	= 5;

	public static final int MSG_EQUALS 				= 6;
	public static final int MSG_EQUALS_RETURN 		= 7;
	
	public static final int MSG_HASHCODE 			= 8;
	public static final int MSG_HASHCODE_RETURN 	= 9;

    private SimonMessageConstants() {
    }
}
