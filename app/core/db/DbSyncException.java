package core.db;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.exception.GenericJDBCException;

import play.exceptions.PlayException;
import play.exceptions.SourceAttachment;

/**
 * DbSync exception
 */
public class DbSyncException extends PlayException implements SourceAttachment {

	public DbSyncException(String message) {
		super(message, null);
	}

	public DbSyncException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getErrorTitle() {
		return "DbSync problem";
	}

	@Override
	public String getErrorDescription() {
		if (isSourceAvailable()) {
			String SQL = ((GenericJDBCException) getCause()).getSQL();
			return String.format(
					"A DbSync error occurred (%s): <strong>%s</strong>. This is likely because the batch has broken some referential integrity. Check your cascade delete, in case of ...",
					getMessage(), getCause() == null ? "" : getCause().getMessage(), SQL);
		}
		return String.format("A DbSync error occurred: <strong>%s</strong>", getMessage());
	}

	@Override
	public boolean isSourceAvailable() {
		return getCause() != null && getCause() instanceof GenericJDBCException;
	}

	@Override
	public Integer getLineNumber() {
		return 1;
	}

	public List<String> getSource() {
		List<String> sql = new ArrayList<String>();
		if (getCause() != null && getCause() instanceof GenericJDBCException) {
			sql.add(((GenericJDBCException) getCause()).getSQL());
		}
		return sql;
	}

	@Override
	public String getSourceFile() {
		String sql = "(no file available)";
		if (isSourceAvailable())
			sql = "source-file.sql";
		return sql;
	}

}
