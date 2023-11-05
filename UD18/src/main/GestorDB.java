package main;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

public class GestorDB {

		public GestorDB() {
			conexion = null;
		}
		
		public Connection connectDB() {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306?useTimeZone=true&serverTimezone=UTC", "root", "password");
				System.out.println("Server Connected");
			}
			catch(SQLException | ClassNotFoundException ex) {
				System.out.println("No se ha podido conectar con mi base de datos");
				System.out.println(ex);
			}

			return conexion;
		}
		
		public void closeConnection() {
			try {
				conexion.close();
				JOptionPane.showMessageDialog(null, "Se ha finalizado la conexion con el servidor");
			}
			catch(SQLException ex) {
				System.out.println("Error al desconectar de la base de datos : "+ex.getMessage());
			}
		}
		
		public void createDB(String name) {
			try {
				String Query = "CREATE DATABASE " + name;
				Statement st = conexion.createStatement();
				st.executeUpdate(Query);
				System.out.println("Se ha creado la base de datos");
			}
			catch(Exception ex) {
				System.out.println("Error creando la base de datos: "+ex.getMessage());
			}
		}
		
		public void createTable(String db, String name) {
			try {
				String Querydb = "USE "+db+";";
				Statement stdb = conexion.createStatement();
				stdb.executeUpdate(Querydb);
				String natributos = JOptionPane.showInputDialog("Cuantos atributos tiene la tabla "+name+"?");
				String crear_tabla = "CREATE TABLE "+name+"(\n";
				String datos_atributo = "";
				for(int i=0; i<Integer.parseInt(natributos); i++) {
					String nombre = JOptionPane.showInputDialog("Entra el nombre del atributo "+(i+1));
					datos_atributo +=nombre+" ";
					String tipo = JOptionPane.showInputDialog("Entra el tipo del atributo "+(i+1));
					datos_atributo+=tipo+" ";
					String autoincrement = JOptionPane.showInputDialog("El atributo es autoincremental? (S/N)");
					if(autoincrement.equals("S")) 
						datos_atributo+=" AUTO_INCREMENT";
					else {
						String unique = JOptionPane.showInputDialog("El atributo es unico? (S/N)");
						if(unique.equals("S")) 
							datos_atributo+=" UNIQUE";
						String not_null = JOptionPane.showInputDialog("El atributo debe tener un valor? (S/N)");
						if(not_null.equals("S")) 
							datos_atributo+=" NOT NULL";
					}
					datos_atributo+=", \n";
				}
				String primary = JOptionPane.showInputDialog("Entra el nombre del atributo usado como clave primaria de esta tabla (ponlos todos separados por comas si son varios)");
				datos_atributo +=" PRIMARY KEY("+primary+")";
				String nforeign = JOptionPane.showInputDialog("Cuantas claves foraneas hay?");
				int num_foraneas = Integer.parseInt(nforeign);
				if(num_foraneas == 0) {
					datos_atributo+="\n );";
				}
				else {
					datos_atributo+=",\n";
					for(int i=0; i<num_foraneas; i++) {
						String foreign_attribute = JOptionPane.showInputDialog("Entra el nombre del "+(i+1)+" atributo usado como clave foranea");
						String foreign_table = JOptionPane.showInputDialog("Entra el nombre de la tabla referenciada");
						if(foreign_table.equals(name)) {
							datos_atributo +=" FOREIGN KEY("+foreign_attribute+") REFERENCES "+foreign_table+ "("+primary+")\n ON DELETE CASCADE ON UPDATE CASCADE";
						}
						else {
						    DatabaseMetaData metaData = conexion.getMetaData();
							ResultSet rs = metaData.getPrimaryKeys(null, null, foreign_table);
							rs.next();
							datos_atributo +=" FOREIGN KEY("+foreign_attribute+") REFERENCES "+foreign_table+ "("+rs.getString("COLUMN_NAME")+")\n ON DELETE CASCADE ON UPDATE CASCADE";
						}
						if(i<num_foraneas-1) {
							datos_atributo+=", \n";
						}
						else {
							datos_atributo+=" \n );";
						}
					}
					
				}
				crear_tabla+=datos_atributo;
				Statement st = conexion.createStatement();
				st.executeUpdate(crear_tabla);
				System.out.println("Tabla creada con exito");
			}
			catch(SQLException ex) {
				System.out.println("Error al crear la tabla: "+ex.getMessage());
			}
		}
		
		public void insertData(String db, String table_name) {
			try {
				String Querydb = "USE "+db+";";
				Statement stdb = conexion.createStatement();
				stdb.executeUpdate(Querydb);
				
				String query_nombre_columnas = "SELECT *FROM "+table_name;
				Statement st_col = conexion.createStatement();
				ResultSet rs = st_col.executeQuery(query_nombre_columnas);
		        ResultSetMetaData md = (ResultSetMetaData) rs.getMetaData();
		        int num_columnas = md.getColumnCount();
				String Query = "INSERT INTO "+table_name+" (";
				String columnas[] = new String[num_columnas];
				int columna_autoincrementada = -1;
				for(int i = 0; i<num_columnas; i++){
					String nombre_columna = md.getColumnLabel(i+1);
					if(md.isAutoIncrement(i+1))
						columna_autoincrementada = i;
					else {
						if(i<num_columnas-1)
							Query+= nombre_columna+",";
						else
							Query += nombre_columna+")";
						
						columnas[i] = nombre_columna;
					}
				}
				Query += " VALUES (";
				for(int i=0; i<num_columnas; i++) {
					if(columna_autoincrementada != i) {
						String atributo = JOptionPane.showInputDialog("Entra el valor del atributo "+columnas[i]+" de la tabla "+table_name);
						if(atributo.equals("null") || atributo.equals("NULL")) {
							if(i<num_columnas-1)
								Query += atributo+", ";
							else 
								Query += atributo+")";
						}
						else {
							if(i<num_columnas-1)
								Query +="\'"+ atributo+"\', ";
							else 
								Query +="\'"+atributo+"\')";
							}
						}
				}
				Statement st = conexion.createStatement();
				st.executeUpdate(Query);
				System.out.println("Datos guardados");
			}
			catch(SQLException ex) {
				System.out.println("No se han podido guardar los datos: "+ex.getMessage());
			}
		}
		
		public void getValues(String db, String table_name) {
			try {
				String Querydb = "USE "+db+";";
				Statement stdb = conexion.createStatement();
				stdb.executeUpdate(Querydb);
				
				String Query = "SELECT * FROM "+ table_name;
				Statement st = conexion.createStatement();
				ResultSet rs = st.executeQuery(Query);
				ResultSetMetaData rsmd = rs.getMetaData();
				int num_atributos = rsmd.getColumnCount();
				while(rs.next()) {
					for(int i=0; i<num_atributos; i++) {
						String nombre_columna = rsmd.getColumnLabel(i+1);
						System.out.println(nombre_columna+": "+rs.getString(nombre_columna));
					}
					System.out.println("---");
				}
				System.out.println("------------------------------------");
			}
			catch(SQLException ex) {
				System.out.println("No se han podido obtener los datos: "+ex.getMessage());
			}
		}
		
		public void deleteRecord(String table_name, String ID) {
			try {
				String Query = "DELETE FROM "+table_name+" WHERE ID =\""+ID+"\"";
				Statement st = conexion.createStatement();
				st.executeUpdate(Query);
				System.out.println("Datos eliminados");
			}
			catch(SQLException ex) {
				System.out.println("No se ha podido eliminar el elemento: "+ex.getMessage());
			}
		}

		private Connection conexion;
}
