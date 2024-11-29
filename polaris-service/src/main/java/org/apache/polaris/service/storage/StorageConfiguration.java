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
package org.apache.polaris.service.storage;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;

@ConfigMapping(prefix = "polaris.storage")
public interface StorageConfiguration {

  @WithName("aws.awsAccessKey")
  Optional<String> awsAccessKey();

  @WithName("aws.awsSecretKey")
  Optional<String> awsSecretKey();

  @WithName("gcp.token")
  Optional<String> gcpAccessToken();

  @WithName("gcp.lifespan")
  @WithDefault("PT1H")
  Duration gcpAccessTokenLifespan();

  default Supplier<StsClient> stsClientSupplier() {
    return () -> {
      StsClientBuilder stsClientBuilder = StsClient.builder();
      if (awsAccessKey().isPresent() && awsSecretKey().isPresent()) {
        LoggerFactory.getLogger(StorageConfiguration.class)
            .warn("Using hard-coded AWS credentials - this is not recommended for production");
        StaticCredentialsProvider awsCredentialsProvider =
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey().get(), awsSecretKey().get()));
        stsClientBuilder.credentialsProvider(awsCredentialsProvider);
      }
      return stsClientBuilder.build();
    };
  }

  default Supplier<GoogleCredentials> gcpCredentialsSupplier() {
    return () -> {
      if (gcpAccessToken().isEmpty()) {
        try {
          return GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
          throw new RuntimeException("Failed to get GCP credentials", e);
        }
      } else {
        AccessToken accessToken =
            new AccessToken(
                gcpAccessToken().get(),
                new Date(Instant.now().plus(gcpAccessTokenLifespan()).toEpochMilli()));
        return GoogleCredentials.create(accessToken);
      }
    };
  }
}
