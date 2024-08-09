#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# coding: utf-8

"""
    Apache Iceberg REST Catalog API

    Defines the specification for the first version of the REST Catalog API. Implementations should ideally support both Iceberg table specs v1 and v2, with priority given to v2.

    The version of the OpenAPI document: 0.0.1
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import json
from enum import Enum
from typing_extensions import Self


class TokenType(str, Enum):
    """
    Token type identifier, from RFC 8693 Section 3  See https://datatracker.ietf.org/doc/html/rfc8693#section-3
    """

    """
    allowed enum values
    """
    URN_COLON_IETF_COLON_PARAMS_COLON_OAUTH_COLON_TOKEN_MINUS_TYPE_COLON_ACCESS_TOKEN = 'urn:ietf:params:oauth:token-type:access_token'
    URN_COLON_IETF_COLON_PARAMS_COLON_OAUTH_COLON_TOKEN_MINUS_TYPE_COLON_REFRESH_TOKEN = 'urn:ietf:params:oauth:token-type:refresh_token'
    URN_COLON_IETF_COLON_PARAMS_COLON_OAUTH_COLON_TOKEN_MINUS_TYPE_COLON_ID_TOKEN = 'urn:ietf:params:oauth:token-type:id_token'
    URN_COLON_IETF_COLON_PARAMS_COLON_OAUTH_COLON_TOKEN_MINUS_TYPE_COLON_SAML1 = 'urn:ietf:params:oauth:token-type:saml1'
    URN_COLON_IETF_COLON_PARAMS_COLON_OAUTH_COLON_TOKEN_MINUS_TYPE_COLON_SAML2 = 'urn:ietf:params:oauth:token-type:saml2'
    URN_COLON_IETF_COLON_PARAMS_COLON_OAUTH_COLON_TOKEN_MINUS_TYPE_COLON_JWT = 'urn:ietf:params:oauth:token-type:jwt'

    @classmethod
    def from_json(cls, json_str: str) -> Self:
        """Create an instance of TokenType from a JSON string"""
        return cls(json.loads(json_str))


