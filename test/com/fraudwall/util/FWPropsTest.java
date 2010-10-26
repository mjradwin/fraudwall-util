/**
 * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of Anchor Intelligence nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.fraudwall.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.fraudwall.util.io.IOUtils;

/**
 * Tests the {@link FWProps} implementation.
 *
 * @author Allan Heydon
 */
public class FWPropsTest extends AbstractPropsTest {

	private static final String TEST_LONG_CUSTOMER = TEST_CUSTOMER + ".foo";
	private static final String TEST_APP = "testApp";

	private static final String TEST_CUSTOMER_CODE = "ag";

	@Override
	protected void tearDown() throws Exception {
		FWProps.initialize(null, null);
		super.tearDown();
	}

	/**
	 * Sets the property with given <code>propName</code> to the given <code>value</code>.
	 * <b>This method is intended for use only by unit tests.<b>
	 */
	public static void setProperty(String propName, String value) {
		FWProps.setProperty(propName, value);
	}

	/** {@link FWProps#initialize()} ------------------------------- hostName ----------------------------- */

	public void testInitializeSetsHostnameToCurrentHost() {
		FWProps.initialize(null, null);
		assertEquals(Utilities.getCurrentHost(), FWProps.INSTANCE.hostName);
	}

	/** {@link FWProps#initialize()} --------------------------- customerName ----------------------------- */

	public void testInitializeIsTolerantOfNullCustomerName() {
		checkCustomerNameEquals(null);
	}

	public void testInitializeIsTolerantOfEmptyCustomerName() {
		checkCustomerNameEquals("");
	}

	public void testInitializeThrowsIfCustomerNameIsUnknown() {
		try {
			FWProps.initialize(null, "NoSuchCustomerName");
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Unknown customer NoSuchCustomerName", e.getMessage());
		}
	}

	public void testInitializePreservesCaseOfCustomerName() {
		checkCustomerNameEquals("AdGuys");
	}

	private void checkCustomerNameEquals(String customerName) {
		FWProps.initialize(null, customerName);
		assertEquals(customerName, FWProps.INSTANCE.customerName);
		assertEquals(customerName, FWProps.getCustomerName());
	}

	public void testSetCustomerNameStripsTrailingDottedComponentForShortName() {
		checkShortCustomerNameEquals("AdGuys.wzuy", "AdGuys");
	}

	public void testSetCustomerNameSetsShortCustomerNameToNullWhenNullPassed() {
		checkShortCustomerNameEquals(null, null);
	}

	public void testSetCustomerNameSetsShortCustomerNameEmptyStringWhenNullPassed() {
		checkShortCustomerNameEquals("", "");
	}

	private void checkShortCustomerNameEquals(String customerName, String shortCustomerName) {
		FWProps.initialize(null, customerName);
		assertEquals(shortCustomerName, FWProps.INSTANCE.shortCustomerName);
	}

	/** {@link FWProps#initialize()} -------------------------- applicationName --------------------------- */

	public void testInitializeIsTolerantOfNullApplicationName() {
		checkApplicationNameEquals(null);
	}

	public void testInitializeIsTolerantOfEmptyApplicationName() {
		checkApplicationNameEquals("");
	}

	public void testInitializeLowecasesCaseOfApplicationName() {
		checkApplicationNameEquals("TestApp");
	}

	private void checkApplicationNameEquals(String applicationName) {
		FWProps.initialize(applicationName, null);
		assertEquals(StringUtils.lowerCase(applicationName), FWProps.INSTANCE.applicationName);
		assertEquals(StringUtils.lowerCase(applicationName), FWProps.getApplicationName());
	}

	/** {@link FWProps#initialize()} ------------------------ Customer property --------------------------- */

	public void testInitializeSetsCustomerPropertyToUnknownWhenCustomerIsNull() {
		FWProps.initialize(null, null);
		assertEquals("UnknownCustomer", FWProps.getProperty("Customer"));
	}

	public void testInitializeSetsTheCustomerPropertyIfNonNull() {
		FWProps.initialize(null, TEST_CUSTOMER);
		assertEquals(TEST_CUSTOMER, FWProps.getProperty("Customer"));
	}

	/** {@link FWProps#initialize()} ---------------------------- ROOT property --------------------------- */

	public void testInitializeSetsRootPropertyIfNonNull() {
		FWProps.initialize(null, TEST_CUSTOMER);
		assertEquals(IOUtils.getRootDir(), FWProps.getProperty("ROOT"));
	}

	/** {@link FWProps#initialize()} -------------------- CustomerCode property --------------------------- */

