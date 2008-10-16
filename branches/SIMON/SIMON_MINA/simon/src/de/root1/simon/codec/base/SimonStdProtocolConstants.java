package de.root1.simon.codec.base;
/**
 * Provides SumUp protocol constants.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 576217 $, $Date: 2007-09-17 01:55:27 +0200 (lun, 17 sep 2007) $
 */
public class SimonStdProtocolConstants {
    public static final int TYPE_LEN = 2;

    public static final int SEQUENCE_LEN = 4;

    public static final int HEADER_LEN = TYPE_LEN + SEQUENCE_LEN;

	public static final int LOOKUP_MSG = 0;

	public static final int LOOKUP_RETURN_MSG = 1;


    private SimonStdProtocolConstants() {
    }
}
