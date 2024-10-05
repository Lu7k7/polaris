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
import pprint
import re  # noqa: F401
import json

from importlib import import_module
from pydantic import BaseModel, ConfigDict, Field, StrictInt, StrictStr
from typing import Any, ClassVar, Dict, List, Optional, Union
from polaris.catalog.models.file_format import FileFormat
from polaris.catalog.models.primitive_type_value import PrimitiveTypeValue
from typing import Optional, Set
from typing_extensions import Self

from typing import TYPE_CHECKING
if TYPE_CHECKING:
    from polaris.catalog.models.data_file import DataFile
    from polaris.catalog.models.equality_delete_file import EqualityDeleteFile
    from polaris.catalog.models.position_delete_file import PositionDeleteFile

class ContentFile(BaseModel):
    """
    ContentFile
    """ # noqa: E501
    content: StrictStr
    file_path: StrictStr = Field(alias="file-path")
    file_format: FileFormat = Field(alias="file-format")
    spec_id: StrictInt = Field(alias="spec-id")
    partition: Optional[List[PrimitiveTypeValue]] = Field(default=None, description="A list of partition field values ordered based on the fields of the partition spec specified by the `spec-id`")
    file_size_in_bytes: StrictInt = Field(description="Total file size in bytes", alias="file-size-in-bytes")
    record_count: StrictInt = Field(description="Number of records in the file", alias="record-count")
    key_metadata: Optional[StrictStr] = Field(default=None, description="Encryption key metadata blob", alias="key-metadata")
    split_offsets: Optional[List[StrictInt]] = Field(default=None, description="List of splittable offsets", alias="split-offsets")
    sort_order_id: Optional[StrictInt] = Field(default=None, alias="sort-order-id")
    __properties: ClassVar[List[str]] = ["content", "file-path", "file-format", "spec-id", "partition", "file-size-in-bytes", "record-count", "key-metadata", "split-offsets", "sort-order-id"]

    model_config = ConfigDict(
        populate_by_name=True,
        validate_assignment=True,
        protected_namespaces=(),
    )


    # JSON field name that stores the object type
    __discriminator_property_name: ClassVar[str] = 'content'

    # discriminator mappings
    __discriminator_value_class_map: ClassVar[Dict[str, str]] = {
        'data': 'DataFile','equality-deletes': 'EqualityDeleteFile','position-deletes': 'PositionDeleteFile'
    }

    @classmethod
    def get_discriminator_value(cls, obj: Dict[str, Any]) -> Optional[str]:
        """Returns the discriminator value (object type) of the data"""
        discriminator_value = obj[cls.__discriminator_property_name]
        if discriminator_value:
            return cls.__discriminator_value_class_map.get(discriminator_value)
        else:
            return None

    def to_str(self) -> str:
        """Returns the string representation of the model using alias"""
        return pprint.pformat(self.model_dump(by_alias=True))

    def to_json(self) -> str:
        """Returns the JSON representation of the model using alias"""
        # TODO: pydantic v2: use .model_dump_json(by_alias=True, exclude_unset=True) instead
        return json.dumps(self.to_dict())

    @classmethod
    def from_json(cls, json_str: str) -> Optional[Union[DataFile, EqualityDeleteFile, PositionDeleteFile]]:
        """Create an instance of ContentFile from a JSON string"""
        return cls.from_dict(json.loads(json_str))

    def to_dict(self) -> Dict[str, Any]:
        """Return the dictionary representation of the model using alias.

        This has the following differences from calling pydantic's
        `self.model_dump(by_alias=True)`:

        * `None` is only added to the output dict for nullable fields that
          were set at model initialization. Other fields with value `None`
          are ignored.
        """
        excluded_fields: Set[str] = set([
        ])

        _dict = self.model_dump(
            by_alias=True,
            exclude=excluded_fields,
            exclude_none=True,
        )
        # override the default output from pydantic by calling `to_dict()` of each item in partition (list)
        _items = []
        if self.partition:
            for _item in self.partition:
                if _item:
                    _items.append(_item.to_dict())
            _dict['partition'] = _items
        return _dict

    @classmethod
    def from_dict(cls, obj: Dict[str, Any]) -> Optional[Union[DataFile, EqualityDeleteFile, PositionDeleteFile]]:
        """Create an instance of ContentFile from a dict"""
        # look up the object type based on discriminator mapping
        object_type = cls.get_discriminator_value(obj)
        if object_type ==  'DataFile':
            return import_module("polaris.catalog.models.data_file").DataFile.from_dict(obj)
        if object_type ==  'EqualityDeleteFile':
            return import_module("polaris.catalog.models.equality_delete_file").EqualityDeleteFile.from_dict(obj)
        if object_type ==  'PositionDeleteFile':
            return import_module("polaris.catalog.models.position_delete_file").PositionDeleteFile.from_dict(obj)

        raise ValueError("ContentFile failed to lookup discriminator value from " +
                            json.dumps(obj) + ". Discriminator property name: " + cls.__discriminator_property_name +
                            ", mapping: " + json.dumps(cls.__discriminator_value_class_map))

