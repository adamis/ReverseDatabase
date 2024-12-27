/**
 * 
 */
package br.com.adamis.executions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.adamis.conexao.Conexao;
import br.com.adamis.executions.responses.ForeignKeysResponse;
import br.com.adamis.executions.responses.IndicesResponse;
import br.com.adamis.executions.responses.PrimaryKeyResponse;
import br.com.adamis.executions.responses.TablesResponse;

/**
 * 
 */
public class StaticSQL {

	private Conexao conexao;

	public StaticSQL(Conexao conexao) {
		this.conexao = conexao;
	}

	/**
	 * Listagem de Tabelas
	 * @return
	 */
	public List<String> listTables() {
		List<String> tables = new ArrayList<>();

		String query = "SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME";

		try {
			ResultSet resultSet = conexao.executeQuery(query);

			while (resultSet.next()) {
				tables.add(resultSet.getString("TABLE_NAME"));
			}

		} catch (Exception e) {
			System.err.println("Error while listing tables: " + e.getMessage());
			e.printStackTrace();
		}

		return tables;
	}

	/**
	 * Listagem de colunas de uma Tabela
	 * @param tableName
	 * @return
	 */
	public List<TablesResponse> listColumns(String tableName) {
		List<TablesResponse> columns = new ArrayList<>();

//		String query = "SELECT "
//							+ "TABLE_NAME, "
//							+ "COLUMN_NAME, "
//							+ "DATA_TYPE, "
//							+ "DATA_LENGTH, "
//							+ "DATA_PRECISION, "
//							+ "DATA_SCALE, "
//							+ "NULLABLE, "
//							+ "COLUMN_ID "
//						+ "FROM USER_TAB_COLUMNS "
//						+ "WHERE TABLE_NAME = '" + tableName.toUpperCase() + "'";

		String query = "SELECT " +
                "    C.TABLE_NAME, " +
                "    C.COLUMN_NAME, " +
                "    C.DATA_TYPE, " +
                "    C.DATA_LENGTH, " +
                "    C.DATA_PRECISION, " +
                "    C.DATA_SCALE, " +
                "    C.NULLABLE, " +
                "    C.COLUMN_ID " +
                "FROM USER_TAB_COLUMNS C " +
                "WHERE C.TABLE_NAME = '" + tableName.toUpperCase() + "' " +
                "  AND C.COLUMN_NAME NOT IN ( " +
                "      SELECT COLS.COLUMN_NAME " +
                "      FROM USER_CONSTRAINTS CONS " +
                "      JOIN USER_CONS_COLUMNS COLS ON CONS.CONSTRAINT_NAME = COLS.CONSTRAINT_NAME " +
                "      WHERE (CONS.CONSTRAINT_TYPE = 'R' OR CONS.CONSTRAINT_TYPE = 'P') " +
                "        AND COLS.TABLE_NAME = '" + tableName.toUpperCase() + "' " +
                "  )";

		
		try {
			ResultSet resultSet = conexao.executeQuery(query);

			while (resultSet.next()) {

				String tableOrigemName = resultSet.getString("TABLE_NAME");
				String columnName = resultSet.getString("COLUMN_NAME");
				String dataType = resultSet.getString("DATA_TYPE");
				Integer dataLength = resultSet.getInt("DATA_LENGTH");
				Integer dataPrecision = resultSet.getObject("DATA_PRECISION") != null ? resultSet.getInt("DATA_PRECISION") : null;
				Integer dataScale = resultSet.getObject("DATA_SCALE") != null ? resultSet.getInt("DATA_SCALE") : null;
				String nullable = resultSet.getString("NULLABLE");
				String columId = resultSet.getString("COLUMN_ID");

				TablesResponse tablesDetail = new TablesResponse(
						tableOrigemName,
						columnName, 
						dataType,
						dataLength,
						dataPrecision,
						dataScale,
						nullable,
						columId
						) {};                
						columns.add(tablesDetail);
			}

		} catch (Exception e) {
			System.err.println("Error while listing columns: " + e.getMessage());
			e.printStackTrace();
		}

		return columns;
	}

