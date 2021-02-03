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

  public Cuadrupla(String op, String res){
    this.op = op;
    this.arg1 = "";
    this.arg2 = "";
    this.res = res;
    this.tipo = 1;
    System.out.println(toString());
  }

  public Cuadrupla(String op){
    this.op = op;
    this.arg1 = "";
    this.arg2 = "";
    this.res = "";
    this.tipo = 2;
    System.out.println(toString());
  }
  
  public String toString(){
    switch(this.tipo){
      case 0:
        if(op.equals("if")){
          return op+" "+arg1+" "+arg2+" "+res+"\n";
        }
        return res+" = "+arg1+" "+op+" "+arg2+"\n";
      case 1:
        if(op.equals("label")){
          return res+": ";
        }
        return op+" "+res+"\n";
      case 2:
        return op+"\n"; 
      default:
        return res+arg1+op+arg2+"\n";
    }
  }
}