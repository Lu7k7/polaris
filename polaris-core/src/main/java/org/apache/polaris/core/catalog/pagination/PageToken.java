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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a page token that can be used by operations like `listTables`. Clients that specify a
 * `pageSize` (or a `pageToken`) may receive a `next-page-token` in the response, the content of
 * which is a serialized PageToken.
 *
 * <p>By providing that in the next query's `pageToken`, the client can resume listing where they
 * left off. If the client provides a `pageToken` or `pageSize` but `next-page-token` is null in the
 * response, that means there is no more data to read.
 */
public abstract class PageToken {

  public int pageSize;

  public static final PageToken DONE = null;
  public static final int DEFAULT_PAGE_SIZE = 1000;

  protected void validate() {
    if (pageSize <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero");
    }
  }

  /**
   * Get a new PageTokenBuilder from a PageToken. The PageTokenBuilder type should match the
   * PageToken type. Implementations may also provide a static `builder` method to obtain the same
   * PageTokenBuilder.
   */
  protected abstract PageTokenBuilder<?> getBuilder();

  /** Allows `PageToken` implementations to implement methods like `fromLimit` */
  public abstract static class PageTokenBuilder<T extends PageToken> {

    /**
     * A prefix that tokens are expected to start with, ideally unique across `PageTokenBuilder`
     * implementations.
     */
    public abstract String tokenPrefix();

    /**
     * The number of expected components in a token. This should match the number of components
     * returned by getComponents and shouldn't account for the prefix or the checksum.
     */
    public abstract int expectedComponents();

    /** Deserialize a string into a `PageToken` */
    public final PageToken fromString(String tokenString) {
      if (tokenString == null) {
        return ReadEverythingPageToken.get();
      } else if (tokenString.isEmpty()) {
        if (this instanceof ReadEverythingPageToken.ReadEverythingPageTokenBuilder) {
          return ReadEverythingPageToken.get();
        } else {
          return fromLimit(DEFAULT_PAGE_SIZE);
        }
      } else {
        try {
          String decoded =
              new String(Base64.getDecoder().decode(tokenString), StandardCharsets.UTF_8);
          String[] parts = decoded.split(":");

          // +2 to account for the prefix and checksum.
          if (parts.length != expectedComponents() + 2 || !parts[0].equals(tokenPrefix())) {
            throw new IllegalArgumentException("Invalid token format in token: " + tokenString);
          }

          T result = fromStringComponents(Arrays.asList(parts).subList(1, parts.length - 1));
          result.validate();
          return result;
        } catch (Exception e) {
          throw new IllegalArgumentException("Failed to decode page token: " + tokenString, e);
        }
      }
    }

    /**
     * PageTokenBuilder implementations should implement this to build a PageToken from components
     * in a string token. These components should be the same ones returned by `getComponents` and
     * won't include the token prefix or the checksum.
     */
    protected abstract T fromStringComponents(List<String> components);

    /** Construct a `PageToken` from a plain limit */
    public abstract T fromLimit(int limit);
  }

  /** Convert this PageToken to components that the serialized token string will be built from. */
  protected abstract List<String> getComponents();

  /**
   * Builds a new page token to reflect new data that's been read. If the amount of data read is
   * less than the pageSize, this will return `PageToken.DONE` (done)
   */
  protected abstract PageToken updated(List<?> newData);

  /**
   * Builds a `PolarisPage<T>` from a `List<T>`. The `PageToken` attached to the new
   * `PolarisPage<T>` is the same as the result of calling `updated(data)` on this `PageToken`.
   */
  public final <T> PolarisPage<T> buildNextPage(List<T> data) {
    return new PolarisPage<T>(updated(data), data);
  }

  /**
   * Return a new PageToken with an updated pageSize. If the pageSize provided is null, the existing
   * pageSize will be preserved.
   */
  public abstract PageToken withPageSize(Integer pageSize);

  /** Serialize a PageToken into a string */
  @Override
  public final String toString() {
    List<String> components = getComponents();
    String prefix = getBuilder().tokenPrefix();
    String componentString = String.join(":", components);
    String checksum = String.valueOf(componentString.hashCode());
    List<String> allElements =
        Stream.of(prefix, componentString, checksum)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    String rawString = String.join(":", allElements);
    return Base64.getEncoder().encodeToString(rawString.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public final boolean equals(Object o) {
    if (o instanceof PageToken) {
      return this.toString().equals(o.toString());
    } else {
      return false;
    }
  }

  @Override
  public final int hashCode() {
    if (toString() == null) {
      return 0;
    } else {
      return toString().hashCode();
    }
  }
}
