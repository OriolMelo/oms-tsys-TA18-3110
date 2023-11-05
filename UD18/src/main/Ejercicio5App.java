package main;

public class Ejercicio5App {

	public static void main(String[] args) {
		GestorDB gestor = new GestorDB();
		String nombre_db="UD18_EJ05";
		String[] tablas = new String[] {"despachos", "directores"};
		crearDB(gestor, nombre_db, tablas);
	}

	public static void crearDB(GestorDB gestor, String nombre_db, String[] tablas) {
		gestor.connectDB();
		gestor.createDB(nombre_db);
		int num_tablas = tablas.length;
		int num_inserciones = 5;
		for(int i=0; i<num_tablas; i++) {
			gestor.createTable(nombre_db, tablas[i]);
			for(int j=0; j<num_inserciones; j++){
				gestor.insertData(nombre_db, tablas[i]);
			}	
		}
		for(int i=0; i<num_tablas; i++) {
			gestor.getValues(nombre_db, tablas[i]);
		}
		gestor.closeConnection();
	}
}
