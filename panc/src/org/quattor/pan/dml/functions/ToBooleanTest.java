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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/ToBooleanTest.java $
 $Id: ToBooleanTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

public class ToBooleanTest extends BuiltInFunctionTestUtils {

	@Test
	public void checkGetInstance() {
		checkClassRequirements(ToBoolean.class);
	}

	@Test
	public void convertString() throws SyntaxException {

		Element r1 = runDml(ToBoolean.getInstance(null, StringProperty
				.getInstance("false")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		BooleanProperty s1 = (BooleanProperty) r1;
		assertFalse(s1.getValue().booleanValue());

		r1 = runDml(ToBoolean.getInstance(null, StringProperty
				.getInstance("FALSE")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		s1 = (BooleanProperty) r1;
		assertFalse(s1.getValue().booleanValue());

		r1 = runDml(ToBoolean.getInstance(null, StringProperty.getInstance("")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		s1 = (BooleanProperty) r1;
		assertFalse(s1.getValue().booleanValue());

		r1 = runDml(ToBoolean.getInstance(null, StringProperty
				.getInstance("ok")));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		s1 = (BooleanProperty) r1;
		assertTrue(s1.getValue().booleanValue());
	}

	@Test
	public void convertLong() throws SyntaxException {

		Element r1 = runDml(ToBoolean.getInstance(null, LongProperty
				.getInstance(0L)));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		BooleanProperty s1 = (BooleanProperty) r1;
		assertFalse(s1.getValue().booleanValue());

		r1 = runDml(ToBoolean.getInstance(null, LongProperty.getInstance(1L)));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		s1 = (BooleanProperty) r1;
		assertTrue(s1.getValue().booleanValue());
	}

	@Test
	public void convertDouble() throws SyntaxException {

		Element r1 = runDml(ToBoolean.getInstance(null, DoubleProperty
				.getInstance(0.0)));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		BooleanProperty s1 = (BooleanProperty) r1;
		assertFalse(s1.getValue().booleanValue());

		r1 = runDml(ToBoolean
				.getInstance(null, DoubleProperty.getInstance(1.0)));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		s1 = (BooleanProperty) r1;
		assertTrue(s1.getValue().booleanValue());
	}

	@Test
	public void convertBooleans() throws SyntaxException {

		Element r1 = runDml(ToBoolean.getInstance(null, BooleanProperty
				.getInstance(false)));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		BooleanProperty s1 = (BooleanProperty) r1;
		assertFalse(s1.getValue().booleanValue());

		r1 = runDml(ToBoolean.getInstance(null, BooleanProperty
				.getInstance(true)));

		// Check result.
		assertTrue(r1 instanceof BooleanProperty);
		s1 = (BooleanProperty) r1;
		assertTrue(s1.getValue().booleanValue());
	}

	@Test(expected = EvaluationException.class)
	public void illegalArguments() throws SyntaxException {
		runDml(ToBoolean.getInstance(null, new ListResource()));
	}

	@Test(expected = SyntaxException.class)
	public void tooFewArguments() throws SyntaxException {
		ToBoolean.getInstance(null);
	}

	@Test(expected = SyntaxException.class)
	public void tooManyArguments() throws SyntaxException {
		ToBoolean.getInstance(null, StringProperty.getInstance("OK"),
				StringProperty.getInstance("OK"));
	}

}
