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
package net.codestory.http;

import net.codestory.http.annotations.*;
import net.codestory.http.errors.*;
import net.codestory.http.templating.*;
import net.codestory.http.testhelpers.*;

import org.junit.*;

public class AnnotationsTest extends AbstractProdWebServerTest {
  @Test
  public void annotated_resources() {
    configure(routes -> routes.add(new MyResource()));

    get("/hello").should().contain("Hello");
    get("/").should().contain("Hello");
    get("/bye/Bob").should().contain("Good Bye Bob");
    get("/add/22/20").should().haveType("application/json").contain("42");
    get("/void").should().respond(200).haveType("text/html").contain("");
    get("/voidJson").should().respond(200).haveType("application/json").contain("");
    get("/1variable").should().respond(200).haveType("text/html").contain("Hello Bob");
    get("/helloJoe").should().respond(200).haveType("text/html").contain("Hello Joe");
    get("/notFound").should().respond(404);
  }

  @Test
  public void resources_class() {
    configure(routes -> routes.add(MyResource.class));

    get("/hello").should().contain("Hello");
  }

  public static class MyResource {
    @Get("/hello")
    @Get("/")
    public String hello() {
      return "Hello";
    }

    @Get("/bye/:whom")
    public String bye(String whom) {
      return "Good Bye " + whom;
    }

    @Get("/add/:left/:right")
    public int add(int left, int right) {
      return left + right;
    }

    @Get("/void")
    public void empty() {
    }

    @Get("/voidJson")
    @Produces("application/json")
    public void emptyJson() {
    }

    @Get("/1variable")
    @Produces("text/html")
    public Model helloBob() {
      return Model.of("name", "Bob");
    }

    @Get("/helloJoe")
    @Produces("text/html")
    public ModelAndView helloJoe() {
      return ModelAndView.of("1variable", "name", "Joe");
    }

    @Get("/notFound")
    public void notFound() {
      throw new NotFoundException();
    }
  }

  @Test
  public void prefix() {
    configure(routes -> routes.add("/say", new TestResource()));

    get("/say/hello").should().contain("Hello");
  }

  @Test
  public void add_with_prefix() {
    configure(routes -> routes.add("/say", TestResource.class));

    get("/say/hello").should().contain("Hello");
  }

  @Test
  public void resource_with_prefix() {
    configure(routes -> routes.add(ResourceWithPrefix.class));

    get("/prefix/route1").should().contain("Route 1");
    get("/prefix/route2").should().contain("Route 2");
  }

  @Test
  public void add_prefixed_resource_with_prefix() {
    configure(routes -> routes.add("/test", ResourceWithPrefix.class));

    get("/test/prefix/route1").should().contain("Route 1");
    get("/test/prefix/route2").should().contain("Route 2");
  }

  @Test
  public void inject_parameters() {
    configure(routes -> routes.add(ResourceWithInjection.class));

    get("/injection/first/second").should().contain("first/second/Context/SimpleRequest/SimpleResponse/SimpleCookies");
  }

  public static class TestResource {
    @Get("/hello")
    public String hello() {
      return "Hello";
    }
  }

  @Prefix("/prefix")
  public static class ResourceWithPrefix {
    @Get("/route1")
    public String route1() {
      return "Route 1";
    }

    @Get("/route2")
    public String route2() {
      return "Route 2";
    }
  }

  public static class ResourceWithInjection {
    @Get("/injection/:param1/:param2")
    public String route(String param1, String param2, Context context, Request request, Response response, Cookies cookies) {
      return String.join("/", param1, param2, context.getClass().getSimpleName(), request.getClass().getSimpleName(), response.getClass().getSimpleName(), cookies.getClass().getSimpleName());
    }
  }
}
