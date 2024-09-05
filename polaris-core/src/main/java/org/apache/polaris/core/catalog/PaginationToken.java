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

import java.util.Base64;

public class PaginationToken {

    public final long offset;
    public final long pageSize;

    public static final String TOKEN_PREFIX = "polaris";
    public static final long TOKEN_START = 0;
    public static final long DEFAULT_PAGE_SIZE = 1000;

    public PaginationToken(long offset, long pageSize) {
        this.offset = offset;
        this.pageSize = pageSize;
    }

    /**
     * Construct a PaginationToken from a plain limit
     */
    public static PaginationToken fromLimit(int limit) {
        return new PaginationToken(TOKEN_START, limit);
    }

    /**
     * Construct a PaginationToken from a plain limit
     */
    public static PaginationToken readEverything() {
        return new PaginationToken(TOKEN_START, Integer.MAX_VALUE);
    }

    /**
     * Decode a token string into a PaginationToken object
     */
    public static PaginationToken fromString(String tokenString) {
        if (tokenString == null || tokenString.isEmpty()) {
            return new PaginationToken(TOKEN_START, DEFAULT_PAGE_SIZE);
        }

        if (!tokenString.startsWith(TOKEN_PREFIX)) {
            throw new IllegalArgumentException("Invalid token format");
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(tokenString));
            String[] parts = decoded.split(":");

            if (parts.length != 3 || !parts[0].equals(TOKEN_PREFIX)) {
                throw new IllegalArgumentException("Invalid token format");
            }

            long offset = Long.parseLong(parts[1]);
            long pageSize = Long.parseLong(parts[2]);

            return new PaginationToken(offset, pageSize);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode token: " + tokenString, e);
        }
    }

    @Override
    public String toString() {
        String tokenContent = TOKEN_PREFIX + ":" + offset + ":" + pageSize;
        return Base64.getEncoder().encodeToString(tokenContent.getBytes());
    }
}