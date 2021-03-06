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
package net.codestory.http.extensions;

import net.codestory.http.compilers.CompilersConfiguration;
import net.codestory.http.misc.Env;
import net.codestory.http.templating.BasicResolver;
import net.codestory.http.testhelpers.AbstractProdWebServerTest;
import org.junit.Test;

public class CustomHandlebarsResolverTest extends AbstractProdWebServerTest {
  @Test
  public void add_resolver() {
    configure(routes -> routes.setExtensions(new Extensions() {
      @Override
      public void configureCompilers(CompilersConfiguration compilers, Env env) {
        compilers.addHandlebarsResolver(new HelloWorldResolver());
      }
    }));

    get("/extensions/custom_resolver").should().contain("Hello World");
  }

  static class HelloWorldResolver implements BasicResolver {
    @Override
    public String tag() {
      return "greeting";
    }

    @Override
    public Object resolve(Object context) {
      return "Hello World";
    }
  }
}