	public void testInitializeSetsCustomerCodeToEmptyStringWhenCustomerIsNull() {
		FWProps.initialize(null, null);
		assertEquals("", FWProps.getProperty("CustomerCode"));
	}

	public void testInitializeSetsTheCustomerCodePropertyWhenCustomerIsNonNull() {
		FWProps.initialize(null, TEST_CUSTOMER);
		assertEquals(TEST_CUSTOMER_CODE, FWProps.getProperty("CustomerCode"));
	}

	/** {@link FWProps#getApplicationName()} -------------------------------------------------------------- */

	public void testGetApplicationNameReturnsNullIfNoApplicationNameSpecified() {
		FWProps.initialize(null, TEST_CUSTOMER);
		assertNull(FWProps.getApplicationName());
	}

	public void testGetApplicationNameReturnsCorrectApplicationName() {
		FWProps.initialize(DEFAULT, TEST_CUSTOMER);
		assertEquals(DEFAULT, FWProps.getApplicationName());
	}

	/** {@link FWProps#getCustomerName()} ----------------------------------------------------------------- */

	public void testGetCustomerNameReturnsNullIfNoCustomerNameSpecified() {
		FWProps.initialize(DEFAULT, null);
		assertNull(FWProps.getCustomerName());
	}

	public void testGetCustomerNameReturnsCamelCaseCustomerName() {
		FWProps.initialize(DEFAULT, TEST_CUSTOMER);
		assertEquals(TEST_CUSTOMER, FWProps.getCustomerName());
	}

	/** {@link FWProps#getCustomerCode()} ----------------------------------------------------------------- */

	public void testGetCustomerCodeReturnsNullIfCustomerNameIsNull() {
		FWProps.initialize(null, null);
		assertNull(FWProps.getCustomerCode());
	}

	public void testGetCustomerCodeReturnsNullIfNoCustomerCodeSpecified() {
		FWProps.initialize(null, "HebCal");
		assertNull(FWProps.getCustomerCode());
	}

	public void testGetCustomerCodeReturnsNullIfCustomerCodeIsBlank() {
		FWProps.initialize(null, "HebCal");
		setProperty("customer.code.HebCal", "");
		assertNull(FWProps.getCustomerCode());
	}

	public void testGetCustomerCodeReturnsCorrectCustomerCode() {
		FWProps.initialize(null, TEST_CUSTOMER);
		assertEquals(TEST_CUSTOMER_CODE, FWProps.getCustomerCode());
	}

	/** {@link FWProps#expandAllMacros(Properties)} ------------------------------------------------------- */

	public void testExpandAllMacrosIsANoOpIfNoValuesContainMacros() {
		Properties props = new Properties();
		props.setProperty("test.a", "This is a property value");
		props.setProperty("test.b", "This is a second property value");
		FWProps.expandAllMacros(/*INOUT*/ props);
		assertEquals("This is a property value", props.getProperty("test.a"));
		assertEquals("This is a second property value", props.getProperty("test.b"));
	}

	public void testExpandAllMacrosWorksWithOneLevelOfMacroExpansion() {
		Properties props = new Properties();
		props.setProperty("test.a", "bar");
		props.setProperty("test.b", "${test.a}");
		props.setProperty("test.c", "foo ${test.a} baz");
		FWProps.expandAllMacros(/*INOUT*/ props);
		assertEquals("bar", props.getProperty("test.a"));
		assertEquals("bar", props.getProperty("test.b"));
		assertEquals("foo bar baz", props.getProperty("test.c"));
	}

	public void testExpandAllMacrosExpandsMultipleOneLevelMacrosInSameValue() {
		Properties props = new Properties();
		props.setProperty("test.a", "bar");
		props.setProperty("test.b", "cat");
		props.setProperty("test.c", "foo ${test.a} baz ${test.b} dog");
		props.setProperty("test.d", "${test.a}${test.b}");
		FWProps.expandAllMacros(/*INOUT*/ props);
		assertEquals("bar", props.getProperty("test.a"));
		assertEquals("cat", props.getProperty("test.b"));
		assertEquals("foo bar baz cat dog", props.getProperty("test.c"));
		assertEquals("barcat", props.getProperty("test.d"));
	}

	public void testExpandAllMacrosWorksWithMultipleLevelsOfMacroExpansion() {
		Properties props = new Properties();
		props.setProperty("test.a", "a");
		props.setProperty("test.b", "is ${test.a} test");
		props.setProperty("test.c", "This ${test.b}!");
		FWProps.expandAllMacros(/*INOUT*/ props);
		assertEquals("a", props.getProperty("test.a"));
		assertEquals("is a test", props.getProperty("test.b"));
		assertEquals("This is a test!", props.getProperty("test.c"));
	}

