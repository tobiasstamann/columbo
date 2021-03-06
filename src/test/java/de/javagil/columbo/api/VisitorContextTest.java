/** License based on "The MIT License (MIT)":

	Copyright (c) 2014, "Michael Hönnig" <michael.hoennig@javagil.de>
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
	
	This license becomes void immediately in case of the licensee opening any 
	law suit against the licensor concerning patent infringement issues. 
*/
package de.javagil.columbo.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import de.javagil.columbo.testutil.AssertionContext;

/**
 * Unit test for class {@VisitorContext}
 * 
 * @author michael.hoennig@javagil.de
 */
public class VisitorContextTest {

	private VisitorContext ctx = new VisitorContext();
	
	@Test
	public final void initialGetterTest() {
		assertNull(ctx.getCurrentClassName());
		assertNull(ctx.getCurrentSource());
		assertNull(ctx.getCurrentLineNumber());
		assertNull(ctx.getCurrentMethodName());
		assertNull(ctx.getCurrentMethodDesc());
	}
	
	@Test
	public final void enteringResource() throws MalformedURLException {
		URL someResourceURL = new URL("file:////dev/null");
		ctx.enteringResource(someResourceURL);
		assertSame(someResourceURL, ctx.getCurrentResource());
	}
	
	@Test
	public final void leavingResource() throws MalformedURLException {
		URL someResourceURL = new URL("file:////dev/null");
		ctx.enteringResource(someResourceURL);
		ctx.leavingResource();
		assertNull(ctx.getCurrentResource());
	}
	
	@Test
	public final void enteringClassTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		assertEquals("de/javagil/mypackage/TestClass", ctx.getCurrentClassName());
		assertNull(ctx.getCurrentSource());
		assertNull(ctx.getCurrentMethodName());
		assertNull(ctx.getCurrentMethodDesc());
		assertNull(ctx.getCurrentLineNumber());
	}

	@Test
	public final void enteringClassTwiceTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		try {
			ctx.enteringClass("de/javagil/mypackage/AnotherTestClass");
		} catch (AssertionError exc) {
			assertEquals("multiple calls to enteringClass without leavingClass", exc.getMessage());
		}
	}
	
	@Test
	public final void enteringClassWithInvalidNameFormatTest() {
		new AssertionContext() {
			
			@Override
			protected void when() throws Exception {
				ctx.enteringClass("de.javagil.mypackage.TestClass");
			}
		}.thenExpectAssertionError("not a proper internal Java class name ('/' as separator, not '.')");
		
		
		
	}



	@Test
	public final void enteringSourceTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		ctx.enteringSource("TestClass.java");
		assertEquals("de/javagil/mypackage/TestClass", ctx.getCurrentClassName());
		assertEquals("TestClass.java", ctx.getCurrentSource());
		assertNull(ctx.getCurrentMethodName());
		assertNull(ctx.getCurrentMethodDesc());
		assertNull(ctx.getCurrentLineNumber());
	}
	
	@Test
	public final void enteringMethodTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		ctx.enteringSource("TestClass.java");
		ctx.enteringMethod("myMethod", "()");
		assertEquals("de/javagil/mypackage/TestClass", ctx.getCurrentClassName());
		assertEquals("TestClass.java", ctx.getCurrentSource());
		assertEquals("myMethod", ctx.getCurrentMethodName());
		assertEquals("()", ctx.getCurrentMethodDesc());
		assertNull(ctx.getCurrentLineNumber());
	}
	
	@Test
	public final void inspectingLineNumberTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		ctx.enteringSource("TestClass.java");
		ctx.enteringMethod("myMethod", "()");
		ctx.inspectingLineNumber(42);
		assertEquals("de/javagil/mypackage/TestClass", ctx.getCurrentClassName());
		assertEquals("TestClass.java", ctx.getCurrentSource());
		assertEquals("myMethod", ctx.getCurrentMethodName());
		assertEquals("()", ctx.getCurrentMethodDesc());
		assertEquals(new Integer(42), ctx.getCurrentLineNumber());
	}
	
	@Test
	public final void leavingMethodTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		ctx.enteringSource("TestClass.java");
		ctx.enteringMethod("myMethod", "()");
		ctx.leavingMethod();
		assertEquals("de/javagil/mypackage/TestClass", ctx.getCurrentClassName());
		assertEquals("TestClass.java", ctx.getCurrentSource());
		assertNull(ctx.getCurrentMethodName());
		assertNull(ctx.getCurrentMethodDesc());
		assertNull(ctx.getCurrentLineNumber());
	}

	@Test
	public final void leavingClassTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		ctx.enteringSource("TestClass.java");
		ctx.enteringMethod("myMethod", "()");
		ctx.leavingMethod();
		ctx.leavingClass();
		assertNull(ctx.getCurrentClassName());
		// do NOT reset the source: assertNull(ctx.getCurrentSource()); 
		assertNull(ctx.getCurrentMethodName());
		assertNull(ctx.getCurrentMethodDesc());
		assertNull(ctx.getCurrentLineNumber());
	}

	@Test
	public final void toReferrerTest() {
		ctx.enteringClass("de/javagil/mypackage/TestClass");
		ctx.enteringSource("TestClass.java");
		ctx.enteringMethod("myMethod", "()");
		ctx.inspectingLineNumber(42);
		
		Referrer referrer = ctx.toReferrer();
		
		assertEquals("de.javagil.mypackage.TestClass", referrer.getJavaElement().className);
		assertEquals("myMethod", referrer.getJavaElement().methodName);
		assertEquals("()", referrer.getJavaElement().methodDesc);
		assertEquals("TestClass.java", referrer.getSourceFile());
		assertEquals(new Integer(42), referrer.getLineNo());
	}
}
