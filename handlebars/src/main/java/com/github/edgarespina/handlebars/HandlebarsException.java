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
 * If something goes wrong this exception will happen.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class HandlebarsException extends RuntimeException {

  /**
   * The serial UUID.
   */
  private static final long serialVersionUID = -294368972176956335L;

  /**
   * Creates a new {@link HandlebarsException}.
   *
   * @param message The error's message.
   */
  public HandlebarsException(final String message) {
    super(message);
  }

  /**
   * Creates a new {@link HandlebarsException}.
   *
   * @param cause The error's cause.
   */
  public HandlebarsException(final Exception cause) {
    super(cause);
  }

  /**
   * Creates a new {@link HandlebarsException}.
   *
   * @param message The error's message.
   * @param cause The error's cause.
   */
  public HandlebarsException(final String message, final Exception cause) {
    super(message, cause);
  }
}
