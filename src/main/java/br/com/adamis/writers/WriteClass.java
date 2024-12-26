package br.com.adamis.writers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.adamis.conexao.Conexao;
import br.com.adamis.executions.StaticSQL;
import br.com.adamis.executions.responses.ForeignKeysResponse;
import br.com.adamis.executions.responses.PrimaryKeyResponse;
import br.com.adamis.executions.responses.TablesResponse;
import br.com.adamis.utils.Utils;

public class WriteClass {

	private Conexao conexao;

	private List<String> importList = new ArrayList();
	private StaticSQL staticSQL;

	List<String> listWrite;


	public WriteClass(Conexao conexao) {
		this.conexao = conexao;
	}

	public void make(String table, String pack, String path) throws Exception {

		String montaNameClasse = montaNameClasse(table);
		staticSQL = new StaticSQL(conexao);
		listWrite = new ArrayList<String>();

		//PACKAGE
		listWrite.add(montaPackage(pack));
		listWrite.add("");//Pula Linha

		//Adiciona o marcador de import
		listWrite.add("{import}");//IMPORT

		//Monta as anotações da entity
		montaEntityAnotation(montaNameClasse, listWrite);

		//Monta a nomeclatura da Classe
		montaNomeclaturaClasse(montaNameClasse);

		//Escreve as primary keys
		montaPrimaryKeys(table);

		//Monta todas as colunas da tabela na classe
		montaColunas(table);

		montaFks(table);

		try {
			//Faz o replace da variavel pelo valor das importaçoes
			replaceImports();			
			listWrite.add("");//Pula Linha
			listWrite.add("}");//Fim da Classe
			writeClasse(path+(path.endsWith("\\")?"":File.separator)+montaNameClasse+".java", listWrite);
		} catch (Exception e) {		
			e.printStackTrace();
		}

	}

	private void montaFks(String table) {
		List<ForeignKeysResponse> listColumns = staticSQL.listForeignKeys(table);
		
		if(listColumns.size() > 0) {
			listWrite.add("");//Pula Linha
			listWrite.add("");//Pula Linha
			listWrite.add("	//FOREIGN KEYS");//Pula Linha
		}
		
		for (int i = 0; i < listColumns.size(); i++) {			
			ForeignKeysResponse foreignKeysResponse = listColumns.get(i);
			
			listWrite.add("");//Pula Linha
			
			listWrite.add("	@ManyToOne(fetch = FetchType.LAZY)");
			listWrite.add("	@JoinColumn(name = \""+foreignKeysResponse.getFkColumn()+"\")");	
			listWrite.add("	private "+Utils.normalizerStringCaps(foreignKeysResponse.getReferencedTable())+" "+Utils.normalizerStringCommomNotCap(foreignKeysResponse.getReferencedTable())+";");
			
			addImport("jakarta.persistence.ManyToOne;");
			addImport("jakarta.persistence.JoinColumn;");			
			
		}
		
		
	}

