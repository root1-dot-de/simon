package de.root1.simon.codec.base;
/**
 * Provides Simon protocol constants.
 *
 * @author ACHR
 */
public class SimonStdProtocolConstants {
	
    public static final int TYPE_LEN = 2;

    public static final int SEQUENCE_LEN = 4;

    public static final int HEADER_LEN = TYPE_LEN + SEQUENCE_LEN;
    
    // ---------------------

	public static final int LOOKUP_MSG = 0;

	public static final int LOOKUP_RETURN_MSG = 1;
	
	public static final int INVOKE_MSG = 2;
	
	public static final int INVOKE_RETURN_MSG = 3;


    private SimonStdProtocolConstants() {
    }
}
