package de.root1.simon.test.utils;

import de.root1.simon.annotation.SimonRemote;

@SimonRemote(value={InhertedInterface.class})
public class ExtendedBasisClass extends BasisClass implements InhertedInterface {
	private static final long serialVersionUID = -388850163638247799L;
}
