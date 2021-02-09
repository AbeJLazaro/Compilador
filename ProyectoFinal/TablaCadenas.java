/*
Autores:      Lázaro Martínez Abraham Josué

Versión:      1
Fecha:        8 de febrero de 2021
Nombre:       TablaCadenas.java
*/
import java.util.ArrayList;

public class TablaCadenas{

  private ArrayList<String> tabla;

  public TablaCadenas(){
    this.tabla = new ArrayList<String>();
  }

  public void agregar(String cadena){
    this.tabla.add(cadena);
  }

  public int getUltimaPos(){
    return this.tabla.size()-1;
  }

  public void printTC(){
    if(tabla.size()==0){
      System.out.println("Tabla de cadenas vacía");
      return;
    }
    System.out.println("Tabla de cadenas");
    System.out.println("id\tcadena");
    int i =0;
    for(String s: tabla){
      System.out.println(i+"\t"+s);
      i++;
    }
  }

}