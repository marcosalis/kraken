/*
 * Copyright 2013 Marco Salis - fast3r@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.marcosalis.kraken.testing.framework;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import junit.framework.AssertionFailedError;

/**
 * Small wrapper to execute asserts in a Runnable when performing tests that
 * require callbacks, especially in the case they're not called in the same
 * thread as the test runner.
 * 
 * Running these asserts in another thread would cause the test to crash instead
 * of simply failing, which is usually something we don't want to happen.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@ThreadSafe
public class TestAssertsWrapper {

	private Runnable mAssertsRunnable;

	public synchronized void setAsserts(@Nonnull Runnable asserts) {
		mAssertsRunnable = asserts;
	}

	public synchronized void runAsserts() {
		if (mAssertsRunnable != null) {
			mAssertsRunnable.run();
		} else {
			throw new AssertionFailedError("No assertions set");
		}
	}

}