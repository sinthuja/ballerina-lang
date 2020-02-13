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

public type Client abstract client object {

    # The call remote function implementation for SQL Client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `Error` will be returned if there is any error
    public remote function call(@untainted string sqlQuery, typedesc<record {}>[]? recordType, Param... parameters)
                                returns @tainted table<record {}>[]|()|Error;

    # The select remote function implementation for SQL Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + parameters - The parameters to be passed to the select query
    # + return - A `table` returned by the SQL query statement else `Error` will be returned if there is an error
    public remote function select(@untainted string sqlQuery, typedesc<record{}>? recordType, Param... parameters)
                                  returns @tainted table<record {}>|Error;

    # The update remote function implementation for SQL Client to insert/delete/modify data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - The parameters to be passed to the update query
    # + return - `UpdateResult` with the updated row count and key column values,
    #             else `Error` will be returned if there is an error
    public remote function update(@untainted string sqlQuery, Param... parameters)
                                  returns UpdateResult|Error;

    # The batchUpdate remote function implementation for SQL Client to execute batch operations.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - Variable number of parameter arrays each representing the set of parameters belonging to each
    #                update statement
    # + rollbackAllInFailure - If one of the commands in a batch update fails to execute properly, the SQL driver
    #           may or may not continue to process the remaining commands in the batch. This property can be
    #           used to override this behavior. When it is set to true, if there is a failure in a few commands and
    #           the SQL driver continues with the remaining commands, the successfully executed commands in the batch
    #           also will get rolled back.
    # + return - A `BatchUpdateResult` with the updated row count and returned error if any. If all the commands
    #            in the batch have executed successfully, the error will be `nil`. If one or more commands have failed,
    #            the `returnedError` field will give the corresponding `Error` along with the int[] which
    #            contains updated row count or the status returned from each command in the batch.
    public remote function batchUpdate(@untainted string sqlQuery, boolean rollbackAllInFailure,
                                       Param?[]... parameters)
                                       returns BatchUpdateResult;

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns error?;
};
