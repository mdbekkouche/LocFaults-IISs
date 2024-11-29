#include "jni_GlpkSolver.h"
#include "jni_common.h"

#include <iostream>
#include <sstream>
#include <iomanip>
#include <climits> // max and min integer values
#include <string>

#include "glpk.h"

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkVersion
 * Signature: ()Ljava/lang/String;
 *
 * Returns the GLPK version number.
 * 
 */
JNIEXPORT jstring JNICALL Java_solver_glpk_GlpkSolver_glpkVersion
(JNIEnv *jniEnv, jclass)
{
  return jniEnv->NewStringUTF(glp_version()); 
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkInit
 * Signature: (JZ)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_glpk_GlpkSolver_glpkInit
(JNIEnv *, jclass, jlong p_lp, jboolean verbose)
{
  glp_prob *lp = (glp_prob *)p_lp;
  if (lp == NULL)
    lp = glp_create_prob();
  else
    glp_erase_prob(lp);

  if (verbose)
    glp_term_out(GLP_ON);
  else
    glp_term_out(GLP_OFF);

  //glp_bfcp parm;
  /* retrieve current values of control parameters */
  //glp_get_bfcp(lp, &parm);
  /* eps_tol is 1e-15 by default */
  //parm.eps_tol = 1e-20;
  /* set new values of control parameters */
  //glp_set_bfcp(lp, &parm);

  return (jlong)lp;
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkRelease
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_glpk_GlpkSolver_glpkRelease
  (JNIEnv *, jclass, jlong p_lp)
{
  glp_prob *lp = (glp_prob *)p_lp;
  if (lp != NULL) {
    /* TODO: Really deleting the problem and then recreating it 
     * maybe causes out-of-memory errors inside the JVM (to be confirmed!). 
     * Would it be more memory efficient to only erase the current problem ?
     * glp_erase_prob(lp);
     */
    glp_delete_prob(lp);
  }
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkNext
 * Signature: (J)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_solver_glpk_GlpkSolver_glpkSolve
(JNIEnv *, jclass, jlong p_lp)
{
  int res;
  glp_prob *lp = (glp_prob *)p_lp;

  //Uncomment to write the problems into files in CPLEX LP format
  //std::stringstream ss;
  //static int num = 1;
  //ss << "GLPK" << num++ << ".log";
  //glp_write_lp(lp, NULL, ss.str().c_str());

  //glp_smcp smcp;
  //glp_init_smcp(&smcp);
  //smcp.presolve = GLP_ON;
  //glp_sort_matrix(lp);
  //glp_scale_prob(lp, GLP_SF_AUTO);
  //glp_simplex(lp, NULL);

  //TODO: GLPK manual suggests performance improvements by calling glp_simplex 
  //      to find an optimal basis before calling glp_exact.
  if ((res=glp_exact(lp, NULL)) == 0) 
  {
    switch (glp_get_status(lp))
    {
      case GLP_OPT:    //solution is optimal
      case GLP_FEAS:   //solution is feasible
        return JNI_TRUE;
        break;
      default:
        return JNI_FALSE;
        break;
    }
  }
  else
  {
    std::cerr << "GLPK Solver error(" << res << "): the search failed!" << std::endl;
    return JNI_FALSE;
  }
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkAddVar
 * Signature: (JLjava/lang/String;)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_glpk_GlpkSolver_glpkAddVar
  (JNIEnv *jniEnv, jclass, jlong p_lp, jstring varName)
{
  const char *var_name;
  int col;
  glp_prob *lp = (glp_prob *)p_lp;

  var_name = jniEnv->GetStringUTFChars(varName, 0);

  //Adds a new column which represents a free variable :  -inf < var_name < +inf
  col = glp_add_cols(lp, 1);
  glp_set_col_name(lp, col, var_name);
  glp_set_col_bnds(lp, col, GLP_FR, 0.0, 0.0);

  jniEnv->ReleaseStringUTFChars(varName, var_name); 
	
  return col;
}

/**
 * Sets the bounds of the given LP matrix column.
 */
void setColBounds(glp_prob *lp, 
		  int col, 
		  bool boundedMin, 
		  double min, 
		  bool boundedMax, 
		  double max)
{
  if (boundedMin)
  { //Lower bound
    if (boundedMax)
    {  //Double bounded variable
      if (min == max)
        glp_set_col_bnds(lp, col, GLP_FX, min, max);
      else
        glp_set_col_bnds(lp, col, GLP_DB, min, max);
    }
    else //variable with lower bound
    {
       glp_set_col_bnds(lp, col, GLP_LO, min, max);
    }
  }
  else { //No lower bound
    if (boundedMax)
    {  //variable with upper bound
      glp_set_col_bnds(lp, col, GLP_UP, min, max);
    }
    else //unbounded variable
    {
       glp_set_col_bnds(lp, col, GLP_FR, min, max);
    }
  }
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkAddBoundedVar
 * Signature: (JLjava/lang/String;ZDZD)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_glpk_GlpkSolver_glpkAddBoundedVar(
  JNIEnv *jniEnv, 
  jclass, 
  jlong p_lp,
  jstring varName, 
  jboolean boundedMin, 
  jdouble min, 
  jboolean boundedMax, 
  jdouble max)
{
  const char *var_name;
  int col;
  glp_prob *lp = (glp_prob *)p_lp;

  var_name = jniEnv->GetStringUTFChars(varName, 0);

  //Add a new column
  col = glp_add_cols(lp, 1);
  glp_set_col_name(lp, col, var_name);

  //Set the bounds of this var
  setColBounds(lp, col, boundedMin, min, boundedMax, max);
 
  jniEnv->ReleaseStringUTFChars(varName, var_name); 
	
  return col;
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkSetVarBounds
 * Signature: (JIZDZD)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_glpk_GlpkSolver_glpkSetVarBounds(
  JNIEnv *jniEnv, 
  jclass, 
  jlong p_lp,
  jint col, 
  jboolean boundedMin, 
  jdouble min, 
  jboolean boundedMax, 
  jdouble max)
{
  setColBounds((glp_prob *)p_lp, col, boundedMin, min, boundedMax, max);
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkAddCtr
 * Signature: (JDII[I[D)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_glpk_GlpkSolver_glpkAddCtr(
  JNIEnv *jniEnv, 
  jclass, 
  jlong p_lp,
  jdouble bound, 
  jint opCode, 
  jint nb, 
  jintArray indArray, 
  jdoubleArray coeffArray)
{
  jint *ind = jniEnv->GetIntArrayElements(indArray, NULL);
  jdouble *coeff = jniEnv->GetDoubleArrayElements(coeffArray, NULL);
  glp_prob *lp = (glp_prob *)p_lp;
  
  //Creates a new row
  int row = glp_add_rows(lp, 1);

  //Sets the coefficients for this row
  //GLPK expects arrays indexed from 1, since ours are indexed from 0,
  //we pass a pointer to one address before the array so that adding
  //indexes from 1 to nb actually accesses from &array+0 to
  //&array+nb-1
  //We hope GLPK never accesses to index 0 of the arrays we pass to it... 
  glp_set_mat_row(lp, row, nb, ind-1, coeff-1);

  //Sets the bound for this row
  switch (opCode)
  {
    case LEQ:  //Less or equal operator
      glp_set_row_bnds(lp, row, GLP_UP, bound, bound);
      break;
    case EQU:  //Equal operator
      glp_set_row_bnds(lp, row, GLP_FX, bound, bound);
      break;
    case GEQ:  //Greater or equal operator
      glp_set_row_bnds(lp, row, GLP_LO, bound, bound);
      break;
    default:
      std::cerr << "This operator (code = " 
		<< opCode 
		<< ") should not appear in a standard form LP!" 
		<< std::endl;
      std::cerr << "Only <=, >= et == operators are allowed." << std::endl;
      break;
  }

  jniEnv->ReleaseDoubleArrayElements(coeffArray, coeff, 0);
  jniEnv->ReleaseIntArrayElements(indArray, ind, 0);
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkSetObjective
 * Signature: (JDZI[I[D)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_glpk_GlpkSolver_glpkSetObjective(
  JNIEnv *jniEnv, 
  jclass, 
  jlong p_lp,
  jdouble constant, 
  jboolean minimize, 
  jint nb, 
  jintArray indArray, 
  jdoubleArray coeffArray)
{
  jint *ind = jniEnv->GetIntArrayElements(indArray, NULL);
  jdouble *coeff = jniEnv->GetDoubleArrayElements(coeffArray, NULL);
  glp_prob *lp = (glp_prob *)p_lp;

  //Sets the objective direction
  if (minimize)
    glp_set_obj_dir(lp, GLP_MIN);
  else
    glp_set_obj_dir(lp, GLP_MAX);

  //Sets the constant factor of the objective
  glp_set_obj_coef(lp, 0, constant);

  //Sets the objective coefficients
  for (int i=0; i<nb; i++) 
  {
    glp_set_obj_coef(lp, ind[i], coeff[i]);
  }  

  jniEnv->ReleaseDoubleArrayElements(coeffArray, coeff, 0);
  jniEnv->ReleaseIntArrayElements(indArray, ind, 0);
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkSetObjectiveDirection
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_solver_glpk_GlpkSolver_glpkSetObjectiveDirection
  (JNIEnv *, jclass, jlong p_lp, jboolean minimize) 
{
  glp_prob *lp = (glp_prob *)p_lp;

  //Sets the objective direction
  if (minimize)
    glp_set_obj_dir(lp, GLP_MIN);
  else
    glp_set_obj_dir(lp, GLP_MAX);
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkValue
 * Signature: (JI)D
 */
extern "C"
JNIEXPORT jdouble JNICALL Java_solver_glpk_GlpkSolver_glpkValue
  (JNIEnv *, jclass, jlong p_lp, jint col)
{
  return (jdouble)glp_get_col_prim((glp_prob *)p_lp, col);
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkDelCols
 * Signature: (JI[I)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_glpk_GlpkSolver_glpkDelCols
  (JNIEnv *jniEnv, jclass, jlong p_lp, jint nb, jintArray colsArray)
{
  jint *cols = jniEnv->GetIntArrayElements(colsArray, NULL);
  glp_prob *lp = (glp_prob *)p_lp;

  //GLPK expects arrays indexed from 1, since ours are indexed from 0,
  //we pass a pointer to one address before the array so that adding
  //indexes from 1 to nb actually accesses from &array+0 to
  //&array+nb-1
  //We hope GLPK never accesses to index 0 of the arrays we pass to it... 
  glp_del_cols(lp, nb, cols-1);

  jniEnv->ReleaseIntArrayElements(colsArray, cols, 0);
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkDelRows
 * Signature: (JI[I)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_glpk_GlpkSolver_glpkDelRows
  (JNIEnv *jniEnv, jclass, jlong p_lp, jint nb, jintArray rowsArray) 
{
  jint *rows = jniEnv->GetIntArrayElements(rowsArray, NULL);
  glp_prob *lp = (glp_prob *)p_lp;

  //GLPK expects arrays indexed from 1, since ours are indexed from 0,
  //we pass a pointer to one address before the array so that adding
  //indexes from 1 to nb actually accesses from &array+0 to
  //&array+nb-1
  //We hope GLPK never accesses to index 0 of the arrays we pass to it... 
  glp_del_rows(lp, nb, rows-1);

  jniEnv->ReleaseIntArrayElements(rowsArray, rows, 0);
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkRowToString
 * Signature: (JI)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_solver_glpk_GlpkSolver_glpkRowToString
  (JNIEnv *jniEnv, jclass, jlong p_lp, jint row)
{
  std::stringstream s;
  glp_prob *lp = (glp_prob *)p_lp;
  int type = glp_get_row_type(lp, row);

  s << "";

  if (type != GLP_FR) /* skip free row */
  {
    int nb_max_cols = glp_get_num_cols(lp);
    int *ind = new int[nb_max_cols];
    double *val = new double[nb_max_cols];
    int nb_cols = glp_get_mat_row(lp, row, ind, val);
  
    if (nb_cols > 0)
    {
      if (type == GLP_DB)
      {
        s <<  glp_get_row_lb(lp, row) << " <= ";
      } 
      s << val[1] << " * " << glp_get_col_name(lp, ind[1]);
      for (int i=2; i<=nb_cols; i++) 
      {
        s << " + " << val[i] << " * " << glp_get_col_name(lp, ind[i]);		
      }
    }
    switch (type)
    {
     case GLP_FX:
       s << " = " << glp_get_row_lb(lp, row);
       break;
     case GLP_LO:
       s << " >= " << glp_get_row_lb(lp, row);
       break;
     case GLP_UP:
       s << " <= " << glp_get_row_ub(lp, row);
       break;
     case GLP_DB:
       s << " <= " << glp_get_row_ub(lp, row);
       break;
     default:
       return jniEnv->NewStringUTF("Error (GLPK): unknown row type!");
    }
  }

  return jniEnv->NewStringUTF(s.str().c_str()); 
}

/*
 * Class:     solver_glpk_GlpkSolver
 * Method:    glpkSolutionFound
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_solver_glpk_GlpkSolver_glpkSolutionFound
  (JNIEnv *, jclass, jlong p_lp)
{
  switch (glp_get_status((glp_prob *)p_lp))
  {
    case GLP_OPT:
    case GLP_FEAS:
    case GLP_UNBND:
      return JNI_TRUE;
    default:
      return JNI_FALSE;
  }
}


