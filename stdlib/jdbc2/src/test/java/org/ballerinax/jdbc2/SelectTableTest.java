/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.jdbc2;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc2.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * This test case executes the test cases to select a table records.
 */
public class SelectTableTest {
    private static final String SELECT_TABLE_TEST = "SELECT_TABLE_TEST";
    private CompileResult result;
    private static final String TEST_DB_NAME = "SELECT_TEST";
    private static final String SQL_FILE_NAME = Utils.SQL_TEST_RESOURCE_DIR + "select_test_data.sql";


    @BeforeClass
    public void setup() {
        Utils.initDatabase(Utils.getH2JdbcUrl(TEST_DB_NAME), Utils.USER_NAME, Utils.PASSWORD, SQL_FILE_NAME);
        result = BCompileUtil.compile(Paths.get("ballerina", "select_test.bal").toString());
    }

    @Test(groups = SELECT_TABLE_TEST)
    public void selectTable() {
        String query = "test query";
        BValue[] args = {new BString(Utils.getH2JdbcUrl(TEST_DB_NAME)), new BString(query)};
        BValue[] returns = BRunUtil.invoke(result, "selectTable", args);
        Assert.assertEquals(returns[0].stringValue(), query);
    }

    @AfterClass
    public void cleanup() {
        Utils.deleteH2Database(TEST_DB_NAME);
    }
}
