package br.com.adamis.conexao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

public class Conexao {
	private Statement statement;
	private ResultSet resultSet;
	private Connection conexao = null;
	private DatabaseType databaseType = null;
	private DatabaseTypeFile databaseTypeFile = null;
	//private String path = "";
	private String user ="";
	private String password= "";
	private String ip = "";
	private String port = "";
	private String nameBD = "";
	private String charset = "";

	/**
	 * 
	 * @param databaseType = 'mysql' or 'sybase' or 'postgress' or 'oracle'
	 * @param user
	 * @param password
	 * @param ip = ip or url for Database
	 * @param port = default 'mysql' = 3306 and 'sybase' = 2638 and 'postgress' = 5432 and 'oracle' = 1521
	 * @param nameBD
	 * @param charset = default(utf_8)
	 */
	public Conexao(DatabaseType databaseType, String user, String password,String ip, String port, String nameBD,String charset) {
		this.databaseType = databaseType; 
		this.user = user;
		this.password = password;
		this.ip = ip;
		this.port = port;
		this.nameBD = nameBD;
		this.charset = charset;
	}


	/**
	 * 
	 * @param databaseType = 'firebird'or 'access'
	 * @param user
	 * @param password
	 * @param path = path for database
	 * @param ip = ip or url for Database
	 * @param charset = default(utf_8)
	 */
	public Conexao(DatabaseTypeFile databaseTypeFile,String user, String password,String path,String ip,String charset) {
		this.databaseTypeFile = databaseTypeFile; 
		//this.path = path;
		this.user = user;
		this.password = password;
		this.ip = ip;
		this.charset = charset;
	}

	public void conect() {

		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		if (charset.isEmpty()) {
			charset = "utf_8";
		}
		props.setProperty("encoding", charset);

		try {
//			if(databaseType == DatabaseType.MYSQL){    //MYSQL
//				if (port.isEmpty()) {
//					port = "3306";
//				}
//				Class.forName("org.gjt.mm.mysql.Driver");
//				conexao = (Connection) DriverManager.getConnection("jdbc:mysql://"+ip+":"+port+"/"+nameBD+"?autoReconnect=true&relaxAutoCommit=true",props); 
//				
//			}else if(databaseType == DatabaseType.SYBASE){ //SYBASE
//				if (port.isEmpty()) {
//					port = "2638";
//				}
//				Class.forName("com.sybase.jdbc3.jdbc.SybDriver");
//				conexao = (Connection) DriverManager.getConnection("jdbc:sybase:Tds:"+ip+":"+port+"/"+nameBD,props); 
//
//			}else if(databaseType == DatabaseType.POSTGRESS){ //POSTREGRESS
//				if (port.isEmpty()) {
//					port = "5432";
//				}
//				Class.forName("org.postgresql.Driver");
//				conexao = (Connection) DriverManager.getConnection("jdbc:postgresql://"+ip+":"+port+"/"+nameBD,props); 
//
//			}else 
			if(databaseType == DatabaseType.ORACLE){    //ORACLE
				if (port.isEmpty()) {
					port = "1521";
				}
				Class.forName("oracle.jdbc.driver.OracleDriver");
				System.err.println("URL_ORACLE> "+"jdbc:oracle:thin:@"+ip+":"+port+"/"+nameBD);
				conexao = DriverManager.getConnection("jdbc:oracle:thin:@"+ip+":"+port+"/"+nameBD,props);

				System.err.println("Conectado!");
//			} else if(databaseTypeFile == DatabaseTypeFile.FIREBIRD){  //FIREBIRD
//				Class.forName("org.firebirdsql.jdbc.FBDriver");
//				path = path.replace('\\', '/').trim();
//				conexao = DriverManager.getConnection("jdbc:firebirdsql:"+ip+"/3050:"+path,props);
//
//			}else if(databaseTypeFile == DatabaseTypeFile.ACCESS){      //ACCESS
//
//				String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
//				path = path.replace('\\', '/').trim();
//				String caminho = "jdbc:odbc:"+path;
//				Class.forName(driver);
//				conexao = (Connection) DriverManager.getConnection(caminho, props);

			}else{
				System.out.println("Conexao não Encontrada");
			}
	
			
		} catch (Exception e) {
			System.out.println("ERROR Conect120 : "+e.getMessage());
			System.out.println("Não pode ser conectado!");
		}
	}

	public void disconect() {
		try {
			if (conexao != null && !conexao.isClosed()) {
				conexao.commit();
				conexao.close();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * INSERT OR DELETE OR UPDATE
	 * @param sql
	 * @return String "OK" or "null"
	 * @throws SQLException
	 */
	public String executeQueryUpdate(String sql) throws SQLException {
		String resultado ="";
		
		try {
			System.out.println(sql);
			
			statement = conexao.createStatement();
			statement.executeUpdate(sql);
			//System.out.println(""+sql);
			resultado = "OK";
			
		} catch (SQLException e) {
			System.err.println("SQL: "+sql+" ERROR: "+e.getMessage());
			rollBack();
			e.printStackTrace();
			resultado = "ERROR: "+e.getMessage();
		}
		return resultado;
	}

	/**
	 * SELECT
	 * @param sql
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		try {
			//conect();
			
			//System.out.println(sql);
			
			DatabaseMetaData metadata = conexao.getMetaData();
			 
            boolean isScrollSensitive = metadata.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE);
 
            if (!isScrollSensitive) {
                //System.out.println("The database doesn't support scrollable and sensitive result sets.");
                statement = conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE  , ResultSet.CONCUR_READ_ONLY);    
            }else {
            	//System.out.println("The database support scrollable and sensitive result sets.");
            	statement = conexao.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);	
            }
			
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			System.err.println("ERROR: "+e.getMessage());
			e.printStackTrace();
		}
		//disconect();
		return resultSet;
	}

	public Integer countAll(String table) throws SQLException {
		int num = 0;
		
		try {	
			
            statement = conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE  , ResultSet.CONCUR_READ_ONLY);    
            String sql = "select count(*) from "+table;
			
			resultSet = statement.executeQuery(sql);
			
            while(resultSet.next()){
                num = (resultSet.getInt(1));
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return num;
	}
	
	public void beginTransaction() {
		try {
			conexao.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getCause() != null ? e.getCause().toString() : "Erro Interno");
		}
	}

	public void commit() {
		try {
			conexao.commit();
			conexao.setAutoCommit(true);
			if (statement != null)
				statement.close();
			if (resultSet != null)
				resultSet.close();
			conexao.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getCause() != null ? e.getCause().toString() : "Erro Interno");
		}
	}

	public void rollBack() {
		try {
			conexao.rollback();
			conexao.setAutoCommit(true);
			if (statement != null)
				statement.close();
			if (resultSet != null)
				resultSet.close();
			//conexao.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getCause() != null ? e.getCause().toString() : "Erro Interno");
		}
	}

	public static String checkDate(Date date){
		String guia = "";
		if(date == null){
			guia = "null";
		}else{
			guia = "'"+date+"'";
		}
		return guia;
	}	

	public static String checkString(String string){
		String guia = "";

		string = string.replace("'", "");
		if(string.isEmpty() || string.equals("null")){
			guia = "''";
		}else{
			guia = "'"+string+"'";
		}
		return guia;
	}


	public DatabaseType getDatabaseType() {
		return databaseType;
	}


	public DatabaseTypeFile getDatabaseTypeFile() {
		return databaseTypeFile;
	}
	
}