	/**
	 * Monta todas as colunas da tabela na classe
	 * @param table
	 */
	private void montaColunas(String table) {
		
		List<TablesResponse> listColumns = staticSQL.listColumns(table);
		
		if(listColumns.size() > 0) {
			listWrite.add("");//Pula Linha
			listWrite.add("");//Pula Linha
			listWrite.add("	//COLUMNS");//Pula Linha
		}
		
		
		for (int i = 0; i < listColumns.size(); i++) {
			TablesResponse tablesResponse = listColumns.get(i);

			listWrite.add("");//Pula Linha

			switch (tablesResponse.getDataType().toUpperCase()) {
			// Tipos Numéricos
			case "NUMBER":
			case "DECIMAL":
			case "INTEGER":
			case "SMALLINT":
			case "FLOAT":
			case "BINARY_FLOAT":
			case "BINARY_DOUBLE":
			case "DOUBLE PRECISION":
				
				String add = "";
				
				if(tablesResponse.getDataPrecision() != null && tablesResponse.getDataPrecision() > 0) {
					add = ", precision = "+tablesResponse.getDataPrecision();
				}
				
				if(tablesResponse.getDataScale() != null && tablesResponse.getDataScale() > 0) {
					add = ", scale = "+tablesResponse.getDataScale();
				}
				
				
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\""+add+")");
				listWrite.add("    private BigDecimal " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				addImport("java.math.BigDecimal;");				
				
				System.out.println("Tipo Numérico detectado: " + tablesResponse.getDataType());
				break;

				// Tipos de Caracteres
			case "CHAR":
			case "VARCHAR2":
			case "NCHAR":
			case "NVARCHAR2":
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", length = " + tablesResponse.getDataLength() + ")");
				listWrite.add("    private String " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				System.out.println("Tipo de Caracteres detectado: " + tablesResponse.getDataType());
				break;

				// Tipos LOB
			case "CLOB":
			case "NCLOB":
				listWrite.add("    @Lob");
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", columnDefinition = \"CLOB\")");
				listWrite.add("    private String " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				addImport("jakarta.persistence.Lob;");
				System.out.println("Tipo LOB detectado: " + tablesResponse.getDataType());
				break;
			case "BLOB":
			case "BFILE":
				listWrite.add("    @Lob");
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", columnDefinition = \"BLOB\")");
				listWrite.add("    private byte[] " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				addImport("jakarta.persistence.Lob;");
				System.out.println("Tipo LOB detectado: " + tablesResponse.getDataType());
				break;

				// Tipos de Datas
			case "DATE":
				
				listWrite.add("    @Temporal(TemporalType.DATE)");
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", length = "+tablesResponse.getDataLength()+")");
				listWrite.add("    private Date " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				
				addImport("jakarta.persistence.Temporal;");
				addImport("jakarta.persistence.TemporalType;");
				addImport("java.util.Date;");
				
				System.out.println("Tipo de Data detectado: " + tablesResponse.getDataType());
				break;
				
				// Tipos de Datas e Horários
			case "TIMESTAMP":
			case "TIMESTAMP WITH TIME ZONE":
			case "TIMESTAMP WITH LOCAL TIME ZONE":
			case "INTERVAL YEAR TO MONTH":
			case "INTERVAL DAY TO SECOND":				
				
				listWrite.add("    @Temporal(TemporalType.TIMESTAMP)");
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", length = "+tablesResponse.getDataLength()+")");
				listWrite.add("    private Calendar " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				
				addImport("jakarta.persistence.Temporal;");
				addImport("jakarta.persistence.TemporalType;");
				addImport("java.util.Calendar;");
				
				System.out.println("Tipo de Data/Hora detectado: " + tablesResponse.getDataType());
				break;

				// Tipos Binários
			case "RAW":
			case "LONG RAW":
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", columnDefinition = \"RAW\")");
				listWrite.add("    private byte[] " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				System.out.println("Tipo Binário detectado: " + tablesResponse.getDataType());
				break;

				// Tipos Especializados
			case "ROWID":
			case "UROWID":
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\")");
				listWrite.add("    private String " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				System.out.println("Tipo Especializado detectado: " + tablesResponse.getDataType());
				break;
			case "XMLTYPE":
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", columnDefinition = \"XMLTYPE\")");
				listWrite.add("    private String " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				System.out.println("Tipo Especializado detectado: " + tablesResponse.getDataType());
				break;

				// Tipos Obsoletos ou Legados
			case "LONG":
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\", columnDefinition = \"LONG\")");
				listWrite.add("    private String " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				System.out.println("Tipo Obsoleto detectado: " + tablesResponse.getDataType());
				break;

				// Tipos Desconhecidos ou Não Mapeados
			default:
				listWrite.add("    @Column(name = \"" + tablesResponse.getColumnName() + "\")");
				listWrite.add("    private String " + Utils.normalizerStringCommomNotCap(tablesResponse.getColumnName()) + ";");
				System.out.println("Tipo de coluna desconhecido: " + tablesResponse.getDataType());
				break;
			}

		}
	}

