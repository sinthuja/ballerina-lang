// Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerinax/mysql;

function testWithMandatoryFields(string hostname, int port, string user, string pw, string db) returns boolean {
    mysql:Client testDB = new ({
            host: hostname,
            port: port,
            username: user,
            password: pw,
            database: db
    });
    var dt = testDB->select("SELECT * FROM Customers", ());
    boolean success = false;
    if (dt is table<record {}>) {
        success = true;
    }
    checkpanic testDB.close();
    return success;
}



//function testConnectionPoolProperties2(string jdbcURL) returns table<record {}>|sql:Error {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: properties
//    });
//
//    return testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//}
//
//function testConnectionPoolProperties3(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: ""
//    });
//
//    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//    json j = getJsonConversionResult(dt);
//    checkpanic testDB.stop();
//    return j;
//}
//
//
//function testConnectorWithDefaultPropertiesForListedDB(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: {}
//    });
//
//    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//    json j = getJsonConversionResult(dt);
//    checkpanic testDB.stop();
//    return j;
//}
//
//function testConnectorWithWorkers(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: {}
//    });
//
//    worker w1 returns json {
//        int x = 0;
//        json y;
//
//        var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//        json j = getJsonConversionResult(dt);
//        checkpanic testDB.stop();
//        return j;
//    }
//    worker w2 {
//        int x = 10;
//    }
//    return wait w1;
//}
//
//function testConnectorWithDataSourceClass(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: properties3,
//        dbOptions: propertiesMap
//    });
//
//    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//    json j = getJsonConversionResult(dt);
//    checkpanic testDB.stop();
//    return j;
//}
//
//function testConnectorWithDataSourceClassAndProps(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: properties4,
//        dbOptions: propertiesMap2
//    });
//
//    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//    json j = getJsonConversionResult(dt);
//    checkpanic testDB.stop();
//    return j;
//}
//
//function testConnectorWithDataSourceClassWithoutURL(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: properties5
//    });
//
//    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//    json j = getJsonConversionResult(dt);
//    checkpanic testDB.stop();
//    return j;
//}
//
//function testConnectorWithDataSourceClassURLPriority(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: properties6,
//        dbOptions: propertiesMap3
//    });
//
//
//    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//    json j = getJsonConversionResult(dt);
//    checkpanic testDB.stop();
//    return j;
//}
//
//
//function testPropertiesGetUsedOnlyIfDataSourceGiven(string jdbcURL) returns @tainted json {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: {maximumPoolSize: 1},
//        dbOptions: {"invalidProperty": 109}
//    });
//
//    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
//
//    json j = getJsonConversionResult(dt);
//    checkpanic testDB.stop();
//    return j;
//}
//
//function testConnectionFailure(string jdbcURL) {
//    jdbc:Client testDB = new ({
//        url: jdbcURL,
//        username: "SA",
//        password: "",
//        poolOptions: {maximumPoolSize: 1},
//        dbOptions: {"IFEXISTS": true}
//    });
//
//}
//
//function testInvalidJdbcUrl1() {
//    jdbc:Client testDB = new ({
//        url: "",
//        username: "SA",
//        password: "",
//        poolOptions: {maximumPoolSize: 1},
//        dbOptions: {"IFEXISTS": true}
//    });
//}
//
//function testInvalidJdbcUrl2() {
//    jdbc:Client testDB = new ({
//        url: "localhost:3306/testdb",
//        username: "SA",
//        password: "",
//        poolOptions: {maximumPoolSize: 1},
//        dbOptions: {"IFEXISTS": true}
//    });
//}
//
//function testInvalidJdbcUrl3() {
//    jdbc:Client testDB = new ({
//        url: "jdbc://dbhost.com/testdb",
//        username: "SA",
//        password: "",
//        poolOptions: {maximumPoolSize: 1},
//        dbOptions: {"IFEXISTS": true}
//    });
//}