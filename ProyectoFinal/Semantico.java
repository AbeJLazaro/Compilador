import java.util.ArrayList;

public class Semantico{

  public static int numTemporal=0;
  public static int numEtiqueta=0;

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
    if(tipo1 == 0 && tipo2 == 1) return true;
    if(tipo1 == 1 && tipo2 == 0) return true;
    if(tipo1 == -1 || tipo2==-1) return true;
    return false;
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

  // Método para ampliar un tipo de dato 
  public static String ampliar(String d, int menor, int mayor, CodigoIntermedio cod){
    if(menor == mayor) return d;
    String temp;
    if(menor==0 && mayor==1){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("=","(float)"+d,"",temp)); // temp = (float)d
      return temp;
    }
    return null;
  }

  //método que encuentra el tipo de dato más grande
  public static int maximo(int a, int b){
    if(a==1 || b==1) {
      return 1;
    }else{
      return 0;
    } 
  }

  // Método para reducir un tipo de dato
  public static String reducir(String d, int mayor, int menor, CodigoIntermedio cod){
    if(menor == mayor) return d;
    String temp;
    if(menor==0 && mayor==1){
      temp = nuevaTemporal();
      cod.genCod(new Cuadrupla("=","(int)"+d,"",temp)); // temp = (int)d
      return temp;
    }
    return null;
  }

  public static ArrayList<Integer> invertir(ArrayList<Integer> lista){
    ArrayList<Integer> retorno = new ArrayList<Integer>();
    for(int i=lista.size()-1;i>=0;i--){
      retorno.add(lista.get(i));
    }
    return retorno;
  }
}