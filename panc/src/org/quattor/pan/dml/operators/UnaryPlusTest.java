/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/UnaryPlusTest.java $
 $Id: UnaryPlusTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class UnaryPlusTest extends AbstractOperationTestUtils {

	@Test
	public void testLong() throws SyntaxException {

		long va = 3;

		// Execute the operations.
		Operation uplus = UnaryPlus.newOperation(null, LongProperty
				.getInstance(va));

		// Pull the value off the stack.
		assertTrue(uplus instanceof LongProperty);
		LongProperty result = (LongProperty) uplus;
		long lvalue = result.getValue().longValue();
		assertTrue(va == lvalue);
	}

	@Test
	public void testDouble() throws SyntaxException {

		double va = 3.0;

		// Execute the operations.
		Operation uplus = UnaryPlus.newOperation(null, DoubleProperty
				.getInstance(va));

		// Pull the value off the stack.
		assertTrue(uplus instanceof DoubleProperty);
		DoubleProperty result = (DoubleProperty) uplus;
		double dvalue = result.getValue().doubleValue();
		assertTrue(va == dvalue);
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		UnaryPlus.newOperation(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		LongProperty i = LongProperty.getInstance(1L);
		UnaryPlus.newOperation(null, i, i, i);
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidArgument() throws SyntaxException {
		HashResource h = new HashResource();
		UnaryPlus.newOperation(null, h);
	}

}
