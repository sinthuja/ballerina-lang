// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//TODO: stream<Type, E>, supporting E is not implemented yet.
# A type parameter that is a subtype of `anydata|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type PureType1 anydata | error;

@typeParam
type PureType2 anydata | error;

# Selects the members from an array for which a function returns true.
#
# + strm - The stream
# + func - a predicate to apply to each member to test whether it should be selected
# + return - new stream only containing members of `strm` for which `func` evaluates to true
public function filter(stream<PureType1> strm, function(PureType1 val) returns boolean func) returns stream<PureType1> = external;

# Returns the next element in the stream wrapped in a record or () if the stream ends.
#
# + strm - The stream
# + return - If the stream has elements, return the element wrapped in a record with single field called `value`,
#            otherwise returns ()
public function next(stream<PureType1> strm) returns record {PureType1 value;}? = external;

# Applies a function to each member of a stream and returns a new stream of the results.
#
# + strm - The stream
# + func - A function to apply to each member
# + return - New stream containing result of applying `func` to each member of `strm` in order
public function 'map(stream<PureType1> strm, function(PureType1 val) returns PureType2 func) returns stream<PureType2> = external;
