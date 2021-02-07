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

  public void genCod(String op, String res){
    codigo.add(new Cuadrupla(op,res));
  }

  public void genCod(String cadena){
    codigo.add(new Cuadrupla(cadena));
  }

  // get para mostrar el código generado
  public String getCodigo(){
    String texto="";
    for (Cuadrupla cuad : codigo) {
      texto+=cuad.toString();
    }
    return texto;
  }

  public ArrayList<Cuadrupla> getCodigoCuadruplas(){
    return this.codigo;
  }

  public void popCodigo(){
    this.codigo.remove(this.codigo.size()-1);
  }

}