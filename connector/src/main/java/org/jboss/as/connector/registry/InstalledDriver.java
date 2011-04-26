/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.connector.registry;

import org.jboss.modules.ModuleIdentifier;

/**
 * Metadata describing a JDBC driver that has been installed as a service in the runtime.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public final class InstalledDriver {

    private final ModuleIdentifier moduleName;
    private final String deploymentUnitName;
    private final String driverClassName;
    private final int majorVersion;
    private final int minorVersion;
    private final boolean jdbcCompliant;

    /**
     * Creates a new InstalledDriver for a driver that was loaded from the module path.
     *
     * @param moduleName the name of the module from which the driver was loaded
     * @param driverClassName the name of the {@link java.sql.Driver} implementation class
     * @param majorVersion the driver major version
     * @param minorVersion the driver minor version
     * @param jdbcCompliant whether the driver is JDBC compliant
     */
    public InstalledDriver(final ModuleIdentifier moduleName, final String driverClassName, final int majorVersion,
            final int minorVersion, final boolean jdbcCompliant) {
        this.deploymentUnitName = null;
        this.moduleName = moduleName;
        this.driverClassName = driverClassName;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.jdbcCompliant = jdbcCompliant;
    }
    /**
     * Creates a new InstalledDriver for a driver that was installed from a deployment.
     *
     * @param deploymentUnitName the name of the deployment unit from which the driver was installed
     * @param driverClassName the name of the {@link java.sql.Driver} implementation class
     * @param majorVersion the driver major version
     * @param minorVersion the driver minor version
     * @param jdbcCompliant whether the driver is JDBC compliant
     */
    public InstalledDriver(final String deploymentUnitName, final String driverClassName, final int majorVersion,
            final int minorVersion, final boolean jdbcCompliant) {
        this.deploymentUnitName = deploymentUnitName;
        this.moduleName = null;
        this.driverClassName = driverClassName;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.jdbcCompliant = jdbcCompliant;

    }

    /**
     * Gets the name of the module from which the driver was loaded, if it was
     * loaded from the module path.
     *
     * @return the module name, or {@code null} if {@link #isFromDeployment()} returns {@code true}
     */
    public ModuleIdentifier getModuleName() {
        return moduleName;
    }
    /**
     * Gets the name of the deployment unit from which the driver was loaded, if it was
     * loaded from a deployment.
     *
     * @return the deployment unit name, or {@code null} if {@link #isFromDeployment()} returns {@code false}
     */
    public String getDeploymentUnitName() {
        return deploymentUnitName;
    }
    /**
     * Gets the fully qualified class name of the driver's implementation of {@link java.sql.Driver}
     *
     * @return the class name. Will not be {@code null}
     */
    public String getDriverClassName() {
        return driverClassName;
    }
    /**
     * Gets the driver's major version number.
     *
     * @return the major version number
     */
    public int getMajorVersion() {
        return majorVersion;
    }
    /**
     * Gets the driver's minor version number.
     *
     * @return the minor version number
     */
    public int getMinorVersion() {
        return minorVersion;
    }
    /**
     * Gets whether the driver is JDBC compliant.
     *
     * @return {@code true} if the driver is JDBC compliant; {@code false} if not
     */
    public boolean isJdbcCompliant() {
        return jdbcCompliant;
    }
    /**
     * Gets whether the driver was loaded from a deployment unit.
     * @return {@code true} if the driver was loaded from a deployment unit; {@code false}
     *         if it was loaded from a module on the module path
     */
    public boolean isFromDeployment() {
        return deploymentUnitName != null;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + driverClassName.hashCode();
        result = 37 * result + majorVersion;
        result = 37 * result + minorVersion;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof InstalledDriver) {
            InstalledDriver other = (InstalledDriver) obj;
            return driverClassName.equals(other.driverClassName)
                && majorVersion == other.majorVersion && minorVersion == other.minorVersion;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (moduleName != null) {
            sb.append(moduleName);
        }
        else {
            sb.append(deploymentUnitName);
        }
        sb.append(':');
        sb.append(driverClassName);
        sb.append('#');
        sb.append(majorVersion);
        sb.append('#');
        sb.append(minorVersion);

        return sb.toString();
    }
}