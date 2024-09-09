/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.polaris.core.catalog.pagination;

import com.sun.jersey.core.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** A PageToken implementation that uses an offset to manage pagination. */
public class OffsetPageToken extends PageToken {

  /**
   * The offset of the token. If this is `5` for example, the first 5 entities returned by a list
   * operation that uses this token will be skipped.
   */
  public final int offset;

  /** The offset to use to start with. */
  private static final int BASE_OFFSET = 0;

  public OffsetPageToken(int offset, int pageSize) {
    this.offset = offset;
    this.pageSize = pageSize;
  }

  @Override
  public PageTokenBuilder<OffsetPageToken> builder() {
    return new OffsetPageTokenBuilder();
  }

  @Override
  protected List<String> getComponents() {
    return List.of(String.valueOf(this.offset), String.valueOf(this.pageSize));
  }

  public static class OffsetPageTokenBuilder extends PageTokenBuilder<OffsetPageToken> {

    @Override
    public String tokenPrefix() {
      return "polaris-offset";
    }

    @Override
    public int expectedComponents() {
      // offset + limit
      return 2;
    }

    @Override
    public OffsetPageToken readEverything() {
      return new OffsetPageToken(BASE_OFFSET, Integer.MAX_VALUE);
    }

    @Override
    protected OffsetPageToken fromStringComponents(List<String> components) {
      OffsetPageToken token =
          new OffsetPageToken(
              Integer.parseInt(components.get(1)), Integer.parseInt(components.get(2)));

      if (token.hashCode() != Integer.parseInt(components.get(3))) {
        throw new IllegalArgumentException(
            "Invalid checksum for offset token: " + token.toString());
      }
      return token;
    }

    @Override
    public OffsetPageToken fromLimit(int limit) {
      return new OffsetPageToken(BASE_OFFSET, limit);
    }
  }

  @Override
  public OffsetPageToken updated(List<?> newData) {
    return new OffsetPageToken(this.offset + newData.size(), pageSize);
  }

  @Override
  public OffsetPageToken withPageSize(Integer pageSize) {
    return new OffsetPageToken(this.offset, pageSize);
  }
}
