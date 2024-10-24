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
package org.apache.polaris.service.persistence;

import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.Identifier;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.polaris.core.PolarisDiagnostics;
import org.apache.polaris.core.auth.PolarisSecretsManager.PrincipalSecretsResult;
import org.apache.polaris.core.context.RealmContext;
import org.apache.polaris.core.persistence.LocalPolarisMetaStoreManagerFactory;
import org.apache.polaris.core.persistence.PolarisMetaStoreManager;
import org.apache.polaris.core.persistence.PolarisMetaStoreSession;
import org.apache.polaris.core.persistence.PolarisTreeMapMetaStoreSessionImpl;
import org.apache.polaris.core.persistence.PolarisTreeMapStore;
import org.apache.polaris.core.storage.PolarisStorageIntegrationProvider;
import org.apache.polaris.service.context.RealmContextResolver;

@ApplicationScoped
@Identifier("in-memory")
public class InMemoryPolarisMetaStoreManagerFactory
    extends LocalPolarisMetaStoreManagerFactory<PolarisTreeMapStore> {

  private final Set<String> bootstrappedRealms = new HashSet<>();
  private final RealmContextResolver realmContextResolver;
  private final PolarisStorageIntegrationProvider storageIntegration;

  @Inject
  public InMemoryPolarisMetaStoreManagerFactory(
      PolarisStorageIntegrationProvider storageIntegration,
      RealmContextResolver realmContextResolver) {
    this.storageIntegration = storageIntegration;
    this.realmContextResolver = realmContextResolver;
  }

  @Startup
  public void init() {
    // For in-memory metastore we need to bootstrap Service and Service principal at startup
    // (for default realm)
    getOrCreateMetaStoreManager(realmContextResolver::getDefaultRealm);
  }

  @Override
  protected PolarisTreeMapStore createBackingStore(@Nonnull PolarisDiagnostics diagnostics) {
    return new PolarisTreeMapStore(diagnostics);
  }

  @Override
  protected PolarisMetaStoreSession createMetaStoreSession(
      @Nonnull PolarisTreeMapStore store, @Nonnull RealmContext realmContext) {
    return new PolarisTreeMapMetaStoreSessionImpl(
        store, storageIntegration, secretsGenerator(realmContext));
  }

  @Override
  public synchronized PolarisMetaStoreManager getOrCreateMetaStoreManager(
      RealmContext realmContext) {
    String realmId = realmContext.getRealmIdentifier();
    if (!bootstrappedRealms.contains(realmId)) {
      bootstrapRealmAndPrintCredentials(realmId);
    }
    return super.getOrCreateMetaStoreManager(realmContext);
  }

  @Override
  public synchronized Supplier<PolarisMetaStoreSession> getOrCreateSessionSupplier(
      RealmContext realmContext) {
    String realmId = realmContext.getRealmIdentifier();
    if (!bootstrappedRealms.contains(realmId)) {
      bootstrapRealmAndPrintCredentials(realmId);
    }
    return super.getOrCreateSessionSupplier(realmContext);
  }

  private void bootstrapRealmAndPrintCredentials(String realmId) {
    Map<String, PrincipalSecretsResult> results =
        this.bootstrapRealms(Collections.singletonList(realmId));
    bootstrappedRealms.add(realmId);

    PrincipalSecretsResult principalSecrets = results.get(realmId);

    String msg =
        String.format(
            "realm: %1s root principal credentials: %2s:%3s",
            realmId,
            principalSecrets.getPrincipalSecrets().getPrincipalClientId(),
            principalSecrets.getPrincipalSecrets().getMainSecret());
    System.out.println(msg);
  }
}
