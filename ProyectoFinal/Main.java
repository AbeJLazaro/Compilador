/*
Autores:      Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1.0
Fecha:        10 de enero de 2021
Nombre:       lexer.jflex
*/
import java.io.*;

public class Main{
  public static void main(String[] args) {
    Lexer lexer;
    try{
      File f = new File(args[0]);
      FileReader fr = new FileReader(f);
      BufferedReader br = new BufferedReader(fr);
      lexer = new Lexer(br);
      Parser parser = new Parser(lexer);
      parser.parse();
      br.close();
    }catch(IOException e){
      System.out.println("Error al abrir el archivo");
    }catch(ErrorCompilador e){
      System.out.println(e.toString());
    }
  }
}