public class Cuadrupla{

  public String op;
  public String arg1;
  public String arg2;
  public String res;
  public int tipo;

  // Constructor
  public Cuadrupla(String op, String arg1, String arg2, String res){
    this.op = op;
    this.arg1 = arg1;
    this.arg2 = arg2;
    this.res = res;
    this.tipo = 0;
    System.out.println(toString());
  }

  public Cuadrupla(int tipo, String codigo){
    switch(tipo){
      case 1:
        op = codigo;
        arg1 = "";
        arg2 = "";
        res = "";
        this.tipo = 1;
        break;
      case 2:
        op = codigo;
        arg1 = "";
        arg2 = "";
        res = "";
        this.tipo = 2;
        break;
    }
    System.out.println(toString());
  }
  
  public String toString(){
    switch(this.tipo){
      case 0:
        return res+"="+arg1+op+arg2;
      case 1:
        return op;
      case 2:
        return op; 
      default:
        return res+arg1+op+arg2;
    }
  }
}