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

package org.ballerinax.jdbc2.utils;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.h2.tools.DeleteDbFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class holds some utility methods that can be shared by test cases.
 */
public class Utils {
    public static final String SQL_TEST_RESOURCE_DIR = "sql" + File.separator;
    public static final String USER_NAME = "sa";
    public static final String PASSWORD = "";

    private static final String DB_DIRECTORY = Paths.get(".", "target", "tempdb").toString() + File.separator;
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Making the constructor private to make this as a utility class.
     */
    private Utils() {
    }

    /**
     * Create a DB and initialize with given SQL file.
     *
     * @param jdbcURL  JDBC URL
     * @param username Username for the DB
     * @param password Password to connect to the DB
     * @param sqlFile  SQL statements for initialization.
     */
    public static void initDatabase(String jdbcURL, String username, String password, String sqlFile) {
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement st = connection.createStatement()) {
            String sql = loadFileContent(sqlFile);
            String[] sqlQuery = sql.trim().split("/");
            for (String query : sqlQuery) {
                st.executeUpdate(query.trim());
            }
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            log.error("Error while initializing database: ", e);
        }
    }

    private static String loadFileContent(String path) {
        // The name of a resource is a '/'-separated path name that identifies the resource.
        // Hence regardless of the separator corresponding to the OS forward slash should be used.
        URL fileResource = BCompileUtil.class.getClassLoader().getResource(path.replace("\\", "/"));
        try {
            return FileUtils.readFileToString(new File(fileResource.toURI()), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            log.error("File reading failed", e);
        }
        return null;
    }


    /**
     * Returns a H2 database's JDBC URL for the caller.
     *
     * @param dbName Name of the database
     * @return The JDBC URL for the provided h2 database name
     */
    public static String getH2JdbcUrl(String dbName) {
        return "jdbc:h2:file:" + DB_DIRECTORY + dbName;
    }

    /**
     * Drops the database created.
     *
     * @param dbName Name of the database to be deleted.
     */
    public static void deleteH2Database(String dbName) {
        DeleteDbFiles.execute(DB_DIRECTORY, dbName, true);
    }
}
