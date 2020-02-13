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

import ballerina/crypto;
import ballerinax/sql;
import ballerinax/java.jdbc;

public type Client client object {
    *sql:Client;
    jdbc:Client jdbcClient;

    public function __init(ClientConfiguration clientConfig){
        string jdbcMySQL = "jdbc:mysql://"+clientConfig.host+":"+clientConfig.port.toString()+"/"+clientConfig.database;
        map<anydata> jdbcOptions = getJdbcOptions(clientConfig.options);
        self.jdbcClient = new ({
          url: jdbcMySQL,
          username: clientConfig.username,
          password: clientConfig.password,
          dbOptions: jdbcOptions
        });
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
       return self.jdbcClient->call(sqlQuery, recordType, ...parameters);
    }

    # The select remote function implementation for JDBC Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + parameters - The parameters to be passed to the select query
    # + return - A `table` returned by the SQL query statement else `Error` will be returned if there is an error
    public remote function select(@untainted string sqlQuery, typedesc<record{}>? recordType, sql:Param... parameters)
                                  returns @tainted table<record {}>|sql:Error {
        return self.jdbcClient->select(sqlQuery, recordType, ...parameters);
    }

    # The update remote function implementation for SQL Client to insert/delete/modify data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - The parameters to be passed to the update query
    # + return - `UpdateResult` with the updated row count and key column values,
    #             else `Error` will be returned if there is an error
    public remote function update(@untainted string sqlQuery, sql:Param... parameters)
                                  returns sql:UpdateResult|sql:Error {
         return self.jdbcClient->update(sqlQuery, ...parameters);
    }

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
                                       sql:Param?[]... parameters)
                                       returns sql:BatchUpdateResult{
          return self.jdbcClient->batchUpdate(sqlQuery, rollbackAllInFailure, ...parameters);
    }

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns error?{
        return self.jdbcClient.close();
    }

};

function getJdbcOptions(Options options) returns map<anydata>{
     map<anydata> jdbcOptions = {};
     return jdbcOptions;
}

public type ClientConfiguration record {|
    *sql:ClientConfiguration;
    string host;
    int? port = 3306;
    string database;
    sql:PoolOptions poolOptions?;
    Options options = {};
|};

public type Options record{|
    //https://blog.querypie.com/mysql-ssl-connection-using-jdbc/
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
    // To support CA and hostname, certs should be validated.
    // And this require the keys to be imported in the turst store via
    // java key tool. As this will be exposing the details of java, ignoring those properties.
    // If someone requires it, they can use JDBC connector.
    //*Specific to mysql 8.0.13 or later*
    SSLMode? sslMode = PREFERRED;
    //Sets the collation used for client-server interaction on connection. In contrast to charset,
    // collation does not issue additional queries. If the specified collation is unavailable on the target server, the connection will fail.
    //A list of valid charsets for a server is retrievable with SHOW COLLATION.
    //The default collation (utf8mb4_general_ci) is supported from MySQL 5.5.
    //You should use an older collation (e.g. utf8_general_ci) for older MySQL.
    string? collation = "utf8mb4_general_ci";
    // Maximum allowed packet size to send to server. If not set, the value of system variable 'max_allowed_packet'
    // in server will be used to initialize this upon connecting.
    // This value will not take effect if set larger than the value of 'max_allowed_packet'.
    //default is 65535. Is this required?
    int? maxAllowedPacket = 65535;
    //If 0, no read/write timeout (socketTimeout in JDBC properties).
    int? readWriteTimeoutInSeconds = 30;

|};

const NONE = "NONE";
const PREFERRED = "PREFERRED";

public type SSLMode NONE | PREFERRED | Required ;

//Empty record will map to REQUIRED.
public type Required record {|
   //Setting clientKeystore or trustKeystore will enable VERIFY_CA
  crypto:KeyStore? clientCertKeystore = ();
  crypto:KeyStore? trustCertKeystore = ();
  //Enabling this will enable host name verification as well.
  boolean? verifyHostname =  false;
|};

