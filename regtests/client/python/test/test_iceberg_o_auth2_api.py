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


import unittest

from polaris.catalog.api.iceberg_o_auth2_api import IcebergOAuth2API


class TestIcebergOAuth2API(unittest.TestCase):
    """IcebergOAuth2API unit test stubs"""

    def setUp(self) -> None:
        self.api = IcebergOAuth2API()

    def tearDown(self) -> None:
        pass

    def test_get_token(self) -> None:
        """Test case for get_token

        Get a token using an OAuth2 flow
        """
        pass


if __name__ == '__main__':
    unittest.main()