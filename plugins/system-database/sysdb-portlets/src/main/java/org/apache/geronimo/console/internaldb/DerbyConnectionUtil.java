/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.internaldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.derby.DerbySystemGBean;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.management.JCAManagedConnectionFactory;

/**
 * A static class to handle retreiving connections. This class is built to
 * handle lookups to the SystemDatabase as a special case. If a connection is
 * requested for the SystemDatabase this class gets a DataSource from an admin
 * object registered in the geronimo kernel otherwise the DataSource is looked
 * up via JNDI.
 *
 * @version $Rev$ $Date$
 */
public class DerbyConnectionUtil {

    private final static Log log = LogFactory.getLog(DerbyConnectionUtil.class);

	public static final String CREATE_DB_PROP = ";create=true";

    public static final String SHUTDOWN_DB_PROP = ";shutdown=true";

    private static final int RDBMS_DERBY = 1;

    private static final int RDBMS_MSSQL = 2;

    private static final String SYSTEM_DB = "SYSTEMDATABASE";

    private static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String PROTOCOL = "jdbc:derby:";

    private static final String EMPTY_PROPS = "";
    
    private static AbstractName SYSTEM_DATASOURCE_NAME = null;
    
    static {
        try {
            log.debug("Looking up system datasource name...");

            // cache the name for the system data source
            AbstractNameQuery query = new AbstractNameQuery(JCAManagedConnectionFactory.class.getName());
            Set<AbstractName> names = KernelRegistry.getSingleKernel().listGBeans(query);
            for (AbstractName name : names) {
                String nameProperty = name.getNameProperty("name");
                if ("SystemDatasource".equals(nameProperty)) {
                    SYSTEM_DATASOURCE_NAME = name;
                    log.debug("Using system datasource name: " + SYSTEM_DATASOURCE_NAME);
                }
            }
            
            if (SYSTEM_DATASOURCE_NAME == null) {
                log.warn("Failed to lookup system datasource name");
            }
        }
        catch (Throwable t) {
            //
            // HACK: Log any errors which occur when this is loading...
            //       the system is not logging the full detail, which it should
            //       but for now lets show the details here
            //
            log.error("Failed to initialize", t);
            throw new Error(t);
        }
    }
    
    private static String derbyHome = null;
    
    /**
     * Get the Derby home directory path.
     */
    public static String getDerbyHome() {
        if (derbyHome == null) {
            try {
                derbyHome = (String)KernelRegistry.getSingleKernel().getAttribute(DerbySystemGBean.class, "derbyHome");
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to query derbyHome", e);
            }
        }
        return derbyHome;
    }
    
    /**
     * Get database connection.
     *
     * @param dbName
     * @return
     * @throws SQLException
     */
    private static Connection getConnection(String dbName, String properties,
            String protocol, String driver) throws SQLException {
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
        	log.error("Problem loading driver class", e);
        }
        // If we are looking for the SystemDatabase get it from the kernel
        // because it is not binded to our JNDI Context.
        if (SYSTEM_DB.equalsIgnoreCase(dbName)) {
            return getSystemDBConnection();
        } else {
            return DriverManager.getConnection(protocol + dbName + properties);
        }
    }

    /**
     * Get a connection to derby.
     *
     * @param dbName
     *            the name of the database to connect to.
     * @param properties
     *            the properties to pass to the connection string.
     * @return connection
     */
    public static Connection getDerbyConnection(String dbName, String properties)
            throws SQLException {
        return getConnection(dbName, properties, PROTOCOL, DERBY_DRIVER);
    }

    public static Connection getDerbyConnection(String dbName)
            throws SQLException {
        return getDerbyConnection(dbName, EMPTY_PROPS);
    }

    /**
     * Get a connection to the SystemDatabase.
     *
     * @return
     * @throws SQLException
     */
    public static Connection getSystemDBConnection() throws SQLException {
        DataSource ds = null;
        try {
            ds = getDataSource(SYSTEM_DB);
            return ds.getConnection();
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Get the datasource if dbName is == SYSTEM_DB, otherwise returns null.
     *
     * @param dbName
     * @return datasource
     */
    public static DataSource getDataSource(String dbName) {
        try {
            if (SYSTEM_DATASOURCE_NAME!=null && SYSTEM_DB.equalsIgnoreCase(dbName)) {
            	return (DataSource) KernelRegistry.getSingleKernel().invoke(
            			SYSTEM_DATASOURCE_NAME, "$getResource");
            }
        } catch (Exception e) {
        	log.error("Problem getting datasource " + dbName, e);
        }
        return null;
    }

}