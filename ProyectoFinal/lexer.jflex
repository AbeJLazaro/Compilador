/*
Autores:      Cabrera Gaytán Jazmín Andrea
              Camacho Morales Gerardo Iván
              Nicolas Marin Brian Geovanny
              Lázaro Martínez Abraham Josué
              Oropeza Castañeda Ángel Eduardo

Versión:      1.2
Fecha:        18 de enero de 2021
Nombre:       lexer.jflex
*/
import java.util.*;

%%
%public
%class Lexer
%line
%int 
%eofval{
  //Al encontrar el fin de archivo, regresa un valor 0
  return 0;
%eofval}

%{
//Definición de constantes para definir que número regresará el 
//analizador para cada token
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

public String linea = "";

public int getYyline(){
  return this.yyline;
}

public void anexar(){
  linea+=yytext();
}

public void limpiar(){
  linea = "";
}
%}
 
/* Inicio de Expresiones regulares */

letra=[a-zA-Z_]
digito=[0-9]

//TIPOS DE DATOS
INT=int
FLOAT=float
CHAR=char
DOUBLE=double
VOID=void

//PALABRAS RESERVADAS
FUNC=func
IF=if
ELSE=else
WHILE=while
DO=do
BREAK=break
SWITCH=switch
CASE=case
DEFAULT=default
RETURN=return
PRINT=print
SCAN=scan

//OPERADORES
ASIGNACION="="
OR="||"
AND="&&"
IGUAL="=="
DESIGUAL="!="
LT="<"
LET="<="
MT=">"
MET=">="
SUMA="+"
RESTA="-"
MULTI="*"
DIVISION="/"
MODULO="%"
NEGACION="!"

//PUNTUACIÓN 
L1="{"
L2="}"
C1="["
C2="]"
P1="("
P2=")"
DOSPUNTOS=":"
PUNTOYCOMA=";"
COMA=","

//IDENTIFICADOR
identifier={letra}({letra}|{digito})*

//STRING
comillas1=\'((\\[uxnU\"\'\\])*([^\\\'])*)*\'
comillas2=\"((\\[uxnU\"\'\\])*([^\\\"])*)*\"
string_lit={comillas1}|{comillas2}

//BOOLEANOS
TRUE=true
FALSE=false

//INT
int_lit={digito}([_]?{digito})*

//FLOAT
exp=([eE][+-]?{int_lit}+)
float_lit=((({int_lit})*\.({digito})+|({int_lit})+\.({digito})*){exp}?)|({int_lit})+({exp})

//ESPACIOS - IGNORAR
esps=[ \r\t]
enter=[\n]

%%
{esps} {/*No se hace nada*/}
{enter} {limpiar();}

{INT} {anexar();return INT;}
{FLOAT} {anexar();return FLOAT;}
{CHAR} {anexar();return CHAR;}
{DOUBLE} {anexar();return DOUBLE;}
{VOID} {anexar();return VOID;}

{FUNC} {anexar();return FUNC;}
{IF} {anexar();return IF;}
{ELSE} {anexar();return ELSE;}
{WHILE} {anexar();return WHILE;}
{DO} {anexar();return DO;}
{BREAK} {anexar();return BREAK;}
{SWITCH} {anexar();return SWITCH;}
{CASE} {anexar();return CASE;}
{DEFAULT} {anexar();return DEFAULT;}
{RETURN} {anexar();return RETURN;}
{PRINT} {anexar();return PRINT;}
{SCAN} {anexar();return SCAN;}

{TRUE} {anexar();return TRUE;}
{FALSE} {anexar();return TRUE;}

{identifier} {anexar();return IDENTIFIER;}

{int_lit} {anexar();return INT_LIT;}

{float_lit} {anexar();return FLOAT_LIT;}

{string_lit} {anexar();return STRING_LIT;}

{ASIGNACION} {anexar();return ASIGNACION;}
{OR} {anexar();return OR;}
{AND} {anexar();return AND;}
{IGUAL} {anexar();return IGUAL;}
{DESIGUAL} {anexar();return DESIGUAL;}
{MT} {anexar();return MT;}
{MET} {anexar();return MET;}
{LT} {anexar();return LT;}
{LET} {anexar();return LET;}
{SUMA} {anexar();return SUMA;}
{RESTA} {anexar();return RESTA;}
{MULTI} {anexar();return MULTI;}
{DIVISION} {anexar();return DIVISION;}
{MODULO} {anexar();return MODULO;}
{NEGACION} {anexar();return NEGACION;}

{P1} {anexar();return P1;}
{P2} {anexar();return P2;}
{C1} {anexar();return C1;}
{C2} {anexar();return C2;}
{L1} {anexar();return L1;}
{L2} {anexar();return L2;}
{DOSPUNTOS} {anexar();return DOSPUNTOS;}
{PUNTOYCOMA} {anexar();return PUNTOYCOMA;}
{COMA} {anexar();return COMA;}

. {anexar();return -1;}
