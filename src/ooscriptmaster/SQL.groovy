package ooscriptmaster

/**
 * Execute SQL statement in FileMaker.
 */
public class SQL extends Functions {
	public static String[][] sqlArray = null

	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Execute SQL statement, return result as text.
	 *
	 * @param fmpro object from ScriptMaster
	 * @param statement SQL to execute
	 * @param columnDelimiter character to use between field (column) values
	 * @param rowDelimiter character to use between records(rows)
	 * @return result of SQL statement
	 */
	public static def sql(fmpro, statement, columnDelimiter, rowDelimiter) {
		paramRequired(fmpro)
		paramRequired(statement)

		if (columnDelimiter == null) {
			columnDelimiter = "|" as String
		} else if (columnDelimiter.size() > 1) {
			throw new ValidationException(2,
					": columnDelimiter can only contain 1 character (value=${columnDelimiter})"
			)
		}
		if (rowDelimiter == null) {
			rowDelimiter = "\r" as String
		} else if (rowDelimiter.size() > 1) {
			throw new ValidationException(2,
					": rowDelimiter can only contain 1 character (value=${rowDelimiter})"
			)
		}

		String result
		try {
			result = fmpro.executeSql(statement, columnDelimiter, rowDelimiter)
		} catch (e) {
			ErrorCodes.setLastError(1000, e)
			throw (e)
		}
		success(result)
	}

	/**
	 * Execute SQL statement, save result as an array in this class.
	 *
	 * @param fmpro object from ScriptMaster
	 * @param statement SQL to execute
	 * @return true on success, otherwise throw Exception
	 */
	public static boolean sqlArray(fmpro, statement) {
		paramRequired(fmpro)
		paramRequired(statement)

		String[][] result
		try {
			result = fmpro.executeSqlArray(statement)
		} catch (e) {
			ErrorCodes.setLastError(1000, e)
			throw (e)
		}

		this.sqlArray = result
		success()
	}

	/**
	 * Count rows in saved array
	 *
	 * @return # of rows
	 */
	public static int sqlArrayCountRows() {
		if (this.sqlArray == null) { throw new ValidationException(1000.01) }
		success(this.sqlArray.size()) as int
	}

	/**
	 * Count columns in saved array
	 *
	 * @return # of columns
	 */
	public static int sqlArrayCountColumns() {
		if (this.sqlArray == null) { throw new ValidationException(1000.01) }

		def result
		try {
			result = this.sqlArray[0].size()
		} catch (e) {
			if (e instanceof ArrayIndexOutOfBoundsException) {
				throw new ValidationException(1000.01)
			} else {
				ErrorCodes.setLastError(-1, e)
				throw (e)
			}
		}
		success(result) as int
	}

	/**
	 * Retrieve value from saved array
	 *
	 * @param rowNum (starting at 1)
	 * @param columnNum (starting at 1)
	 * @return value at specified location in array
	 */
	public static def sqlArrayValue(rowNum, columnNum) {
		paramRequired(rowNum)
		paramRequired(columnNum)
		if (this.sqlArray == null) { throw new ValidationException(1000.01) }

		try {
			rowNum = rowNum as Integer
			columnNum = columnNum as Integer
		} catch (e) {
			throw new ValidationException(2.04, " (parameters:${rowNum}, ${columnNum})")
		}
		// subtract 1 from each parameter to make accessing values 1 based, NOT 0 based as in Java
		// this is more like how FileMaker works
		rowNum--
		columnNum--

		def result
		try {
			result = this.sqlArray[rowNum][columnNum]
		} catch (e) {
			if (e instanceof ArrayIndexOutOfBoundsException) {
				// try to append description to error text
				def maxRowNum
				def maxColumnNum
				try {
					maxRowNum = this.sqlArray.size()
					maxColumnNum = this.sqlArray[0].size()
				} catch (e2) {
					throw new ValidationException(1000.02)
				}
				rowNum++
				columnNum++
				throw new ValidationException(1000.02,
						" (parameters:${rowNum}, ${columnNum})"
								+ " (max:${maxRowNum}, ${maxColumnNum})"
				)
			} else {
				ErrorCodes.setLastError(-1, e)
				throw (e)
			}
		}
		success(result)
	}

	/**
	 * Format field name to work in an SQL statement.
	 * Designed to accept input from GetFieldName() from FileMaker.
	 *
	 * @param field name, with or without the tablename:: preceding it
	 * @return field name escaped for use in SQL statement
	 */
	public static def sqlField(field) {
		paramRequired(field)
		field = field as String
		String table = ''

		// separate table and field, if table exists
		if (field.contains('::')) {
			def tokens = field.tokenize('::')
			if (tokens.size() > 2) {
				throw new ValidationException(2, 'field contained more than one ::')
			}
			// quote table
			table = '"' + tokens[0] + '"'
			// set field
			field = tokens[1]
		}

		// quote field
		field = '"' + field + '"'

		if (table != '') {
			return success(table + '.' + field)
		} else {
			return success(field)
		}
	}

	/**
	 * Format table name to work in an SQL statement.
	 * Designed to accept input from GetFieldName() from FileMaker.
	 *
	 * @param table name, with or without a ::fieldName following it
	 * @return table name escaped for use in SQL statement
	 */
	public static def sqlTable(table) {
		paramRequired(table)
		table = table as String

		// separate table and field, if table exists
		if (table.contains('::')) {
			def tokens = table.tokenize('::')
			if (tokens.size() > 2) {
				throw new ValidationException(2, 'table contained more than one ::')
			}
			table = tokens[0]
		}

		// quote
		table = '"' + table + '"'

		return success(table)
	}

}
