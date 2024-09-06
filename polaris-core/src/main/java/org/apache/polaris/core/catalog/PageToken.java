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
package org.apache.polaris.core.catalog;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Represents a page token that can be used by operations like `listTables`. Clients that specify a
 * `pageSize` (or a `pageToken`) may receive a `next-page-token` in the response, the content of
 * which is a serialized PageToken.
 *
 * <p>By providing that in the next query's `pageToken`, the client can resume listing where they
 * left off. If the client provides a `pageToken` or `pageSize` but `next-page-token` is null in the
 * response, that means there is no more data to read.
 */
public class PageToken {

  public final int offset;
  public final int pageSize;

  private static final String TOKEN_PREFIX = "polaris";
  private static final int TOKEN_START = 0;
  private static final int DEFAULT_PAGE_SIZE = 1000;

  public static PageToken DONE = null;

  public PageToken(int offset, int pageSize) {
    this.offset = offset;
    this.pageSize = pageSize;
  }

  /** Construct a PageToken from a plain limit */
  public static PageToken fromLimit(int limit) {
    return new PageToken(TOKEN_START, limit);
  }

  /** Construct a PageToken to read everything */
  public static PageToken readEverything() {
    return new PageToken(TOKEN_START, Integer.MAX_VALUE);
  }

  /** Deserialize a token string into a PageToken object */
  public static PageToken fromString(String tokenString) {
    if (tokenString == null) {
      return PageToken.readEverything();
    } else if (tokenString.isEmpty()) {
      return PageToken.fromLimit(DEFAULT_PAGE_SIZE);
    } else {
      try {
        String decoded =
            new String(Base64.getDecoder().decode(tokenString), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":");

        if (parts.length != 4 || !parts[0].equals(TOKEN_PREFIX)) {
          throw new IllegalArgumentException("Invalid token format in token: " + tokenString);
        }

        int offset = Integer.parseInt(parts[1]);
        int pageSize = Integer.parseInt(parts[2]);
        int checksum = Integer.parseInt(parts[3]);
        PageToken token = new PageToken(offset, pageSize);

        if (token.hashCode() != checksum) {
          throw new IllegalArgumentException("Invalid checksum for token: " + tokenString);
        } else {
          return token;
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Failed to decode page token: " + tokenString, e);
      }
    }
  }

  /**
   * Builds a new page token to reflect new data that's been read. If the amount of data read is
   * less than the pageSize, this will return `PageToken.DONE` (done)
   */
  public PageToken updated(List<?> newData) {
    if (newData == null || newData.isEmpty() || newData.size() < pageSize) {
      return PageToken.DONE;
    } else {
      return new PageToken(offset + newData.size(), pageSize);
    }
  }

  /**
   * Builds a `PolarisPage<T>` from a `List<T>`. The `PageToken` attached to the new
   * `PolarisPage<T>` is the same as the result of calling `updated(data)` on this `PageToken`.
   */
  public <T> PolarisPage<T> buildNextPage(List<T> data) {
    return new PolarisPage<T>(this.updated(data), data);
  }

  /**
   * Return a new PageToken with an updated pageSize. If the pageSize provided is null, the existing
   * pageSize will be preserved.
   */
  public PageToken withPageSize(Integer pageSize) {
    if (pageSize == null) {
      return new PageToken(this.offset, this.pageSize);
    } else {
      return new PageToken(this.offset, pageSize);
    }
  }

  /** Serialize a PageToken into a string */
  @Override
  public String toString() {
    String tokenContent = TOKEN_PREFIX + ":" + offset + ":" + pageSize + ":" + hashCode();
    return Base64.getEncoder().encodeToString(tokenContent.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PageToken) {
      PageToken other = (PageToken) o;
      return offset == other.offset && pageSize == other.pageSize;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return offset + pageSize;
  }
}