	/**
	 * Faz o replace da variavel pelo valor das importaçoes
	 */
	private void replaceImports() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < importList.size(); i++) {
			sb.append(importList.get(i));
			sb.append("\r\n");
		}		

		for (int i = 0; i < listWrite.size(); i++) {
			if(listWrite.get(i).equals("{import}")) {				
				listWrite.set(i,sb.toString());
			}
		}
	}

	/**
	 * Monta e adiciona as primary keys
	 * @param table
	 */
	private void montaPrimaryKeys(String table) {
		try {
			
			List<PrimaryKeyResponse> listPrimaryKeys = staticSQL.listPrimaryKeys(table);
			
			if(listPrimaryKeys.size() > 0) {
				listWrite.add("");//Pula Linha
				listWrite.add("");//Pula Linha
				listWrite.add("	//PRIMARY KEYS");//Pula Linha
			}
			
			for (int i = 0; i < listPrimaryKeys.size(); i++) {
				PrimaryKeyResponse primaryKey = listPrimaryKeys.get(i);
				listWrite.add("");//Pula Linha
				listWrite.add("	@Id");	
				addImport("jakarta.persistence.Id;");

				listWrite.add("	@GeneratedValue(strategy = GenerationType.IDENTITY)");				
				addImport("jakarta.persistence.GeneratedValue;");
				addImport("jakarta.persistence.GenerationType;");

				listWrite.add("	@Column(name = \""+primaryKey.getColumnName().toUpperCase()+"\", unique = true, nullable = false)");
				addImport("jakarta.persistence.Column;");

				listWrite.add("	private "+(primaryKey.getDataType().contains("NUMBER")?"Long":"String")+" "+Utils.normalizerStringCommomNotCap(primaryKey.getColumnName())+";");

			}


		} catch (Exception e) {			
			e.printStackTrace();
		}
	}

	/**
	 * Monta nomeclatura da classe
	 * @param montaNameClasse
	 */
	private void montaNomeclaturaClasse(String montaNameClasse) {
		listWrite.add("public class "+montaNameClasse+" implements java.io.Serializable {");		
	}

	/**
	 * Monta as anotaçoes do JPA da classe
	 * @param montaNameClasse
	 * @param listWrite
	 */
	private void montaEntityAnotation(String montaNameClasse, List<String> listWrite) {
		listWrite.add("");

		listWrite.add("@Entity");
		addImport("jakarta.persistence.Entity;");

		listWrite.add("@Table(name = \""+montaNameClasse+"\")");
		addImport("jakarta.persistence.Table;");

		listWrite.add("@Data");
		addImport("lombok.Data;");	

	}

	/**
	 * Monta o package da classe
	 * @param pack
	 * @return
	 */
	private String montaPackage(String pack) {
		return "package "+pack+(pack.endsWith(";")?"":";");
	}

	/**
	 * Monta as importaçoes da classe de forma cumulativa
	 * @param lineImport
	 */
	private void addImport(String lineImport) {
		if(!importList.contains("import "+lineImport+(lineImport.endsWith(";")?"":";"))) {
			importList.add("import "+lineImport+(lineImport.endsWith(";")?"":";"));
		}
	}

	/**
	 * Monta o nome da classe
	 * @param table
	 * @return
	 * @throws Exception
	 */
	private String montaNameClasse(String table) throws Exception {
		String normalizerStringCaps = Utils.normalizerStringCaps(table);
		if(normalizerStringCaps.isEmpty() || normalizerStringCaps.isBlank()) {
			throw new Exception();
		}
		return normalizerStringCaps;
	}


	/**
	 * Escreve o arquivo da classe
	 * @param path
	 * @param listWrite
	 */
	private void writeClasse(String path, List<String> listWrite) {

		try {
			Utils.writeTxtList(path, listWrite, false);
		} catch (Exception e) {			
			e.printStackTrace();
		}

	}

}
