/*
Autores:      Camacho Morales Gerardo Iván
              Nicolas Marin Brian Geovanny
              Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1.2
Fecha:        10 de enero de 2021
Nombre:       Parser.java
*/

import java.util.ArrayList;
import java.util.Stack;
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
  private static final int RETURN=45;
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
  private String lexema;
  private int dir;

  private TablaSimbolos fondoTS;
  private TablaTipos fondoTT;
  // pilas
  private Stack<TablaSimbolos> PilaTS;
  private Stack<TablaTipos> PilaTT;

  private Stack<Integer> PilaDir;

  private ArrayList<Integer> listaRetorno;

  private CodigoIntermedio codigo;


  //CONSTRUCTOR 
  public Parser(Lexer lexer) throws IOException,Exception{
    // asignamos el analizador léxico
    analizadorLexico = lexer;
    dir = 0;
    tokenActual = analizadorLexico.yylex();
    lexema = analizadorLexico.yytext();

    fondoTS = new TablaSimbolos();
    fondoTT = new TablaTipos();

    PilaTS = new Stack<TablaSimbolos>();
    PilaTT = new Stack<TablaTipos>();
    PilaDir = new Stack<Integer>();

    PilaTS.push(fondoTS);
    PilaTT.push(fondoTT);

    codigo = new CodigoIntermedio();
  }

  // método que inicia
  public void parse() throws IOException,Exception{
    programa();
    System.out.println("Cadena aceptada");
    PilaTT.peek().printTT();
    PilaTS.peek().printTS();
    System.out.println(codigo.getCodigo());
  }

  /*
   *************************************************
  programa → declaraciones funciones 
  declaraciones → tipo lista_var ; declaraciones | ε 
  tipo → basico compuesto 
  basico → int | float | char | double | void 
  compuesto → [ numero ] compuesto | ε

  */

  private void programa() throws IOException,Exception{
    declaraciones();
    funciones();
  }

  private void declaraciones() throws IOException,Exception{
    switch(tokenActual){
      case INT:
      case FLOAT:
      case CHAR:
      case DOUBLE:
      case VOID:
        int tipoTipo = tipo();
        lista_var(tipoTipo);
        eat(PUNTOYCOMA);
        declaraciones();
        break;
      //default:
        // producción vacía
    }
  }

  private int tipo() throws IOException,Exception{
    int basicoBase = basico();
    int compuestoTipo = compuesto(basicoBase);
    return compuestoTipo;
  }

  private int basico() throws IOException,Exception{
    switch(tokenActual){
      case INT:
        eat(INT);
        return 0;
      case FLOAT:
        eat(FLOAT);
        return 1;
      case CHAR:
        eat(CHAR);
        return 2;
      case DOUBLE:
        eat(DOUBLE);
        return 3;
      case VOID:
        eat(VOID);
        return 4;
      default:
        error("Error de sintaxis, se esperaba un tipo de dato primitivo");
    }
    return -1;
  }

  private int compuesto(int basicoBase)  throws IOException,Exception{
    if(tokenActual==C1){
      eat(C1);
      String valor = lexema;
      eat(INT_LIT);
      eat(C2);
      int compuesto1Tipo = compuesto(basicoBase);
      int compuestoTipo = PilaTT.peek().insertar("array", Integer.parseInt(valor), compuesto1Tipo);
      return compuestoTipo;
    }
    //producción vacía
    return basicoBase;
  }

  /*
    IVAN *************************************************
  */
  private void lista_var(int tipoTipo) throws IOException,Exception{
    if(tokenActual==IDENTIFIER){
      if(!PilaTS.peek().buscar(lexema)){
        PilaTS.peek().insertar(new Simbolo(lexema,dir,tipoTipo,"var",null));
        dir += PilaTT.peek().getTam(tipoTipo);
      }else{
        error("Error semántico, el id "+lexema+" ya se encuentra declarado");
      }
      eat(IDENTIFIER);
      lista_var_p(tipoTipo);
    }else{
      error("Error sintáctico, se esperaba un identificador");
    }
  }

  private void lista_var_p(int tipoTipo) throws IOException,Exception{
    if(tokenActual==COMA){
      eat(COMA);
      if(!PilaTS.peek().buscar(lexema)){
        PilaTS.peek().insertar(new Simbolo(lexema,dir,tipoTipo,"var",null));
        dir += PilaTT.peek().getTam(tipoTipo);
      }else{
        error("Error semántico, el id "+lexema+" ya se encuentra declarado");
      }
      eat(IDENTIFIER);
      lista_var_p(tipoTipo);
    }
  }

  private void funciones() throws IOException,Exception{
    if(tokenActual==FUNC){
      listaRetorno = new ArrayList<Integer>();

      PilaTS.push(new TablaSimbolos());
      PilaTT.push(new TablaTipos());
      PilaDir.push(dir);
      dir = 0;

      // come palabra func
      eat(FUNC);
      // tipo de la función
      int tipoTipo = tipo();

      //identificador de la función
      String id = lexema;      
      
      if(!fondoTS.buscar(id)){
        eat(IDENTIFIER);
        // argumentos de la función
        eat(P1);
        ArrayList<Integer> argumentosLista = argumentos();
        eat(P2);

        // bloque
        String bloqueSig = Semantico.nuevaEtiqueta();

        codigo.genCod("label",id);
        codigo.genCod("label",bloqueSig);

        // bloque
        bloque(bloqueSig);

        // comprobación
        if(Semantico.equivalentesLista(listaRetorno,tipoTipo)){
          PilaTT.peek().printTT();
          PilaTS.peek().printTS();
          PilaTS.pop();
          PilaTT.pop();
          dir = PilaDir.pop();
          PilaTS.peek().insertar(new Simbolo(id,0,tipoTipo,"func",argumentosLista));
        }else{
          error("El tipo de retorno no son equivalentes");
        }
      }else{
        error("Error semántico, el id "+id+" ya está definido");
      }
      funciones();
    }
    // Producción vacía
  }

  private ArrayList<Integer> argumentos() throws IOException,Exception{
    switch(tokenActual){
      case INT:
      case FLOAT:
      case CHAR:
      case DOUBLE:
      case VOID:
        ArrayList<Integer> argumentosLista = lista_args();
        return argumentosLista;
    }
    // Producción vacía
    return null;
  }

  private ArrayList<Integer> lista_args() throws IOException,Exception{
    switch(tokenActual){
      case INT:
      case FLOAT:
      case CHAR:
      case DOUBLE:
      case VOID:

        int tipoTipo = tipo();
        String id = lexema;

        if(!PilaTS.peek().buscar(id)){
          eat(IDENTIFIER);
          PilaTS.peek().insertar(new Simbolo(lexema,dir,tipoTipo,"var",null));
          dir += PilaTT.peek().getTam(tipoTipo);
        }else{
          error("Error semántico, el identificador "+id+" ya se encuentra delarado como argumento");
        }
        ArrayList<Integer> lista_argsLista = lista_args_p();
        lista_argsLista.add(tipoTipo);

        return Semantico.invertir(lista_argsLista);
      default:
        error("Error sintáctico, se esperaba un tipo de dato");
    }
    return null;
  }

  private ArrayList<Integer> lista_args_p() throws IOException,Exception{
    if(tokenActual==COMA){
      eat(COMA);

      int tipoTipo = tipo();
      String id = lexema;

      if(!PilaTS.peek().buscar(id)){
        eat(IDENTIFIER);
        PilaTS.peek().insertar(new Simbolo(lexema,dir,tipoTipo,"var",null));
        dir += PilaTT.peek().getTam(tipoTipo);
      }else{
        error("Error semántico, el identificador "+id+" ya se encuentra delarado como argumento");
      }

      ArrayList<Integer> lista_argsLista = lista_args_p();
      lista_argsLista.add(tipoTipo);
      return lista_argsLista;
    }
    return new ArrayList<Integer>();
  }

  private void bloque(String bloqueSig) throws IOException,Exception{
    if(tokenActual==L1){
      eat(L1);
      declaraciones();
      codigo.genCod("label",bloqueSig);
      instrucciones(bloqueSig);
      eat(L2);
    }else{
      error("Error sintáctico, se esperaba {");
    }
  }
  /*
    BRIAN *************************************************
  */
  private void instrucciones(String instruccionesSig) throws IOException,Exception{
    /*String sentenciaSig = Semantico.nuevaEtiqueta();
    sentencia(sentenciaSig);
    codigo.genCod("label",sentenciaSig);
    instrucciones_p(instruccionesSig);*/
    String sentenciaSig = Semantico.nuevaEtiqueta();
    sentencia();
    codigo.genCod("label",sentenciaSig);
    instrucciones_p(instruccionesSig);
  }

  private void instrucciones_p(String instrucciones_pSig) throws IOException,Exception{
    switch(tokenActual){
      case IF:
      case IDENTIFIER:
      case WHILE:
      case DO:
      case BREAK:
      case L1:
      case SWITCH:
      case RETURN:
        /*String sentenciaSig = Semantico.nuevaEtiqueta();
        sentencia(sentenciaSig);
        codigo.genCod("label",sentenciaSig);
        instrucciones_p(instrucciones_pSig);*/
        sentencia();
        instrucciones_p(instrucciones_pSig);
        break;
      //default:
        //producción vacía
    }
    // producción vacía
  }

  private void sentencia() throws IOException,Exception{
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
        eat(PUNTOYCOMA);
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
        bloque("");
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
      case RETURN:
        eat(RETURN);
        return_p();
        break;
      //default:
        // producción vacía
    }
  }

  private void condicional() throws IOException,Exception{
    if(tokenActual == ELSE){
      eat(ELSE);
      sentencia();
    }
    //produccion vacia
  }

  private void return_p() throws IOException,Exception{
    if(tokenActual==PUNTOYCOMA){
      eat(PUNTOYCOMA);
    }else{
      exp();
      eat(PUNTOYCOMA);
    }
  }

  private void casos() throws IOException,Exception{
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

  private void caso() throws IOException,Exception{
    eat(CASE);
    eat(INT_LIT);
    eat(DOSPUNTOS);
    instrucciones("");
  }

  private void predeterminado() throws IOException,Exception{
    eat(DEFAULT);
    eat(DOSPUNTOS);
    instrucciones("");
    sentencia();
    instrucciones_p("");
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
        eat(IDENTIFIER);
        factor_p();
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

  private void factor_p() throws IOException,Exception{
    if(tokenActual==P1){
      eat(P1);
      parametros();
      eat(P2);
    }else{
      localizacion_p();
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
    }else{
      error("Error de sintaxis, se esperaba un identificador 1");
    }
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
      lexema = analizadorLexico.yytext();
      // se revisa que el nuevo token sea un número correcto
      if(tokenActual==-1){
        throw new Exception("Error léxico, línea "+analizadorLexico.getYyline());
      }
    }else{
      String cadena1=mapear(i);
      String cadena2=mapear(tokenActual);
      error("Error de sintaxis, se esperaba "+cadena1+" se encontró "+cadena2);
    }
  }

  private String mapear(int i){
    switch(i){
      case 1: return "int";
      case 2: return "float";
      case 3: return "char";
      case 4: return "double";
      case 5: return "void";
      //PALABRAS RESERVADAS
      case 6: return "func";
      case 7: return "if";
      case 8: return "else";
      case 9: return "while";
      case 10: return "do";
      case 11: return "break";
      case 12: return "switch";
      case 13: return "case";
      case 14: return "default";
      //OPERADORES
      case 15: return "=";
      case 16: return "||";
      case 17: return "&&";
      case 18: return "==";
      case 19: return "!=";
      case 20: return "<";
      case 21: return "<=";
      case 22: return ">";
      case 23: return ">=";
      case 24: return "+";
      case 25: return "-";
      case 26: return "*";
      case 27: return "/";
      case 28: return "%";
      case 29: return "!";
      //PUNTUACION
      case 30: return "{";
      case 31: return "}";
      case 32: return "[";
      case 33: return "]";
      case 34: return "(";
      case 35: return ")";
      case 36: return ":";
      case 37: return ";";
      case 38: return ",";
      //LITERALES
      case 39: return "identificador";
      case 40: return "STRING_LIT";
      case 41: return "true";
      case 42: return "false";
      case 43: return "INT_LIT";
      case 44: return "FLOAT_LIT";
      case 45: return "return";
    }
    return "desconocido";
  }

  // Método que muestra la existencia de un error
  private void error(String mensaje) throws Exception{
    throw new Exception(mensaje+", línea "+(analizadorLexico.getYyline()+1)+"\n"+analizadorLexico.linea);
  }

}
