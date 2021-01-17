/*
Autores:      Cabrera Gaytán Jazmín Andrea
              Camacho Morales Gerardo Iván
              Nicolas Marin Brian Geovanny
              Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1.0
Fecha:        10 de enero de 2021
Nombre:       Parser.java
*/

import java.util.ArrayList;
import java.io.IOException;

public class Parser{
  /*Utilizamos el orden definido en el analizador léxico para reutilizar
  el analizador léxico*/
  //TIPOS DE DATOS
  private static final int INT=1;
  private static final int FLOAT=2;
  private static final int CHAR=3;
  private static final int DOUBLE=4;
  private static final int VOID=5;
  //PALABRAS RESERVADAS
  private static final int FUNC=6;
  private static final int IF=7;
  private static final int ELSE=8;
  private static final int WHILE=9;
  private static final int DO=10;
  private static final int BREAK=11;
  private static final int SWITCH=12;
  private static final int CASE=13;
  private static final int DEFAULT=14;
  //OPERADORES
  private static final int ASIGNACION=15;
  private static final int OR=16;
  private static final int AND=17;
  private static final int IGUAL=18;
  private static final int DESIGUAL=19;
  private static final int LT=20;
  private static final int LET=21;
  private static final int MT=22;
  private static final int MET=23;
  private static final int SUMA=24;
  private static final int RESTA=25;
  private static final int MULTI=26;
  private static final int DIVISION=27;
  private static final int MODULO=28;
  private static final int NEGACION=29;
  //PUNTUACION
  private static final int L1=30;
  private static final int L2=31;
  private static final int C1=32;
  private static final int C2=33;
  private static final int P1=34;
  private static final int P2=35;
  private static final int DOSPUNTOS=36;
  private static final int PUNTOYCOMA=37;
  private static final int COMA=38;
  //LITERALES
  private static final int IDENTIFIER=39;
  private static final int STRING_LIT=40;
  private static final int TRUE=41;
  private static final int FALSE=42;
  private static final int INT_LIT=43;
  private static final int FLOAT_LIT=44;
  // otros atributos necesarios
  private Lexer analizadorLexico;
  private int tokenActual;
  private int dir;

  // cambiar estos atributos, generar una clase para cada tabla
  private TablaSimbolos TS;
  private TablaTipos TT;

  //CONSTRUCTOR 
  public Parser(Lexer lexer) throws IOException,Exception{
    // asignamos el analizador léxico
    analizadorLexico = lexer;
    dir = 0;
    tokenActual = analizadorLexico.yylex();
    TS = new TablaSimbolos();
    TT = new TablaTipos();

  }

  // método que inicia
  public void parse() throws IOException,Exception{
    boolean a = TS.buscar("hola");
    TS.insertar(new Simbolo("a", dir , 0, "var", null));
    int b = TS.getTipo("hola");
    ArrayList<Integer> c = TS.getArgs("hola");
    int d = TT.getTam(0);
    int e = TT.getTipoBase(0);
    String f = TT.getNombre(0);
    TT.insertar(new Tipo(5, "array", 16, 4, 0));
  }

  /*

    ANDREA *************************************************

  */

  private void declaraciones() throws IOException,Exception{
    
  }
  private void tipo() throws IOException,Exception{

  }

  /*
    IVAN *************************************************
  */
  private void lista_var() throws IOException,Exception{
    if(tokenActual==IDENTIFIER){
      eat(IDENTIFIER);
      lista_var_p();
    }else{
      error("Error sintáctico, se esperaba un identificador");
    }
  }

  private void lista_var_p() throws IOException,Exception{
    if(tokenActual==COMA){
      eat(COMA);
      eat(IDENTIFIER);
      lista_var_p();
    }
    // Producción vacía
  }

  private void funciones() throws IOException,Exception{
    if(tokenActual==FUNC){
      eat(FUNC);
      eat(IDENTIFIER);
      eat(P1);
      argumentos();
      eat(P2);
      bloque();
      funciones();
    }
    // Producción vacía
  }

  private void argumentos() throws IOException,Exception{
    switch(tokenActual){
      case INT:
      case FLOAT:
      case CHAR:
      case DOUBLE:
      case VOID:
        lista_args();
        break;
    }
    // Producción vacía
  }

