/*
Autores:      Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1.0
Fecha:        16 de enero de 2021
Nombre:       TablaSimbolos.java
*/

import java.util.ArrayList;

public class TablaSimbolos{

  private ArrayList<Simbolo> tabla;

  // Constructor
  public TablaSimbolos(){
    tabla = new ArrayList<Simbolo>();
  }

  // Buscar simbolo por id
  public boolean buscar(String id){
    for (Simbolo s : tabla ) {
      if(s.id.equals(id)){
        return true;
      }
    }
    return false;
  }

  // Insertar nuevo simbolo
  public void insertar(Simbolo s){
    tabla.add(s);
  }

  // obtener el tipo de un simbolo
  public int getTipo(String id){
    for (Simbolo s : tabla ) {
      if(s.id.equals(id)){
        return s.tipo;
      }
    }
    return -1;
  }

  // obtener los argumento
  public ArrayList<Integer> getArgs(String id){
    for (Simbolo s : tabla ) {
      if(s.id.equals(id) && s.var.equals("func")){
        return s.args;
      }
    }
    return null;
  }

  // imprime la tabla de simbolos
  public void printTS(){
    int i=0;
    System.out.println("Tabla de simbolos");
    System.out.println("pos\tid\ttipo\tdir\tvar\targs");
    for(Simbolo s: tabla){
      System.out.println(i+"\t"+s.id+"\t"+s.tipo+"\t"+s.dir+"\t"+s.var+"\t"+s.args);
      i++;
    }
  }

}