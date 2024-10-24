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
package org.apache.polaris.service.test;

import static org.apache.polaris.service.context.DefaultRealmContextResolver.REALM_PROPERTY_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.test.junit.QuarkusMock;
import jakarta.enterprise.inject.Vetoed;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.iceberg.aws.s3.S3FileIO;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.apache.iceberg.io.FileIO;
import org.apache.polaris.core.PolarisCallContext;
import org.apache.polaris.core.admin.model.GrantPrincipalRoleRequest;
import org.apache.polaris.core.admin.model.Principal;
import org.apache.polaris.core.admin.model.PrincipalRole;
import org.apache.polaris.core.admin.model.PrincipalWithCredentials;
import org.apache.polaris.core.context.CallContext;
import org.apache.polaris.core.context.RealmContext;
import org.apache.polaris.core.entity.PolarisEntityConstants;
import org.apache.polaris.core.entity.PolarisEntitySubType;
import org.apache.polaris.core.entity.PolarisEntityType;
import org.apache.polaris.core.entity.PolarisPrincipalSecrets;
import org.apache.polaris.core.persistence.PolarisMetaStoreManager;
import org.apache.polaris.core.persistence.PolarisMetaStoreSession;
import org.apache.polaris.core.storage.aws.PolarisS3FileIOClientFactory;
import org.apache.polaris.service.auth.TokenUtils;
import org.apache.polaris.service.catalog.io.DefaultFileIOFactory;
import org.apache.polaris.service.catalog.io.FileIOFactory;
import org.apache.polaris.service.persistence.InMemoryPolarisMetaStoreManagerFactory;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisIntegrationTestFixture {

  public record SnowmanIdentifier(String principalName, String principalRoleName) {}

  public record SnowmanCredentials(
      String clientId, String clientSecret, SnowmanIdentifier identifier) {}

  private static final Logger LOGGER = LoggerFactory.getLogger(PolarisIntegrationTestFixture.class);

  private final PolarisIntegrationTestHelper helper;

  public final String realm;
  public final PolarisPrincipalSecrets adminSecrets;
  public final SnowmanCredentials snowmanCredentials;
  public final String adminToken;
  public final String userToken;
  public final Client client;

  private final URI baseUri;

  public PolarisIntegrationTestFixture(
      PolarisIntegrationTestHelper helper, TestEnvironment testEnv, TestInfo testInfo) {
    this.helper = helper;
    this.client = ClientBuilder.newClient();
    this.baseUri = testEnv.baseUri();
    QuarkusMock.installMockForType(new MockFileIOFactory(), FileIOFactory.class);
    // Generate unique realm using test name for each test since the tests can run in parallel
    realm = testInfo.getTestClass().orElseThrow().getName().replace('.', '_');
    adminSecrets = fetchAdminSecrets();
    adminToken =
        TokenUtils.getTokenFromSecrets(
            client,
            baseUri,
            adminSecrets.getPrincipalClientId(),
            adminSecrets.getMainSecret(),
            realm);
    snowmanCredentials = createSnowmanCredentials();
    userToken =
        TokenUtils.getTokenFromSecrets(
            client,
            baseUri,
            snowmanCredentials.clientId(),
            snowmanCredentials.clientSecret(),
            realm);
  }

  private PolarisPrincipalSecrets fetchAdminSecrets() {
    if (!(helper.metaStoreManagerFactory instanceof InMemoryPolarisMetaStoreManagerFactory)) {
      helper.metaStoreManagerFactory.bootstrapRealms(List.of(realm));
    }

    RealmContext realmContext =
        helper.realmContextResolver.resolveRealmContext(
            baseUri.toString(), "GET", "/", Map.of(), Map.of(REALM_PROPERTY_KEY, realm));

    PolarisMetaStoreSession metaStoreSession =
        helper.metaStoreManagerFactory.getOrCreateSessionSupplier(realmContext).get();
    PolarisCallContext polarisContext =
        new PolarisCallContext(
            metaStoreSession, helper.diagServices, helper.configurationStore, helper.clock);
    try (CallContext ctx = CallContext.of(realmContext, polarisContext)) {
      CallContext.setCurrentContext(ctx);
      PolarisMetaStoreManager metaStoreManager =
          helper.metaStoreManagerFactory.getOrCreateMetaStoreManager(ctx.getRealmContext());
      PolarisMetaStoreManager.EntityResult principal =
          metaStoreManager.readEntityByName(
              ctx.getPolarisCallContext(),
              null,
              PolarisEntityType.PRINCIPAL,
              PolarisEntitySubType.NULL_SUBTYPE,
              PolarisEntityConstants.getRootPrincipalName());

      Map<String, String> propertiesMap = readInternalProperties(principal);
      return metaStoreManager
          .loadPrincipalSecrets(ctx.getPolarisCallContext(), propertiesMap.get("client_id"))
          .getPrincipalSecrets();
    } finally {
      CallContext.unsetCurrentContext();
    }
  }

  private SnowmanCredentials createSnowmanCredentials() {

    SnowmanIdentifier snowmanIdentifier = getSnowmanIdentifier();
    PrincipalRole principalRole = new PrincipalRole(snowmanIdentifier.principalRoleName());

    try (Response createPrResponse =
        client
            .target(String.format("%s/api/management/v1/principal-roles", baseUri))
            .request("application/json")
            .header("Authorization", "Bearer " + adminToken)
            .header(REALM_PROPERTY_KEY, realm)
            .post(Entity.json(principalRole))) {
      assertThat(createPrResponse)
          .returns(Response.Status.CREATED.getStatusCode(), Response::getStatus);
    }

    Principal principal = new Principal(snowmanIdentifier.principalName());
    SnowmanCredentials snowmanCredentials;

    try (Response createPResponse =
        client
            .target(String.format("%s/api/management/v1/principals", baseUri))
            .request("application/json")
            .header("Authorization", "Bearer " + adminToken) // how is token getting used?
            .header(REALM_PROPERTY_KEY, realm)
            .post(Entity.json(principal))) {
      assertThat(createPResponse)
          .returns(Response.Status.CREATED.getStatusCode(), Response::getStatus);

      PrincipalWithCredentials snowmanWithCredentials =
          createPResponse.readEntity(PrincipalWithCredentials.class);
      try (Response rotateResp =
          client
              .target(
                  String.format(
                      "%s/api/management/v1/principals/%s/rotate", baseUri, principal.getName()))
              .request(MediaType.APPLICATION_JSON)
              .header(
                  "Authorization",
                  "Bearer "
                      + TokenUtils.getTokenFromSecrets(
                          client,
                          baseUri,
                          snowmanWithCredentials.getCredentials().getClientId(),
                          snowmanWithCredentials.getCredentials().getClientSecret(),
                          realm))
              .header(REALM_PROPERTY_KEY, realm)
              .post(Entity.json(snowmanWithCredentials))) {

        assertThat(rotateResp).returns(Response.Status.OK.getStatusCode(), Response::getStatus);

        // Use the rotated credentials.
        snowmanWithCredentials = rotateResp.readEntity(PrincipalWithCredentials.class);
      }
      snowmanCredentials =
          new SnowmanCredentials(
              snowmanWithCredentials.getCredentials().getClientId(),
              snowmanWithCredentials.getCredentials().getClientSecret(),
              snowmanIdentifier);
    }
    try (Response assignPrResponse =
        client
            .target(
                String.format(
                    "%s/api/management/v1/principals/%s/principal-roles",
                    baseUri, principal.getName()))
            .request("application/json")
            .header("Authorization", "Bearer " + adminToken) // how is token getting used?
            .header(REALM_PROPERTY_KEY, realm)
            .put(Entity.json(new GrantPrincipalRoleRequest(principalRole)))) {
      assertThat(assignPrResponse)
          .returns(Response.Status.CREATED.getStatusCode(), Response::getStatus);
    }
    return snowmanCredentials;
  }

  public void destroy() {
    try {
      if (realm != null) {
        helper.metaStoreManagerFactory.purgeRealms(List.of(realm));
      }
    } catch (Exception e) {
      LOGGER.error("Failed to purge realm", e);
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (Exception e) {
          LOGGER.error("Failed to close client", e);
        }
      }
    }
  }

  private Map<String, String> readInternalProperties(
      PolarisMetaStoreManager.EntityResult principal) {
    try {
      return helper.objectMapper.readValue(
          principal.getEntity().getInternalProperties(), new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static SnowmanIdentifier getSnowmanIdentifier() {
    return new SnowmanIdentifier("snowman", "catalog-admin");
  }

  /** Workaround for class loading issues with Quarkus tests. */
  @Vetoed
  private static class MockFileIOFactory extends DefaultFileIOFactory {

    @Override
    public FileIO loadFileIO(String impl, Map<String, String> properties) {
      if (impl.equals("org.apache.iceberg.aws.s3.S3FileIO")) {
        PolarisS3FileIOClientFactory factory = new PolarisS3FileIOClientFactory();
        factory.initialize(properties);
        return new S3FileIO(factory::s3, new S3FileIOProperties(properties));
      }
      return super.loadFileIO(impl, properties);
    }
  }
}
