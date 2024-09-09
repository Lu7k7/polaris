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

import java.util.List;

// TODO implement and comment
public class EntityIdPageToken extends PageToken {
  public int id;

  @Override
  public PageTokenBuilder<?> builder() {
    return null;
  }

  @Override
  protected List<String> getComponents() {
    return List.of();
  }

  public static class EntityIdPageTokenBuilder extends PageTokenBuilder<EntityIdPageToken> {

    @Override
    public String tokenPrefix() {
      return "";
    }

    @Override
    public int expectedComponents() {
      return 0;
    }

    @Override
    public EntityIdPageToken readEverything() {
      return null;
    }

    @Override
    protected EntityIdPageToken fromStringComponents(List<String> components) {
      return null;
    }

    @Override
    public EntityIdPageToken fromLimit(int limit) {
      return null;
    }
  }

  @Override
  public PageToken updated(List<?> newData) {
    return null;
  }

  @Override
  public PageToken withPageSize(Integer pageSize) {
    return null;
  }
}
