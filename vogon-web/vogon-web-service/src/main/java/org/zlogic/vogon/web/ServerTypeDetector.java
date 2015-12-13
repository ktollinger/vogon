/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Server type detector
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class ServerTypeDetector {

	/**
	 * Java server type
	 */
	public enum ServerType {

		/**
		 * Spring boot Tomcat
		 */
		STANDALONE,
		/**
		 * Tomcat
		 */
		TOMCAT,
		/**
		 * WildFly or JBoss
		 */
		WILDFLY,
		/**
		 * Jetty
		 */
		JETTY
	}

	/**
	 * Cloud provider type
	 */
	public enum CloudType {

		/**
		 * Non-cloud deployment
		 */
		NONE,
		/**
		 * OpenShift
		 */
		OPENSHIFT,
		/**
		 * Heroku
		 */
		HEROKU,
		/**
		 * Azure
		 */
		AZURE
	}

	/**
	 * Database type
	 */
	public enum DatabaseType {

		/**
		 * Standalone file-based H2
		 */
		H2,
		/**
		 * PostgreSQL
		 */
		POSTGRESQL
	}

	/**
	 * Keystore file
	 */
	@Value("${vogon.keystore.file:}")
	private String keystoreFile;
	/**
	 * Keystore password
	 */
	@Value("${vogon.keystore.pass:}")
	private String keystorePass;

	/**
	 * Returns true if environment has a variable matching the pattern
	 *
	 * @param pattern the pattern to search
	 * @return true if environment has a variable matching the pattern
	 */
	protected boolean hasEnvironmentVariable(Pattern pattern) {
		for (String variable : System.getenv().keySet())
			if (pattern.matcher(variable).matches())
				return true;
		return false;
	}

	/**
	 * Returns the detected cloud provider type
	 *
	 * @return the detected cloud provider type
	 */
	public CloudType getCloudType() {
		if (hasEnvironmentVariable(Pattern.compile("^DYNO$")) && hasEnvironmentVariable(Pattern.compile("^DATABASE_URL$"))) //NOI18N
			return CloudType.HEROKU;
		if (hasEnvironmentVariable(Pattern.compile("^OPENSHIFT_.*$"))) //NOI18N
			return CloudType.OPENSHIFT;
		if (hasEnvironmentVariable(Pattern.compile("^AZURE_.*$"))) //NOI18N
			return CloudType.AZURE;
		return CloudType.NONE;
	}

	/**
	 * Returns the Java server type
	 *
	 * @return the Java server type
	 */
	public ServerType getServerType() {
		if (getCloudType() == CloudType.HEROKU)
			return ServerType.TOMCAT;
		if (getCloudType() == CloudType.OPENSHIFT) {
			if (hasEnvironmentVariable(Pattern.compile("^OPENSHIFT_JBOSSEWS.*"))) //NOI18N
				return ServerType.TOMCAT;
			if (hasEnvironmentVariable(Pattern.compile("^OPENSHIFT_WILDFLY.*"))) //NOI18N
				return ServerType.WILDFLY;
			return ServerType.STANDALONE;
		}
		if (System.getProperty("jboss.server.data.dir") != null) //NOI18N
			return ServerType.WILDFLY;
		if (System.getProperty("catalina.home") != null) //NOI18N
			return ServerType.TOMCAT;
		if (System.getProperty("jetty.base") != null) //NOI18N
			return ServerType.JETTY;
		return ServerType.STANDALONE;
	}

	/**
	 * Returns the database type
	 *
	 * @return the database type
	 */
	public DatabaseType getDatabaseType() {
		if (getCloudType() == CloudType.HEROKU)
			return DatabaseType.POSTGRESQL;
		if (getCloudType() == CloudType.OPENSHIFT) {
			if (hasEnvironmentVariable(Pattern.compile("^OPENSHIFT_POSTGRESQL.*$"))) //NOI18N
				return DatabaseType.POSTGRESQL;
			else
				return DatabaseType.H2;
		}
		return DatabaseType.H2;
	}

	/**
	 * Returns the keystore file
	 *
	 * @return the keystore file
	 */
	public String getKeystoreFile() {
		return keystoreFile;
	}

	/**
	 * Returns the keystore password
	 *
	 * @return the keystore password
	 */
	public String getKeystorePassword() {
		return keystorePass;
	}

	/**
	 * Returns true if SSL can be used (and false if SSL is not supported or its
	 * support cannot be verified)
	 *
	 * @return true if SSL can be used
	 */
	public boolean isSslSupported() {
		return (getCloudType() != ServerTypeDetector.CloudType.HEROKU)
				&& (getCloudType() != ServerTypeDetector.CloudType.AZURE);
	}
}
