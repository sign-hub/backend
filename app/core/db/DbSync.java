package core.db;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.spi.PersistentCollection;
//import org.hibernate.collection.PersistentCollection;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.type.Type;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.db.DB;
import play.db.Evolutions;
import play.db.jpa.JPABase;
import play.exceptions.JPAException;
import play.exceptions.PlayException;
import play.utils.Utils;

/**
 * Fornisce informazioni sulla sincronizzazione dello schema del db SQL
 * 
 * Crea le evoluzioni SOLO in sviluppo. E' integrato con evolution (per la
 * revisione)
 * 
 * @author gannunziata
 * 
 */
@SuppressWarnings("deprecation")
public class DbSync extends PlayPlugin {

	private final String folderbase_default = "db/sync";
	static boolean started = false;

	public DbSync() {

	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int idx) {
		this.index = idx;
	}

	@Override
	public void onApplicationStart() {

		if (isDisabled() || Play.mode.isProd())
			return;

		Evolutions ev = Play.plugin(Evolutions.class);
		// int evIndex = 400;
		// if (ev != null)
		// evIndex = ev.index;
		// if (ev == null || this.index < evIndex) {
		// throw new DbSyncException(
		// "Configure DbSync with a higher priority (current is "
		// + this.getIndex()
		// + ", more than Evolutions Plugin (use '"
		// + (evIndex + 5)
		// + ":plugins.DbSync' in conf/play.plugins)");
		// }

		// check EVOLUTION FIRST!!
		ev.beforeInvocation();

		//
		checkDbSync();
	}

	private void checkDbSync() {

		Integer rev = getLastEvolutionRevision();

		// execute update?
		boolean doUpdate = false;

		// NON ESEGUE MAI un update!!
		// doUpdate = Boolean.parseBoolean(Play.configuration.getProperty(
		// "dbsync.doupdate", "true"));

		// write files
		boolean writeFiles = false;
		writeFiles = Boolean.parseBoolean(Play.configuration.getProperty("dbsync.writeToFiles", "false"));

		String folderbase = Play.configuration.getProperty("dbsync.folderbase", folderbase_default);

		String folder = folderbase + "/" + rev;
		String basename = getDate();

		Logger.info("DbSync: check and sync schema with models...");

		File dir = Play.getFile(folder);
		if (writeFiles)
			dir.mkdirs();

		String fd = dir.getAbsolutePath();
		StringBuilder sql = new StringBuilder(200);
		List<String> resFiles = updateSchema(rev, fd, basename, doUpdate, writeFiles, sql);

		StringBuilder files = new StringBuilder(100);
		for (String rf : resFiles) {

			if (writeFiles) {
				File ff = new File(rf);
				if (ff.exists()) {
					// se non ha generato nulla cancella il file
					if (ff.length() == 0) {
						Logger.debug("DbSync do NOT save schema to '" + rf + "' (removed)");
						ff.delete();
					} else {
						Logger.info("DbSync save schema to '" + rf + "'");
						files.append(ff).append("\n");
					}
				}
			} else {
				files.append(rf).append("\n");
			}
		}
		String s = sql.toString();
		if (!s.isEmpty()) {
			throw new DbToUpdateException(s, rev, files.toString());
		}
		// Logger.info("DbSync done.");
	}

	public static class DbToUpdateException extends PlayException {

		String syncScripts;
		Integer revision;
		String files;

		public DbToUpdateException(String scripts, Integer rev, String files) {
			this.syncScripts = scripts;
			this.files = files;
			this.revision = rev;
		}

		@Override
		public String getErrorTitle() {
			return "Your database needs to SYNC";
		}

		@Override
		public String getErrorDescription() {
			return "An SQL script must be integrated in <b>evolution</b> script: " + (this.revision + 1) + ".sql";
		}

		@Override
		public String getMoreHTML() {
			return "<h3>This SQL script must be integrated in '" + (this.revision + 1) + ".sql'</h3>"
					+ "<pre style=\"background:#fff; border:1px solid #ccc; padding: 5px\">" + syncScripts + "</pre>"
					+ "<p>&nbsp;</p>" + "<p>You will find this scripts in this file(s):</p>"
					+ "<pre style=\"background:#fff; border:1px solid #ccc; padding: 5px\">" + files + "</pre>";

		}
	}

	private String getDate() {
		Date d = new Date();
		SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd-hhmmss");
		return dt.format(d);
	}

	private Integer getLastEvolutionRevision() {

		Connection connection = null;
		try {
			connection = getNewConnection();
			ResultSet rs = connection.createStatement()
					.executeQuery("select id from play_evolutions where state = 'applied' order by id desc");
			if (rs.next()) {
				Integer res = rs.getInt(1);
				return res;
			} else
				return 1;
		} catch (Exception e) {
			return 1;
		}

	}