	/**
	 * lista as primary key de uma tabela
	 * @param tableName
	 * @return
	 */
	public List<PrimaryKeyResponse> listPrimaryKeys(String tableName) {
		List<PrimaryKeyResponse> primaryKeysList = new ArrayList<>();

		String query = "SELECT COLS.COLUMN_NAME, TABCOLS.DATA_TYPE, TABCOLS.DATA_LENGTH " +
				"FROM USER_CONSTRAINTS CONS " +
				"JOIN USER_CONS_COLUMNS COLS ON CONS.CONSTRAINT_NAME = COLS.CONSTRAINT_NAME " +
				"JOIN USER_TAB_COLUMNS TABCOLS ON COLS.TABLE_NAME = TABCOLS.TABLE_NAME " +
				"AND COLS.COLUMN_NAME = TABCOLS.COLUMN_NAME " +
				"WHERE COLS.TABLE_NAME = '" + tableName.toUpperCase() + "' " +
				"AND CONS.CONSTRAINT_TYPE = 'P'";

		try (ResultSet resultSet = conexao.executeQuery(query)){

			while (resultSet.next()) {
				String columName = resultSet.getString("COLUMN_NAME");
				String dataType = resultSet.getString("DATA_TYPE");
				Integer dataLength = resultSet.getInt("DATA_LENGTH");
				primaryKeysList.add(new PrimaryKeyResponse(columName, dataType, dataLength));
			}

		} catch (Exception e) {
			System.err.println("Error while listing primary keys: " + e.getMessage());
			e.printStackTrace();
		}
		

		return primaryKeysList;
	}
	
