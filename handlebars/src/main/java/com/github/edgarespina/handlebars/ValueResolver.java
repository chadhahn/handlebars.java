/**
 * Copyright (c) 2012 Edgar Espina
 *
 * This file is part of Handlebars.java.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.edgarespina.handlebars;

/**
 * A hook interface for resolving values from the {@link Context context stack}.
 *
 * @author edgar.espina
 * @since 0.1.1
 */
public interface ValueResolver {

  /**
   * A mark object.
   */
  Object UNRESOLVED = new Object();

  /**
   * Resolve the attribute's name in the context object. If a
   * {@link #UNRESOLVED} is returned, the {@link Context context stack} will
   * continue with the next value resolver in the chain.
   *
   * @param context The context object. Not null.
   * @param name The attribute's name. Not null.
   * @return A {@link #UNRESOLVED} is returned, the {@link Context context
   *         stack} will continue with the next value resolver in the chain.
   *         Otherwise, it returns the associated value.
   */
  Object resolve(Object context, String name);
}
