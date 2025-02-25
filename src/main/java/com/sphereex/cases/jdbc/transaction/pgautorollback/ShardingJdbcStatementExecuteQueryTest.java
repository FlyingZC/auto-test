package com.sphereex.cases.jdbc.transaction.pgautorollback;

import com.sphereex.cases.ShardingJdbcBaseTest;
import com.sphereex.core.AutoTest;
import com.sphereex.core.CaseInfo;
import com.sphereex.core.DBType;
import com.sphereex.core.Status;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@AutoTest
public final class ShardingJdbcStatementExecuteQueryTest extends ShardingJdbcBaseTest {
    
    @Getter
    private final DBType dbType = DBType.OPENGAUSS;
    
    @Getter
    private final String yamlFile = null;
    
    private final Logger logger = LoggerFactory.getLogger(ShardingJdbcStatementExecuteQueryTest.class);
    
    @Override
    public Status pre(){
        Status s = super.pre();
        if (!s.isSuccess()) {
            return s;
        }
        try {
            Connection conn = getAutoDataSource().getConnection();
            Statement dropTable = conn.createStatement();
            dropTable.execute("drop table if exists account;");
            Statement createTable = conn.createStatement();
            createTable.execute("create table account(id int, balance float ,transaction_id int);");
            Statement statement = conn.createStatement();
            statement.execute("insert into account(id, balance, transaction_id) values(1,1,1);");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Status(false, e.getMessage());
        }
        return new Status(true, "");
    }
    
    @Override
    public Status run() {
        try {
            innerRun();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Status(false, e.getMessage());
        }
        return new Status(true, "");
    }
    
    private void innerRun() throws SQLException {
        ShardingSphereConnection conn = (ShardingSphereConnection) getAutoDataSource().getConnection();
        conn.setAutoCommit(false);
        if (conn.getConnectionManager().getConnectionTransaction().isRollbackOnly()) {
            logger.error("expect transaction is not rollback only, but transaction rollback only.");
            throw new SQLException("expect transaction is not rollback only, but transaction rollback only.");
        }
        Statement statement2 = conn.createStatement();
        Statement statement3 = conn.createStatement();
        try {
            statement2.execute("update account set balance=100 where id=1;");
            statement3.executeQuery("select * from account1;");
            logger.error("expect report SQLException, but not report");
            throw new SQLException("expect report SQLException, but not report");
        } catch (SQLException ex) {
            if (!conn.getConnectionManager().getConnectionTransaction().isRollbackOnly()) {
                logger.error("expect transaction rollback only, but not");
                throw new SQLException("expect transaction rollback only, but not");
            }
        }
        conn.commit();
        Statement statement4 = conn.createStatement();
        ResultSet r = statement4.executeQuery("select * from account;");
        if (r.next()) {
            int balance = r.getInt("balance");
            if (1 != balance) {
                logger.error("expect balance is 1, but balance:{}.", balance);
                throw new SQLException(String.format("expect balance is 1, but balance:{}.", balance));
            }
        } else {
            logger.error("expect one recode, but not.");
            throw new SQLException("expect one recode, but not.");
        }
    }
    
    @Override
    public void initCase() {
        String name = "ShardingJdbcStatementExecuteQueryTest";
        String feature = "jdbc-transaction";
        String tag = "pg-og-auto-rollback";
        String message = "";
        CaseInfo caseInfo = new CaseInfo(name, feature, tag, message);
        setCaseInfo(caseInfo);
    }
}
