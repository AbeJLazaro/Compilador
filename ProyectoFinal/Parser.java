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
  private static final int PRINT=46;
  private static final int SCAN=47;
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

  // tablas globales, checar si son necesarias al final ********************************************
  private TablaSimbolos fondoTS;
  private TablaTipos fondoTT;

  // pilas
  private Stack<TablaSimbolos> PilaTS;
  private Stack<TablaTipos> PilaTT;
  private TablaCadenas tablaCadenas;

  private Stack<Integer> PilaDir;

  private ArrayList<Integer> listaRetorno;

  private CodigoIntermedio codigo;


  //CONSTRUCTOR 
  public Parser(Lexer lexer) throws IOException,ErrorCompilador{
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
    tablaCadenas = new TablaCadenas();

    PilaTS.push(fondoTS);
    PilaTT.push(fondoTT);

    codigo = new CodigoIntermedio();
  }

  // método que inicia
  public void parse() throws IOException,ErrorCompilador{
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

  private void programa() throws IOException,ErrorCompilador{
    declaraciones();
    funciones();
  }

  private void declaraciones() throws IOException,ErrorCompilador{
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

  private int tipo() throws IOException,ErrorCompilador{
    int basicoBase = basico();
    int compuestoTipo = compuesto(basicoBase);
    return compuestoTipo;
  }

  private int basico() throws IOException,ErrorCompilador{
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

  private int compuesto(int basicoBase)  throws IOException,ErrorCompilador{
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
  private void lista_var(int tipoTipo) throws IOException,ErrorCompilador{
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

  private void lista_var_p(int tipoTipo) throws IOException,ErrorCompilador{
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

  private void funciones() throws IOException,ErrorCompilador{
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

  private ArrayList<Integer> argumentos() throws IOException,ErrorCompilador{
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

  private ArrayList<Integer> lista_args() throws IOException,ErrorCompilador{
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

  private ArrayList<Integer> lista_args_p() throws IOException,ErrorCompilador{
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

  private void bloque(String bloqueSig) throws IOException,ErrorCompilador{
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
  private void instrucciones(String instruccionesSig) throws IOException,ErrorCompilador{
    String sentenciaSig = Semantico.nuevaEtiqueta();
    sentencia(sentenciaSig);
    codigo.genCod("label",sentenciaSig);
    instrucciones_p(instruccionesSig);
  }

  private void instrucciones_p(String instrucciones_pSig) throws IOException,ErrorCompilador{
    switch(tokenActual){
      case IF:
      case IDENTIFIER:
      case WHILE:
      case DO:
      case BREAK:
      case L1:
      case SWITCH:
      case RETURN:
        String sentenciaSig = Semantico.nuevaEtiqueta();
        sentencia(sentenciaSig);
        codigo.genCod("label",sentenciaSig);
        instrucciones_p(instrucciones_pSig);
        break;
      //default:
        //producción vacía
    }
    // producción vacía
  }

  private void sentencia(String sentenciaSig) throws IOException,ErrorCompilador{
    String boolVddr,boolFls,sentencia1Sig;
    ArrayList<String> boolAtributos,parte_izquierdaAtributos,expAtributos;

    switch(tokenActual){
      case IF:
        eat(IF);
        eat(P1);

        boolVddr = Semantico.nuevaEtiqueta();
        boolFls = sentenciaSig;
        bool(boolVddr,boolFls);

        eat(P2);
        codigo.genCod("label",boolVddr);
        sentencia(sentenciaSig);
        int flag = condicional(sentenciaSig);

        if(flag == 1){
          boolFls=Semantico.nuevaEtiqueta();
          codigo.genCod("goto "+sentenciaSig);
          codigo.genCod("label",boolFls);
        }
        break;
      case IDENTIFIER:
        // este va a cambiar su nombre, revisarlo
        parte_izquierdaAtributos = parte_izquierda();
        eat(ASIGNACION);

        boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
        eat(PUNTOYCOMA);

        int parte_izquierdaTipo = Integer.parseInt(parte_izquierdaAtributos.get(0));
        String parte_izquierdaDir = parte_izquierdaAtributos.get(1);

        int boolTipo = Integer.parseInt(boolAtributos.get(0));
        String boolDir = boolAtributos.get(1);
        if(Semantico.equivalentes(parte_izquierdaTipo,boolTipo)){
          String d1 = Semantico.reducir(boolDir, boolTipo, parte_izquierdaTipo, codigo);
          codigo.genCod(parte_izquierdaDir+"="+d1);
        }else{
          error("Error semántico, tipos incompatibles");
        }
        break;
      case WHILE:
        eat(WHILE);
        eat(P1);

        sentencia1Sig = Semantico.nuevaEtiqueta();
        codigo.genCod("label",sentencia1Sig);

        boolVddr = Semantico.nuevaEtiqueta();
        boolFls = sentenciaSig;

        bool(boolVddr,boolFls);
        eat(P2);

        sentencia(sentencia1Sig);
        codigo.genCod("label",boolVddr);
        codigo.genCod("goto "+sentencia1Sig);

        break;
      case DO:
        eat(DO);

        sentencia1Sig = Semantico.nuevaEtiqueta();
        boolVddr = Semantico.nuevaEtiqueta();
        boolFls = sentenciaSig;

        codigo.genCod("label",boolVddr);
        sentencia(sentencia1Sig);
        codigo.genCod("label",sentencia1Sig);

        eat(WHILE);
        eat(P1);
        bool(boolVddr,boolFls);
        eat(P2);
        break;
      case BREAK:
        eat(BREAK);
        eat(PUNTOYCOMA);
        codigo.genCod("goto "+sentenciaSig);
        break;
      case L1:
        bloque(sentenciaSig);
        break;
      case SWITCH: 
        eat(SWITCH);
        eat(P1);

        boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
        String casosEtqPrueba = Semantico.nuevaEtiqueta();
        codigo.genCod("goto "+casosEtqPrueba);

        eat(P2);
        eat(L1);

        String casosSig = sentenciaSig;
        String casosId = boolAtributos.get(1);
        String casosPrueba = casos(casosSig, casosId);

        eat(L2);
        codigo.genCod(casosPrueba);
        break;
      case RETURN:
        eat(RETURN); 
        int returnTipo = return_p();
        listaRetorno.add(returnTipo);
        break;
      case PRINT:
        eat(PRINT);
        expAtributos = exp("","");
        codigo.genCod("print "+expAtributos.get(1));
        eat(PUNTOYCOMA);
      case SCAN:
        eat(SCAN);
        parte_izquierdaAtributos = parte_izquierda();
        codigo.genCod("print "+parte_izquierdaAtributos.get(1));
        eat(PUNTOYCOMA);
      //default:
        // producción vacía
    }
  }

  private int condicional(String condicionalSig) throws IOException,ErrorCompilador{
    if(tokenActual == ELSE){
      eat(ELSE);
      sentencia(condicionalSig);
      return 1;
    }
    return 0;
  }

  private int return_p() throws IOException,ErrorCompilador{
    if(tokenActual==PUNTOYCOMA){
      eat(PUNTOYCOMA);
      codigo.genCod("return");
      // número de void
      return 4;
    }else{
      ArrayList<String> expAtributos = exp("","");
      int expTipo = Integer.parseInt(expAtributos.get(0));
      String expDir = expAtributos.get(1);
      codigo.genCod("return "+expDir);
      eat(PUNTOYCOMA);
      return expTipo;
    }
  }

  private String casos(String casosSig, String casosId) throws IOException,ErrorCompilador{
    switch(tokenActual){
      case CASE:
        String casoPrueba = caso(casosSig,casosId);
        String casos1Prueba = casos(casosSig,casosId);
        return casoPrueba + casos1Prueba;
      case DEFAULT:
        String predeterminadoPrueba = predeterminado(casosSig);
        return predeterminadoPrueba;
    }
    return "";
  }

  private String caso(String casoSig, String casoId) throws IOException,ErrorCompilador{
    eat(CASE);
    String numero = lexema;
    if(tokenActual==INT_LIT){
      eat(INT_LIT);
    }else if(tokenActual==FLOAT_LIT){
      eat(FLOAT_LIT);
    }else{
      error("Error sintáctico, se esperaba un número");
    }
    eat(DOSPUNTOS);
    
    String casoInicio = Semantico.nuevaEtiqueta();
    String casoPrueba = "if "+casoId+"=="+numero+" goto "+casoInicio;
    codigo.genCod("label",casoInicio);
    instrucciones(casoSig);

    return casoPrueba;
  }

  private String predeterminado(String predeterminadoSig) throws IOException,ErrorCompilador{
    eat(DEFAULT);
    eat(DOSPUNTOS);
    String predeterminadoInicio = Semantico.nuevaEtiqueta();
    String instruccionesSig = predeterminadoSig;
    String predeterminadoPrueba = "goto "+predeterminadoInicio;
    codigo.genCod("label",predeterminadoInicio);
    instrucciones(instruccionesSig);
    return predeterminadoPrueba;
  }

  private ArrayList<String> parte_izquierda() throws IOException,ErrorCompilador{
    String parte_izquierda_pBase = lexema;
    eat(IDENTIFIER);
    ArrayList<String> parte_izquierda_pAtributos = parte_izquierda_p(parte_izquierda_pBase);

    int parte_izquierda_pTipo = Integer.parseInt(parte_izquierda_pAtributos.get(0));
    String parte_izquierda_pDir = parte_izquierda_pAtributos.get(1);

    ArrayList<String> parte_izquierdaAtributos = new ArrayList<String>();
    parte_izquierdaAtributos.add(Integer.toString(parte_izquierda_pTipo));
    parte_izquierdaAtributos.add(parte_izquierda_pDir);
    return parte_izquierdaAtributos;
  } 

  private ArrayList<String> parte_izquierda_p(String parte_izquierda_pBase) throws IOException,ErrorCompilador{
    ArrayList<String> parte_izquierda_pAtributos = new ArrayList<String>();

    if(tokenActual == C1){
      String localizacionBase = parte_izquierda_pBase;
      ArrayList<String> localizacionAtributos = localizacion(localizacionBase);

      int localizacionTipo = Integer.parseInt(localizacionAtributos.get(0));
      String localizacionDir = localizacionAtributos.get(1);

      parte_izquierda_pAtributos.add(Integer.toString(localizacionTipo));
      parte_izquierda_pAtributos.add(localizacionDir);
      return parte_izquierda_pAtributos;

    }else{

      if(PilaTS.peek().buscar(parte_izquierda_pBase)){
        parte_izquierda_pAtributos.add(Integer.toString(PilaTS.peek().getTipo(parte_izquierda_pBase)));
        parte_izquierda_pAtributos.add(parte_izquierda_pBase);
        return parte_izquierda_pAtributos;
      }else if(fondoTS.buscar(parte_izquierda_pBase)){
        parte_izquierda_pAtributos.add(Integer.toString(fondoTS.getTipo(parte_izquierda_pBase)));
        parte_izquierda_pAtributos.add(parte_izquierda_pBase);
        return parte_izquierda_pAtributos;
      }else{
        error("Error semántico, el id "+parte_izquierda_pBase+" no esta declarado");
      }
      return null;
    } 
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
  private ArrayList<String> bool(String boolVddr,String boolFls) throws IOException,ErrorCompilador{

    int boolTipo = 0; //int
    String boolDir="";

    String combVddr = boolVddr;
    String combFls = Semantico.nuevaEtiqueta();
    String bool_pVddr = boolVddr;
    String bool_pFls = boolFls;

    ArrayList<String> combAtributos = comb(combVddr,combFls);
    ArrayList<String> bool_pAtributos = bool_p(bool_pVddr,bool_pFls);

    int combTipo = Integer.parseInt(combAtributos.get(0));
    String combDir = combAtributos.get(1);

    int bool_pTipo = Integer.parseInt(bool_pAtributos.get(0));
    String bool_pDir = bool_pAtributos.get(1);

    ArrayList<String> atributosRet = new ArrayList<String>();

    if(Semantico.equivalentes(bool_pTipo,combTipo)){
      if(bool_pTipo!=-1){
        codigo.genCod("label",combFls);
      }else{
        boolTipo = combTipo;
        boolDir = combDir;
      }
      atributosRet.add(Integer.toString(boolTipo));
      atributosRet.add(boolDir);
      return atributosRet;
    }else{
      error("Error semántico, tipos incompatibles");
    }

    return null;
  }
  
  //bool_p → || comb bool_p | ε 
  private ArrayList<String> bool_p(String bool_pVddr,String bool_pFls) throws IOException,ErrorCompilador{

    int bool_pTipo = 0; //int
    String bool_pDir="";

    String combVddr = bool_pVddr;
    String combFls = Semantico.nuevaEtiqueta();
    String bool_p1Vddr = bool_pVddr;
    String bool_p1Fls = bool_pFls;

    ArrayList<String> atributosRet = new ArrayList<String>();

    if(tokenActual==OR){
      eat(OR);
      ArrayList<String> combAtributos = comb(combVddr,combFls);
      ArrayList<String> bool_p1Atributos = bool_p(bool_p1Vddr,bool_p1Fls);

      int combTipo = Integer.parseInt(combAtributos.get(0));
      String combDir = combAtributos.get(1);

      int bool_p1Tipo = Integer.parseInt(bool_p1Atributos.get(0));
      String bool_p1Dir = bool_p1Atributos.get(1);

      if(Semantico.equivalentes(bool_p1Tipo,combTipo)){
        if(bool_p1Tipo!=-1){
          codigo.genCod("label",combFls);
        }else{
          bool_pTipo = combTipo;
          bool_pDir = combDir;
        }
        atributosRet.add(Integer.toString(bool_pTipo));
        atributosRet.add(bool_pDir);

        return atributosRet;

      }else{
        error("Error semántico, tipos incompatibles");
      }
      return null;
    }else{
      bool_pTipo = -1;
      bool_pDir = "";
      atributosRet.add(Integer.toString(bool_pTipo));
      atributosRet.add(bool_pDir);

      return atributosRet;
    }  
  }
  
  //comb → igualdad comb_p 
  private ArrayList<String> comb(String combVddr, String combFls) throws IOException,ErrorCompilador{

    int combTipo = 0; //int
    String combDir="";

    String igualdadVddr = Semantico.nuevaEtiqueta();
    String igualdadFls = combFls;
    String comb_pVddr = combVddr;
    String comb_pFls = combFls;

    ArrayList<String> igualdadAtributos = igualdad(igualdadVddr,igualdadFls);
    ArrayList<String> comb_pAtributos = comb_p(comb_pVddr,comb_pFls);

    int igualdadTipo = Integer.parseInt(igualdadAtributos.get(0));
    String igualdadDir = igualdadAtributos.get(1);

    int comb_pTipo = Integer.parseInt(comb_pAtributos.get(0));
    String comb_pDir = comb_pAtributos.get(1);

    ArrayList<String> atributosRet = new ArrayList<String>();

    if(Semantico.equivalentes(comb_pTipo,igualdadTipo)){
      if(comb_pTipo!=-1){
        codigo.genCod("label",igualdadFls);
      }else{
        combTipo=igualdadTipo;
        combDir=igualdadDir;
      }
      atributosRet.add(Integer.toString(combTipo));
      atributosRet.add(combDir);
      return atributosRet;
    }else{
      error("Error semántico, tipos incompatibles");
    }
    return null;
  }
  
  //comb_p → && igualdad comb_p | ε
  private ArrayList<String> comb_p(String comb_pVddr, String comb_pFls) throws IOException,ErrorCompilador{
    int comb_pTipo = 0; //int
    String comb_pDir="";

    String igualdadVddr = Semantico.nuevaEtiqueta();
    String igualdadFls = comb_pFls;
    String comb_p1Vddr = comb_pVddr;
    String comb_p1Fls = comb_pFls;

    ArrayList<String> atributosRet = new ArrayList<String>();

    if(tokenActual==AND){
      eat(AND);
      ArrayList<String> igualdadAtributos = igualdad(igualdadVddr,igualdadFls);
      ArrayList<String> comb_p1Atributos = comb_p(comb_p1Vddr,comb_p1Fls);

      int igualdadTipo = Integer.parseInt(igualdadAtributos.get(0));
      String igualdadDir = igualdadAtributos.get(1);

      int comb_p1Tipo = Integer.parseInt(comb_p1Atributos.get(0));
      String comb_p1Dir = comb_p1Atributos.get(1);

      if(Semantico.equivalentes(comb_pTipo,igualdadTipo)){
        if(comb_p1Tipo!=-1){
          codigo.genCod("label",igualdadFls);
        }else{
          comb_pTipo=igualdadTipo;
          comb_pDir=igualdadDir;
        }
        atributosRet.add(Integer.toString(comb_pTipo));
        atributosRet.add(comb_pDir);

        return atributosRet;
      }else{
        error("Error semántico, tipos incompatibles");
      }
      return null;
    }else{
      comb_pTipo=-1;
      comb_pDir="";
      atributosRet.add(Integer.toString(comb_pTipo));
      atributosRet.add(comb_pDir);

      return atributosRet;
    }
  }
  
  //igualdad → rel igualdad_p
  private ArrayList<String> igualdad(String igualdadVddr, String igualdadFls) throws IOException,ErrorCompilador{
    int igualdadTipo = 0; //int
    String igualdadDir="";

    String relVddr = igualdadVddr;
    String relFls = igualdadFls;
    String igualdad_pVddr = igualdadVddr;
    String igualdad_pFls = igualdadFls;

    ArrayList<String> relAtributos = rel(relVddr,relFls);
    ArrayList<String> igualdad_pAtributos = igualdad_p(igualdad_pVddr,igualdad_pFls);

    int relTipo = Integer.parseInt(relAtributos.get(0));
    String relDir = relAtributos.get(1);

    int igualdad_pTipo = Integer.parseInt(igualdad_pAtributos.get(0));
    String igualdad_pDir = igualdad_pAtributos.get(1);
    String igualdad_pOp = igualdad_pAtributos.get(2);

    ArrayList<String> atributosRet = new ArrayList<String>();

    int tipoTemp;
    String d1,d2;

    if(Semantico.equivalentes(igualdad_pTipo,relTipo)){
      if(igualdad_pTipo!=-1){
        igualdadDir = Semantico.nuevaTemporal();
        tipoTemp = Semantico.maximo(igualdad_pTipo, relTipo); 
        d1 = Semantico.ampliar(igualdad_pDir, igualdad_pTipo, tipoTemp,codigo);
        d2 = Semantico.ampliar(relDir, relTipo, tipoTemp,codigo);
        codigo.genCod(new Cuadrupla(igualdad_pOp,d1,d2,igualdadDir));
        codigo.genCod("if "+igualdadDir+" goto "+relVddr); 
        codigo.genCod("goto "+relFls);
      }else{
        igualdadTipo=relTipo;
        igualdadDir=relDir;
      }
      atributosRet.add(Integer.toString(igualdadTipo));
      atributosRet.add(igualdadDir);
      return atributosRet ;
    }else{
      error("Error semántico, tipos incompatibles");
    }
    return null;
  }
  
  //igualdad_p → == rel igualdad_p | != rel igualdad_p | ε
  private ArrayList<String> igualdad_p(String igualdad_pVddr, String igualdad_pFls) throws IOException,ErrorCompilador{
    int igualdad_pTipo = 0; //int
    String igualdad_pDir="";
    String igualdad_pOp="";

    String relVddr = igualdad_pVddr;
    String relFls = igualdad_pFls;
    String igualdad_p1Vddr = igualdad_pVddr;
    String igualdad_p1Fls = igualdad_pFls;

    ArrayList<String> atributosRet = new ArrayList<String>();
    int tipoTemp;
    String d1,d2;

    if(tokenActual==IGUAL){
      eat(IGUAL);
      igualdad_pOp="==";
    }else if(tokenActual==DESIGUAL){
      eat(DESIGUAL);
      igualdad_pOp="!=";
    }else{
      igualdad_pTipo=-1;
      igualdad_pDir="";
      igualdad_pOp="";
      atributosRet.add(Integer.toString(igualdad_pTipo));
      atributosRet.add(igualdad_pDir);
      atributosRet.add(igualdad_pOp);
      return atributosRet;
    }

    ArrayList<String> relAtributos = rel(relVddr,relFls);
    ArrayList<String> igualdad_p1Atributos = igualdad_p(igualdad_p1Vddr,igualdad_p1Fls);

    int relTipo = Integer.parseInt(relAtributos.get(0));
    String relDir = relAtributos.get(1);

    int igualdad_p1Tipo = Integer.parseInt(igualdad_p1Atributos.get(0));
    String igualdad_p1Dir = igualdad_p1Atributos.get(1);
    String igualdad_p1Op = igualdad_p1Atributos.get(2);

    if(Semantico.equivalentes(igualdad_p1Tipo,relTipo)){
      if(igualdad_p1Tipo!=-1){
        igualdad_pDir = Semantico.nuevaTemporal();
        tipoTemp = Semantico.maximo(igualdad_p1Tipo, relTipo); 
        d1 = Semantico.ampliar(igualdad_p1Dir, igualdad_p1Tipo, tipoTemp, codigo);
        d2 = Semantico.ampliar(relDir, relTipo, tipoTemp, codigo);
        codigo.genCod(new Cuadrupla(igualdad_p1Op,d1,d2,igualdad_pDir));
        codigo.genCod("if "+igualdad_pDir+" goto "+relVddr); 
        codigo.genCod("goto "+relFls);
      }else{
        igualdad_pTipo=relTipo;
        igualdad_pDir=relDir;
      }
      atributosRet.add(Integer.toString(igualdad_pTipo));
      atributosRet.add(igualdad_pDir);
      atributosRet.add(igualdad_pOp);
      return atributosRet;
    }else{
      error("Error semántico, tipos incompatibles");
    }
    return null;
  }
  
  //rel → exp rel_p
  private ArrayList<String> rel(String relVddr,String relFls) throws IOException,ErrorCompilador{

    String expVddr = relVddr;
    String expFls = relFls;
    String rel_pVddr = relVddr;
    String rel_pFls = relFls;

    int relTipo=0; //int
    String relDir="";
    ArrayList<String> atributosRet = new ArrayList<String>();

    ArrayList<String> expAtributos = exp(expVddr,expFls);
    ArrayList<String> rel_pAtributos = rel_p(rel_pVddr,rel_pFls);

    int expTipo = Integer.parseInt(expAtributos.get(0));
    String expDir = expAtributos.get(1);

    int rel_pTipo = Integer.parseInt(rel_pAtributos.get(0));
    String rel_pDir = rel_pAtributos.get(1);
    String rel_pOp = rel_pAtributos.get(2);

    String d1, d2;
    int tipoTemp;
    if(Semantico.equivalentes(rel_pTipo,expTipo)){
      if(rel_pTipo!=-1){
        relDir = Semantico.nuevaTemporal(); 
        tipoTemp = Semantico.maximo(rel_pTipo, expTipo); 
        d1 = Semantico.ampliar(rel_pDir, rel_pTipo, tipoTemp, codigo); 
        d2 = Semantico.ampliar(expDir, expTipo, tipoTemp, codigo);
        codigo.genCod(new Cuadrupla(rel_pOp,d1,d2,relDir));
        codigo.genCod("if "+relDir+" goto "+rel_pVddr); 
        codigo.genCod("goto "+rel_pFls);
      }else{
        relTipo = expTipo; 
        relDir = expDir;
      }
      atributosRet.add(Integer.toString(relTipo));
      atributosRet.add(relDir);
      return atributosRet;
    }else{
      error("Error semántico, tipos incopatibles");
    }
    return null;
  }
  
  //rel_p → < exp | <= exp | >= exp | > exp | ε
  private ArrayList<String> rel_p(String rel_pVddr,String rel_pFls) throws IOException,ErrorCompilador{

    String expVddr = rel_pVddr;
    String expFls = rel_pFls;

    ArrayList<String> atributosRet = new ArrayList<String>();
    String rel_pOp;
    if(tokenActual==LT){
      eat(LT);
      rel_pOp = "<";
    }else if(tokenActual==LET){
      eat(LET);
      rel_pOp = "<=";
    }else if(tokenActual==MET){
      eat(MET);
      rel_pOp = ">";
    }else if(tokenActual==MT){
      eat(MT);
      rel_pOp = ">=";
    }else{
      atributosRet.add("-1");
      atributosRet.add("");
      atributosRet.add("");
      return atributosRet;
    }

    atributosRet = exp(expVddr,expFls);
    atributosRet.add(rel_pOp);
    return atributosRet;
  }
  
  //exp → term exp_p
  private ArrayList<String> exp(String expVddr, String expFls) throws IOException,ErrorCompilador{

    ArrayList<String> atributosRet = new ArrayList<String>();

    ArrayList<String> termAtributos = term();
    ArrayList<String> exp_pAtributos = exp_p();

    int termTipo = Integer.parseInt(termAtributos.get(0));
    String termDir = termAtributos.get(1);

    int exp_pTipo = Integer.parseInt(exp_pAtributos.get(0));
    String exp_pDir = exp_pAtributos.get(1);
    String exp_pOp = exp_pAtributos.get(2);

    if(Semantico.equivalentes(exp_pTipo, termTipo)){
      int expTipo;
      String expDir, d1, d2;

      if(exp_pTipo != -1){
        expTipo = Semantico.maximo(exp_pTipo, termTipo);
        expDir = Semantico.nuevaTemporal();
        d1 = Semantico.ampliar(exp_pDir, exp_pTipo, expTipo,codigo);
        d2 = Semantico.ampliar(termDir, termTipo, expTipo,codigo);
        codigo.genCod(new Cuadrupla(exp_pOp, d1, d2, expDir));
      }else{
        expTipo = termTipo;
        expDir = termDir;
      }

      atributosRet.add(Integer.toString(expTipo));
      atributosRet.add(expDir);
      return atributosRet;
    }else{
      error("Error semántico tipos incompatibles");
    }
    return null;
  }
  
  //exp_p → + term exp_p | - term exp_p | ε
  private ArrayList<String> exp_p() throws IOException,ErrorCompilador{
    ArrayList<String> atributosRet = new ArrayList<String>();
    String exp_pOp;
    if(tokenActual==SUMA){
      eat(SUMA);
      exp_pOp = "+";
    }else if(tokenActual==RESTA){
      eat(RESTA);
      exp_pOp = "-";
    }else{
      atributosRet.add("-1");
      atributosRet.add("");
      atributosRet.add("");
      return atributosRet;
    }

    ArrayList<String> termAtributos = term();
    ArrayList<String> exp_p1Atributos = exp_p();
    
    int termTipo = Integer.parseInt(termAtributos.get(0));
    String termDir = termAtributos.get(1);

    int exp_p1Tipo = Integer.parseInt(exp_p1Atributos.get(0));
    String exp_p1Dir = exp_p1Atributos.get(1);
    String exp_p1Op = exp_p1Atributos.get(2);

    if(Semantico.equivalentes(exp_p1Tipo, termTipo)){
      int exp_pTipo;
      String exp_pDir, d1, d2;

      if(exp_p1Tipo != -1){
        exp_pTipo = Semantico.maximo(exp_p1Tipo, termTipo);
        exp_pDir = Semantico.nuevaTemporal();
        d1 = Semantico.ampliar(exp_p1Dir, exp_p1Tipo, exp_pTipo,codigo);
        d2 = Semantico.ampliar(termDir, termTipo, exp_pTipo,codigo);
        codigo.genCod(new Cuadrupla(exp_p1Op, d1, d2, exp_pDir));
      }else{
        exp_pTipo = termTipo;
        exp_pDir = termDir;
      }
      atributosRet.add(Integer.toString(exp_pTipo));
      atributosRet.add(exp_pDir);
      atributosRet.add(exp_pOp);
      return atributosRet;

    }else{
      error("Error semántico tipos incompatibles");
    }
    return null;
  }
  
  /*
    LAZARO *************************************************

  */
  //
  private ArrayList<String> term() throws IOException,ErrorCompilador{
    ArrayList<String> unarAtributos = unario();
    ArrayList<String> term_pAtributos = term_p();
    ArrayList<String> atributosRet = new ArrayList<String>();

    int term_pTipo = Integer.parseInt(term_pAtributos.get(0));
    String term_pDir = term_pAtributos.get(1);
    String term_pOp = term_pAtributos.get(2);

    int unarTipo = Integer.parseInt(unarAtributos.get(0));
    String unarDir = unarAtributos.get(1);
    if(Semantico.equivalentes(term_pTipo, unarTipo)){
      int termTipo;
      String termDir, d1, d2;
      if (term_pTipo != -1){
        if(term_pOp != "%"){
          termTipo = Semantico.maximo(term_pTipo, unarTipo);
          termDir = Semantico.nuevaTemporal();
          d1 = Semantico.ampliar(term_pDir, term_pTipo, termTipo,codigo);
          d2 = Semantico.ampliar(unarDir, unarTipo, termTipo,codigo);
          codigo.genCod(new Cuadrupla(term_pOp, d1,d2, termDir));
        } else{
          termTipo = 0; //int
          termDir = Semantico.nuevaTemporal();
          codigo.genCod(new Cuadrupla("%",unarDir, term_pDir, termDir));
        }
      }else{
        termDir = unarDir;
        termTipo = unarTipo;
      }
      //AÑADIR ATRIBUTOS A RETORNAR
      atributosRet.add(Integer.toString(termTipo));
      atributosRet.add(termDir);
      return atributosRet;
    }else{
      error("Error semántico, tipos incompatibles");
    }
    return null;
  }

  private ArrayList<String> term_p() throws IOException,ErrorCompilador{
    String term_pOp;
    ArrayList<String> atributosRet = new ArrayList<String>();

    switch(tokenActual){
      case MULTI:
        eat(MULTI);
        term_pOp = "*";
        break;
      case DIVISION:
        eat(DIVISION);
        term_pOp = "/";
        break;
      case MODULO:
        eat(MODULO);
        term_pOp = "%";
        break;
      default:
        atributosRet.add("-1");
        atributosRet.add("");
        atributosRet.add("");
        return atributosRet;
    }

    ArrayList<String> unarioAtributos = unario();
    ArrayList<String> term_p1Atributos = term_p();

    int term_p1Tipo = Integer.parseInt(term_p1Atributos.get(0));
    String term_p1Dir = term_p1Atributos.get(1);
    String term_p1Op = term_p1Atributos.get(2);

    int unarioTipo = Integer.parseInt(unarioAtributos.get(0));
    String unarioDir = unarioAtributos.get(1);

    if(Semantico.equivalentes(term_p1Tipo, unarioTipo)){
      int term_pTipo;
      String term_pDir, d1, d2;

      if (term_p1Tipo != -1){
        if(term_p1Op != "%"){
          term_pTipo = Semantico.maximo(term_p1Tipo, unarioTipo);
          term_pDir = Semantico.nuevaTemporal();
          d1 = Semantico.ampliar(term_p1Dir, term_p1Tipo, term_pTipo,codigo);
          d2 = Semantico.ampliar(unarioDir, unarioTipo, term_pTipo,codigo);
          codigo.genCod(new Cuadrupla(term_p1Op, d1,d2, term_pDir));
        }else{
          term_pTipo = 0; //int
          term_pDir = Semantico.nuevaTemporal();
          codigo.genCod(new Cuadrupla("%",unarioDir, term_p1Dir, term_pDir));
        }
      }else{
        term_pDir = unarioDir;
        term_pTipo = unarioTipo;
      }

      atributosRet.add(Integer.toString(term_pTipo));
      atributosRet.add(term_pDir);
      atributosRet.add(term_pOp);
      return atributosRet;
    }else{
      System.out.println("Error semántico tipos incompatibles");
    }
    return null;
  }

  private ArrayList<String> unario() throws IOException,ErrorCompilador{

    ArrayList<String> atributosRet = new ArrayList<String>();

    int unarioTipo;
    String unarioDir,unarioOp;

    switch(tokenActual){
      case NEGACION:
        eat(NEGACION);
        unarioOp="!";
        
        break;
      case RESTA:
        eat(RESTA);
        unarioOp="-";
        break;

      default:

        ArrayList<String> factorAtributos = factor();
        int factorTipo = Integer.parseInt(factorAtributos.get(0));
        String factorDir = factorAtributos.get(1);

        unarioDir = factorDir;
        unarioTipo = factorTipo;

        atributosRet.add(Integer.toString(unarioTipo));
        atributosRet.add(unarioDir);
        atributosRet.add("");
        return atributosRet;   
    }

    unarioDir = Semantico.nuevaTemporal();
    ArrayList<String> unario1Atributos = unario();
    int unario1Tipo = Integer.parseInt(unario1Atributos.get(0));
    String unario1Dir = unario1Atributos.get(1);
    String unario1Op = unario1Atributos.get(2);

    codigo.genCod(new Cuadrupla(unario1Op, unario1Dir, "", unarioDir));

    atributosRet.add(Integer.toString(unario1Tipo));
    atributosRet.add(unarioDir);
    atributosRet.add(unarioOp);
    return atributosRet;
  }

  private ArrayList<String> factor() throws IOException,ErrorCompilador{
    ArrayList<String> atributosRet = new ArrayList<String>();
    switch(tokenActual){
      //(bool)
      case P1:
        eat(P1);
        atributosRet=bool("","");
        eat(P2);
        return atributosRet;
      // número
      case INT_LIT:
        atributosRet.add("0");
        atributosRet.add(lexema);
        eat(INT_LIT);
        return atributosRet;
      case FLOAT_LIT:
        atributosRet.add("1");
        atributosRet.add(lexema);
        eat(FLOAT_LIT);
        return atributosRet;
      // cadena
      case STRING_LIT:
        tablaCadenas.agregar(lexema);
        eat(STRING_LIT); 
        atributosRet.add("5");
        atributosRet.add("SP"+Integer.toString(tablaCadenas.getUltimaPos()));
        return atributosRet;
      //true
      case TRUE:
        eat(TRUE);
        atributosRet.add("0");
        atributosRet.add("true");
        return atributosRet;
      //false
      case FALSE:
        eat(FALSE);
        atributosRet.add("0");
        atributosRet.add("false");
        return atributosRet;
      // id
      case IDENTIFIER:
        String factor_pBase = lexema;
        eat(IDENTIFIER);
        atributosRet = factor_p(factor_pBase);
        return atributosRet;
      default:
        error("Error de sintaxis");
        return null;
    }
  }

  private ArrayList<String> factor_p(String factor_pBase) throws IOException,ErrorCompilador{
    ArrayList<String> atributosRet = new ArrayList<String>();
    int factor_pTipo;
    String factor_pDir;
    switch(tokenActual){
      case C1:
        String localizacionBase = factor_pBase;
        ArrayList<String> localizacionAtributos = localizacion(localizacionBase);

        int localizacionTipo = Integer.parseInt(localizacionAtributos.get(0));
        String localizacionDir = localizacionAtributos.get(1);

        factor_pTipo = localizacionTipo;
        factor_pDir = Semantico.nuevaTemporal();

        atributosRet.add(Integer.toString(factor_pTipo));
        atributosRet.add(factor_pDir);

        codigo.genCod(factor_pDir+" = "+ factor_pBase + " [" + localizacionDir + "]");
        return atributosRet;

      case P1:
        eat(P1);
        ArrayList<Integer> parametrosLista = parametros();
        eat(P1);

        if(fondoTS.buscar(factor_pBase)){
          if(fondoTS.getVar(factor_pBase).equals("func")){
            if(Semantico.equivalentesListas(parametrosLista,fondoTS.getArgs(factor_pBase))){
              factor_pTipo = PilaTS.peek().getTipo(factor_pBase);
              factor_pDir = Semantico.nuevaTemporal();
              codigo.genCod(factor_pDir+" = call "+ factor_pBase + ","+Integer.toString(parametrosLista.size()));
              atributosRet.add(Integer.toString(factor_pTipo));
              atributosRet.add(factor_pDir);
              return atributosRet;
            }else{
              error("Error semántico, el número o tipo de parámetros no coincide");
            }
          }else{
            error("Error semántico, el id no es una función");
          }
        }else{
          error("Error semántico, el id no se encuentra declarado");
        }
        return null;

      default:
        factor_pDir = factor_pBase;
        if(PilaTS.peek().buscar(factor_pDir)){
          factor_pTipo = PilaTS.peek().getTipo(factor_pDir);
        }else{
          factor_pTipo = fondoTS.getTipo(factor_pDir);
        }
        atributosRet.add(Integer.toString(factor_pTipo));
        atributosRet.add(factor_pDir);
        return atributosRet;
    }
  }

  private ArrayList<Integer> parametros() throws IOException,ErrorCompilador{
    switch(tokenActual){
      case NEGACION:
      case RESTA:
      case IDENTIFIER:
      case P1:
      case INT_LIT:
      case FLOAT_LIT:
      case STRING_LIT:
      case TRUE:
      case FALSE:
        ArrayList<Integer> parametrosLista = lista_param();
        return parametrosLista;
      default:
        return new ArrayList<Integer>();
    } 
  }

  private ArrayList<Integer> lista_param() throws IOException,ErrorCompilador{
    ArrayList<String> boolAtributos = bool("","");
    ArrayList<Integer> lista_param_pAtributos = lista_param_p();
    ArrayList<Integer> lista_param = lista_param_pAtributos;
    lista_param.add(Integer.parseInt(boolAtributos.get(0)));
    return Semantico.invertir(lista_param); 
  }

  private ArrayList<Integer> lista_param_p() throws IOException,ErrorCompilador{
    if(tokenActual==COMA){
      eat(COMA);
      ArrayList<String> boolAtributos = bool("","");
      ArrayList<Integer> lista_param_p1Atributos = lista_param_p();
      ArrayList<Integer> lista_param = lista_param_p1Atributos;
      lista_param.add(Integer.parseInt(boolAtributos.get(0)));
      return lista_param;
    }
    // producción vacía
    ArrayList<Integer> lista_param = new ArrayList<Integer>();
    return lista_param;
  }

  private ArrayList<String> localizacion(String localizacionBase) throws IOException,ErrorCompilador{
    if(tokenActual==C1){
      eat(C1);

      ArrayList<String> boolAtributos = bool("","");
      int boolTipo = Integer.parseInt(boolAtributos.get(0));
      String boolDir = boolAtributos.get(1);

      eat(C2);

      int tipoTemp, localizacion_pTipo, localizacion_pTipoS, localizacion_pTam;
      String localizacion_pDir, localizacion_pDirS;
      ArrayList<String> localizacion_pAtributos;

      if(PilaTS.peek().buscar(localizacionBase)){

        tipoTemp = PilaTS.peek().getTipo(localizacionBase);
        localizacion_pTipo = PilaTT.peek().getTipoBase(tipoTemp);
        localizacion_pDir = Semantico.nuevaTemporal();
        localizacion_pTam = PilaTT.peek().getTam(localizacion_pTipo);

        localizacion_pAtributos = localizacion_p(localizacion_pTipo, localizacion_pDir);
        localizacion_pTipoS = Integer.parseInt(localizacion_pAtributos.get(0));
        localizacion_pDirS = localizacion_pAtributos.get(1); 

        if(boolTipo == 0){
          if(PilaTT.peek().getNombre(tipoTemp).equals("array")){

            String localizacionDir = localizacion_pDirS;
            int localizacionTipo = localizacion_pTipoS;

            codigo.genCod(localizacionDir +" = "+ boolDir +"*"+ localizacion_pTam);

            ArrayList<String> atributosRet = new ArrayList<String>();
            atributosRet.add(Integer.toString(localizacionTipo));
            atributosRet.add(localizacionDir);
            return atributosRet;

          }else{
            error("Error semántico, el id no es un arreglo");
          }
        }else{
          error("Error semántico, el indice de un arreglo debe ser entero");
        }
      }else if(fondoTS.buscar(localizacionBase)){

        tipoTemp = fondoTS.getTipo(localizacionBase);
        localizacion_pTipo = fondoTT.getTipoBase(tipoTemp);
        localizacion_pDir = Semantico.nuevaTemporal();
        localizacion_pTam = fondoTT.getTam(localizacion_pTipo);

        localizacion_pAtributos = localizacion_p(localizacion_pTipo, localizacion_pDir);
        localizacion_pTipoS = Integer.parseInt(localizacion_pAtributos.get(0));
        localizacion_pDirS = localizacion_pAtributos.get(1); 

        if(boolTipo == 0){
          if(fondoTT.getNombre(tipoTemp).equals("array")){

            String localizacionDir = localizacion_pDirS;
            int localizacionTipo = localizacion_pTipoS;

            codigo.genCod(localizacionDir +" = "+ boolDir +"*"+ localizacion_pTam);

            ArrayList<String> atributosRet = new ArrayList<String>();
            atributosRet.add(Integer.toString(localizacionTipo));
            atributosRet.add(localizacionDir);
            return atributosRet;

          }else{
            error("Error semántico, el id no es un arreglo");
          }
        }else{
          error("Error semántico, el indice de un arreglo debe ser entero");
        }
      }else{
        error("Error semántico, el id "+localizacionBase+" no esta declarado");
      }
    }else{
      error("Error de sintaxis, se esperaba un identificador 1");
    }
    return null;
  }

  private ArrayList<String> localizacion_p(int localizacion_pTipo, String localizacion_pDir) throws IOException,ErrorCompilador{
    if(tokenActual==C1){
      eat(C1);

      ArrayList<String> boolAtributos = bool(Semantico.nuevaTemporal(),Semantico.nuevaTemporal());
      int boolTipo = Integer.parseInt(boolAtributos.get(0));
      String boolDir = boolAtributos.get(1);

      eat(C2);

      int localizacion_p1Tipo = PilaTT.peek().getTipoBase(localizacion_pTipo);
      String localizacion_p1Dir = Semantico.nuevaTemporal();
      int localizacion_p1Tam = PilaTT.peek().getTam(localizacion_p1Tipo); 

      String dirTemp = Semantico.nuevaTemporal();

      ArrayList<String> localizacion_p1Atributos = localizacion_p(localizacion_p1Tipo, localizacion_p1Dir);
      int localizacion_p1TipoS = Integer.parseInt(localizacion_p1Atributos.get(0));
      String localizacion_p1DirS = localizacion_p1Atributos.get(1);

      if(boolTipo == 0){
        if(PilaTT.peek().getNombre(localizacion_pTipo).equals("array")){

          localizacion_pDir = localizacion_p1DirS;
          localizacion_pTipo = localizacion_p1TipoS;

          codigo.genCod(dirTemp+" = "+boolDir+"*"+localizacion_p1Tam);                
          codigo.genCod(localizacion_p1Dir+" = "+localizacion_pDir+"+"+dirTemp);

          ArrayList<String> atributosRet = new ArrayList<String>();
          atributosRet.add(Integer.toString(localizacion_pTipo));
          atributosRet.add(localizacion_pDir);
          return atributosRet;

        }else{
          error("Error semántico, el id no es un arreglo");
        }
      }else{
        error("Error semántico, el indice de un arreglo debe ser entero");
      }return null;
    // producción vacía
    }else{
      String localizacion_pDirS=localizacion_pDir;
      int localizacion_pTipoS=localizacion_pTipo;

      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(Integer.toString(localizacion_pTipoS));
      atributosRet.add(localizacion_pDirS);
      return atributosRet;
    }
  }
  // MÉTODOS DE AYUDA

  // Método para avanzar de token
  public void eat(int i) throws IOException,ErrorCompilador{
    if(tokenActual==i){
      tokenActual = analizadorLexico.yylex();
      lexema = analizadorLexico.yytext();
      System.out.println(lexema);
      // se revisa que el nuevo token sea un número correcto
      if(tokenActual==-1){
        throw new ErrorCompilador("Error léxico, línea "+analizadorLexico.getYyline());
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
  private void error(String mensaje) throws ErrorCompilador{
    throw new ErrorCompilador(mensaje+", línea "+(analizadorLexico.getYyline()+1)+"\n"+analizadorLexico.linea);
  }

}
