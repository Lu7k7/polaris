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
 * Represents a page token
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

    /**
     * Construct a PaginationToken from a plain limit
     */
    public static PageToken fromLimit(int limit) {
        return new PageToken(TOKEN_START, limit);
    }

    /**
     * Construct a PaginationToken from a plain limit
     */
    public static PageToken readEverything() {
        return new PageToken(TOKEN_START, Integer.MAX_VALUE);
    }

    /**
     * Decode a token string into a PaginationToken object
     */
    public static PageToken fromString(String tokenString) {
        if (tokenString == null || tokenString.isEmpty()) {
            return new PageToken(TOKEN_START, DEFAULT_PAGE_SIZE);
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(tokenString), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");

            if (parts.length != 3 || !parts[0].equals(TOKEN_PREFIX)) {
                throw new IllegalArgumentException("Invalid token format");
            }

            int offset = Integer.parseInt(parts[1]);
            int pageSize = Integer.parseInt(parts[2]);

            return new PageToken(offset, pageSize);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode token: " + tokenString, e);
        }
    }

    /**
     * Builds a new page token to reflect new data that's been read
     */
    public PageToken updated(List<?> newData) {
        return new PageToken(offset + newData.size(), pageSize);
    }

    public PageToken withPageSize(Integer pageSize) {
        if (pageSize == null) {
            return this;
        } else {
            return new PageToken(offset, pageSize);
        }
    }

    @Override
    public String toString() {
        String tokenContent = TOKEN_PREFIX + ":" + offset + ":" + pageSize;
        return Base64.getEncoder().encodeToString(tokenContent.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PageToken) {
            PageToken other = (PageToken)o;
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