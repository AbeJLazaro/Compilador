import java.util.ArrayList;

public class CodigoIntermedio{

  private ArrayList<Cuadrupla> codigo;

  // Constructor
  public CodigoIntermedio(){
    codigo = new ArrayList<Cuadrupla>();
  }

  // método que agrega código
  public void genCod(Cuadrupla cuadrupla){
    codigo.add(cuadrupla);
  }

  public void genCod(String tipo, String cadena){
    if(tipo.equals("label")){
      codigo.add(new Cuadrupla(1,cadena));
    }
  }

  public void genCod(String cadena){
    codigo.add(new Cuadrupla(1,cadena));
  }

  // get para mostrar el código generado
  public String getCodigo(){
    String texto="";
    for (Cuadrupla cuad : codigo) {
      texto+=cuad.toString()+"\n";
    }
    return texto;
  }

}