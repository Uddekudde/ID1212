/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.server;

import se.kth.id1212.tudd.db.server.Client;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author udde
 */
public class FileDAO {
    private static final String USER_TABLE = "USERS";
    private static final String PASSWORD_COLUMN = "PASSWORD";
    private static final String USERNAME_COLUMN = "USERNAME";
    private static final String FILE_TABLE = "FILE";
    private static final String FILENAME_COLUMN = "FILENAME";
    private static final String SIZE_COLUMN = "SIZE";
    private static final String OWNER_COLUMN = "OWNER";
    private static final String PUBLIC_COLUMN = "ACCESS";
    private static final String dbms = "derby";
    private static final String fileCatalogName = "FileCatalog";
    private PreparedStatement createUserStmt;
    private PreparedStatement findUserStmt;
    private PreparedStatement findAllAccountsStmt;
    private PreparedStatement deleteUserStmt;
    private PreparedStatement changeBalanceStmt;
    private PreparedStatement createFileStmt;
    private PreparedStatement deleteFileStmt;
    private PreparedStatement findFileStmt;
    private PreparedStatement findAllFilesStmt;
    private PreparedStatement updateFileStmt;
    
    public FileDAO() throws Exception {
        try {
            Connection connection = createDatasource();
            prepareStatements(connection);
        } catch (ClassNotFoundException | SQLException exception) {
            System.out.println(exception.getMessage());
            throw new Exception("Could not connect to datasource.", exception);
        }
    }
    
    private Connection connectToFileDB(String dbms, String datasource)
            throws ClassNotFoundException, SQLException, Exception {
        if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            return DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else {
            throw new Exception("Unable to create datasource, unknown dbms.");
        }
    }
    
    private boolean tablesExist(Connection connection) throws SQLException {
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        try (ResultSet rs = dbm.getTables(null, null, null, null)) {
            for (; rs.next();) {
                if (rs.getString(tableNameColumn).equals(USER_TABLE)) 
                    return true;
                if (rs.getString(tableNameColumn).equals(FILE_TABLE))
                    return true;
            }
            return false;
        }
    }
    
