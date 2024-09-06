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
package org.apache.polaris.service.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.List;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.polaris.core.catalog.PageToken;
import org.apache.polaris.core.catalog.PolarisPage;

public class ListNamespacesResponseWithPageToken extends ListNamespacesResponse {
  @JsonProperty("next-page-token")
  private final PageToken pageToken;

  private final List<Namespace> namespaces;

  public ListNamespacesResponseWithPageToken(PageToken pageToken, List<Namespace> namespaces) {
    this.pageToken = pageToken;
    this.namespaces = namespaces;
    Preconditions.checkArgument(this.namespaces != null, "Invalid namespace: null");
  }

  public static ListNamespacesResponseWithPageToken fromPolarisPage(
      PolarisPage<Namespace> polarisPage) {
    return new ListNamespacesResponseWithPageToken(polarisPage.pageToken, polarisPage.data);
  }

  public PageToken getPageToken() {
    return pageToken;
  }

  @Override
  public List<Namespace> namespaces() {
    return this.namespaces != null ? this.namespaces : List.of();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("namespaces", this.namespaces)
        .add("pageToken", this.pageToken)
        .toString();
  }
}
