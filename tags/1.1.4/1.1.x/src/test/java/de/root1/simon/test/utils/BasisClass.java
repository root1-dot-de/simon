package de.root1.simon.test.utils;

import de.root1.simon.annotation.SimonRemote;

@SimonRemote(value={BasisInterface.class})
public class BasisClass implements ExtendedBasisInterface {
	private static final long serialVersionUID = 7059327535633065297L;
}
