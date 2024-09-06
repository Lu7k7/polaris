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
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.polaris.core.catalog.PageToken;
import org.apache.polaris.core.catalog.PolarisPage;

import java.util.List;

public class ListTablesResponseWithPageToken extends ListTablesResponse {
    @JsonProperty("next-page-token")
    private final PageToken pageToken;
    private final List<TableIdentifier> identifiers;


    public ListTablesResponseWithPageToken(PageToken pageToken, List<TableIdentifier> identifiers) {
        this.pageToken = pageToken;
        this.identifiers = identifiers;
        this.validate();
    }

    public static ListTablesResponseWithPageToken fromPolarisPage(PolarisPage<TableIdentifier> polarisPage) {
        return new ListTablesResponseWithPageToken(polarisPage.pageToken, polarisPage.data);
    }

    public PageToken getPageToken() {
        return pageToken;
    }

    @Override
    public List<TableIdentifier> identifiers() {
        return this.identifiers != null ? this.identifiers : List.of();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("identifiers", this.identifiers)
            .add("pageToken", this.pageToken)
            .toString();
    }
}
