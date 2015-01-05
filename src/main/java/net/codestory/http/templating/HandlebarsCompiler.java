/**
 * Copyright (C) 2013-2014 all@code-story.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.codestory.http.templating;

import static java.util.Arrays.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.*;
import java.util.function.*;

import net.codestory.http.compilers.*;
import net.codestory.http.io.*;
import net.codestory.http.markdown.MarkdownCompiler;
import net.codestory.http.misc.Env;
import net.codestory.http.templating.helpers.*;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.cache.*;
import com.github.jknack.handlebars.context.*;
import com.github.jknack.handlebars.helper.*;
import com.github.jknack.handlebars.io.*;

public class HandlebarsCompiler {
  private final Handlebars handlebars;
  private final List<ValueResolver> resolvers;

  public HandlebarsCompiler(Env env, Resources resources, CompilerFacade compilers) {
    this.handlebars = handlebars(env, resources, compilers);
    this.resolvers = new ArrayList<>(asList(
        MapValueResolver.INSTANCE,
        JavaBeanValueResolver.INSTANCE,
        FieldValueResolver.INSTANCE,
        MethodValueResolver.INSTANCE,
        Site.SiteValueResolver.INSTANCE
    ));
  }

  public String compile(String template, Map<String, ?> variables) throws IOException {
    return handlebars.compileInline(template).apply(context(variables));
  }

  private static Handlebars handlebars(Env env, Resources resources, CompilerFacade compilers) {
    return new Handlebars()
      .startDelimiter("[[")
      .endDelimiter("]]")
      .registerHelpers(new EachReverseHelperSource())
      .registerHelpers(new EachValueHelperSource())
      .registerHelpers(new GoogleAnalyticsHelper())
      .registerHelpers(new AssetsHelperSource(env.prodMode(), compilers))
      .registerHelpers(StringHelpers.class)
      .with(new ConcurrentMapTemplateCache())
      .with(new AbstractTemplateLoader() {
        @Override
        public TemplateSource sourceAt(String location) throws IOException {
          Path include = resources.findExistingPath("_includes/" + location);
          if (include == null) {
            throw new IOException("Template not found " + location);
          }

          String body = resources.sourceFile(include).getSource();

          if (MarkdownCompiler.supports(include)) {
            body = MarkdownCompiler.INSTANCE.compile(body);
          }

          return new StringTemplateSource(location, body);
        }
      });
  }

  private Context context(Map<String, ?> variables) {
    return Context.newBuilder(null)
        .resolver(resolvers.stream().toArray(ValueResolver[]::new))
        .combine(variables)
        .build();
  }

  public void configure(Consumer<Handlebars> action) {
    action.accept(handlebars);
  }

  public void addResolver(ValueResolver resolver) {
    resolvers.add(resolver);
  }
}
