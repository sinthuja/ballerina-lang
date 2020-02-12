// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/config;

public type ClientConfiguration record {|
   string username;
   string password;
   PoolOptions poolOptions;
|};

public type PoolOptions record {|
    string connectionInitSql = config:getAsString("b7a.sql.pool.connectionInitSql", "");
    //string dataSourceName = config:getAsString("b7a.sql.pool.dataSourceName", "");
    boolean autoCommit = config:getAsBoolean("b7a.sql.pool.autoCommit", true);
    boolean isXA = config:getAsBoolean("b7a.sql.pool.isXA", false);
    int maximumPoolSize = config:getAsInt("\"b7a.sql.pool.maximumPoolSize\"", 15);
    int connectionTimeoutInMillis = config:getAsInt("\"b7a.sql.pool.connectionTimeoutInMillis\"", 30000);
    int idleTimeoutInMillis = config:getAsInt("b7a.sql.pool.idleTimeoutInMillis", 600000);
    int minimumIdle = config:getAsInt("b7a.sql.pool.minimumIdle", 15);
    int maxLifetimeInMillis = config:getAsInt("b7a.sql.pool.maxLifetimeInMillis", 1800000);
    int validationTimeoutInMillis = config:getAsInt("\"b7a.sql.pool.validationTimeoutInMillis\"", 5000);
|};
