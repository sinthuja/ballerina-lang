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

import ballerinax/java;
import ballerinax/java.sql;

# Represents a JDBC client endpoint.
#
public type Client client object {
    *sql:Client;
    private boolean clientActive = true;

    # Gets called when the JDBC client is instantiated.
    public function __init(ClientConfiguration c) {
        createClient(self, c, globalPoolConfigContainer.getGlobalPoolConfig());
    }

    # The call remote function implementation for JDBC Client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `Error` will be returned if there is any error
    public remote function call(@untainted string sqlQuery, typedesc<record {}>[]? recordType, sql:Param... parameters)
                                returns @tainted table<record {}>[]|()|sql:Error {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return nativeCall(self, java:fromString(sqlQuery), recordType, parameters);
    }

    # The select remote function implementation for JDBC Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + parameters - The parameters to be passed to the select query
    # + return - A `table` returned by the SQL query statement else `Error` will be returned if there is an error
    public remote function select(@untainted string sqlQuery, typedesc<record{}>? recordType, sql:Param... parameters)
                                  returns @tainted table<record {}>|sql:Error {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return nativeSelect(self, java:fromString(sqlQuery), recordType, parameters);
    }

    # The update remote function implementation for JDBC Client to insert/delete/modify data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - The parameters to be passed to the update query
    # + return - `UpdateResult` with the updated row count and key column values,
    #             else `Error` will be returned if there is an error
    public remote function update(@untainted string sqlQuery, sql:Param... parameters)
                                  returns sql:UpdateResult|sql:Error {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return nativeUpdate(self, java:fromString(sqlQuery), parameters);
    }

    # The batchUpdate remote function implementation for JDBC Client to execute batch operations.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - Variable number of parameter arrays each representing the set of parameters belonging to each
    #                update statement
    # + rollbackAllInFailure - If one of the commands in a batch update fails to execute properly, the JDBC driver
    #           may or may not continue to process the remaining commands in the batch. This property can be
    #           used to override this behavior. When it is set to true, if there is a failure in a few commands and
    #           the JDBC driver continues with the remaining commands, the successfully executed commands in the batch
    #           also will get rolled back.
    # + return - A `sql:BatchUpdateResult` with the updated row count and returned error if any. If all the commands
    #            in the batch have executed successfully, the error will be `nil`. If one or more commands have failed,
    #            the `returnedError` field will give the corresponding `Error` along with the int[] which
    #            contains updated row count or the status returned from each command in the batch.
    public remote function batchUpdate(@untainted string sqlQuery, boolean rollbackAllInFailure,
                                       sql:Param?[]... parameters)
                                       returns sql:BatchUpdateResult {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocationForBatchUpdate();
        }
        return nativeBatchUpdate(self, java:fromString(sqlQuery), rollbackAllInFailure, ...parameters);
    }

    # Stops the JDBC client.
    #
    # + return - Possible error during closing the client
    public function close() returns error? {
        self.clientActive = false;
        return close(self);
    }

    # *Deprecated API* Stops the JDBC client.
    #
    # + return - Possible error during closing the client
    public function stop() returns error? {
        return self.close();
    }

    function handleStoppedClientInvocation() returns sql:Error {
       sql:ApplicationError e = sql:ApplicationError(message = "Client has been stopped");
        return e;
    }

    function handleStoppedClientInvocationForBatchUpdate() returns sql:BatchUpdateResult {
        int[] rowCount = [];
        sql:ApplicationError e = sql:ApplicationError(message = "Client has been stopped");
        sql:BatchUpdateResult res = {updatedRowCount: rowCount, generatedKeys: {}, returnedError: e};
        return res;
    }
};

function nativeSelect(Client jdbcClient, @untainted handle sqlQuery, typedesc<record {}>? recordType,
    sql:Param[] parameters) returns @tainted table<record {}>|sql:Error = @java:Method {
        class: "org.ballerinax.jdbc.methods.ExternActions"
    } external;

function nativeCall(Client jdbcClient, @untainted handle sqlQuery, typedesc<record {}>[]? recordType,
    sql:Param[] parameters) returns @tainted table<record {}>[]|()|sql:Error = @java:Method {
       class: "org.ballerinax.jdbc.methods.ExternActions"
    } external;

function nativeUpdate(Client jdbcClient, @untainted handle sqlQuery, sql:Param[] parameters)
    returns sql:UpdateResult|sql:Error = @java:Method {
        class: "org.ballerinax.jdbc.methods.ExternActions"
    } external;

function nativeBatchUpdate(Client jdbcClient, @untainted handle sqlQuery, boolean rollbackAllInFailure,
    sql:Param?[]... parameters) returns sql:BatchUpdateResult = @java:Method {
        class: "org.ballerinax.jdbc.methods.ExternActions"
    } external;

function createClient(Client jdbcClient, ClientConfiguration config, PoolOptions globalPoolOptions) = @java:Method {
    class: "org.ballerinax.jdbc.methods.ExternFunctions"
} external;

function close(Client jdbcClient) returns error? = @java:Method {
    class: "org.ballerinax.jdbc.methods.ExternFunctions"
} external;
