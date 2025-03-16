/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.ai.completion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.sql.loader.SQLEditorSupport;

/**
 *
 * @author Gaurav Gupta
 */
public class SQLCompletion {

    private final DatabaseConnection dbConnection;

    public SQLCompletion(SQLEditorSupport sQLEditorSupport) {
        this(sQLEditorSupport.getDatabaseConnection());
    }

    public SQLCompletion(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public String getMetaData() {
        StringBuilder info = new StringBuilder();
        if (dbConnection != null) {
            Connection connection = dbConnection.getJDBCConnection();
            if (connection == null) {
                JOptionPane.showMessageDialog(null, "Warning: Database connection is not active!", "Connection Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String dbName = metaData.getDatabaseProductName();
                String dbVersion = metaData.getDatabaseProductVersion();
                String tableMetadata = extractTableStructures(connection);
                info.append("DB Name:").append(dbName).append("\n");
                info.append("DB Version:").append(dbVersion).append("\n");
                info.append("DB Structure:").append(tableMetadata).append("\n");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return info.toString();
    }

    private static String extractTableStructures(Connection connection) {
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("Table Name,Column Name,Column Type,Nullable,Default Value,Primary Key,Unique,Index,Auto Increment,Column Size,Decimal Digits,Foreign Key,Remarks\n");

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, connection.getSchema(), null, new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                // Retrieve primary keys for the current table
                Set<String> primaryKeys = new HashSet<>();
                ResultSet pkResultSet = metaData.getPrimaryKeys(null, connection.getSchema(), tableName);
                while (pkResultSet.next()) {
                    primaryKeys.add(pkResultSet.getString("COLUMN_NAME"));
                }
                pkResultSet.close();

                // Retrieve unique indexes
                Set<String> uniqueIndexes = new HashSet<>();
                ResultSet uniqueIndexResultSet = metaData.getIndexInfo(null, connection.getSchema(), tableName, true, false);
                while (uniqueIndexResultSet.next()) {
                    uniqueIndexes.add(uniqueIndexResultSet.getString("COLUMN_NAME"));
                }
                uniqueIndexResultSet.close();

                // Retrieve all indexes (both unique and non-unique)
                Set<String> allIndexes = new HashSet<>();
                ResultSet indexResultSet = metaData.getIndexInfo(null, connection.getSchema(), tableName, false, false);
                while (indexResultSet.next()) {
                    allIndexes.add(indexResultSet.getString("COLUMN_NAME"));
                }
                indexResultSet.close();

                // Retrieve foreign keys for the current table
                Set<String> foreignKeys = new HashSet<>();
                ResultSet foreignKeyResultSet = metaData.getImportedKeys(null, connection.getSchema(), tableName);
                while (foreignKeyResultSet.next()) {
                    foreignKeys.add(foreignKeyResultSet.getString("FKCOLUMN_NAME"));
                }
                foreignKeyResultSet.close();

                // Retrieve columns for the current table
                ResultSet columns = metaData.getColumns(null, null, tableName, null);
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    String isNullable = columns.getString("NULLABLE").equals("1") ? "YES" : "NO";
                    String columnDefault = columns.getString("COLUMN_DEF");
                    String isAutoIncrement = columns.getString("IS_AUTOINCREMENT").equals("YES") ? "YES" : "NO";
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                    String remarks = columns.getString("REMARKS");

                    // Determine if the column is a primary key, unique, or indexed
                    String isPrimaryKey = primaryKeys.contains(columnName) ? "YES" : "NO";
                    String isUnique = uniqueIndexes.contains(columnName) ? "YES" : "NO";
                    String isIndexed = allIndexes.contains(columnName) ? "YES" : "NO";
                    String isForeignKey = foreignKeys.contains(columnName) ? "YES" : "NO";

                    // Append column details in CSV format
                    csvBuilder.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%d,%s,%s\n",
                            tableName,
                            columnName,
                            columnType,
                            isNullable,
                            columnDefault == null ? "" : columnDefault,
                            isPrimaryKey,
                            isUnique,
                            isIndexed,
                            isAutoIncrement,
                            columnSize,
                            decimalDigits,
                            isForeignKey,
                            remarks == null ? "" : remarks));
                }
                columns.close(); // Close columns ResultSet
            }
            tables.close(); // Close tables ResultSet
        } catch (SQLException e) {
            e.printStackTrace(); // Handle any SQL exceptions
        }

        return csvBuilder.toString(); // Return the collected CSV data
    }

}