	static Connection getNewConnection() throws SQLException {
		Connection connection = DB.datasource.getConnection();
		connection.setAutoCommit(true); // Yes we want auto-commit
		return connection;
	}

	/**
	 * Checks if evolutions is disabled in application.conf (property
	 * "evolutions.enabled")
	 */
	private boolean isDisabled() {
		return "false".equals(Play.configuration.getProperty("dbsync.enabled", "true"));
	}

	private List<String> updateSchema(int version, String outputDir, String basename, boolean doUpdate,
			boolean writeFiles, StringBuilder sql) {
		List<String> res = new ArrayList<String>();

		Logger.info("DbSync in execution... ");
		Configuration configuration = getHibernateConfiguration();
		if (configuration == null) {
			Logger.info("DbSync skipped.");
			return res;
		}

		try {
			String outputFile = outputDir + File.separatorChar + basename;

			if (writeFiles) {
				File dir = new File(outputDir);
				if (!(dir.isDirectory() && dir.exists())) {
					dir.mkdirs();
					if (!dir.isDirectory()) {
						throw new DbSyncException("problem on schema exporting with folder '" + outputDir + "'");
					}
				}

				// la versione 1, senza file, crea il db iniziale (??)

				String[] list = dir.list();

				for (String f : list) {
					if (f.endsWith("-error.sql")) {
						Logger.error("There is one or more errors, correct manually. DbSync skipped!");
						doUpdate = false;
						return res;
					}
				}
			}

			doUpdate(doUpdate, writeFiles, res, configuration, outputFile, sql);

			Logger.info("DbSync correctly executed.");
			return res;

		} catch (Exception e) {
			Logger.error(e, "DbSync error on schema exporting");
			throw new DbSyncException("problem on schema exporting", e);
		}

	}