  private void lista_args() throws IOException,Exception{
    switch(tokenActual){
      case INT:
      case FLOAT:
      case CHAR:
      case DOUBLE:
      case VOID:
        tipo();
        eat(IDENTIFIER);
        lista_args_p();
        break;
      default:
        error("Error sintáctico, se esperaba un tipo de dato");
    }
  }

  private void lista_args_p() throws IOException,Exception{
    if(tokenActual==COMA){
      eat(COMA);
      tipo();
      eat(IDENTIFIER);
      lista_args_p();
    }
    // producción vacía
  }

  private void bloque() throws IOException,Exception{
    if(tokenActual==L1){
      eat(L1);
      declaraciones();
      instrucciones();
      eat(L2);
    }else{
      error("Error sintáctico, se esperaba {");
    }
  }
  /*

    BRIAN *************************************************

  */
    private void instrucciones(){
      sentencia();
      instrucciones_p();
  }
  
  private void instrucciones_p(){
      if(tokenActual==OR){
         sentencia();
         instrucciones_p();
    }
    // producción vacía
  }
  
  private void sentencia(){
      switch(tokenActual){
        case IF:
              eat(IF);
              eat(P1);
              bool();
              eat(P2);
              sentencia();
              condicional();
              break;
        case IDENTIFIER:
            localizacion();
            eat(ASIGNACION);
            bool();
            break;
        case WHILE:
               eat(WHILE);
               eat(P1);
               bool();
               eat(P2);
               sentencia();
               break;
        case DO:
            eat(DO);
            sentencia();
            eat(WHILE);
            eat(P1);
            bool();
            eat(P2);
            break;
        case BREAK:
            eat(BREAK);
            eat(PUNTOYCOMA);
            break;
        case L1:
            bloque();
            break;
        case SWITCH:
            eat(SWITCH);
            eat(P1);
            bool();
            eat(P2);
            eat(L1);
            casos();
            eat(L2);
            break;
        default:
            error("Error de sintaxis");
      }
  }
  
  private void condicional(){
      if(tokenActual == ELSE){
          eat(ELSE);
          sentencia();
      }
      //produccion vacia
  }
  
  private void casos(){
      switch(tokenActual){
          case CASE:
              caso();
              casos();
              break;
          case DEFAULT:
              predeterminado();
              break;
          default:
              error("Error de sintaxis");
      }
  }
  
  private void caso(){
      eat(CASE);
      eat(INT_LIT);
      eat(DOSPUNTOS);
      instrucciones();
  }
  
  private void predeterminado(){
      eat(DEFAULT);
      eat(DOSPUNTOS);
      instrucciones();
  }

  /*
    ANGEL *************************************************
  bool → comb bool_p
  bool_p → || comb bool_p | ε 
  comb → igualdad comb_p 
  comb_p → && igualdad comb_p | ε
  igualdad → rel igualdad_p
  igualdad_p → == rel igualdad_p | != rel igualdad_p | ε
  rel → exp rel_p
  rel_p → < exp | <= exp | >= exp | > exp | ε
  */
  
  // bool -> comb bool_p
  private void bool() throws IOException,Exception{
    comb();
    bool_p();
  }
  
  //bool_p → || comb bool_p | ε 
  private void bool_p() throws IOException,Exception{
    if(tokenActual==OR){
      eat(OR);
      comb();
      bool_p();
    }
  }
  
  //comb → igualdad comb_p 
  private void comb() throws IOException,Exception{
    igualdad();
    comb_p();
  }
  
  //comb_p → && igualdad comb_p | ε
  private void comb_p() throws IOException,Exception{
    if(tokenActual==AND){
      eat(AND);
      igualdad();
      comb_p();
    }
  }
  
  //igualdad → rel igualdad_p
  private void igualdad() throws IOException,Exception{
    rel();
    igualdad_p();
  }
  
  //igualdad_p → == rel igualdad_p | != rel igualdad_p | ε
  private void igualdad_p() throws IOException,Exception{
    if(tokenActual==IGUAL){
      eat(IGUAL);
      rel();
      igualdad_p();
    }else if(tokenActual==DESIGUAL){
      eat(DESIGUAL);
      rel();
      igualdad_p();
    }
  }
  
