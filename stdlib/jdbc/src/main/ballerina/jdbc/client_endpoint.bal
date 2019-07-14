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

# Represents a JDBC client endpoint.
#
# + jdbcClient - The base JDBC Client
public type Client client object {

    private JdbcClient jdbcClient;
    private boolean clientActive = true;

    # Gets called when the JDBC client is instantiated.
    public function __init(ClientEndpointConfig c) {
        self.jdbcClient = createClient(c, getGlobalPoolConfigContainer().getGlobalPoolConfig());
    }

    # The call remote function implementation for JDBC Client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call. The number of parameters is variable
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `JdbcClientError` will be returned if there is any error
    public remote function call(@untainted string sqlQuery, typedesc[]? recordType, Param... parameters)
                                returns @tainted table<record {}>[]|()|JdbcClientError {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return self.jdbcClient->call(sqlQuery, recordType, ...parameters);
    }

    # The select remote function implementation for JDBC Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + parameters - The parameters to be passed to the select query. The number of parameters is variable
    # + return - A `table` returned by the sql query statement else `JdbcClientError` will be returned if there
    # is any error
    public remote function select(@untainted string sqlQuery, typedesc? recordType, Param... parameters)
                                  returns @tainted table<record {}>|JdbcClientError {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return self.jdbcClient->select(sqlQuery, recordType, ...parameters);
    }

    # The update remote function implementation for JDBC Client to update data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + keyColumns - Names of auto generated columns for which the auto generated key values are returned
    # + parameters - The parameters to be passed to the update query. The number of parameters is variable
    # + return - `UpdateResult` with the updated row count and key column values,
    #             else  `JdbcClientError` will be returned if there is any error
    public remote function update(@untainted string sqlQuery, string[]? keyColumns = (), Param... parameters)
                                  returns UpdateResult|JdbcClientError {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return self.jdbcClient->update(sqlQuery, keyColumns = keyColumns, ...parameters);
    }

    # The batchUpdate remote function implementation for JDBC Client to batch data insert.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - Variable number of parameter arrays each representing the set of parameters of belonging to each
    #                individual update
    # + return - A `BatchUpdateResult` with the updated row count and returned error. If all the commands in the batch
    #                has executed successfully, the error will be `nil`. If one or more commands has failed, the
    #               `returnedError` field will give the correspoing `JdbcClientError` along with the int[] which
    #                conains updated row count or the status returned from the each command in the batch.
    public remote function batchUpdate(@untainted string sqlQuery, Param?[]... parameters)
                                       returns BatchUpdateResult {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocationForBatchUpdate();
        }
        return self.jdbcClient->batchUpdate(sqlQuery, ...parameters);
    }

    # Stops the JDBC client.
    # + return - Possible error during closing
    public function stop() returns error? {
        self.clientActive = false;
        return close(self.jdbcClient);
    }

    function handleStoppedClientInvocation() returns JdbcClientError {
        ApplicationError e = error(message = "Client has been stopped");
        return e;
    }

    function handleStoppedClientInvocationForBatchUpdate() returns BatchUpdateResult {
        int[] rowCount = [];
        ApplicationError e = error(message = "Client has been stopped");
        BatchUpdateResult res = { updatedRowCount: rowCount, returnedError : e};
        return res;
    }
};