	public void testExpandAllMacrosThrowsIfNonExistingPropertyReferenced() {
		Properties props = new Properties();
		props.setProperty("test.a", "This is ${test.unknown} test!");
		try {
			FWProps.expandAllMacros(/*INOUT*/ props);
			fail();
		} catch (IllegalStateException ex) {
			// expected case
			String errMsg =
				"Unknown property name 'test.unknown' referenced from property "
				+ "'test.a' with value 'This is ${test.unknown} test!'.";
			assertEquals(errMsg, ex.getMessage());
		}
	}

	public void testExpandAllMacrosThrowsIfReferenceIsUnterminated() {
		Properties props = new Properties();
		props.setProperty("test.a", "This is ${test.unknown test!");
		try {
			FWProps.expandAllMacros(/*INOUT*/ props);
			fail();
		} catch (IllegalStateException ex) {
			// expected case
			String errMsg =
				"Opening '${' unmatched by closing '}' in property 'test.a' "
				+ "with value 'This is ${test.unknown test!'.";
			assertEquals(errMsg, ex.getMessage());
		}
	}

	public void testExpandAllMacrosThrowsIfReferencesAreCyclic() {
		Properties props = new Properties();
		props.setProperty("test.a", "my ${test.c}");
		props.setProperty("test.b", "is ${test.a} test");
		props.setProperty("test.c", "Whoa ${test.b}");
		try {
			FWProps.expandAllMacros(/*INOUT*/ props);
			fail();
		} catch (IllegalStateException ex) {
			// expected case
			String errMsg = "Cycle found among properties: [test.c, test.b, test.a].";
			assertEquals(errMsg, ex.getMessage());
		}
	}

	/** {@link FWProps#replaceAllProps(String)} ----------------------------------------------------------- */

	public void testReplaceAllPropsThrowsIfNoMatchingEndBraceFound() {
		try {
			FWProps.replaceAllProps("foo/${bar/baz");
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("String 'foo/${bar/baz' contains '${' with no matching '}'.", ex.getMessage());
		}
	}

	public void testReplaceAllPropsThrowsIfMacroNamesUndefinedProperty() {
		FWProps.initialize(null, TEST_CUSTOMER);
		try {
			FWProps.replaceAllProps("${Customer}/${a.b.c}/baz");
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("String '${Customer}/${a.b.c}/baz' names an undefined property: a.b.c", ex.getMessage());
		}
	}

	public void testReplaceAllPropsReturnsExpectedValue() {
		FWProps.initialize(null, TEST_CUSTOMER);
		assertEquals("AdGuys", FWProps.replaceAllProps("${Customer}"));
		assertEquals("XAdGuys", FWProps.replaceAllProps("X${Customer}"));
		assertEquals("AdGuysX", FWProps.replaceAllProps("${Customer}X"));
		assertEquals("AdGuys/AdGuys", FWProps.replaceAllProps("${Customer}/${Customer}"));
		assertEquals("/AdGuys/AdGuys/foo", FWProps.replaceAllProps("/${Customer}/${Customer}/foo"));
		assertEquals("===AdGuys+++AdGuys---", FWProps.replaceAllProps("===${Customer}+++${Customer}---"));
	}

	/** {@link FWProps#flattenProperties(Properties...)} -------------------------------------------------- */

	public void testFlattenPropertiesNone() {
		Properties out = FWProps.flattenProperties();
		assertEquals(out.size(), 0);
	}

	public void testFlattenPropertiesTwoEmpty() {
		Properties p1 = new Properties();
		Properties p2 = new Properties();
		Properties out = FWProps.flattenProperties(p1, p2);
		assertEquals(out.size(), 0);
	}

	public void testFlattenPropertiesSingle() {
		Properties p1 = makeProp1();
		Properties out = FWProps.flattenProperties(p1);
		assertEquals(out.size(), 2);
		assertEquals(out.getProperty("foo"), "bar");
	}

	public void testFlattenPropertiesCollisionSize() {
		Properties p1 = makeProp1();
		Properties p2 = makeProp2();
		Properties out = FWProps.flattenProperties(p1, p2);
		assertEquals(out.size(), 2);
	}

	public void testFlattenPropertiesNoCollisionSize() {
		Properties p1 = makeProp1();
		Properties p3 = makeProp3();
		Properties out = FWProps.flattenProperties(p1, p3);
		assertEquals(out.size(), 3);
	}

