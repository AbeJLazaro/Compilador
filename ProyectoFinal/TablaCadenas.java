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

}