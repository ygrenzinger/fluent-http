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
package net.codestory.http.websockets;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface WebSocketSession {
  ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  void register(WebSocketListener listener) throws IOException;

  void remove(WebSocketListener listener) throws IOException;

  void close() throws IOException;

  void close(String code, String reason) throws IOException;

  void send(String type, byte[] message) throws IOException;

  Map<Object, Object> getAttributes();

  default Object getAttribute(Object key) {
    return getAttributes().get(key);
  }

  default void send(String type, String message) throws IOException {
    send(type, message.getBytes(UTF_8));
  }

  default void send(String type, Object object) throws IOException {
    send(type, OBJECT_MAPPER.writer().writeValueAsBytes(object));
  }
}
