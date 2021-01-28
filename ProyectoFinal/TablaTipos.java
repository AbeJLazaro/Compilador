/*
Autores:      Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1.0
Fecha:        16 de enero de 2021
Nombre:       TablaTipos.java
*/

import java.util.ArrayList;

public class TablaTipos{

  private ArrayList<Tipo> tabla;

  // Constructor
  public TablaTipos(){
    tabla = new ArrayList<Tipo>();

    tabla.add(new Tipo(0, "int", 4, -1, -1));
    tabla.add(new Tipo(1, "float", 4, -1, -1));
    tabla.add(new Tipo(2, "char", 1, -1, -1));
    tabla.add(new Tipo(3, "double", 8, -1, -1));
    tabla.add(new Tipo(4, "void", 0, -1, -1));
    tabla.add(new Tipo(5, "string", 10, -1, -1));
  }

  // Método para obtener el tamaño de un tipo de dato
  public int getTam(int id){
    for (Tipo t : tabla ) {
      if(t.id==id){
        return t.tam;
      }
    }
    return -1;
  }

  // Método para obtener el tipo de dato base de un tipo de dato
  public int getTipoBase(int id){
    for (Tipo t : tabla ) {
      if(t.id==id){
        return t.tipoBase;
      }
    }
    return -1;
  }

  // Método para agregar un nuevo tipo de dato
  public int insertar(String tipo, int elem, int tipoBase){
    int tamBase = getTam(tipoBase);
    Tipo t = new Tipo(tabla.size(),tipo, elem*tamBase, elem, tipoBase);
    tabla.add(t);
    return tabla.size()-1;
  }

  // Obtiene el nombre del tipo
  public String getNombre(int id){
    for (Tipo t : tabla ) {
      if(t.id==id){
        return t.tipo;
      }
    }
    return "";
  }

  public void printTT(){
    System.out.println("Tabla de tipos");
    System.out.println("id\ttipo\ttam\t#e\tTipoBase");
    for(Tipo t: tabla){
      System.out.println(t.id+"\t"+t.tipo+"\t"+t.tam+"\t"+t.elem+"\t"+t.tipoBase);
    }
  }
}