  //rel → exp rel_p
  private void rel() throws IOException,Exception{
    exp();
    rel_p();
  }
  
  //rel_p → < exp | <= exp | >= exp | > exp | ε
  private void rel_p() throws IOException,Exception{
    if(tokenActual==LT){
      eat(LT);
      exp();
    }else if(tokenActual==LET){
      eat(LET);
      exp();
    }else if(tokenActual==MET){
      eat(MET);
      exp();
    }else if(tokenActual==MT){
      eat(MT);
      exp();
    }
  }
  
  //exp → term exp_p
  private void exp() throws IOException,Exception{
    term();
    exp_p();
  }
  
  //exp_p → + term exp_p | - term exp_p | ε
  private void exp_p() throws IOException,Exception{
    if(tokenActual==SUMA){
      eat(SUMA);
      term();
      exp_p();
    }else if(tokenActual==RESTA){
      eat(RESTA);
      term();
      exp_p();
    }
  }
  
  /*
    LAZARO *************************************************

  */
  private void term() throws IOException,Exception{
    unario();
    term_p();
  }

  private void term_p() throws IOException,Exception{
    switch(tokenActual){
      case MULTI:
        eat(MULTI);
        unario();
        term_p();
        break;
      case DIVISION:
        eat(DIVISION);
        unario();
        term_p();
        break;
      case MODULO:
        eat(MODULO);
        unario();
        term_p();
        break;
      default:
        // aquí entra en producción vacía
        break;
    }
  }

  private void unario() throws IOException,Exception{
    switch(tokenActual){
      case NEGACION:
        eat(NEGACION);
        unario();
        break;
      case RESTA:
        eat(RESTA);
        unario();
        break;
      default:
        factor();
    }
  }

  private void factor() throws IOException,Exception{
    switch(tokenActual){
      //(bool)
      case P1:
        eat(P1);
        bool();
        eat(P2);
        break;
      //localización
      //id(parametros)
      case IDENTIFIER:
        String id = analizadorLexico.yytext();
        eat(IDENTIFIER);
        if(tokenActual!=P1){
          localizacion();
        }
        eat(P1);
        parametros();
        break;
      // número
      case INT_LIT:
        eat(INT_LIT);
        break;
      case FLOAT_LIT:
        eat(FLOAT_LIT);
        break;
      // cadena
      case STRING_LIT:
        eat(STRING_LIT);
        break;
      //true
      case TRUE:
        eat(TRUE);
        break;
      //false
      case FALSE:
        eat(FALSE);
        break;
      default:
        error("Error de sintaxis");
    }
  }

  private void parametros() throws IOException,Exception{
    if(tokenActual==OR){
      lista_param();
    }
    // producción vacía
  }

  private void lista_param() throws IOException,Exception{
    bool();
    lista_param_p();
  }

  private void lista_param_p() throws IOException,Exception{
    if(tokenActual==COMA){
      eat(COMA);
      bool();
      lista_param_p();
    }
    // producción vacía
  }

  private void localizacion() throws IOException,Exception{
    if(tokenActual==IDENTIFIER){
      eat(IDENTIFIER);
      localizacion_p();
    }
    error("Error de sintaxis, se esperaba un identificador");
  }

  private void localizacion_p() throws IOException,Exception{
    if(tokenActual==C1){
      eat(C1);
      bool();
      eat(C2);
      localizacion_p();
    }
    // producción vacía
  }
  // MÉTODOS DE AYUDA

  // Método para avanzar de token
  public void eat(int i) throws IOException,Exception{
    if(tokenActual==i){
      tokenActual = analizadorLexico.yylex();
      // se revisa que el nuevo token sea un número correcto
      if(tokenActual==-1){
        throw new Exception("Error léxico, línea "+analizadorLexico.getYyline());
      }
    }else{
      error("Error de sintaxis, se esperaba "+i+" se encontró "+tokenActual);
    }
  }

  // Método que muestra la existencia de un error
  private void error(String mensaje) throws Exception{
    throw new Exception(mensaje+", línea "+analizadorLexico.getYyline()+"\n"+analizadorLexico.linea);
  }

}
