#include "jni_Z3Solver.h"
#include "jni_common.h"

#include <iostream>
#include <sstream>
#include <string>

#include "z3.h"

/*
 * TODO: I'm not sure the z3 API methods copy the strings passed as parameters.
 *       We should do the copies ourselves...
 *
 */

/*
 * The method takes a Java String object and returns the corresponding
 * C (UTF8 and null terminated) character string. The method allocates
 * dynamically the memory required to store the string: it is up to
 * the caller to free this allocated memory space once not needed
 * anymore.
 *
 * TODO: Put implementation in a dedicated .cpp file.
 */
static char * allocateCharString(JNIEnv *jniEnv, jstring string) {
  int length = jniEnv->GetStringUTFLength(string);
  char *c_string = new char[length+1];
  jniEnv->GetStringUTFRegion(string, 0, length, c_string);  
  c_string[length] = '\0';
  return c_string;
}

/*
 * A simple error handler for z3 errors.
 */
static void z3_simple_error_handler(Z3_error_code e) 
{
  std::cerr << "Incorrect use of Z3" << std::endl;
  std::cerr << "z3 Error code: " << e << std::endl;
  std::cerr << "z3 Error message: " << Z3_get_error_msg(e) << std::endl;
}

/*
 * A convenience function to factorize code that creates an integer constant.
 */
