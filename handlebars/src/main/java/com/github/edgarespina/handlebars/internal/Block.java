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
package com.github.edgarespina.handlebars.internal;

import static org.parboiled.common.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.github.edgarespina.handlebars.BuiltInHelpers;
import com.github.edgarespina.handlebars.Context;
import com.github.edgarespina.handlebars.Handlebars;
import com.github.edgarespina.handlebars.Helper;
import com.github.edgarespina.handlebars.Lambda;
import com.github.edgarespina.handlebars.Template;

/**
 * Blocks render blocks of text one or more times, depending on the value of
 * the key in the current context.
 * A section begins with a pound and ends with a slash. That is, {{#person}}
 * begins a "person" section while {{/person}} ends it.
 * The behavior of the block is determined by the value of the key if the block
 * isn't present.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
class Block extends HelperResolver {

  /**
   * The body template.
   */
  private BaseTemplate body;

  /**
   * The section's name.
   */
  private final String name;

  /**
   * True if it's inverted.
   */
  private final boolean inverted;

  /**
   * Section's description '#' or '^'.
   */
  private final String type;

  /**
   * The start delimiter.
   */
  private String startDelimiter;

  /**
   * The end delimiter.
   */
  private String endDelimiter;

  /**
   * Inverse section for if/else clauses.
   */
  private BaseTemplate inverse;

  /**
   * Creates a new {@link Block}.
   *
   * @param handlebars The handlebars object.
   * @param name The section's name.
   * @param inverted True if it's inverted.
   * @param params The parameter list.
   * @param hash The hash.
   */
  public Block(final Handlebars handlebars, final String name,
      final boolean inverted, final List<Object> params,
      final Map<String, Object> hash) {
    super(handlebars);
    this.name = checkNotNull(name, "Section's name is required.");
    this.inverted = inverted;
    this.type = inverted ? "^" : "#";
    params(params);
    hash(hash);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void merge(final Context context,
      final Writer writer) throws IOException {
    Helper<Object> helper = helper(name);
    BaseTemplate template = body;
    final Object childContext;
    Context currentScope = context;
    if (helper == null) {
      childContext = transform(context.get(name));
      if (inverted) {
        helper = BuiltInHelpers.UNLESS;
      } else if (childContext instanceof Iterable) {
        helper = BuiltInHelpers.EACH;
      } else if (childContext instanceof Boolean) {
        helper = BuiltInHelpers.IF;
      } else if (childContext instanceof Lambda) {
        helper = BuiltInHelpers.WITH;
        template = Lambdas
            .compile(handlebars,
                (Lambda<Object, Object>) childContext,
                context, template,
                startDelimiter, endDelimiter);
      } else {
        helper = BuiltInHelpers.WITH;
        currentScope = Context.newContext(context, childContext);
      }
    } else {
      childContext = determineContext(context);
    }
    DefaultOptions options =
        new DefaultOptions(handlebars, template, inverse, currentScope,
            params(currentScope), hash(context));
    CharSequence result = helper.apply(childContext, options);
    if (result != null) {
      writer.append(result);
    }
    options.destroy();
  }

  /**
   * The section's name.
   *
   * @return The section's name.
   */
  public String name() {
    return name;
  }

  /**
   * True if it's an inverted section.
   *
   * @return True if it's an inverted section.
   */
  public boolean inverted() {
    return inverted;
  }

  @Override
  public boolean remove(final Template child) {
    return body.remove(child);
  }

  /**
   * Set the template body.
   *
   * @param body The template body. Required.
   * @return This section.
   */
  public Block body(final BaseTemplate body) {
    this.body = checkNotNull(body, "The template's body is required.");
    return this;
  }

  /**
   * Set the inverse template.
   *
   * @param inverse The inverse template. Required.
   * @return This section.
   */
  public Template inverse(final BaseTemplate inverse) {
    this.inverse =
        checkNotNull(inverse, "The inverse template's body is required.");
    return this;
  }

  /**
   * The inverse template for else clauses.
   *
   * @return The inverse template for else clauses.
   */
  public Template inverse() {
    return inverse;
  }

  /**
   * Set the end delimiter.
   *
   * @param endDelimiter The end delimiter.
   * @return This section.
   */
  public Block endDelimiter(final String endDelimiter) {
    this.endDelimiter = endDelimiter;
    return this;
  }

  /**
   * Set the start delimiter.
   *
   * @param startDelimiter The start delimiter.
   * @return This section.
   */
  public Block startDelimiter(final String startDelimiter) {
    this.startDelimiter = startDelimiter;
    return this;
  }

  /**
   * The template's body.
   *
   * @return The template's body.
   */
  public Template body() {
    return body;
  }

  @Override
  public String text() {
    return text(true);
  }

  /**
   * Build a text version of this block.
   *
   * @param complete True if the inner block should be added.
   * @return A string version of this block.
   */
  private String text(final boolean complete) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("{{").append(type).append(name);
    String params = paramsToString();
    if (params.length() > 0) {
      buffer.append(" ").append(params);
    }
    String hash = hashToString();
    if (hash.length() > 0) {
      buffer.append(" ").append(hash);
    }
    buffer.append("}}");
    if (complete) {
      buffer.append(body == null ? "" : body.text());
    } else {
      buffer.append(" ... ");
    }
    buffer.append("{{/").append(name).append("}}");
    return buffer.toString();
  }

  /**
   * The start delimiter.
   *
   * @return The start delimiter.
   */
  public String startDelimiter() {
    return startDelimiter;
  }

  /**
   * The end delimiter.
   *
   * @return The end delimiter.
   */
  public String endDelimiter() {
    return endDelimiter;
  }

  @Override
  public String toString() {
    return text(false);
  }
}
