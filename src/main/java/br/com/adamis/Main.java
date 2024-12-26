/**
 * 
 */
package br.com.adamis;

import java.util.List;

import br.com.adamis.conexao.Conexao;
import br.com.adamis.conexao.DatabaseType;
import br.com.adamis.executions.StaticSQL;
import br.com.adamis.executions.responses.ForeignKeysResponse;
import br.com.adamis.executions.responses.PrimaryKeyResponse;
import br.com.adamis.executions.responses.TablesResponse;
import br.com.adamis.writers.WriteClass;

/**
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//ORACLE
		Conexao oracle = new Conexao(DatabaseType.ORACLE, "hr", "hr", "192.168.0.224", "", "XEPDB1","");
		oracle.conect();
		
		StaticSQL staticSQL = new StaticSQL(oracle);
		List<String> listTables = staticSQL.listTables();
		
		for (int i = 0; i < listTables.size(); i++) {
			
//			System.err.println("Table: "+listTables.get(i));
//			
//			List<PrimaryKeyResponse> listPrimaryKeys = staticSQL.listPrimaryKeys(listTables.get(i));
//			
//			for (int j = 0; j < listPrimaryKeys.size(); j++) {
//				System.err.println("Primary Key:"+listPrimaryKeys.get(j).getColumnName()+", Type:"+listPrimaryKeys.get(j).getDataType()+", Length:"+listPrimaryKeys.get(j).getDataLength());
//			}
//			
//			List<TablesResponse> listColumns = staticSQL.listColumns(""+listTables.get(i));
//			
//			for (int j = 0; j < listColumns.size(); j++) {
//				System.err.println("Coluna: "+listColumns.get(j).getColumnName()+", Type: "+listColumns.get(j).getDataType()+", Size: "+listColumns.get(j).getDataLength());
//			}
//			
//			
//			
//			List<ForeignKeysResponse> listForeignKeys = staticSQL.listForeignKeys(listTables.get(i));
//			
//			for (int j = 0; j < listForeignKeys.size(); j++) {
//				System.err.println("FK: "+listForeignKeys.get(j).getFkColumn()+", REFERENCETABLE:"+listForeignKeys.get(j).getReferencedTable()+", REFERENCECOLUM:"+listForeignKeys.get(j).getReferencedColumn());
//			}
//			
//			
//			System.err.println("-----------------------------");
//			
//		}
			
		WriteClass writeClass = new WriteClass(oracle);
		try {
			writeClass.make(listTables.get(i), "br.com.adamis.entity", "C:\\Users\\Adami\\Downloads\\Projetos\\src\\main\\java\\br\\com\\adamis\\entity");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	}
	

}
