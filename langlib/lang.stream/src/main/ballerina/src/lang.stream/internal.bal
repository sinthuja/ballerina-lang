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
@typeParam
type PureType3 anydata | error;

# Takes in a lambda function and returns a new stream out of it.
#
# + td - A type description.
# + func - A lambda function.
# + return - New stream containing results of `func` invocation.
function construct(typedesc<PureType3> td, function() returns PureType3 func) returns stream<PureType3> = external;

# Represent the iterator type returned when `iterator` method is invoked.
type StreamIterator object {

    private stream<PureType1> strm;

    public function __init(stream<PureType1> strm) {
        self.strm = strm;
    }

    # Return the next member in stream iterator, nil if end of iterator is reached.
    # + return - iterator result
    public function next() returns record {|
        PureType1 value;
    |}? = external;
};
