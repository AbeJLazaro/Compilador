/*
Autores:      Camacho Morales Gerardo Iván
              Nicolas Marin Brian Geovanny
              Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1.6
Fecha:        8 de febrero de 2021
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
  private static final int CHAR_LIT=48;
  private static final int TRUE=41;
  private static final int FALSE=42;
  private static final int INT_LIT=43;
  private static final int FLOAT_LIT=44;
  // otros atributos necesarios
  private Lexer analizadorLexico;
  private int tokenActual;
  private String lexema;

  private int dir;
  private boolean arreglo;

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

  private Stack<String> pilaBreak;


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

    pilaBreak = new Stack<String>(); 

    arreglo = false;
  }

  // método que inicia
  public void parse() throws IOException,ErrorCompilador{
    programa();
    System.out.println("Cadena aceptada");
    PilaTT.peek().printTT();
    PilaTS.peek().printTS();
    tablaCadenas.printTC();
    System.out.println(codigo.getCodigo());
  }

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
        return 3;
      case DOUBLE:
        eat(DOUBLE);
        return 2;
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
        eat(IDENTIFIER);
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
      eat(IDENTIFIER);
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
      instrucciones();
      eat(L2);
    }else{
      error("Error sintáctico, se esperaba {");
    }
  }
  /*
    BRIAN *************************************************
  */
  private void instrucciones() throws IOException,ErrorCompilador{
    String sentenciaSig = Semantico.nuevaEtiqueta();
    sentencia(sentenciaSig);
    codigo.genCod("label",sentenciaSig);
    instrucciones_p();
  }

  private void instrucciones_p() throws IOException,ErrorCompilador{
    switch(tokenActual){
      case IF:
      case IDENTIFIER:
      case WHILE:
      case DO:
      case BREAK:
      case L1:
      case SWITCH:
      case RETURN:
      case PRINT:
      case SCAN:
        String sentenciaSig = Semantico.nuevaEtiqueta();
        sentencia(sentenciaSig);
        codigo.genCod("label",sentenciaSig);
        instrucciones_p();
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
        boolFls = Semantico.nuevoIndice();
        bool(boolVddr,boolFls);

        eat(P2);

        sentencia1Sig = sentenciaSig;
        codigo.genCod("label",boolVddr);

        sentencia(sentencia1Sig);
        
        String condicionalSig = sentenciaSig;
        ArrayList<String> condicionalListaIndices = Semantico.nuevaListaIndices();
        condicionalListaIndices.add(boolFls);

        condicional(sentenciaSig,condicionalListaIndices);

        break;
      case IDENTIFIER:
        // este va a cambiar su nombre, revisarlo
        parte_izquierdaAtributos = parte_izquierda();
        int parte_izquierdaTipo = Integer.parseInt(parte_izquierdaAtributos.get(0));
        String parte_izquierdaDir = parte_izquierdaAtributos.get(1);

        eat(ASIGNACION);

        boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
        eat(PUNTOYCOMA);

        int boolTipo = Integer.parseInt(boolAtributos.get(0));
        String boolDir = boolAtributos.get(1);
        if(Semantico.equivalentes(parte_izquierdaTipo,boolTipo)){
          String d1 = Semantico.reducir(boolDir, boolTipo, parte_izquierdaTipo, codigo);
          if(arreglo){
            codigo.genCod(new Cuadrupla("",d1,"",parte_izquierdaAtributos.get(2)+"["+parte_izquierdaDir+"]"));
          }else{
            codigo.genCod(new Cuadrupla("",d1,"",parte_izquierdaDir));
          }
        }else{
          error("Error semántico, tipos incompatibles");
        }
        break;
      case WHILE:
        eat(WHILE);
        eat(P1);
        pilaBreak.push(sentenciaSig);

        sentencia1Sig = Semantico.nuevaEtiqueta();
        codigo.genCod("label",sentencia1Sig);

        boolVddr = Semantico.nuevaEtiqueta();
        boolFls = sentenciaSig;

        bool(boolVddr,boolFls);
        eat(P2);

        codigo.genCod("label",boolVddr);
        sentencia(sentencia1Sig);
        
        codigo.genCod("goto",sentencia1Sig);
        pilaBreak.pop();
        break;
      case DO:
        eat(DO);
        pilaBreak.push(sentenciaSig);
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
        pilaBreak.pop();
        break;
      case BREAK:
        eat(BREAK);
        eat(PUNTOYCOMA);
        codigo.genCod("goto",pilaBreak.peek());
        break;
      case L1:
        bloque(sentenciaSig);
        break;
      case SWITCH: 
        eat(SWITCH);
        eat(P1);

        boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
        String casosEtqPrueba = Semantico.nuevaEtiqueta();
        codigo.genCod("goto",casosEtqPrueba);

        eat(P2);
        eat(L1);

        String casosSig = sentenciaSig;
        pilaBreak.push(casosSig);
        String casosId = boolAtributos.get(1);
        String casosPrueba = casos(casosSig, casosId);

        eat(L2);
        codigo.genCod("label",casosEtqPrueba);
        codigo.genCod(casosPrueba);
        pilaBreak.pop();
        break;
      case RETURN:
        eat(RETURN); 
        int returnTipo = return_p();
        listaRetorno.add(returnTipo);
        break;
      case PRINT:
        eat(PRINT);
        expAtributos = exp("","");
        codigo.genCod("print",expAtributos.get(1));
        eat(PUNTOYCOMA);
        break;
      case SCAN:
        eat(SCAN);
        parte_izquierdaAtributos = parte_izquierda();
        codigo.genCod("scan",parte_izquierdaAtributos.get(1));
        eat(PUNTOYCOMA);
        break;
      //default:
        // producción vacía
    }
  }

  private void condicional(String condicionalSig,ArrayList<String> condicionalListaIndices) throws IOException,ErrorCompilador{
    if(tokenActual == ELSE){
      eat(ELSE);

      String sentenciaSig = condicionalSig;
      codigo.genCod("goto", sentenciaSig);
      codigo.genCod("label", condicionalListaIndices.get(0));

      sentencia(sentenciaSig);

      Semantico.reemplazarIndices(condicionalListaIndices, Semantico.nuevaEtiqueta(), codigo);
    }else{
      Semantico.reemplazarIndices(condicionalListaIndices, condicionalSig, codigo);
    }
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
      codigo.genCod("return",expDir);
      eat(PUNTOYCOMA);
      return expTipo;
    }
  }

  private String casos(String casosSig, String casosId) throws IOException,ErrorCompilador{
    switch(tokenActual){
      case CASE:
        String casoSig = casosSig;
        String casoPrueba = caso(casoSig,casosId);
        String casos1Sig = casosSig;
        String casos1Prueba = casos(casos1Sig,casosId);
        return casoPrueba + casos1Prueba;
      case DEFAULT:
        String predeterminadoSig = casosSig;
        String predeterminadoPrueba = predeterminado(predeterminadoSig);
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

    String instruccionesSig = casoSig;

    instrucciones();

    return casoPrueba+"\n";
  }

  private String predeterminado(String predeterminadoSig) throws IOException,ErrorCompilador{
    eat(DEFAULT);
    eat(DOSPUNTOS);
    String predeterminadoInicio = Semantico.nuevaEtiqueta();
    String instruccionesSig = predeterminadoSig;
    String predeterminadoPrueba = "goto "+predeterminadoInicio;
    codigo.genCod("label",predeterminadoInicio);
    instrucciones();
    return predeterminadoPrueba+"\n";
  }

  private ArrayList<String> parte_izquierda() throws IOException,ErrorCompilador{
    arreglo = false;
    String parte_izquierda_pBase = lexema;
    eat(IDENTIFIER);
    ArrayList<String> parte_izquierda_pAtributos = parte_izquierda_p(parte_izquierda_pBase);

    int parte_izquierda_pTipo = Integer.parseInt(parte_izquierda_pAtributos.get(0));
    String parte_izquierda_pDir = parte_izquierda_pAtributos.get(1);

    ArrayList<String> parte_izquierdaAtributos = new ArrayList<String>();
    parte_izquierdaAtributos.add(Integer.toString(parte_izquierda_pTipo));
    parte_izquierdaAtributos.add(parte_izquierda_pDir);
    parte_izquierdaAtributos.add(parte_izquierda_pBase);
    return parte_izquierdaAtributos;
  } 

  private ArrayList<String> parte_izquierda_p(String parte_izquierda_pBase) throws IOException,ErrorCompilador{
    ArrayList<String> parte_izquierda_pAtributos = new ArrayList<String>();

    if(tokenActual == C1){
      arreglo = true;
      String localizacionBase = parte_izquierda_pBase;
      ArrayList<String> localizacionAtributos = localizacion(localizacionBase);

      int localizacionTipo = Integer.parseInt(localizacionAtributos.get(0));
      String localizacionDir = localizacionAtributos.get(1);

      parte_izquierda_pAtributos.add(Integer.toString(localizacionTipo));
      parte_izquierda_pAtributos.add(localizacionDir);
      return parte_izquierda_pAtributos;

    }else{
      arreglo = false;

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
  */
  
  // bool -> comb bool_p
  private ArrayList<String> bool(String boolVddr, String boolFls) throws IOException,ErrorCompilador{

    String combVddr = boolVddr;
    String combFls = Semantico.nuevoIndice();
    ArrayList <String> combAtributos = comb(combVddr,combFls);
    String combTipo = combAtributos.get(0);
    String combDir = combAtributos.get(1);
    
    String bool_pVddr = boolVddr;
    String bool_pFls = boolFls;
    String bool_pTipoH = combTipo;
    ArrayList<String> bool_pListaIndices = Semantico.nuevaListaIndices();
    bool_pListaIndices.add(combFls);
    codigo.genCod("label",combFls); 

    ArrayList<String> bool_pAtributos = bool_p(bool_pVddr,bool_pFls,bool_pListaIndices,bool_pTipoH);
    String boolTipo = bool_pAtributos.get(0);

    ArrayList<String> atributosRet = new ArrayList<String>();
    atributosRet.add(boolTipo);
    atributosRet.add(combDir);
    return atributosRet;
  }
  
  //bool_p → || comb bool_p | ε 
  private ArrayList<String> bool_p(String bool_pVddr,String bool_pFls, ArrayList<String> bool_pListaIndices,String bool_pTipoH) throws IOException, ErrorCompilador{
    if(tokenActual == OR){
      eat(OR);
      String combVddr = bool_pVddr;
      String combFls = Semantico.nuevoIndice();
      ArrayList <String> combAtributos = comb(combVddr,combFls);
      String combTipo = combAtributos.get(0);
      String combDir = combAtributos.get(1);

      if(Semantico.equivalentes(bool_pTipoH, combTipo)){

        String bool_p1TipoH = combTipo;
        String bool_p1Vddr = bool_pVddr;
        String bool_p1Fls = bool_pFls;
        ArrayList<String> bool_p1ListaIndices = bool_pListaIndices;
        bool_p1ListaIndices.add(combFls);
        codigo.genCod("label", combFls);

        ArrayList<String> bool_p1Atributos = bool_p(bool_pVddr,bool_pFls,bool_pListaIndices,bool_p1TipoH);
        String bool_p1TipoS = bool_p1Atributos.get(0);

        String bool_pTipoS = bool_p1TipoS;

        ArrayList<String> atributosRet = new ArrayList<String>();
        atributosRet.add(bool_pTipoS);
        atributosRet.add(combDir);
        return atributosRet;

      }else{
        error("Error semántico: Tipos incompatibles");
      }
    }else{
      Semantico.reemplazarIndices(bool_pListaIndices, bool_pFls,codigo);
      String bool_pTipoS = bool_pTipoH;
      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(bool_pTipoS);
      codigo.popCodigo();
      return atributosRet;
    }
    return null;
  }
  
  //comb → igualdad comb_p 
  private ArrayList<String> comb(String combVddr,String combFls) throws IOException, ErrorCompilador{
    String igualdadVddr = Semantico.nuevoIndice();
    String igualdadFls = combFls;
    ArrayList<String> igualdadAtributos = igualdad(igualdadVddr,igualdadFls);
    String igualdadTipo = igualdadAtributos.get(0);
    String igualdadDir = igualdadAtributos.get(1);

    String comb_pVddr = combVddr;
    String comb_pFls = combFls;
    String comb_pTipoH = igualdadTipo;
    ArrayList<String> comb_pListaIndices = Semantico.nuevaListaIndices();
    comb_pListaIndices.add(igualdadVddr);
    codigo.genCod("label",igualdadVddr);

    ArrayList<String> comb_pAtributos = comb_p(comb_pVddr,comb_pFls,comb_pListaIndices,comb_pTipoH);
    String comb_pTipoS = comb_pAtributos.get(0);

    String combTipo = comb_pTipoS;

    ArrayList<String> atributosRet = new ArrayList<String>();
    atributosRet.add(combTipo);
    atributosRet.add(igualdadDir);
    return atributosRet;
  }
  
  //comb_p → && igualdad comb_p | ε
  private ArrayList<String> comb_p(String comb_pVddr,String comb_pFls, ArrayList<String> comb_pListaIndices,String comb_pTipoH) throws IOException, ErrorCompilador{
    if(tokenActual == AND){
      eat(AND);

      String igualdadVddr = Semantico.nuevoIndice();
      String igualdadFls = comb_pFls;

      ArrayList<String> igualdadAtributos = igualdad(igualdadVddr,igualdadFls);
      String igualdadTipo = igualdadAtributos.get(0);
      String igualdadDir = igualdadAtributos.get(1);

      if(Semantico.equivalentes(comb_pTipoH, igualdadTipo)){
        
        String comb_p1TipoH = igualdadTipo;
        String comb_p1Vddr = comb_pVddr;
        String comb_p1Fls = comb_pFls;
        ArrayList<String> comb_p1ListaIndices = comb_pListaIndices;
        comb_p1ListaIndices.add(igualdadVddr);
        codigo.genCod("label", igualdadVddr);

        ArrayList<String> comb_p1Atributos = comb_p(comb_p1Vddr,comb_p1Fls,comb_p1ListaIndices,comb_p1TipoH);
        String comb_p1TipoS = comb_p1Atributos.get(0);
        String comb_pTipoS = comb_p1TipoS;

        ArrayList<String> atributosRet = new ArrayList<String>();
        atributosRet.add(comb_pTipoS);
        atributosRet.add(igualdadDir);
        return atributosRet;
      }else{
        error("Error semántico: Tipos incompatibles");
      }
    }else{
      Semantico.reemplazarIndices(comb_pListaIndices, comb_pVddr, codigo);
      String comb_pTipoS = comb_pTipoH;
      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(comb_pTipoS);
      codigo.popCodigo();
      return atributosRet;
    }
    return null;
  }
  
  //igualdad → rel igualdad_p
  private ArrayList<String> igualdad(String igualdadVddr, String igualdadFls) throws IOException,ErrorCompilador{

    String relVddr = igualdadVddr;
    String relFls = igualdadFls;
    ArrayList<String> relAtributos = rel(relVddr,relFls);
    String relTipo = relAtributos.get(0);
    String relDir = relAtributos.get(1);

    String igualdad_pVddr = igualdadVddr;
    String igualdad_pFls = igualdadFls;
    String igualdad_pTipoH = relTipo;
    String igualdad_pDirH = relDir;
    
    ArrayList<String> igualdad_pAtributos = igualdad_p(igualdad_pVddr,igualdad_pFls,igualdad_pTipoH,igualdad_pDirH);
    String igualdad_pTipoS = igualdad_pAtributos.get(0);
    String igualdad_pDirS = igualdad_pAtributos.get(1);

    ArrayList<String> atributosRet = new ArrayList<String>();
    atributosRet.add(igualdad_pTipoS);
    atributosRet.add(igualdad_pDirS);
    return atributosRet;
  }
  
  //igualdad_p → == rel igualdad_p | != rel igualdad_p | ε
  private ArrayList<String> igualdad_p(String igualdad_pVddr, String igualdad_pFls, String igualdad_pTipoH, String igualdad_pDirH) throws IOException,ErrorCompilador{
    String op="";
    if(tokenActual==IGUAL){
      eat(IGUAL);
      op = "==";
    }else if(tokenActual==DESIGUAL){
      eat(DESIGUAL);
      op = "!=";
    }else{
      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(igualdad_pTipoH);
      atributosRet.add(igualdad_pDirH);
      return atributosRet;
    }
    String relVddr = igualdad_pVddr;
    String relFls = igualdad_pFls;
    ArrayList<String> relAtributos = rel(relVddr,relFls);
    String relTipo = relAtributos.get(0);
    String relDir = relAtributos.get(1);

    if(Semantico.equivalentes(igualdad_pTipoH,relTipo)){
      String igualdad_p1Vddr = igualdad_pVddr;
      String igualdad_p1Fls = igualdad_pFls;
      String igualdad_p1TipoH = relTipo;
      String igualdad_p1DirH = relDir;

      ArrayList<String> igualdad_p1Atributos = igualdad_p(igualdad_p1Vddr,igualdad_p1Fls,igualdad_p1TipoH,igualdad_p1DirH);
      String igualdad_p1TipoS = igualdad_p1Atributos.get(0);
      String igualdad_p1DirS = igualdad_p1Atributos.get(1);

      String igualdad_pDir = Semantico.nuevaTemporal();
      int tipoTemp = Semantico.maximo(igualdad_pTipoH,relTipo);
      String d1 = Semantico.ampliar(igualdad_pDirH,igualdad_pTipoH,tipoTemp,codigo);
      String d2 = Semantico.ampliar(relDir,relTipo,tipoTemp,codigo);

      codigo.genCod(new Cuadrupla(op,d1,d2,igualdad_pDir));
      codigo.genCod(new Cuadrupla("if",igualdad_pDir,"goto",relVddr));
      codigo.genCod(new Cuadrupla("goto",relFls));

      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(igualdad_p1TipoS);
      atributosRet.add(igualdad_pDir);
      return atributosRet;
    }else{
      error("Error semántico, tipos incomplatibles");
    }
    return null;
  }
  
  //rel → exp rel_p
  private ArrayList<String> rel(String relVddr,String relFls) throws IOException,ErrorCompilador{
    String expVddr = relVddr;
    String expFls = relFls;
    ArrayList<String> expAtributos = exp(expVddr,expFls);
    String expTipo = expAtributos.get(0);
    String expDir = expAtributos.get(1);

    String rel_pVddr = relVddr;
    String rel_pFls = relFls;
    String rel_pTipoH = expTipo;
    String rel_pDirH = expDir;

    ArrayList<String> rel_pAtributos = rel_p(rel_pVddr,rel_pFls,rel_pTipoH,rel_pDirH);
    String rel_pTipoS = rel_pAtributos.get(0);
    String rel_pDirS = rel_pAtributos.get(1);

    ArrayList<String> atributosRet = new ArrayList<String>();
    atributosRet.add(rel_pTipoS);
    atributosRet.add(rel_pDirS);
    return atributosRet;
  }
  
  //rel_p → < exp | <= exp | >= exp | > exp | ε
  private ArrayList<String> rel_p(String rel_pVddr,String rel_pFls,String rel_pTipoH, String rel_pDirH) throws IOException,ErrorCompilador{
    String op = "";
    if(tokenActual==LT){
      eat(LT);
      op = "<";
    }else if(tokenActual==LET){
      eat(LET);
      op = "<=";
    }else if(tokenActual==MET){
      eat(MET);
      op = ">=";
    }else if(tokenActual==MT){
      eat(MT);
      op = ">";
    }else{
      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(rel_pTipoH);
      atributosRet.add(rel_pDirH);
      return atributosRet;
    }

    String expVddr = rel_pVddr;
    String expFls = rel_pFls;
    ArrayList<String> expAtributos = exp(expVddr,expFls);
    String expTipo = expAtributos.get(0);
    String expDir = expAtributos.get(1);

    if(Semantico.equivalentes(rel_pTipoH,expTipo)){
      String rel_p1Vddr = rel_pVddr;
      String rel_p1Fls = rel_pFls;
      String rel_p1TipoH = expTipo;
      String rel_p1DirH = expTipo;

      ArrayList<String> rel_p1Atributos = rel_p(rel_p1Vddr,rel_p1Fls,rel_p1TipoH,rel_p1DirH);
      String rel_p1TipoS = rel_p1Atributos.get(0);
      String rel_p1DirS = rel_p1Atributos.get(1);

      String rel_pDir = Semantico.nuevaTemporal();
      int tipoTemp = Semantico.maximo(rel_pTipoH,expTipo);
      String d1 = Semantico.ampliar(rel_pDirH,rel_pTipoH,tipoTemp,codigo);
      String d2 = Semantico.ampliar(expDir,expTipo,tipoTemp,codigo);

      codigo.genCod(new Cuadrupla(op,d1,d2,rel_pDir));
      codigo.genCod(new Cuadrupla("if",rel_pDir,"goto",rel_pVddr));
      codigo.genCod(new Cuadrupla("goto",rel_pFls));

      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(rel_p1TipoS);
      atributosRet.add(rel_pDir);
      return atributosRet;
    }else{
      error("Error semántico, tipos incompatibles");
    }
    return null;
  }
  
  //exp → term exp_p
  private ArrayList<String> exp(String expVddr, String expFls) throws IOException,ErrorCompilador{

    ArrayList<String> termAtributos = term();
    String termTipo = termAtributos.get(0);
    String termDir = termAtributos.get(1);

    String exp_pTipoH = termTipo;
    String exp_pDirH = termDir;
    ArrayList<String> exp_pAtributos = exp_p(exp_pTipoH,exp_pDirH);
    String exp_pTipoS = exp_pAtributos.get(0);
    String exp_pDirS = exp_pAtributos.get(1);

    ArrayList<String> atributosRet = new ArrayList<String>();
    atributosRet.add(exp_pTipoS);
    atributosRet.add(exp_pDirS);
    return atributosRet;
  }
  
  //exp_p → + term exp_p | - term exp_p | ε
  private ArrayList<String> exp_p(String exp_pTipoH, String exp_pDirH) throws IOException,ErrorCompilador{

    String op = "";
    if(tokenActual==SUMA){
      eat(SUMA);
      op = "+";
    }else if(tokenActual==RESTA){
      eat(RESTA);
      op = "-";
    }else{
      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(exp_pTipoH);
      atributosRet.add(exp_pDirH);
      return atributosRet;
    }

    ArrayList<String> termAtributos = term();
    String termTipo = termAtributos.get(0);
    String termDir = termAtributos.get(1);

    if(Semantico.equivalentes(exp_pTipoH, termTipo)){

      String exp_p1TipoH = termTipo;
      String exp_p1DirH = termDir;
      ArrayList<String> exp_p1Atributos = exp_p(exp_p1TipoH,exp_p1DirH);
      String exp_p1TipoS = exp_p1Atributos.get(0);
      String exp_p1DirS = exp_p1Atributos.get(1);

      int exp_pTipo = Semantico.maximo(exp_pTipoH, termTipo);
      String exp_pDir = Semantico.nuevaTemporal();
      String d1 = Semantico.ampliar(exp_pDirH, exp_pTipoH, exp_pTipo,codigo);
      String d2 = Semantico.ampliar(termDir, termTipo, exp_pTipo,codigo);
      codigo.genCod(new Cuadrupla(op, d1, d2, exp_pDir));

      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(exp_p1TipoS);
      atributosRet.add(exp_pDir);
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

    ArrayList<String> unarioAtributos = unario();
    String unarioTipo = unarioAtributos.get(0);
    String unarioDir = unarioAtributos.get(1);

    String term_pTipoH = unarioTipo;
    String term_pDirH = unarioDir;
    ArrayList<String> term_pAtributos = term_p(term_pTipoH,term_pDirH);
    String term_pTipoS = term_pAtributos.get(0);
    String term_pDirS = term_pAtributos.get(1);

    ArrayList<String> atributosRet = new ArrayList<String>();
    atributosRet.add(term_pTipoS);
    atributosRet.add(term_pDirS);
    return atributosRet;
  }

  private ArrayList<String> term_p(String term_pTipoH, String term_pDirH) throws IOException,ErrorCompilador{    
    if(tokenActual==MULTI){
      eat(MULTI);

      ArrayList<String> unarioAtributos = unario();
      String unarioTipo = unarioAtributos.get(0);
      String unarioDir = unarioAtributos.get(1);

      if(Semantico.equivalentes(term_pTipoH, unarioTipo)){
        String term_p1TipoH = unarioTipo;
        String term_p1DirH = unarioDir;
        ArrayList<String> term_p1Atributos = term_p(term_p1TipoH,term_p1DirH);
        String term_p1TipoS = term_p1Atributos.get(0);
        String term_p1DirS = term_p1Atributos.get(1);

        int term_pTipo = Semantico.maximo(term_pTipoH, unarioTipo);
        String term_pDir = Semantico.nuevaTemporal();
        String d1 = Semantico.ampliar(term_pDirH, term_pTipoH, term_pTipo,codigo);
        String d2 = Semantico.ampliar(unarioDir, unarioTipo, term_pTipo,codigo);
        codigo.genCod(new Cuadrupla("*", d1,d2, term_pDir));

        ArrayList<String> atributosRet = new ArrayList<String>();
        atributosRet.add(term_p1TipoS);
        atributosRet.add(term_pDir);
        return atributosRet;
      }else{
        error("Error semántico, tipos incompatibles");
      }
    }else if(tokenActual ==DIVISION){
      eat(DIVISION);

      ArrayList<String> unarioAtributos = unario();
      String unarioTipo = unarioAtributos.get(0);
      String unarioDir = unarioAtributos.get(1);

      if(Semantico.equivalentes(term_pTipoH, unarioTipo)){
        String term_p1TipoH = unarioTipo;
        String term_p1DirH = unarioDir;
        ArrayList<String> term_p1Atributos = term_p(term_p1TipoH,term_p1DirH);
        String term_p1TipoS = term_p1Atributos.get(0);
        String term_p1DirS = term_p1Atributos.get(1);

        int term_pTipo = Semantico.maximo(term_pTipoH, unarioTipo);
        String term_pDir = Semantico.nuevaTemporal();
        String d1 = Semantico.ampliar(term_pDirH, term_pTipoH, term_pTipo,codigo);
        String d2 = Semantico.ampliar(unarioDir, unarioTipo, term_pTipo,codigo);
        codigo.genCod(new Cuadrupla("/", d1,d2, term_pDir));

        ArrayList<String> atributosRet = new ArrayList<String>();
        atributosRet.add(term_p1TipoS);
        atributosRet.add(term_pDir);
        return atributosRet;
      }else{
        error("Error semántico, tipos incompatibles");
      }
    }else if(tokenActual ==MODULO){
      eat(MODULO);

      ArrayList<String> unarioAtributos = unario();
      String unarioTipo = unarioAtributos.get(0);
      String unarioDir = unarioAtributos.get(1);

      if(Semantico.equivalentes(term_pTipoH, unarioTipo)){
        String term_p1TipoH = unarioTipo;
        String term_p1DirH = unarioDir;
        ArrayList<String> term_p1Atributos = term_p(term_p1TipoH,term_p1DirH);
        String term_p1TipoS = term_p1Atributos.get(0);
        String term_p1DirS = term_p1Atributos.get(1);

        int term_pTipo = 0; // int
        String term_pDir = Semantico.nuevaTemporal();
        codigo.genCod(new Cuadrupla("%", unarioDir,term_pDirH, term_pDir));

        ArrayList<String> atributosRet = new ArrayList<String>();
        atributosRet.add(term_p1TipoS);
        atributosRet.add(term_pDir);
        return atributosRet;
      }else{
        error("Error semántico, tipos incompatibles");
      }
    }else{
      ArrayList<String> atributosRet = new ArrayList<String>();
      atributosRet.add(term_pTipoH);
      atributosRet.add(term_pDirH);
      return atributosRet;
    }
    return null;
  }

  private ArrayList<String> unario() throws IOException,ErrorCompilador{

    ArrayList<String> atributosRet = new ArrayList<String>();

    String unarioTipo;
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
        String factorTipo = factorAtributos.get(0);
        String factorDir = factorAtributos.get(1);

        unarioDir = factorDir;
        unarioTipo = factorTipo;
        atributosRet.add(unarioTipo);
        atributosRet.add(unarioDir);

        return atributosRet;   
    }

    unarioDir = Semantico.nuevaTemporal();

    ArrayList<String> unario1Atributos = unario();
    String unario1Tipo = unario1Atributos.get(0);
    String unario1Dir = unario1Atributos.get(1);

    codigo.genCod(new Cuadrupla(unarioOp, unario1Dir, "", unarioDir));

    atributosRet.add(unario1Tipo);
    atributosRet.add(unarioDir);
    return atributosRet;
  }

  private ArrayList<String> factor() throws IOException,ErrorCompilador{
    ArrayList<String> atributosRet = new ArrayList<String>();
    switch(tokenActual){
      //(bool)
      case P1:
        eat(P1);
        atributosRet=bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
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
      case CHAR_LIT:
        atributosRet.add("3");
        atributosRet.add(lexema);
        eat(CHAR_LIT);
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

        codigo.genCod(new Cuadrupla("",factor_pBase + " [" + localizacionDir + "]","",factor_pDir));
        return atributosRet;

      case P1:
        eat(P1);
        ArrayList<Integer> parametrosLista = parametros();
        eat(P2);
        if(fondoTS.buscar(factor_pBase)){
          if(fondoTS.getVar(factor_pBase).equals("func")){
            if(Semantico.equivalentesListas(parametrosLista,fondoTS.getArgs(factor_pBase))){
              factor_pTipo = fondoTS.getTipo(factor_pBase);
              factor_pDir = Semantico.nuevaTemporal();
              codigo.genCod(new Cuadrupla(",","call "+factor_pBase,Integer.toString(parametrosLista.size()),factor_pDir));
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
      case CHAR_LIT:
      case TRUE:
      case FALSE:
        ArrayList<Integer> parametrosLista = lista_param();
        return parametrosLista;
      default:
        return new ArrayList<Integer>();
    } 
  }

  private ArrayList<Integer> lista_param() throws IOException,ErrorCompilador{
    ArrayList<String> boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
    ArrayList<Integer> lista_param_pAtributos = lista_param_p();
    ArrayList<Integer> lista_param = lista_param_pAtributos;
    lista_param.add(Integer.parseInt(boolAtributos.get(0)));
    codigo.genCod("param",boolAtributos.get(1));
    return Semantico.invertir(lista_param); 
  }

  private ArrayList<Integer> lista_param_p() throws IOException,ErrorCompilador{
    if(tokenActual==COMA){
      eat(COMA);
      ArrayList<String> boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
      ArrayList<Integer> lista_param_p1Atributos = lista_param_p();
      ArrayList<Integer> lista_param = lista_param_p1Atributos;
      lista_param.add(Integer.parseInt(boolAtributos.get(0)));
      codigo.genCod("param",boolAtributos.get(1));
      return lista_param;
    }
    // producción vacía
    ArrayList<Integer> lista_param = new ArrayList<Integer>();
    return lista_param;
  }

  private ArrayList<String> localizacion(String localizacionBase) throws IOException,ErrorCompilador{
    if(tokenActual==C1){
      eat(C1);

      ArrayList<String> boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
      int boolTipo = Integer.parseInt(boolAtributos.get(0));
      String boolDir = boolAtributos.get(1);

      eat(C2);

      int tipoTemp, localizacion_pTipo, localizacion_pTipoS, localizacion_pTam;
      String localizacion_pDir, localizacion_pDirS;
      ArrayList<String> localizacion_pAtributos;


      if(PilaTS.peek().buscar(localizacionBase)){ 

        if(boolTipo == 0){
          tipoTemp = PilaTS.peek().getTipo(localizacionBase);

          if(PilaTT.peek().getNombre(tipoTemp).equals("array")){
            localizacion_pTipo = PilaTT.peek().getTipoBase(tipoTemp);
            localizacion_pDir = Semantico.nuevaTemporal();
            localizacion_pTam = PilaTT.peek().getTam(localizacion_pTipo);

            codigo.genCod(new Cuadrupla("*",boolDir,Integer.toString(localizacion_pTam),localizacion_pDir));

            localizacion_pAtributos = localizacion_p(localizacion_pTipo, localizacion_pDir);
            localizacion_pTipoS = Integer.parseInt(localizacion_pAtributos.get(0));
            localizacion_pDirS = localizacion_pAtributos.get(1);

            String localizacionDir = localizacion_pDirS;
            int localizacionTipo = localizacion_pTipoS;

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

            codigo.genCod(new Cuadrupla("*", boolDir, Integer.toString(localizacion_pTam), localizacionDir));

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

      ArrayList<String> boolAtributos = bool(Semantico.nuevaEtiqueta(),Semantico.nuevaEtiqueta());
      int boolTipo = Integer.parseInt(boolAtributos.get(0));
      String boolDir = boolAtributos.get(1);

      eat(C2);

      String dirTemp = Semantico.nuevaTemporal();

      int localizacion_p1Tipo = PilaTT.peek().getTipoBase(localizacion_pTipo);
      String localizacion_p1Dir = Semantico.nuevaTemporal();
      int localizacion_p1Tam = PilaTT.peek().getTam(localizacion_p1Tipo); 

      ArrayList<String> localizacion_p1Atributos = localizacion_p(localizacion_p1Tipo, localizacion_p1Dir);
      int localizacion_p1TipoS = Integer.parseInt(localizacion_p1Atributos.get(0));
      String localizacion_p1DirS = localizacion_p1Atributos.get(1);

      if(boolTipo == 0){
        if(PilaTT.peek().getNombre(localizacion_pTipo).equals("array")){

          codigo.genCod(new Cuadrupla("*", boolDir, Integer.toString(localizacion_p1Tam), dirTemp));                
          codigo.genCod(new Cuadrupla("+", localizacion_pDir, dirTemp, localizacion_p1Dir));

          localizacion_pDir = localizacion_p1DirS;
          localizacion_pTipo = localizacion_p1TipoS;

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
      case 46: return "print";
      case 47: return "scan";
      case 48: return "CHAR_LIT";

    }
    return "desconocido";
  }

  // Método que muestra la existencia de un error
  private void error(String mensaje) throws ErrorCompilador{
    throw new ErrorCompilador(mensaje+", línea "+(analizadorLexico.getYyline()+1)+"\n"+analizadorLexico.linea);
  }

}
