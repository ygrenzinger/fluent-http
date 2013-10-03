/**
 * Copyright (C) 2013 all@code-story.net
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
package net.codestory.http.routes;

import static net.codestory.http.UriParser.*;
import static net.codestory.http.routes.Match.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import net.codestory.http.*;
import net.codestory.http.annotations.*;
import net.codestory.http.filters.Filter;

import com.sun.net.httpserver.*;

public class RouteCollection implements Routes {
  private final Deque<Route> routes;
  private final Deque<Filter> filters;

  public RouteCollection() {
    this.routes = new LinkedList<>();
    this.filters = new LinkedList<>();
  }

  @Override
  public void staticDir(String fileOrClassPathDir) {
    routes.addLast(new StaticRoute(fileOrClassPathDir));
  }

  @Override
  public void add(Object resource) {
    add("", resource);
  }

  @Override
  public void add(String urlPrefix, Object resource) {
    // Hack to support Mockito Spies
    Class<?> type = resource.getClass();
    if (resource.getClass().getName().contains("EnhancerByMockito")) {
      type = type.getSuperclass();
    }

    for (Method method : type.getMethods()) {
      int parameterCount = method.getParameterCount();

      for (Get get : method.getDeclaredAnnotationsByType(Get.class)) {
        String uriPattern = urlPrefix + get.value();

        add("GET", checkParametersCount(uriPattern, parameterCount), new ReflectionRoute(resource, method));
      }

      for (Post post : method.getDeclaredAnnotationsByType(Post.class)) {
        String uriPattern = urlPrefix + post.value();

        add("POST", checkParametersCount(uriPattern, parameterCount), new ReflectionRoute(resource, method));
      }
    }
  }

  @Override
  public void get(String uriPattern, Payload payload) {
    get(uriPattern, () -> payload);
  }

  @Override
  public void get(String uriPattern, NoParamRoute noParamRoute) {
    add("GET", checkParametersCount(uriPattern, 0), noParamRoute);
  }

  @Override
  public void get(String uriPattern, OneParamRoute route) {
    add("GET", checkParametersCount(uriPattern, 1), route);
  }

  @Override
  public void get(String uriPattern, TwoParamsRoute route) {
    add("GET", checkParametersCount(uriPattern, 2), route);
  }

  @Override
  public void get(String uriPattern, ThreeParamsRoute route) {
    add("GET", checkParametersCount(uriPattern, 3), route);
  }

  @Override
  public void get(String uriPattern, FourParamsRoute route) {
    add("GET", checkParametersCount(uriPattern, 4), route);
  }

  @Override
  public void post(String uriPattern, NoParamRoute noParamRoute) {
    add("POST", checkParametersCount(uriPattern, 0), noParamRoute);
  }

  @Override
  public void post(String uriPattern, OneParamRoute route) {
    add("POST", checkParametersCount(uriPattern, 1), route);
  }

  @Override
  public void post(String uriPattern, TwoParamsRoute route) {
    add("POST", checkParametersCount(uriPattern, 2), route);
  }

  @Override
  public void post(String uriPattern, ThreeParamsRoute route) {
    add("POST", checkParametersCount(uriPattern, 3), route);
  }

  @Override
  public void post(String uriPattern, FourParamsRoute route) {
    add("POST", checkParametersCount(uriPattern, 4), route);
  }

  @Override
  public void filter(Filter filter) {
    filters.addLast(filter);
  }

  public void reset() {
    routes.clear();
    filters.clear();
  }

  private void add(String method, String uriPattern, AnyRoute route) {
    routes.addFirst(new RouteWrapper(method, uriPattern, route));
  }

  public Match apply(HttpExchange exchange) throws IOException {
    String uri = exchange.getRequestURI().getPath();

    for (Filter filter : filters) {
      if (filter.apply(uri, exchange)) {
        return OK;
      }
    }

    Match bestMatch = WRONG_URL;

    for (Route route : routes) {
      Match match = route.apply(uri, exchange);
      if (match == OK) {
        return OK;
      }
      if (match.isBetter(bestMatch)) {
        bestMatch = match;
      }
    }

    return bestMatch;
  }

  private static String checkParametersCount(String uriPattern, int count) {
    if (paramsCount(uriPattern) != count) {
      throw new IllegalArgumentException("Expected " + count + " parameters in " + uriPattern);
    }
    return uriPattern;
  }
}
