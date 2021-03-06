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

import static org.parboiled.common.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Handlebars built-in helpers are present here.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public enum BuiltInHelpers implements Helper<Object> {

  /**
   * <p>
   * Normally, Handlebars templates are evaluated against the context passed
   * into the compiled method.
   * </p>
   * <p>
   * You can shift the context for a section of a template by using the built-in
   * with block helper.
   * </p>
   */
  WITH {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      return options.fn(context);
    }
  },

  /**
   * You can iterate over a list using the built-in each helper. Inside the
   * block, you can use <code>this</code> to reference the element being
   * iterated over.
   */
  EACH {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      StringBuilder buffer = new StringBuilder();
      @SuppressWarnings("unchecked")
      Iterable<Object> elements = (Iterable<Object>) context;
      if (options.isEmpty(elements)) {
        buffer.append(options.inverse());
      } else {
        for (Object element : elements) {
          buffer.append(options.fn(element));
        }
      }
      return buffer.toString();
    }
  },

  /**
   * You can use the if helper to conditionally render a block. If its argument
   * returns false, null or empty list/array (a "falsy" value), Handlebars will
   * not render the block.
   */
  IF {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      if (options.isEmpty(context)) {
        return options.inverse();
      } else {
        return options.fn();
      }
    }
  },

  /**
   * You can use the unless helper as the inverse of the if helper. Its block
   * will be rendered if the expression returns a falsy value.
   */
  UNLESS {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      if (options.isEmpty(context)) {
        return options.fn();
      } else {
        return options.inverse();
      }
    }
  },

  /**
   * The block helper will replace its section with the partial of the
   * same name if it exists.
   */
  BLOCK {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      if (context == null) {
        throw new IllegalArgumentException(
            "found 'null', expected 'string'");
      }
      if (!(context instanceof String)) {
        throw new IllegalArgumentException(
            "found '" + context + "', expected 'string'");
      }
      String path = (String) context;
      Template template = options.partial(path);
      if (template == null) {
        try {
          template = options.handlebars.compile(URI.create(path));
          options.partial(path, template);
        } catch (IOException ex) {
          // partial not found
          Handlebars.debug(ex.getMessage());
          template = options.fn;
        }
      }
      CharSequence result = options.apply(template);
      return result;
    }
  },

  /**
   * The partial registry helper. It stores templates in the current execution
   * context. Later the {@link #BLOCK} helper read the registry and apply the
   * template.
   */
  PARTIAL {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      if (context == null) {
        throw new IllegalArgumentException(
            "found 'null', expected 'string'");
      }
      if (!(context instanceof String)) {
        throw new IllegalArgumentException(
            "found '" + context + "', expected 'string'");
      }
      options.partial((String) context, options.fn);
      return null;
    }
  },

  /**
   * <p>
   * Usage:
   * </p>
   *
   * <pre>
   *    {{dateFormat date ["format"]}}
   * </pre>
   *
   * Format parameters is one of:
   * <ul>
   * <li>"full": full date format. For example: Tuesday, June 19, 2012</li>
   * <li>"long": long date format. For example: June 19, 2012</li>
   * <li>"medium": medium date format. For example: Jun 19, 2012</li>
   * <li>"short": short date format. For example: 6/19/12</li>
   * <li>"pattern": a date pattern.</li>
   * </ul>
   * Otherwise, the default formatter will be used.
   */
  DATE_FORMAT {
    /**
     * The default date styles.
     */
    @SuppressWarnings("serial")
    private Map<String, Integer> styles = new HashMap<String, Integer>()
    {
      {
        put("full", DateFormat.FULL);
        put("long", DateFormat.LONG);
        put("medium", DateFormat.MEDIUM);
        put("short", DateFormat.SHORT);
      }
    };

    /**
     * <p>
     * Converts a String to a Locale.
     * </p>
     * <p>
     * This method takes the string format of a locale and creates the locale
     * object from it.
     * </p>
     *
     * <pre>
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx") (#)
     * </pre>
     * <p>
     * (#) The behaviour of the JDK variant constructor changed between JDK1.3
     * and JDK1.4. In JDK1.3, the constructor upper cases the variant, in
     * JDK1.4, it doesn't. Thus, the result from getVariant() may vary depending
     * on your JDK.
     * </p>
     * <p>
     * This method validates the input strictly. The language code must be
     * lowercase. The country code must be uppercase. The separator must be an
     * underscore. The length must be correct.
     * </p>
     *
     * @param str the locale String to convert, null returns null
     * @return a Locale, null if null input
     */
    private Locale toLocale(final String str) {
      if (str == null) {
        return null;
      }
      int len = str.length();
      if (len != 2 && len != 5 && len < 7) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      char ch0 = str.charAt(0);
      char ch1 = str.charAt(1);
      if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      if (len == 2) {
        return new Locale(str, "");
      } else {
        if (str.charAt(2) != '_') {
          throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        char ch3 = str.charAt(3);
        if (ch3 == '_') {
          return new Locale(str.substring(0, 2), "", str.substring(4));
        }
        char ch4 = str.charAt(4);
        if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
          throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if (len == 5) {
          return new Locale(str.substring(0, 2), str.substring(3, 5));
        } else {
          if (str.charAt(5) != '_') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
          }
          return new Locale(str.substring(0, 2), str.substring(3, 5),
              str.substring(6));
        }
      }
    }

    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      if (context == null) {
        return null;
      }
      Date date = (Date) context;
      final DateFormat dateFormat;
      Object pattern = options.param(0, "medium");
      String localeStr = options.param(1, Locale.getDefault().toString());
      Locale locale = toLocale(localeStr);
      Integer style = styles.get(pattern);
      if (style == null) {
        dateFormat = new SimpleDateFormat(pattern.toString(), locale);
      } else {
        dateFormat = DateFormat.getDateInstance(style, locale);
      }
      return dateFormat.format(date);
    }

    @Override
    protected void add(final Handlebars handlebars) {
      add("dateFormat", handlebars);
    }
  },

  /**
   * Given:
   * home.hbs
   *
   * <pre>
   * &lt;html&gt;
   * ...
   * {{emdedded "user" ["id"]}}
   * &lt;/html&gt;
   * </pre>
   *
   * where user.hbs is:
   *
   * <pre>
   * &lt;tr&gt;
   * &lt;td&gt;{{firstName}}&lt;/td&gt;
   * &lt;td&gt;{{lastName}}&lt;/td&gt;
   * &lt;/tr&gt;
   * </pre>
   *
   * expected output is:
   *
   * <pre>
   * &lt;script id="user-hbs" type="text/x-handlebars-template"&gt;
   * &lt;tr&gt;
   * &lt;td&gt;{{firstName}}&lt;/td&gt;
   * &lt;td&gt;{{lastName}}&lt;/td&gt;
   * &lt;/tr&gt;
   * &lt;/script&gt;
   * </pre>
   *
   * Optionally, a user can set the template's name:
   *
   * <pre>
   * {{emdedded "user" "user-tmpl" }}
   * </pre>
   *
   * expected output is:
   *
   * <pre>
   * &lt;script id="user-tmpl" type="text/x-handlebars-template"&gt;
   * &lt;tr&gt;
   * &lt;td&gt;{{firstName}}&lt;/td&gt;
   * &lt;td&gt;{{lastName}}&lt;/td&gt;
   * &lt;/tr&gt;
   * &lt;/script&gt;
   * </pre>
   */
  EMBEDDED {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      if (context == null) {
        throw new IllegalArgumentException(
            "found 'null', expected 'string'");
      }
      if (!(context instanceof String)) {
        throw new IllegalArgumentException(
            "found '" + context + "', expected 'string'");
      }
      String path = (String) context;
      String defaultId = path.replace('/', '-').replace('.', '-') + "-hbs";
      String id = options.param(0, defaultId);
      Template template = options.handlebars.compile(URI.create(path));
      StringBuilder script = new StringBuilder();
      script.append("<script id=\"").append(id)
          .append("\" type=\"text/x-handlebars\">\n");
      script.append(template.text()).append("\n");
      script.append("</script>");
      return new Handlebars.SafeString(script);
    }
  },

  /**
   * The log helper.
   */
  LOG {
    @Override
    public CharSequence apply(final Object context, final Options options)
        throws IOException {
      Handlebars.log("Context %s", context);
      return null;
    }
  };

  /**
   * Add this helper to the handle bar instance.
   *
   * @param handlebars The handlebars instance.
   */
  protected void add(final Handlebars handlebars) {
    add(name().toLowerCase(), handlebars);
  }

  /**
   * Add this helper to the handle bar instance.
   *
   * @param name The helper's name.
   * @param handlebars The handlebars instance.
   */
  protected void add(final String name, final Handlebars handlebars) {
    handlebars.registerHelper(name, this);
  }

  /**
   * Regiter all the built-in helpers.
   *
   * @param handlebars The helper's owner.
   */
  static void register(final Handlebars handlebars) {
    checkNotNull(handlebars, "A handlebars object is required.");
    BuiltInHelpers[] helpers = values();
    for (BuiltInHelpers helper : helpers) {
      helper.add(handlebars);
    }
  }
}
