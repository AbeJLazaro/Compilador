/*
Autores:      Nicolas Marin Brian Geovanny
              Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1
Fecha:        8 de febrero de 2021
Nombre:       Semantico.java
*/
import java.util.ArrayList;

public class Semantico{

  public static int numTemporal=0;
  public static int numEtiqueta=0;
  public static int numIndice=0;

  // método para generar una nueva temporal
  public static String nuevaTemporal(){
    return "t"+numTemporal++;
  }

  // método para generar una nueva temporal
  public static String nuevaEtiqueta(){
    return "etq"+numEtiqueta++;
  }

  // método que nos indica si dos tipos son equivalentes
  public static boolean equivalentes(int tipo1, int tipo2){
    if(tipo1 == tipo2) return true;
    // int y float
    if(tipo1 == 0 && tipo2 == 1) return true;
    if(tipo1 == 1 && tipo2 == 0) return true;
    // float y double
    if(tipo1 == 2 && tipo2==1) return true;
    if(tipo1 == 1 && tipo2==2) return true;
    // int y double
    if(tipo1 == 2 && tipo2==0) return true;
    if(tipo1 == 0 && tipo2==2) return true;
    return false;
  }

  public static boolean equivalentes(String t1, String t2){
    int tipo1 = Integer.parseInt(t1);
    int tipo2 = Integer.parseInt(t2);
    return equivalentes(tipo1,tipo2);
  }

  // método que nos indica si los tipos de una lista son equivalentes a los de
  // un tipo especifico
  public static boolean equivalentesLista(ArrayList<Integer> lista, int tipo){
    for (int ele : lista ) {
      if(!equivalentes(ele,tipo)){
        return false;
      }
    }
    return true;
  }

  // indica si dos listas de enteros son iguales
  public static boolean equivalentesListas(ArrayList<Integer> lista1, ArrayList<Integer> lista2){
    if(lista1==null && lista2.size()==0) return true;
    if(lista2==null && lista1.size()==0) return true;
    if(lista1==null || lista2==null) return false;
    int t1 = lista1.size();
    int t2 = lista2.size();
    if(t1!=t1) return false;
    if(t1==0 && t2==0) return true;
    if(t1==0 || t2==0) return false;
    for (int i=0;i<t1;i++) {
      if(!equivalentes(lista1.get(i),lista2.get(i))){
        return false;
      }
    }
    return true;
  }

  // int 0
  // float 1
  // Método para ampliar un tipo de dato 
  public static String ampliar(String d, int menor, int mayor, CodigoIntermedio cod){
    if(menor == mayor) return d;
    String temp;
    if(menor==0 && mayor==1){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("(float)","",d,temp)); // temp = (float)d
      return temp;
    }
    if(menor==1 && mayor==2){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("(double)","",d,temp)); // temp = (double)d
      return temp;
    }
    if(menor==0 && mayor==2){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("(double)","",d,temp)); // temp = (double)d
      return temp;
    }
    return d;
  }

  public static String ampliar(String d, String me, int mayor, CodigoIntermedio cod){
    return ampliar(d,Integer.parseInt(me),mayor,cod);
  }

  //método que encuentra el tipo de dato más grande
  public static int maximo(int a, int b){
    if(a == b) return a;
    // int y float
    if(a == 0 && b == 1) return 1;
    if(a == 1 && b == 0) return 1;
    // float y double
    if(a == 2 && b==1) return 2;
    if(a == 1 && b==2) return 2;
    // int y double
    if(a == 2 && b==0) return 2;
    if(a == 0 && b==2) return 2;

    return -1;
  }

  public static int maximo(String A, String B){
    return maximo(Integer.parseInt(A),Integer.parseInt(B));
  }

  // Método para reducir un tipo de dato
  public static String reducir(String d, int mayor, int menor, CodigoIntermedio cod){
    if(menor == mayor) return d;
    String temp;
    if(menor==0 && mayor==1){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("(int)","",d,temp)); // temp = (int)d
      return temp;
    }
    if(menor==0 && mayor==2){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("(int)","",d,temp)); // temp = (int)d
      return temp;
    }
    if(menor==1 && mayor==2){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("(float)","",d,temp)); // temp = (float)d
      return temp;
    }

    return d;
  }

  // método para invertir un arraylist
  public static ArrayList<Integer> invertir(ArrayList<Integer> lista){
    ArrayList<Integer> retorno = new ArrayList<Integer>();
    for(int i=lista.size()-1;i>=0;i--){
      retorno.add(lista.get(i));
    }
    return retorno;
  }

  // método para generar indices
  public static String nuevoIndice(){
    return "i"+numIndice++;
  }

  // método para se genera una lista para almacenar los indices
  public static ArrayList<String> nuevaListaIndices(){
    return new ArrayList<String>();
  }

  // método para reemplazar los indices
  public static void reemplazarIndices(ArrayList<String> lista, String etiqueta, CodigoIntermedio codigo){
    ArrayList<Cuadrupla> listaCuadruplas = codigo.getCodigoCuadruplas();
    for (String etq:lista){
      if(etq.equals(lista.get(lista.size()-1))){
        for (Cuadrupla c:listaCuadruplas) {
          if(c.res.equals(etq)){
            c.res=etiqueta;
          }
        }
      }else{
        String etq_p = Semantico.nuevaEtiqueta();
        for (Cuadrupla c:listaCuadruplas) {
          if(c.res.equals(etq)){
            c.res=etq_p;
          }
        }
      }
    }
  }
}