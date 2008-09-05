package de.root1.simon.tests.transferDatatypes.shared;

import java.io.Serializable;

public class Dummyobject implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final int UNKNOWN = 0;
	public static final int LOGIN = 1;
	public static final int LOGOUT = 2;

	@SuppressWarnings("unused")
	private String name = "name";
	@SuppressWarnings("unused")
	private String address = "address";
	@SuppressWarnings("unused")
	private int port = 0;
	@SuppressWarnings("unused")
	private int state = 1;
	@SuppressWarnings("unused")
	private int type = 2;
	@SuppressWarnings("unused")
	private String[] lognames = { "a", "b", "c" };

}
