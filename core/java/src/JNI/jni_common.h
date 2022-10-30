#ifndef JNI_COMMON_H
#define JNI_COMMON_H

/*
 * This enumeration mirrors the Java OpCode one (in OpCode.java).
 */
typedef enum {
  LT = 0,  
  LEQ, 
  EQU, 
  GEQ, 
  GT,
  ADD, 
  SUB,
  MUL,
  DIV,
  AND,
  OR,
  IMPLIES,
  NOT,
  NEQ
} OpCode;

/*
 * This enumeration mirrors the Java Type one (in Type.java).
 */
typedef enum {
  BOOL = 0,
  INT,
  FLOAT,
  DOUBLE,
  VOID
} Type;

/*
 * This enumeration mirrors the Java RoundingMode one (in 
 * validation.util.RoundingMode.java).
 */
typedef enum {
  UP = 0,
  DOWN,
  NEAR,
  ZERO
} RoundingMode;

#endif
