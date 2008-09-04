package de.root1.simon.tests.transferDatatypes.shared;

import java.io.Serializable;

public class Dummyobject implements Serializable {
	
	  public static final int UNKNOWN = 0;
	  public static final int LOGIN = 1;
	  public static final int LOGOUT = 2;

	  private String name ="name";
	  private String address = "address";
	  private int port = 0;
	  private int state = 1;
	  private int type = 2;
	  private String[] lognames = {"a", "b", "c"};

}
