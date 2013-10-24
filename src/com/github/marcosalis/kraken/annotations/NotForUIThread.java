/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.common.annotations.Beta;

/**
 * Simple annotation to be used for marking methods which are known to perform
 * (possibly) long-running I/O operations such as network connections or disk
 * accesses, and therefore should not be called from the UI thread.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Inherited
@Target(ElementType.METHOD)
// annotation not available at runtime
@Retention(RetentionPolicy.SOURCE)
public @interface NotForUIThread {

}