static Z3_ast mk_int_cst(Z3_context z3_ctx, jint cst_value)
{
  Z3_sort type = Z3_mk_int_sort(z3_ctx);
  return Z3_mk_int(z3_ctx, cst_value, type);
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3Version
 * Signature: ()Ljava/lang/String;
 *
 * Returns the Z3 version number.
 * 
 */
JNIEXPORT jstring JNICALL Java_solver_z3_Z3Solver_z3Version
  (JNIEnv *jniEnv, jclass)
{
  unsigned int major, minor, build, revision;
  std::stringstream version;

  Z3_get_version(&major, &minor, &build, &revision);
  version << major << "." << minor << "." << build << "." << revision;
  return jniEnv->NewStringUTF(version.str().c_str()); 
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3Init
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3Init
  (JNIEnv *, jclass)
{
  Z3_config cfg = Z3_mk_config();
  Z3_set_param_value(cfg, "MODEL", "true");
  Z3_context z3_ctx = Z3_mk_context(cfg);
  //Z3_trace_to_stderr(z3_ctx);
  Z3_set_error_handler(z3_ctx, z3_simple_error_handler);
  Z3_del_config(cfg);

  return (jlong)z3_ctx;
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3Release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_solver_z3_Z3Solver_z3Release
  (JNIEnv *, jclass, jlong ctx) 
{
  Z3_del_context((Z3_context)ctx);
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3Check
 * Signature: (J)J
 *
 * Returns 0L (NULL) if no solution could be found; otherwise returns a non-null
 * pointer to the Z3 model found.
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3Check
  (JNIEnv *, jclass, jlong ctx)
{
  Z3_lbool result;
  Z3_context z3_ctx = (Z3_context)ctx;
  Z3_model z3_model;

  result = Z3_check_and_get_model(z3_ctx, &z3_model);
  switch (result) {
  case Z3_L_FALSE:
    z3_model = 0L; //Set to NULL
    //std::cout << "unsat" << std::endl;
    break;
  case Z3_L_UNDEF:
    std::cerr << "Error: z3 unknown validation status!" << std::endl;
    std::cerr << "Potential model:" << std::endl;
    std::cerr << Z3_model_to_string(z3_ctx, z3_model);
    break;
  case Z3_L_TRUE:
    //std::cout << "Model found:" << std::endl;
    //std::cout << Z3_model_to_string(z3_ctx, z3_model);
    break;
  }
  return (jlong)z3_model;
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3DelModel
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_solver_z3_Z3Solver_z3DelModel
(JNIEnv *, jclass, jlong ctx, jlong model)
{
  if (model) {
    Z3_del_model((Z3_context)ctx, (Z3_model)model);
  }
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3PushContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_solver_z3_Z3Solver_z3PushContext
  (JNIEnv *, jclass, jlong ctx)
{
  Z3_push((Z3_context)ctx);
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3PopContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_solver_z3_Z3Solver_z3PopContext
  (JNIEnv *, jclass, jlong ctx)
{
  Z3_pop((Z3_context)ctx, 1);
}

Z3_ast mk_var(Z3_context z3_ctx, const char *name, int type)
{
  Z3_sort sort;
  Z3_symbol symb;

  switch(type) 
  {
  case 0: //BOOL type
    sort = Z3_mk_bool_sort(z3_ctx);
    symb = Z3_mk_string_symbol(z3_ctx, name);
    return Z3_mk_const(z3_ctx, symb, sort);
    break;
  case 1: //INT type
    sort = Z3_mk_int_sort(z3_ctx);
    symb = Z3_mk_string_symbol(z3_ctx, name);
    return Z3_mk_const(z3_ctx, symb, sort);
    break;
  case 2: //FLOAT type (same treatment as DOUBLE)
  case 3: //DOUBLE type
    sort = Z3_mk_real_sort(z3_ctx);
    symb = Z3_mk_string_symbol(z3_ctx, name);
    return Z3_mk_const(z3_ctx, symb, sort);    
    break;
  default: //Invalid type
    std::cerr << "Error (z3MkVar): Invalid type ("
	      << type
	      << ") for variable: "
	      << name
	      << "!"
	      << std::endl;
    return NULL;
    break;
  }
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkVar
 * Signature: (Ljava/lang/String;I)J
 *
 * varType represents the type of the variable in the Java source program. 
 * The mapping between values and types depends on the order of the types in 
 * the Java enumeration validation.util.Type (starting from 0).
 * See jni_common.h
 *
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkVar
(JNIEnv *jniEnv, jclass, jlong ctx, jstring varName, jint varType)
{
  const char *var_name = allocateCharString(jniEnv, varName);
  Z3_ast z3_var = mk_var((Z3_context)ctx, var_name, varType);
  return (jlong)z3_var;
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkIntVar
 * Signature: (JLjava/lang/String;II)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkIntVar
  (JNIEnv *jniEnv, jclass, jlong ctx, jstring varName, jint min, jint max)
{
  Z3_context z3_ctx = (Z3_context)ctx;
  const char *var_name = allocateCharString(jniEnv, varName);
  Z3_ast z3_var = mk_var(z3_ctx, var_name, INT);


  // We cannot directly set the variables' domain, so we add constraints
  // for this purpose
  if (z3_var)
  {
    Z3_ast z3_min = mk_int_cst(z3_ctx, min);
    Z3_ast z3_max = mk_int_cst(z3_ctx, max);
    Z3_assert_cnstr(z3_ctx, Z3_mk_le(z3_ctx, z3_min, z3_var));
    Z3_assert_cnstr(z3_ctx, Z3_mk_le(z3_ctx, z3_var, z3_max));
  }

  return (jlong)z3_var;
}

Z3_ast mk_array(Z3_context z3_ctx, const char *name, int type)
{
  Z3_sort index_type, elt_type, array_type;
  Z3_symbol symb;

  switch(type) 
  {
  case 0: //BOOL type
    index_type = Z3_mk_int_sort(z3_ctx);
    elt_type = Z3_mk_bool_sort(z3_ctx);
    array_type = Z3_mk_array_sort(z3_ctx, index_type, elt_type);
    symb = Z3_mk_string_symbol(z3_ctx, name);
    return Z3_mk_const(z3_ctx, symb, array_type);
    break;
  case 1: //INT type
    index_type = Z3_mk_int_sort(z3_ctx);
    elt_type = Z3_mk_int_sort(z3_ctx);
    array_type = Z3_mk_array_sort(z3_ctx, index_type, elt_type);
    symb = Z3_mk_string_symbol(z3_ctx, name);
    return Z3_mk_const(z3_ctx, symb, array_type);
    break;
  case 2: //FLOAT type (same treatment as DOUBLE)
  case 3: //DOUBLE type
    index_type = Z3_mk_int_sort(z3_ctx);
    elt_type = Z3_mk_real_sort(z3_ctx);
    array_type = Z3_mk_array_sort(z3_ctx, index_type, elt_type);
    symb = Z3_mk_string_symbol(z3_ctx, name);
    return Z3_mk_const(z3_ctx, symb, array_type);    
    break;
  default: //Invalid type
    std::cerr << "Error (z3MkArray): Invalid type (" 
	      << type 
	      << ")!" 
	      << std::endl;
    return NULL;
    break;
  }
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkArray
 * Signature: (JLjava/lang/String;I)J
 *
 * eltType represents the type of the array elements in the Java source 
 * program. 
 * The mapping between values and types depends on the order of the types in 
 * the Java enumeration validation.util.Type (starting from 0).
 * See jni_common.h
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkArray
  (JNIEnv *jniEnv, jclass, jlong ctx, jstring arrayName, jint eltType)
{
  const char *array_name = allocateCharString(jniEnv, arrayName);
  Z3_ast z3_array = mk_array((Z3_context)ctx, array_name, eltType);
  return (jlong)z3_array;
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkIntArray
 * Signature: (JLjava/lang/String;III)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkIntArray
(JNIEnv *jniEnv, jclass, jlong ctx, jstring arrayName, jint length, jint min, jint max)
{
  Z3_context z3_ctx = (Z3_context)ctx;
  const char *array_name = allocateCharString(jniEnv, arrayName);
  Z3_ast z3_array = mk_array(z3_ctx, array_name, INT);

  // We cannot directly set the variables' domain, so we add constraints
  // for this purpose
  if (z3_array)
  {
    Z3_ast z3_index, z3_elt;
    Z3_ast z3_min = mk_int_cst(z3_ctx, min);
    Z3_ast z3_max = mk_int_cst(z3_ctx, max);

    for (int i=0; i<length; i++)
    {
      z3_index = mk_int_cst(z3_ctx, i);
      z3_elt = Z3_mk_select(z3_ctx, z3_array, z3_index);
      Z3_assert_cnstr(z3_ctx, Z3_mk_le(z3_ctx, z3_min, z3_elt));
      Z3_assert_cnstr(z3_ctx, Z3_mk_le(z3_ctx, z3_elt, z3_max));
    }
  }

  return (jlong)z3_array;
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkBoolCst
 * Signature: (JZ)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkBoolCst
  (JNIEnv *, jclass, jlong ctx, jboolean cstValue) 
{
  Z3_context z3_ctx = (Z3_context)ctx;
  return (jlong)(cstValue? Z3_mk_true(z3_ctx) : Z3_mk_false(z3_ctx));
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkIntCst
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkIntCst
  (JNIEnv *, jclass, jlong ctx, jint cstValue) 
{
  return (jlong)mk_int_cst((Z3_context)ctx, cstValue);
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkRealCst
 * Signature: (JD)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkRealCst
  (JNIEnv *jniEnv, jclass, jlong ctx, jstring cstValue)
{
  Z3_context z3_ctx = (Z3_context)ctx;

  //We can't make a z3 real from a double value directly with the 
  //existing C API. We need to build a fraction.

  std::string numerator, denominator, s_value;
  const char *c_value;
  //long: for 64 bit architectures
  size_t pos_e = std::string::npos, pos_r = std::string::npos;
  int num_coeff = 0, den_coeff = 0;

  c_value = jniEnv->GetStringUTFChars(cstValue, 0);  
  s_value = c_value;
  jniEnv->ReleaseStringUTFChars(cstValue, c_value); 

  //Find the decimal point and exponential part if it exists
  for (int i=0; i<s_value.size()-1; i++)
  {
    if (s_value[i] == '.')
    {
      pos_r = i;
    }
    else if (s_value[i] == 'e' || s_value[i] == 'E')
    {
      pos_e = i;
      break;
    }
  }

  if (pos_e != std::string::npos) //An exponential part has been found
  {
    //Remove the exponential part and compute the numerator and denominator coeffs

    std::string exponent(s_value.substr(pos_e+1, std::string::npos));
    s_value.resize(pos_e);

    if (exponent[0] == '-') //negative exponent
    {
      std::stringstream(exponent.substr(1, std::string::npos)) >> den_coeff;
    }
    else //positive exponent
    {
      if (exponent[0] == '+') //remove the sign
        std::stringstream(exponent.substr(1, std::string::npos)) >> num_coeff;
      else
        std::stringstream(exponent) >> num_coeff;
    }
  }

  if (pos_r != std::string::npos) //A decimal part has been found
  { 
    // A point has been found, s_value = X.Y and we build a fraction 
    // XY/10^n, with n = nb. char. of Y
    
    numerator = s_value.substr(0, pos_r);
    numerator += s_value.substr(pos_r+1);

    denominator = '1';
    for (int i=0; i<s_value.size()-pos_r-1; i++)
    {
      denominator += '0';
    }
  }
  else
  {
    numerator = s_value;
    denominator = '1';
  }

  //Time to take into account the exponent
  for (int i=0; i<num_coeff; i++)
  {
    numerator += '0';
  }
  for (int i=0; i<den_coeff; i++)
  {
    denominator += '0';
  }


  return (jlong)Z3_mk_numeral(z3_ctx, 
			      (numerator + "/" + denominator).c_str(), 
			      Z3_mk_real_sort(z3_ctx));
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkOp
 * Signature: (JIJJ)J
 * 
 * opCode represents the value of an operator's code. 
 * The mapping between values and operators depends on the order of the 
 * operators in the Java enumeration validation.util.OpCode (starting from 0).
 * See jni_common.h
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkOp
(JNIEnv *, jclass, jlong ctx, jint opCode, jlong arg1, jlong arg2)
{
  Z3_context z3_ctx = (Z3_context)ctx;
  Z3_ast args[2] = {(Z3_ast)arg1, (Z3_ast)arg2};
  Z3_ast exp;

  if (args[1] != NULL)
  {
    Z3_sort sorts[2];
    sorts[0] = Z3_get_sort(z3_ctx, args[0]);
    sorts[1] = Z3_get_sort(z3_ctx, args[1]);
    //std::cout << "s1:" << sorts[0] << " s2:" << sorts[1] << std::endl;
    //std::cout << "opcode:" << opCode << std::endl;
    if(!Z3_is_eq_sort(z3_ctx, sorts[0], sorts[1]))
    {
      std::cout << "Les types ne sont pas identiques" << std::endl;
      if(Z3_is_eq_sort(z3_ctx, sorts[0], Z3_mk_int_sort(z3_ctx)))
        args[0] = Z3_mk_int2real(z3_ctx, args[0]);
      else
        args[1] = Z3_mk_int2real(z3_ctx, args[1]);
    }
  }

  switch(opCode) 
  {
  case LT:
    exp = Z3_mk_lt(z3_ctx, args[0], args[1]);
    break;
  case LEQ:
    exp = Z3_mk_le(z3_ctx, args[0], args[1]);
    break;
  case EQU:
    exp = Z3_mk_eq(z3_ctx, args[0], args[1]);
    break; 
  case GEQ:
    exp = Z3_mk_ge(z3_ctx, args[0], args[1]);
    break; 
  case GT:
    exp = Z3_mk_gt(z3_ctx, args[0], args[1]);
    break;
  case ADD:
    exp = Z3_mk_add(z3_ctx, 2, args);
    break; 
  case SUB:
    exp = Z3_mk_sub(z3_ctx, 2, args);
    break;
  case MUL:
    exp = Z3_mk_mul(z3_ctx, 2, args);
    break;
  case DIV:
    exp = Z3_mk_div(z3_ctx, args[0], args[1]);
    break;
  case AND:
    exp = Z3_mk_and(z3_ctx, 2, args);
    break;
  case OR:
    exp = Z3_mk_or(z3_ctx, 2, args);
    break;
  case IMPLIES:
    exp = Z3_mk_implies(z3_ctx, args[0], args[1]);
    break;
  case NOT:
    exp = Z3_mk_not(z3_ctx, args[0]);
    break;
  default:
    std::cerr << "Error (z3MkOp): Unknown operator (" 
	      << opCode 
	      << ")!" 
	      << std::endl;
    return NULL;
    break;
  }

  return (jlong)exp;
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3AddCtr
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_solver_z3_Z3Solver_z3AddCtr
(JNIEnv *, jclass, jlong ctx, jlong ctr)
{
  Z3_assert_cnstr((Z3_context)ctx, (Z3_ast)ctr);
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3AstToString
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_solver_z3_Z3Solver_z3AstToString
  (JNIEnv *jniEnv, jclass, jlong ctx, jlong astNode)
{
  return jniEnv->NewStringUTF(Z3_ast_to_string((Z3_context)ctx, (Z3_ast)astNode));
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3GetValue
 * Signature: (JJJ)Ljava/lang/String;
 *
 * Returns the value of the given variable in the current model.
 * If an error occured, it returns an error message that won't be parsed as
 * a Java Number and will throw an exception displaying the message.
 * 
 */
JNIEXPORT jstring JNICALL Java_solver_z3_Z3Solver_z3GetValue
  (JNIEnv *jniEnv, jclass, jlong ctx, jlong model, jlong varAst)
{
  Z3_context z3_ctx = (Z3_context)ctx;
  Z3_model z3_model = (Z3_model)model;

  //If the AST kind is Z3_APP_AST, it is safe to use Z3_to_app()
  if (Z3_get_ast_kind(z3_ctx, (Z3_ast)varAst) != Z3_APP_AST)
  {
    return 
      jniEnv->NewStringUTF("Error (z3GetValue): not given a variable AST!");
  }
  else //A z3 variable is a constant (!) i.e. a function with no argument
  {
    Z3_ast value;
    Z3_func_decl cst = 
      Z3_get_app_decl(z3_ctx, Z3_to_app(z3_ctx, (Z3_ast)varAst));

    if (!z3_model || !Z3_eval_func_decl(z3_ctx, z3_model, cst, &value))
    {
      std::string msg = 
	"Error (z3GetValue): could not evaluate the variable (";
      msg += Z3_ast_to_string(z3_ctx, (Z3_ast)varAst);
      msg += ") value in the current model!";
      return jniEnv->NewStringUTF(msg.c_str());
    }
    else
    {
      //return jniEnv->NewStringUTF(Z3_get_numeral_string(z3_ctx, value));
	return jniEnv->NewStringUTF(Z3_ast_to_string(z3_ctx, value));
    }
  }
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3GetArrayValue
 * Signature: (JJI)Ljava/lang/String;
 *
 * Returns the value of the given variable in the current model.
 * If an error occured, it returns an error message that won't be parsed as
 * a Java Number and will throw an exception displaying the message.
 */
JNIEXPORT jstring JNICALL Java_solver_z3_Z3Solver_z3GetArrayValue
(JNIEnv *jniEnv, jclass, jlong ctx, jlong model, jlong arrayAst, jint index)
{
  Z3_ast value;
  Z3_context z3_ctx = (Z3_context)ctx;
  Z3_ast index_ast = mk_int_cst(z3_ctx, index);
  Z3_eval(
	  z3_ctx, 
	  (Z3_model)model, 
	  Z3_mk_select(z3_ctx, (Z3_ast)arrayAst, index_ast), 
	  &value);
  //return jniEnv->NewStringUTF(Z3_get_numeral_string(z3_ctx, value));
  return jniEnv->NewStringUTF(Z3_ast_to_string(z3_ctx, value));

}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3GetArrayVariable
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3GetArrayVariable
  (JNIEnv *, jclass, jlong ctx, jlong arrayAst, jlong indexAst)
{
  return (jlong)Z3_mk_select((Z3_context)ctx, (Z3_ast)arrayAst, (Z3_ast)indexAst);
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkIfThenElse
 * Signature: (JJJJ)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkIfThenElse
  (JNIEnv *, jclass, jlong ctx, jlong condAst, jlong thenAst, jlong elseAst)
{
  return (jlong)Z3_mk_ite((Z3_context)ctx, 
			  (Z3_ast)condAst, 
			  (Z3_ast)thenAst, 
			  (Z3_ast)elseAst);
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkDistinct
 * Signature: (J[J)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkDistinct
  (JNIEnv *jniEnv, jclass, jlong ctx, jlongArray array)
{
  Z3_context z3_ctx = (Z3_context)ctx;
  jlong *elts = jniEnv->GetLongArrayElements(array, NULL);
  // Unfortunately, this does not work when the elements are 'array elements'
  // ((select a i) constraints).
  // jlong ctr = (jlong)Z3_mk_distinct(z3_ctx, 
  //				    jniEnv->GetArrayLength(array), 
  //				    (Z3_ast *)elts);

  int length = jniEnv->GetArrayLength(array);
  Z3_ast ctr[2];
  ctr[0] = Z3_mk_true(z3_ctx);

  for (int i=0; i<length; i++)
  {
    for (int j=i+1; j<length; j++)
    {
      ctr[1] = Z3_mk_not(z3_ctx, Z3_mk_eq(z3_ctx, 
					  (Z3_ast)elts[i], 
					  (Z3_ast)elts[j]));
      ctr[0] = Z3_mk_and(z3_ctx, 2, ctr);
    }
  }   
  jniEnv->ReleaseLongArrayElements(array, elts, 0);
  return (jlong)ctr[0];
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkForAll
 * Signature: (J[JJ)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkForAll
  (JNIEnv *jniEnv, 
   jclass,
   jlong ctx,
   jlongArray boundedVars, 
   jlong body)
{
  Z3_context z3_ctx = (Z3_context)ctx;
  int nb_vars = jniEnv->GetArrayLength(boundedVars);
  jlong *vars = jniEnv->GetLongArrayElements(boundedVars, NULL);
  Z3_app *bounded_vars = new Z3_app[nb_vars];

  for (int i=0; i<nb_vars; i++)
  {
    //If the AST kind is Z3_APP_AST, it is safe to use Z3_to_app()
    if (Z3_get_ast_kind(z3_ctx, (Z3_ast)vars[i]) != Z3_APP_AST)
    {
      std::cerr 
	<< "Error (z3MkForAll): not given a variable AST: "
	<< Z3_ast_to_string(z3_ctx, (Z3_ast)vars[i])
	<< "!"
	<< std::endl;
    }
    else //A z3 variable is a constant (!) i.e. a function with no argument
    {
      bounded_vars[i] = Z3_to_app(z3_ctx, (Z3_ast)vars[i]);
    }
  }

  Z3_ast ctr = Z3_mk_forall_const(z3_ctx, 
				  0,            // Weight 0 = auto
				  nb_vars,      // nb of bounded vars
				  bounded_vars, // z3 symbols of the bounded 
				                // variables
				  0,            // nb of patterns
				  NULL,         // array of patterns (used to 
				                // introduce the forall in a 
				                // proof)
				  (Z3_ast) body // quantifier's body
				  );

  delete[] bounded_vars;
  jniEnv->ReleaseLongArrayElements(boundedVars, vars, 0);

  return (jlong)ctr;
}

/*
 * Class:     solver_z3_Z3Solver
 * Method:    z3MkExist
 * Signature: (J[JJ)J
 */
JNIEXPORT jlong JNICALL Java_solver_z3_Z3Solver_z3MkExist
  (JNIEnv *jniEnv, 
   jclass, 
   jlong ctx,
   jlongArray boundedVars, 
   jlong body)
{
  Z3_context z3_ctx = (Z3_context)ctx;
  int nb_vars = jniEnv->GetArrayLength(boundedVars);
  jlong *vars = jniEnv->GetLongArrayElements(boundedVars, NULL);
  Z3_app *bounded_vars = new Z3_app[nb_vars];

  for (int i=0; i<nb_vars; i++)
  {
    //If the AST kind is Z3_APP_AST, it is safe to use Z3_to_app()
    if (Z3_get_ast_kind(z3_ctx, (Z3_ast)vars[i]) != Z3_APP_AST)
    {
      std::cerr 
	<< "Error (z3MkExist): not given a variable AST: "
	<< Z3_ast_to_string(z3_ctx, (Z3_ast)vars[i])
	<< "!"
	<< std::endl;
    }
    else //A z3 variable is a constant (!) i.e. a function with no argument
    {
      bounded_vars[i] = Z3_to_app(z3_ctx, (Z3_ast)vars[i]);
    }
  }

  Z3_ast ctr = Z3_mk_exists_const(z3_ctx, 
				  0,            // Weight 0 = auto
				  nb_vars,      // nb of bounded vars
				  bounded_vars, // z3 symbols of the bounded 
				                // variables
				  0,            // nb of patterns
				  NULL,         // array of patterns (used to 
				                // introduce the forall in a 
				                // proof)
				  (Z3_ast) body // quantifier's body
				  );

  delete[] bounded_vars;
  jniEnv->ReleaseLongArrayElements(boundedVars, vars, 0);

  return (jlong)ctr;
}