	/**
	 * Lista as FKs de uma tabela
	 * @param tableName
	 * @return
	 */
	public List<ForeignKeysResponse> listForeignKeys(String tableName) {
		
	    List<ForeignKeysResponse> foreignKeys = new ArrayList<>();
	    
	    String query = "SELECT FK_COLS.COLUMN_NAME AS FK_COLUMN, " +
                "       PK_COLS.TABLE_NAME AS REFERENCED_TABLE, " +
                "       PK_COLS.COLUMN_NAME AS REFERENCED_COLUMN, " +
                "       FK_TAB_COL.DATA_TYPE AS FK_COLUMN_TYPE, " +
                "       FK_TAB_COL.DATA_LENGTH AS FK_COLUMN_LENGTH, " +
                "       FK_TAB_COL.DATA_PRECISION AS FK_COLUMN_PRECISION, " +
                "       FK_TAB_COL.DATA_SCALE AS FK_COLUMN_SCALE " +
                "FROM USER_CONSTRAINTS FK_CONS " +
                "JOIN USER_CONS_COLUMNS FK_COLS ON FK_CONS.CONSTRAINT_NAME = FK_COLS.CONSTRAINT_NAME " +
                "JOIN USER_CONSTRAINTS PK_CONS ON FK_CONS.R_CONSTRAINT_NAME = PK_CONS.CONSTRAINT_NAME " +
                "JOIN USER_CONS_COLUMNS PK_COLS ON PK_CONS.CONSTRAINT_NAME = PK_COLS.CONSTRAINT_NAME " +
                "AND FK_COLS.POSITION = PK_COLS.POSITION " +
                "JOIN USER_TAB_COLUMNS FK_TAB_COL ON FK_COLS.TABLE_NAME = FK_TAB_COL.TABLE_NAME " +
                "AND FK_COLS.COLUMN_NAME = FK_TAB_COL.COLUMN_NAME " +
                "WHERE FK_CONS.CONSTRAINT_TYPE = 'R' " +
                "AND FK_COLS.TABLE_NAME = '" + tableName.toUpperCase() + "'";


	    try(ResultSet resultSet = conexao.executeQuery(query)) {
	            while (resultSet.next()) {
	                String fkColumn = resultSet.getString("FK_COLUMN");
	                String referencedTable = resultSet.getString("REFERENCED_TABLE");
	                String referencedColumn = resultSet.getString("REFERENCED_COLUMN");
	                String referencedColumnType = resultSet.getString("FK_COLUMN_TYPE");
	                Integer referencedColumnLength = resultSet.getInt("FK_COLUMN_LENGTH");
	                Integer referencedColumnPrecision = resultSet.getInt("FK_COLUMN_PRECISION");
	                Integer referencedColumnScale = resultSet.getInt("FK_COLUMN_SCALE"); // Novo campo
	                
//	                System.err.println("FK Column: " + fkColumn + 
//	                                ", Referenced Table: " + referencedTable + 
//	                                ", Referenced Column: " + referencedColumn+
//	                                ", Referenced Column Type: " + referencedColumnType+
//	                                ", Referenced Column Length: " + referencedColumnLength+
//	                                ", Referenced Column Precision: " + referencedColumnPrecision+
//	                                ", Referenced Column Scale: " + referencedColumnScale
//	                              );
	                
	                foreignKeys.add(
	                		new ForeignKeysResponse(
	                				fkColumn,
	                				referencedTable,
	                				referencedColumn,
	                				referencedColumnType,
	                				referencedColumnLength,
	                				referencedColumnPrecision,
	                				referencedColumnScale
	                		)
	                );
	            }
	        

	    } catch (SQLException e) {
	        System.err.println("Error while listing foreign keys: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return foreignKeys;
	}

	
	public List<IndicesResponse> listIndexes(String tableName) {
	    List<IndicesResponse> indexes = new ArrayList<>();

	    String query = "SELECT " +
                "    ui.INDEX_NAME, " +
                "    ui.INDEX_TYPE, " +
                "    CASE ui.UNIQUENESS " +
                "        WHEN 'UNIQUE' THEN 'TRUE' " +
                "        ELSE 'FALSE' " +
                "    END AS IS_UNIQUE, " +
                "    ui.TABLE_NAME, " +
                "    ui.STATUS AS INDEX_STATUS, " +
                "    uic.COLUMN_NAME, " +
                "    uic.COLUMN_POSITION, " +
                "    uic.DESCEND " +
                "FROM USER_INDEXES ui " +
                "JOIN USER_IND_COLUMNS uic " +
                "ON ui.INDEX_NAME = uic.INDEX_NAME " +
                "LEFT JOIN USER_CONSTRAINTS uc " +
                "ON ui.INDEX_NAME = uc.INDEX_NAME " +
                "WHERE ui.TABLE_NAME = '" + tableName.toUpperCase() + "' " +
                "  AND (uc.CONSTRAINT_TYPE IS NULL OR uc.CONSTRAINT_TYPE != 'P') " +
                "ORDER BY ui.INDEX_NAME, uic.COLUMN_POSITION";

	    try {
	        ResultSet resultSet = conexao.executeQuery(query);

	        while (resultSet.next()) {
	            String indexName = resultSet.getString("INDEX_NAME");
	            String indexType = resultSet.getString("INDEX_TYPE");
	            Boolean isUnique = resultSet.getString("IS_UNIQUE").equals("TRUE");
	            String tableOrigemName = resultSet.getString("TABLE_NAME");
	            String indexStatus = resultSet.getString("INDEX_STATUS");
	            String columnName = resultSet.getString("COLUMN_NAME");
	            Integer columnPosition = resultSet.getInt("COLUMN_POSITION");
	            String descend = resultSet.getString("DESCEND");

	            IndicesResponse indicesResponse = new IndicesResponse(
	                    tableOrigemName,
	                    indexName,
	                    indexType,
	                    isUnique,
	                    indexStatus,
	                    columnName,
	                    columnPosition,
	                    descend
	            );

	            indexes.add(indicesResponse);
	        }
	    } catch (Exception e) {
	        System.err.println("Error while listing indexes: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return indexes;
	}

	

}
