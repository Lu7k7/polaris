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
package org.apache.polaris.core.persistence.secrets;

import org.apache.polaris.core.entity.PolarisPrincipalSecrets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultPrincipalSecretsGenerator extends PrincipalSecretsGenerator {

  public DefaultPrincipalSecretsGenerator(@Nullable String realmName) {
    super(realmName);
  }

  private PrincipalSecretsGenerator getDelegate(
      @Nullable String realmName, @NotNull String principalName) {
    var envVarGenerator = new EnvVariablePrincipalSecretsGenerator(realmName);
    if (envVarGenerator.systemGeneratedSecrets(principalName)) {
      return new RandomPrincipalSecretsGenerator(realmName);
    } else {
      return envVarGenerator;
    }
  }

  @Override
  public PolarisPrincipalSecrets produceSecrets(@NotNull String principalName, long principalId) {
    PrincipalSecretsGenerator delegate = getDelegate(realmName, principalName);
    return delegate.produceSecrets(principalName, principalId);
  }

  @Override
  public boolean systemGeneratedSecrets(@NotNull String principalName) {
    PrincipalSecretsGenerator delegate = getDelegate(realmName, principalName);
    return delegate.systemGeneratedSecrets(principalName);
  }
}