	public void testFlattenPropertiesCollisionOverride1() {
		Properties p1 = makeProp1();
		Properties p2 = makeProp2();
		Properties out = FWProps.flattenProperties(p1, p2);
		assertEquals(out.getProperty("baaz"), "toto");
	}

	public void testFlattenPropertiesCollisionOverride2() {
		Properties p1 = makeProp1();
		Properties p2 = makeProp2();
		Properties out = FWProps.flattenProperties(p2, p1);
		assertEquals(out.getProperty("baaz"), "quux");
	}

	public void testFlattenPropertiesCollisionOverride3() {
		Properties p1 = makeProp1();
		Properties p2 = makeProp2();
		Properties p3 = makeProp3();
		Properties p4 = makeProp4();
		Properties out = FWProps.flattenProperties(p1, p2, p3, p4);
		assertEquals(out.getProperty("baaz"), "four");
	}

	public void testFlattenPropertiesCollisionOverride4() {
		Properties p1 = makeProp1();
		Properties p2 = makeProp2();
		Properties p3 = makeProp3();
		Properties p4 = makeProp4();
		Properties out = FWProps.flattenProperties(p1, p4, p3, p2);
		assertEquals(out.getProperty("baaz"), "toto");
	}

	public void testFlattenPropertiesManyDupes() {
		Properties p1a = makeProp1();
		Properties p1b = makeProp1();
		Properties p1c = makeProp1();
		Properties p1d = makeProp1();
		Properties p1e = makeProp1();
		Properties out = FWProps.flattenProperties(p1a, p1b, p1c, p1d, p1e);
		assertEquals(out.size(), 2);
		assertEquals(out.getProperty("foo"), "bar");
	}

	/** {@link FWProps#getProperty(String)} --------------------------------------------------------------- */

	public void testGetPropertyReturnsNullForUndefinedProperty() {
		initialize(new Properties());
		assertNull(FWProps.getProperty("foo"));
	}

	public void testGetPropertyReturnsExpectedPropertyValue() {
		Properties props = new Properties();
		props.put("foo", "string-val");
		props.put("bar", "1234");
		initialize(props);
		assertEquals("string-val", FWProps.getProperty("foo"));
		assertEquals("1234", FWProps.getProperty("bar"));
	}

	/** {@link FWProps#getPropVal(String, String)} -------------------------------------------------------- */

	public void testGetPropValReturnsValueWithOptPrefixIfDefined() {
		setProperty("Prefix.propName", "foo");
		setProperty("propName", "bar");
		assertEquals("foo", FWProps.getPropVal("Prefix", "propName"));
	}

	public void testGetPropValReturnsValueWithoutPrefixIfPrefixUndefined() {
		setProperty("propName", "bar");
		assertEquals("bar", FWProps.getPropVal("Prefix", "propName"));
	}

	public void testGetPropValReturnsValueWithoutPrefixIfPrefixIsBlank() {
		setProperty("propName", "bar");
		assertEquals("bar", FWProps.getPropVal("   ", "propName"));
	}

	public void testGetPropValThrowsExceptionIfNeitherPropertyDefined() {
		try {
			FWProps.getPropVal("Prefix", "propName");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("No such properties: \"Prefix.propName\" or \"propName\".", ex.getMessage());
		}
	}

	/** {@link FWProps#getPropName(String, String)} ------------------------------------------------------- */

	public void testGetPropNameReturnsCorrectValueForNonBlankPrefix() {
		assertEquals("[Prefix.]propName", FWProps.getPropName("Prefix", "propName"));
	}

	public void testGetPropNameReturnsCorrectValueForNullOrBlankPrefix() {
		assertEquals("propName", FWProps.getPropName(null, "propName"));
		assertEquals("propName", FWProps.getPropName("  ", "propName"));
	}

	/** {@link FWProps#getBooleanProperty(String)} -------------------------------------------------------- */

	public void testGetBooleanPropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getBooleanProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	private void checkNoSuchPropertyException(IllegalArgumentException ex, String propName) {
		assertEquals("No such property: \"" + propName + "\".", ex.getMessage());
	}