	private void doUpdate(boolean doUpdate, boolean writeFiles, List<String> res, Configuration configuration,
			String outputFile, StringBuilder sql) throws Exception {

		// creo "a mano" quello che fa schemaupdate
		Statement stmt = null;
		FileWriter out = null;
		FileWriter err = null;
		String file = null;
		int num = 1;
		try {
			Dialect dialect = getDialect();
			Connection conn = getNewConnection();

			DatabaseMetadata metadata = new DatabaseMetadata(conn, dialect);
			Formatter formatter = FormatStyle.DDL.getFormatter();
			stmt = conn.createStatement();
			if (writeFiles) {
				file = outputFile + "-update.sql";
				out = new FileWriter(file);
			}

			String[] sa = configuration.generateSchemaUpdateScript(dialect, metadata);
			for (String s : sa) {
				String formatted = formatter.format(s) + ";";

				if (sql != null) {
					String title = "-- schema update # " + num + "\n";
					sql.append(title);
					sql.append(formatted).append("\n\n");

					if (!writeFiles) {
						res.add(title);
					}

					num++;
				}

				if (doUpdate) {
					try {
						stmt.executeUpdate(formatted); // do UPDATE
					} catch (Exception ee) {
						if (writeFiles) {
							if (err == null)
								err = new FileWriter(outputFile + "-error.sql");
							err.write("\n\n/*\n Following query has this exception: \n " + ee.getLocalizedMessage()
									+ "\n*/\n");
							err.write(formatted + "\n");
							err.close();
						}
						Logger.debug(ee, "DbSync - exec update error");
					}
				}

				if (out != null)
					out.write(formatted + "\n");
				// else
				// res.add(formatted + "\n");
			}

			if (writeFiles)
				res.add(file);

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}

			} catch (Exception e) {
				Logger.error(e, "Error closing connection");
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				Logger.error(e, "Error closing file");
			}
			try {
				if (err != null) {
					err.close();
				}
			} catch (Exception e) {
				Logger.error(e, "Error closing file err");
			}
		}
	}

	private Configuration getHibernateConfiguration() {
		Ejb3Configuration cfg = createEjb3Configuration();
		if (cfg == null) {
			Logger.warn("create ejb3 configuration error, DbSync skipped.");
			return null;
		}
		String hbm2ddl = (String) cfg.getProperties().get("hibernate.hbm2ddl.auto");
		if (hbm2ddl != null && !"none".equals(hbm2ddl)) {
			Logger.warn("hibernate.hbm2ddl.auto or jpa.ddl NOT set to 'none' but '" + hbm2ddl + "', DbSync skipped.");
			return null;
		}

		Configuration res = cfg.getHibernateConfiguration();
		// res.configure(); //!!!!
		return res;
	}

	private Dialect getDialect() {
		String dclass = getDefaultDialect(Play.configuration.getProperty("db.driver"));
		try {
			Class<?> c = Class.forName(dclass);
			Dialect d = (Dialect) c.newInstance();
			return d;
		} catch (Exception e) {
			Logger.debug(e, "errore get dialect");
			return null;
		}
	}

	/**
	 * NOTA - crea una Ejb3Configuration - COPIA da
	 * JPAPlugin.onApplicationStart(...)
	 * 
	 * @return Ejb3Configuration
	 */
	protected Ejb3Configuration createEjb3Configuration() {

		// Ejb3Configuration cfg = new Ejb3Configuration();
		// cfg.configure("defaultPersistenceUnit",
		// JPA.entityManagerFactory.getProperties());
		// return cfg;
		return createEjb3ConfigurationInternal();
		// return JPAPlugin.lastConfiguration;
	}

	Ejb3Configuration createEjb3ConfigurationInternal() {
		List<Class> classes = Play.classloader.getAnnotatedClasses(Entity.class);
		if (classes.isEmpty() && Play.configuration.getProperty("jpa.entities", "").equals("")) {
			return null;
		}
		final String dataSource = Play.configuration.getProperty("hibernate.connection.datasource");
		if (StringUtils.isEmpty(dataSource) && DB.datasource == null) {
			throw new DbSyncException("Cannot start DbSync without a properly configured database",
					new NullPointerException("No datasource configured"));
		}

		Ejb3Configuration cfg = new Ejb3Configuration();

		if (DB.datasource != null) {
			cfg.setDataSource(DB.datasource);
			Logger.debug("using datasource. Try to get connection...");
			try {
				Connection conn = DB.datasource.getConnection();
				conn.close();
			} catch (Exception ee) {
				Logger.error(ee, "get Config: Connection error");
			}
		} else {
			Logger.debug("using hibernate.connection:" + dataSource);
		}

		if (!Play.configuration.getProperty("jpa.ddl", Play.mode.isDev() ? "update" : "none").equals("none")) {
			cfg.setProperty("hibernate.hbm2ddl.auto", Play.configuration.getProperty("jpa.ddl", "update"));
		}

		cfg.setProperty("hibernate.dialect", getDefaultDialect(Play.configuration.getProperty("db.driver")));
		cfg.setProperty("javax.persistence.transaction", "RESOURCE_LOCAL");

		// Explicit SAVE for JPABase is implemented here
		// ~~~~~~
		// We've hacked the
		// org.hibernate.event.def.AbstractFlushingEventListener line 271, to
		// flush collection update,remove,recreation
		// only if the owner will be saved.
		// As is:
		// if (session.getInterceptor().onCollectionUpdate(coll,
		// ce.getLoadedKey())) {
		// actionQueue.addAction(...);
		// }
		//
		// This is really hacky. We should move to something better than
		// Hibernate like EBEAN
		cfg.setInterceptor(new EmptyInterceptor() {

			@Override
			public int[] findDirty(Object o, Serializable id, Object[] arg2, Object[] arg3, String[] arg4,
					Type[] arg5) {
				if (o instanceof JPABase && !((JPABase) o).willBeSaved) {
					return new int[0];
				}
				return null;
			}

			@Override
			public boolean onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
				if (collection instanceof PersistentCollection) {
					Object o = ((PersistentCollection) collection).getOwner();
					if (o instanceof JPABase) {
						return ((JPABase) o).willBeSaved;
					}
				} else {
					System.out.println("HOO: Case not handled !!!");
				}
				return super.onCollectionUpdate(collection, key);
			}

			@Override
			public boolean onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
				if (collection instanceof PersistentCollection) {
					Object o = ((PersistentCollection) collection).getOwner();
					if (o instanceof JPABase) {
						return ((JPABase) o).willBeSaved;
					}
				} else {
					System.out.println("HOO: Case not handled !!!");
				}
				return super.onCollectionRecreate(collection, key);
			}

			@Override
			public boolean onCollectionRemove(Object collection, Serializable key) throws CallbackException {
				if (collection instanceof PersistentCollection) {
					Object o = ((PersistentCollection) collection).getOwner();
					if (o instanceof JPABase) {
						return ((JPABase) o).willBeSaved;
					}
				} else {
					System.out.println("HOO: Case not handled !!!");
				}
				return super.onCollectionRemove(collection, key);
			}
		});

		if (Play.configuration.getProperty("jpa.debugSQL", "false").equals("true")) {
			org.apache.log4j.Logger.getLogger("org.hibernate.SQL").setLevel(Level.ALL);
		} else {
			org.apache.log4j.Logger.getLogger("org.hibernate.SQL").setLevel(Level.OFF);
		}
		// inject additional hibernate.* settings declared in Play!
		// configuration
		cfg.addProperties((Properties) Utils.Maps.filterMap(Play.configuration, "^hibernate\\..*"));

		try {
			Field field = cfg.getClass().getDeclaredField("overridenClassLoader");
			field.setAccessible(true);
			field.set(cfg, Play.classloader);
		} catch (Exception e) {
			Logger.error(e, "Error trying to override the hibernate classLoader (new hibernate version ???)");
		}
		for (Class<?> clazz : classes) {
			if (clazz.isAnnotationPresent(Entity.class)) {
				cfg.addAnnotatedClass(clazz);
				Logger.trace("JPA Model : %s", clazz);
			}
		}
		String[] moreEntities = Play.configuration.getProperty("jpa.entities", "").split(", ");
		for (String entity : moreEntities) {
			if (entity.trim().equals("")) {
				continue;
			}
			try {
				cfg.addAnnotatedClass(Play.classloader.loadClass(entity));
			} catch (Exception e) {
				Logger.warn("JPA -> Entity not found: %s", entity);
			}
		}
		for (ApplicationClass applicationClass : Play.classes.all()) {
			if (applicationClass.isClass() || applicationClass.javaPackage == null) {
				continue;
			}
			Package p = applicationClass.javaPackage;
			Logger.info("JPA -> Adding package: %s", p.getName());
			cfg.addPackage(p.getName());
		}
		String mappingFile = Play.configuration.getProperty("jpa.mapping-file", "");
		if (mappingFile != null && mappingFile.length() > 0) {
			cfg.addResource(mappingFile);
		}

		Logger.trace("Initializing JPA configuration...");
		try {
			EntityManagerFactory emf = cfg.buildEntityManagerFactory();
			emf.createEntityManager();
		} catch (Exception e) {
			throw new JPAException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
		}

		Logger.trace("JPA configuration done");
		return cfg;
	}

	static String getDefaultDialect(String driver) {
		String dialect = Play.configuration.getProperty("jpa.dialect");
		if (dialect != null) {
			return dialect;
		} else if (driver.equals("org.h2.Driver")) {
			return "org.hibernate.dialect.H2Dialect";
		} else if (driver.equals("org.hsqldb.jdbcDriver")) {
			return "org.hibernate.dialect.HSQLDialect";
		} else if (driver.equals("com.mysql.jdbc.Driver")) {
			return "play.db.jpa.MySQLDialect";
		} else if (driver.equals("org.postgresql.Driver")) {
			return "org.hibernate.dialect.PostgreSQLDialect";
		} else if (driver.toLowerCase().equals("com.ibm.db2.jdbc.app.db2driver")) {
			return "org.hibernate.dialect.DB2Dialect";
		} else if (driver.equals("com.ibm.as400.access.AS400JDBCDriver")) {
			return "org.hibernate.dialect.DB2400Dialect";
		} else if (driver.equals("com.ibm.as400.access.AS390JDBCDriver")) {
			return "org.hibernate.dialect.DB2390Dialect";
		} else if (driver.equals("oracle.jdbc.driver.OracleDriver")) {
			return "org.hibernate.dialect.Oracle9iDialect";
		} else if (driver.equals("com.sybase.jdbc2.jdbc.SybDriver")) {
			return "org.hibernate.dialect.SybaseAnywhereDialect";
		} else if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(driver)) {
			return "org.hibernate.dialect.SQLServerDialect";
		} else if ("com.sap.dbtech.jdbc.DriverSapDB".equals(driver)) {
			return "org.hibernate.dialect.SAPDBDialect";
		} else if ("com.informix.jdbc.IfxDriver".equals(driver)) {
			return "org.hibernate.dialect.InformixDialect";
		} else if ("com.ingres.jdbc.IngresDriver".equals(driver)) {
			return "org.hibernate.dialect.IngresDialect";
		} else if ("progress.sql.jdbc.JdbcProgressDriver".equals(driver)) {
			return "org.hibernate.dialect.ProgressDialect";
		} else if ("com.mckoi.JDBCDriver".equals(driver)) {
			return "org.hibernate.dialect.MckoiDialect";
		} else if ("InterBase.interclient.Driver".equals(driver)) {
			return "org.hibernate.dialect.InterbaseDialect";
		} else if ("com.pointbase.jdbc.jdbcUniversalDriver".equals(driver)) {
			return "org.hibernate.dialect.PointbaseDialect";
		} else if ("com.frontbase.jdbc.FBJDriver".equals(driver)) {
			return "org.hibernate.dialect.FrontbaseDialect";
		} else if ("org.firebirdsql.jdbc.FBDriver".equals(driver)) {
			return "org.hibernate.dialect.FirebirdDialect";
		} else {
			throw new UnsupportedOperationException("I do not know which hibernate dialect to use with " + driver
					+ " and I cannot guess it, use the property jpa.dialect in config file");
		}
	}
}
