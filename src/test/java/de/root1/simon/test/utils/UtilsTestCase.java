package de.root1.simon.test.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import de.root1.simon.utils.Utils;

public class UtilsTestCase {

	@Test
	public void testFindAllInterfaces() {
		Class<?>[] interfases = Utils.findAllInterfaces(InhertedClass.class);
		assertNotNull(interfases);
		assertEquals(2, interfases.length);
	}

}