	public void testGetBooleanPropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop.bar", "bar");
		props.put("prop.false", "false");
		props.put("prop.true", "true");
		props.put("prop.FALSE", "FALSE");
		props.put("prop.TRUE", "TRUE");
		props.put("prop.0", "0");
		props.put("prop.1", "1");
		initialize(props);
		assertFalse(FWProps.getBooleanProperty("prop.bar"));
		assertFalse(FWProps.getBooleanProperty("prop.false"));
		assertTrue(FWProps.getBooleanProperty("prop.true"));
		assertFalse(FWProps.getBooleanProperty("prop.FALSE"));
		assertTrue(FWProps.getBooleanProperty("prop.TRUE"));
		assertFalse(FWProps.getBooleanProperty("prop.0"));
		assertTrue(FWProps.getBooleanProperty("prop.1"));
	}

	/** {@link FWProps#getBooleanProperty(String, String)} ------------------------------------------------ */

	public void testBooleanPropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "true");
		setProperty("propName", "false");
		assertTrue(FWProps.getBooleanProperty("Prefix", "propName"));
	}

	public void testBooleanPropertyReturnsCorrectFallbackValue() {
		setProperty("propName", "false");
		assertFalse(FWProps.getBooleanProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getCharacterProperty(String)} ------------------------------------------------------ */

	public void testGetCharacterPropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getCharacterProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetCharacterPropertyThrowsIfPropertyValueIsEmpty() {
		Properties props = new Properties();
		props.put("prop", "");
		initialize(props);
		try {
			FWProps.getCharacterProperty("prop");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("Character-valued property 'prop' is empty.", ex.getMessage());
		}
	}

	public void testGetCharacterPropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop1", ",");
		props.put("prop2", "\t");
		initialize(props);
		assertEquals(',', FWProps.getCharacterProperty("prop1"));
		assertEquals('\t', FWProps.getCharacterProperty("prop2"));
	}

	/** {@link FWProps#getCharacterProperty(String, String)} ---------------------------------------------- */

	public void testCharacterPropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "\t");
		setProperty("propName", ",");
		assertEquals('\t', FWProps.getCharacterProperty("Prefix", "propName"));
	}

	public void testCharacterPropertyReturnsCorrectFallbackValue() {
		setProperty("propName", ",");
		assertEquals(',', FWProps.getCharacterProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getStringProperty(String)} --------------------------------------------------------- */

	public void testGetStringPropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getStringProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetStringPropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop1", "string-1");
		props.put("prop2", "string-2");
		initialize(props);
		assertEquals("string-1", FWProps.getStringProperty("prop1"));
		assertEquals("string-2", FWProps.getStringProperty("prop2"));
	}

	/** {@link FWProps#getStringProperty(String, String)} ------------------------------------------------- */

	public void testStringPropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "foo");
		setProperty("propName", "bar");
		assertEquals("foo", FWProps.getStringProperty("Prefix", "propName"));
	}

	public void testStringPropertyReturnsCorrectFallbackValue() {
		setProperty("propName", "bar");
		assertEquals("bar", FWProps.getStringProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getIntegerProperty(String)} -------------------------------------------------------- */

	public void testGetIntegerPropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getIntegerProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetIntegerPropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop1", "1234");
		props.put("prop2", "5678");
		initialize(props);
		assertEquals(1234, FWProps.getIntegerProperty("prop1"));
		assertEquals(5678, FWProps.getIntegerProperty("prop2"));
	}

	/** {@link FWProps#getIntegerProperty(String, String)} ------------------------------------------------ */

	public void testIntegerPropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "1");
		setProperty("propName", "2");
		assertEquals(1, FWProps.getIntegerProperty("Prefix", "propName"));
	}

	public void testIntegerPropertyReturnsCorrectFallback() {
		setProperty("propName", "2");
		assertEquals(2, FWProps.getIntegerProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getLongProperty(String)} ----------------------------------------------------------- */

	public void testGetLongPropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getLongProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetLongPropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop1", "1234");
		props.put("prop2", "56789012345");
		initialize(props);
		assertEquals(1234L, FWProps.getLongProperty("prop1"));
		assertEquals(56789012345L, FWProps.getLongProperty("prop2"));
	}

	/** {@link FWProps#getLongProperty(String, String)} --------------------------------------------------- */

	public void testLongPropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "56789012345");
		setProperty("propName", "1234");
		assertEquals(56789012345L, FWProps.getLongProperty("Prefix", "propName"));
	}

	public void testLongPropertyReturnsCorrectFallbackValue() {
		setProperty("propName", "1234");
		assertEquals(1234L, FWProps.getLongProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getFloatProperty(String)} ---------------------------------------------------------- */

	public void testGetFloatPropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getFloatProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetFloatPropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop1", "1234");
		props.put("prop2", "56.789");
		initialize(props);
		assertEquals(1234.0f, FWProps.getFloatProperty("prop1"), 1.0e-10f);
		assertEquals(56.789f, FWProps.getFloatProperty("prop2"), 1.0e-10f);
	}

	/** {@link FWProps#getFloatProperty(String, String)} -------------------------------------------------- */

	public void testFloatPropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "56.789");
		setProperty("propName", "1234.56");
		assertEquals(56.789F, FWProps.getFloatProperty("Prefix", "propName"));
	}

	public void testFloatPropertyReturnsCorrectFallbackValue() {
		setProperty("propName", "1234.56");
		assertEquals(1234.56F, FWProps.getFloatProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getDoubleProperty(String)} --------------------------------------------------------- */

	public void testGetDoublePropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getDoubleProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetDoublePropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop1", "1234");
		props.put("prop2", "56.7890123456");
		initialize(props);
		assertEquals(1234.0, FWProps.getDoubleProperty("prop1"), 1.0e-20f);
		assertEquals(56.7890123456, FWProps.getDoubleProperty("prop2"), 1.0e-20f);
	}

	/** {@link FWProps#getDoubleProperty(String, String)} ------------------------------------------------- */

	public void testDoublePropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "56.7890123456");
		setProperty("propName", "1234");
		assertEquals(56.7890123456, FWProps.getDoubleProperty("Prefix", "propName"));
	}

	public void testDoublePropertyReturnsCorrectFallbackValue() {
		setProperty("propName", "1234.56789012345");
		assertEquals(1234.56789012345, FWProps.getDoubleProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getTimeProperty(String)} ----------------------------------------------------------- */

	public void testGetTimePropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getTimeProperty("foo");
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetTimePropertyThrowsIllegalArgumentExceptionForIllegalTimeValues() {
		checkGetTimePropertyThrowsForIllegalTimeValues("123");
		checkGetTimePropertyThrowsForIllegalTimeValues("123x");
		checkGetTimePropertyThrowsForIllegalTimeValues("1h5m");
		checkGetTimePropertyThrowsForIllegalTimeValues("1A5m");
	}

	private void checkGetTimePropertyThrowsForIllegalTimeValues(String propVal) {
		Properties props = new Properties();
		props.put("foo", propVal);
		initialize(props);
		try {
			FWProps.getTimeProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			String msg = "property [foo] invalid duration";
			assertEquals(msg, ex.getMessage());
		}
	}

	public void testGetTimePropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop0", "1500ms");
		props.put("prop1", "12s");
		props.put("prop2", "23m");
		props.put("prop3", "34h");
		props.put("prop4", "34H");
		props.put("prop5", "45d");
		props.put("prop6", "56w");
		initialize(props);
		assertEquals(1500, FWProps.getTimeProperty("prop0"));
		assertEquals(12 * DateUtils.MILLIS_PER_SECOND, FWProps.getTimeProperty("prop1"));
		assertEquals(23 * DateUtils.MILLIS_PER_MINUTE, FWProps.getTimeProperty("prop2"));
		assertEquals(34 * DateUtils.MILLIS_PER_HOUR, FWProps.getTimeProperty("prop3"));
		assertEquals(34 * DateUtils.MILLIS_PER_HOUR, FWProps.getTimeProperty("prop4"));
		assertEquals(45 * DateUtils.MILLIS_PER_DAY, FWProps.getTimeProperty("prop5"));
		assertEquals(56 * 7 * DateUtils.MILLIS_PER_DAY, FWProps.getTimeProperty("prop6"));
	}

	/** {@link FWProps#getTimeProperty(String, String)} --------------------------------------------------- */

	public void testTimePropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "23m");
		setProperty("propName", "12s");
		assertEquals(23 * DateUtils.MILLIS_PER_MINUTE, FWProps.getTimeProperty("Prefix", "propName"));
	}

	public void testTimePropertyReturnsCorrectFallbackValue() {
		setProperty("propName", "12s");
		assertEquals(12 * DateUtils.MILLIS_PER_SECOND, FWProps.getTimeProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getCsvProperty(String)} ------------------------------------------------------------ */

	public void testGetCsvPropertyThrowsForUndefinedProperty() {
		initialize(new Properties());
		try {
			FWProps.getCsvProperty("foo");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			checkNoSuchPropertyException(ex, "foo");
		}
	}

	public void testGetCsvPropertyReturnsCorrectValue() {
		Properties props = new Properties();
		props.put("prop1", "a,b,c");
		props.put("prop2", "gg,aj,yy,gc");
		initialize(props);
		assertListEquals(new String[] { "a", "b", "c" }, FWProps.getCsvProperty("prop1"));
		assertListEquals(new String[] { "gg", "aj", "yy", "gc" }, FWProps.getCsvProperty("prop2"));
	}

	/** {@link FWProps#getCsvProperty(String, String)} ---------------------------------------------------- */

	public void testCsvPropertyReturnsCorrectValueForNonBlankPrefix() {
		setProperty("Prefix.propName", "gg,aj,yy,gc");
		setProperty("propName", "a,b,c");
		assertListEquals(new String[] { "gg", "aj", "yy", "gc" }, FWProps.getCsvProperty("Prefix", "propName"));
	}

	public void testCsvPropertyReturnsCorrectFallbackValue() {
		setProperty("propName", "a,b,c");
		assertListEquals(new String[] { "a", "b", "c" }, FWProps.getCsvProperty("Prefix", "propName"));
	}

	/** {@link FWProps#getAllCustomers()} ---------------------------------------------------------------- */

	public void testGetAllCustomersIgnoresDemoAndNocust() throws IOException {
		Properties props = new Properties();
		props.put("customer.code.AdGuys", "ab");
		props.put("customer.code.Foobar", "quux");
		props.put("customer.code.Baaz", "");
		props.put("customer.code.demo", "Demo");
		props.put("customer.code.nocust", "all");
		File configDir = getCreatedOutputDir();
		createMinimalProperties(configDir, false, false, false);
		FWProps fwprops = new FWPropsMock(configDir.toString());
		fwprops.initialize();
		for (Entry<Object, Object> entry : props.entrySet()) {
			fwprops.allProps.put(entry.getKey(), entry.getValue());
		}
		assertListEquals(new String[] { "AdGuys", "Baaz", "Foobar" }, fwprops.getAllCustomers());
	}

	/** {@link FWProps#getApplicationEnabledCustomers(String)} ------------------------------------------- */

	public void testGetApplicationEnabledCustomers() throws IOException {
		Properties props = new Properties();
		props.put("customer.code.AdGuys", "ab");
		props.put("customer.code.Foobar", "quux");
		props.put("customer.code.Baaz", "");
		props.put("customer.code.demo", "Demo");
		props.put("customer.code.nocust", "all");
		File configDir = getCreatedOutputDir();
		createMinimalProperties(configDir, false, false, false);
		createFileFromLines("custs/AdGuys.properties", "kingcrab.enabled=true");
		createFileFromLines("custs/Baaz.properties", "kingcrab.enabled=false");
		createFileFromLines("custs/Foobar.properties", "kingcrab.enabled=true");
		// Since we're initializing as AdGuys, we need AdGuys.properties
		createFileFromLines("custs/AdGuys.properties", "test=true");
		FWProps fwprops = new FWPropsMock(configDir.toString());
		fwprops.initialize();
		for (Entry<Object, Object> entry : props.entrySet()) {
			fwprops.allProps.put(entry.getKey(), entry.getValue());
		}
		assertListEquals(new String[] { "AdGuys", "Foobar" }, fwprops.getApplicationEnabledCustomers("default"));
	}

	/** {@link FWProps#getAllApplications()} ------------------------------------------------------------- */

	public void testGetAllApplications() throws IOException {
		Properties props = new Properties();
		props.put("foo.db.host", "quux");
		props.put("seaspider.db.host", "baaz");
		props.put("quux.db.host", "");
		File configDir = getCreatedOutputDir();
		createMinimalProperties(configDir, false, false, false);
		FWProps fwprops = new FWPropsMock(configDir.toString());
		fwprops.initialize();
		for (Entry<Object, Object> entry : props.entrySet()) {
			fwprops.allProps.put(entry.getKey(), entry.getValue());
		}
		assertListEquals(new String[] { "foo", "quux", "seaspider" }, fwprops.getAllApplications());
	}

	/** {@link FWProps#initialize()} ---------------------------------------------------------------------- */

	public void testInitializeProcessesCorrectFiles() throws IOException {
		checkInitializeProcessesCorrectFiles(/*prod=*/false, /*ignoreUser=*/true, /*ignoreHost=*/true);
		checkInitializeProcessesCorrectFiles(/*prod=*/false, /*ignoreUser=*/true, /*ignoreHost=*/false);
		checkInitializeProcessesCorrectFiles(/*prod=*/false, /*ignoreUser=*/false, /*ignoreHost=*/true);
		checkInitializeProcessesCorrectFiles(/*prod=*/false, /*ignoreUser=*/false, /*ignoreHost=*/false);
		// if prod is true then ignore user is true
		checkInitializeProcessesCorrectFiles(/*prod=*/true, /*ignoreUser=*/true, /*ignoreHost=*/true);
		checkInitializeProcessesCorrectFiles(/*prod=*/true, /*ignoreUser=*/true, /*ignoreHost=*/false);
	}

	private void checkInitializeProcessesCorrectFiles(boolean prod, boolean ignoreUser, boolean ignoreHost) throws IOException {
		File configDir = getCreatedOutputDir();
		List<String> fileNames = createMinimalProperties(configDir, prod, ignoreUser, ignoreHost);
		FWPropsMock fwProps = new FWPropsMock(configDir.toString());
		fwProps.initialize();
//		System.err.println(fileNames);
//		System.err.println(fwProps.loadedPropertiesFiles);
		assertListEquals(fileNames, fwProps.loadedPropertiesFiles);
	}

	private static String[] ALL_FILE_NAMES = {
		"defaults", "$APP", "$APP.prod", "local", "l10n",
		"custs/$CUST", "custs/$CUSTOMER", "custs/$CUST.prod", "custs/$CUSTOMER.prod",
		"custs/$CUST.$APP", "custs/$CUSTOMER.$APP",
		"$HOST", "custs/$CUST.$HOST", "custs/$CUSTOMER.$HOST",
		"custs/$CUST.l10n", "custs/$CUSTOMER.l10n",
		"users/$USER", "users/$USER.$HOST",
		"unittest", "$HOST.unittest", "users/$USER.unittest",
		"local"

	};
	private static List<String> getAllPropFileNames(boolean prod, boolean ignoreUser, boolean ignoreHost) {
		List<String> fileNames = new ArrayList<String>();
		String hostName = Utilities.getCurrentHost();
		String userName = Utilities.getCurrentUser();
		for (String fileName : ALL_FILE_NAMES) {
			if (!prod && fileName.contains(".prod") ||
				ignoreUser && fileName.contains("$USER") ||
				ignoreHost && fileName.contains("$HOST")) {
				continue;
			}
			fileNames.add(fileName
				.replace('/', File.separatorChar)
				.replace("$HOST", hostName)
				.replace("$USER", userName)
				.replace("$APP", TEST_APP.toLowerCase())
				.replace("$CUSTOMER", TEST_LONG_CUSTOMER)
				.replace("$CUST", TEST_CUSTOMER) + ".properties");
		}
		return fileNames;
	}

	private static class FWPropsMock extends FWProps {
		private final List<String> loadedPropertiesFiles = new ArrayList<String>();
		private final String configDir;

		private FWPropsMock(String configDir) {
			this(TEST_APP, TEST_LONG_CUSTOMER, configDir);
		}

		private FWPropsMock(String appName, String customer, String configDir) {
			super(appName, customer, /*forceProd=*/ false);
			this.configDir = configDir;
		}

		@Override protected FWProps newInstance(String appName, String customer) {
			return new FWPropsMock(appName, customer, configDir);
		}

		@Override protected Properties loadProperties(String fileName, boolean isRequired) {
			// record that we attempted to load this file
			loadedPropertiesFiles.add(fileName);

			File file = new File(configDir, fileName);
			try {
				return loadProperties(new FileInputStream(file));
			} catch (IOException e) {
				if (isRequired) {
					throw new AnchorFatalError("Unable to load " + file, e);
				}
			}
			return null;
		}
	}

	// ============================================== private helpers

	private List<String> createMinimalProperties(File configDir, boolean prod, boolean ignoreUser,boolean ignoreHost)
		throws IOException {
		List<String> fileNames = getAllPropFileNames(prod, ignoreUser, ignoreHost);
		for (String fileName : fileNames) {
			File file = new File(configDir, fileName);
			IOUtils.createDirectoryIfNeeded(file.getParentFile());
			file.createNewFile();
		}
		createFileFromLines("defaults.properties",
			"prod.hosts=" + (prod ? Utilities.getCurrentHost() : ""),
			"ignore.user.properties=" + ignoreUser,
			"ignore.host.properties=" + ignoreHost
		);
		return fileNames;
	}

	private static void initialize(Properties props) {
		FWProps.initialize(null, null);
		for (Entry<Object, Object> entry : props.entrySet()) {
			FWProps.INSTANCE.allProps.put(entry.getKey(), entry.getValue());
		}
	}

	private Properties makeProp1() {
		Properties p1 = new Properties();
		p1.setProperty("foo", "bar");
		p1.setProperty("baaz", "quux");
		return p1;
	}

	private Properties makeProp2() {
		Properties p2 = new Properties();
		p2.setProperty("baaz", "toto");
		return p2;
	}

	private Properties makeProp3() {
		Properties p3 = new Properties();
		p3.setProperty("hello", "goodbye");
		return p3;
	}

	private Properties makeProp4() {
		Properties p4 = new Properties();
		p4.setProperty("baaz", "four");
		return p4;
	}
}
