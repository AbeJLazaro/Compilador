public class Cuadrupla{

  public String op;
  public String arg1;
  public String arg2;
  public String res;

  // Constructor
  public Cuadrupla(String op, String arg1, String arg2, String res){
    this.op = op;
    this.arg1 = arg1;
    this.arg2 = arg2;
    this.res = res;
  }

  public Cuadrupla(int tipo, String codigo){
    switch(tipo){
      case 1:
        op = codigo;
        arg1 = "";
        arg2 = "";
        res = "";
      case 2:
        op = "goto";
        arg1 = codigo;
        arg2 = "";
        res = "";
    }
  }
  
  public String toString(){
    return res+" "+arg1+" "+op+" "+arg2;
  }
  
}