    private Connection createDatasource() throws
            ClassNotFoundException, SQLException, Exception {
        Connection connection = connectToFileDB(dbms, fileCatalogName);
        if (!tablesExist(connection)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE " + USER_TABLE
                                    + " (" + USERNAME_COLUMN + " VARCHAR(32) PRIMARY KEY, "
                                    + PASSWORD_COLUMN + " VARCHAR (32))");
            statement.executeUpdate("CREATE TABLE " + FILE_TABLE
                                    + " (" + FILENAME_COLUMN + " VARCHAR(32) PRIMARY KEY, "
                                    + SIZE_COLUMN + " INT, " 
                                    + OWNER_COLUMN + " VARCHAR (32), " 
                                    + PUBLIC_COLUMN + " VARCHAR (32))");
        }
        return connection;
    }
    
    public void createUser(String username, String password) throws Exception {
        String failureMsg = "Could not create the account: " + username;
        try {
            createUserStmt.setString(1, username);
            createUserStmt.setString(2, password);
            int rows = createUserStmt.executeUpdate();
            if (rows != 1) {
                throw new Exception(failureMsg);
            }
        } catch (SQLException sqle) {
            throw new Exception(failureMsg, sqle);
        }
    }
    
    public void deleteUser(String username) throws Exception {
        try {
            deleteUserStmt.setString(1, username);
            deleteUserStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new Exception("Could not delete the account: " + username, sqle);
        }
    }
    
    public void createFile(String filename, int size, String owner, String access) throws Exception {
        String failureMsg = "Could not create file: " + filename;
        try {
            createFileStmt.setString(1, filename);
            createFileStmt.setInt(2, size);
            createFileStmt.setString(3, owner);
            createFileStmt.setString(4, access);
            int rows = createFileStmt.executeUpdate();
            if (rows != 1) {
                throw new Exception(failureMsg);
            }
        } catch (SQLException sqle) {
            throw new Exception(failureMsg, sqle);
        }
    }
    
    public void deleteFile(String file) throws Exception {
        try {
            deleteFileStmt.setString(1, file);
            deleteFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new Exception("Could not delete the file: " + file, sqle);
        }
    }
    
    public void updateFile(String filename, String newFilename, int newSize, String newAccess) throws Exception {
        try {
            updateFileStmt.setString(1, newFilename);
            updateFileStmt.setInt(2, newSize);
            updateFileStmt.setString(3, newAccess);
            updateFileStmt.setString(4, filename);
            updateFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new Exception("Could not update the file: " + filename, sqle);
        }
    }
    
    public Client findUserByName(String user) throws Exception {
        String failureMsg = "Could not search for specified user.";
        ResultSet result = null;
        try {
            findUserStmt.setString(1, user);
            result = findUserStmt.executeQuery();
            if (result.next()) {
                return new Client(result.getString(USERNAME_COLUMN), result.getString(PASSWORD_COLUMN));
            }
        } catch (SQLException sqle) {
            throw new Exception(failureMsg, sqle);
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                throw new Exception(failureMsg, e);
            }
        }
        return null;
    }
    
    public File findFileByName(String filename) throws Exception {
        String failureMsg = "Could not search for specified file.";
        ResultSet result = null;
        try {
            findFileStmt.setString(1, filename);
            result = findFileStmt.executeQuery();
            if (result.next()) {
                return new File(result.getString(FILENAME_COLUMN), result.getInt(SIZE_COLUMN),
                        result.getString(OWNER_COLUMN), result.getString(PUBLIC_COLUMN));
            }
        } catch (SQLException sqle) {
            throw new Exception(failureMsg, sqle);
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                throw new Exception(failureMsg, e);
            }
        }
        return null;
    }
    
    public List<File> findAllFiles(String username) throws Exception {
        String failureMsg = "Could not list files.";
        List<File> accounts = new ArrayList<>();
        findAllFilesStmt.setString(1, username);
        try (ResultSet result = findAllFilesStmt.executeQuery()) {
            while (result.next()) {
                accounts.add(new File(result.getString(FILENAME_COLUMN), result.getInt(SIZE_COLUMN),
                        result.getString(OWNER_COLUMN), result.getString(PUBLIC_COLUMN)));
            }
        } catch (SQLException sqle) {
            throw new Exception(failureMsg, sqle);
        }
        return accounts;
    }
    
    
    private void prepareStatements(Connection connection) throws SQLException {
        createUserStmt = connection.prepareStatement("INSERT INTO "
                                                        + USER_TABLE + " VALUES (?, ?)");
        deleteUserStmt = connection.prepareStatement("DELETE FROM "
                                                        + USER_TABLE
                                                        + " WHERE " + USERNAME_COLUMN + " = ?");
        createFileStmt = connection.prepareStatement("INSERT INTO "
                                                        + FILE_TABLE + " VALUES (?, ?, ?, ?)");
        deleteFileStmt = connection.prepareStatement("DELETE FROM "
                                                        + FILE_TABLE
                                                        + " WHERE " + FILENAME_COLUMN + " = ?");
        updateFileStmt = connection.prepareStatement("UPDATE "
                                                        + FILE_TABLE + " SET "+FILENAME_COLUMN+" = ?, "+SIZE_COLUMN
                                                        + " = ?, "+PUBLIC_COLUMN+" = ? WHERE "+FILENAME_COLUMN+" = ?");
        findUserStmt = connection.prepareStatement("SELECT * from "
                                                      + USER_TABLE + " WHERE " + USERNAME_COLUMN+ " = ?");
        findFileStmt = connection.prepareStatement("SELECT * from "
                                                      + FILE_TABLE + " WHERE " + FILENAME_COLUMN + " = ?");
        findAllFilesStmt = connection.prepareStatement("SELECT * from "
                                                          + FILE_TABLE + " WHERE NOT " + PUBLIC_COLUMN+" = 'private' OR "
                                                          + OWNER_COLUMN +" = ?");
    }
}
