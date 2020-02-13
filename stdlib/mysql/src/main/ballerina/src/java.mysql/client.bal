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
import ballerinax/java.sql;
import ballerinax/java.jdbc;

public type Client client object {
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

