#include "jni_FplibSolver.h"
#include "jni_common.h"

#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <climits> // max and min integer values
#include <cstring>
#include <cstdlib>

#include <map>
#include <queue>

#include "fpc.h"

enum Side {
  LEFT,
  RIGHT
};

class Domain {
public:
  double inf;
  double sup;

  Domain(double inf, double sup) {
    this->inf = inf;
    this->sup = sup;
  }

  Domain() {
    this->inf = -infinity();
    this->sup = infinity();
  }  

  void display() {
    std::cout << "[" << inf << " , " << sup << "]";
  }
};


class ShaveInfo {
public:
  long splitFactor;
  Domain unionDomain;
  Fpc_Variable *var;

  ShaveInfo(): splitFactor(2), unionDomain(infinity(), -infinity()), var(NULL) 
  {}

  void display() {
    std::cout << "split factor=" << splitFactor << ", ";
    unionDomain.display();
  }
};

map<string, ShaveInfo *> SHAVED_VARS_INFO;
map<string, Domain *> FLUCTUAT_DOMAINS;


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
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibInit
 * Signature: (I)J
 *
 * rndMode represents the value of a rounding mode.  The mapping
 * between values and modes depends on the order of the modes in the
 * Java enumeration validation.util.RoundingMode (starting from 0).
 * See jni_common.h
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibInit
(JNIEnv *, jclass, jint rndMode)
{
  //UNCOMMENT to enable debug trace
  //do_trace = 1;

  Fpc_Csp *csp = new Fpc_Csp();
  switch(rndMode)
  {
  case UP:
    SetRoundUp();
    break;
  case DOWN:
    SetRoundDown();
    break;
  case NEAR:
    SetRoundNear();
    break;
  case ZERO:
    SetRoundZero();
    break;
  default:
    std::cerr << "Warning: Unknown rounding mode ("
	      << rndMode
	      << ")! fplib default mode will be used..."
	      << std::endl;
    break;
  }
  return (jlong)csp;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibRelease
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_fplibRelease
(JNIEnv *, jclass, jlong p_solver)
{
  //TODO: Voir avec Claude comment le modèle est aggrégé dans le
  //      solveur et le CSP dans le modèle, car si détruire le solveur
  //      détruit aussi le modèle, il ne faut pas appeler de nouveau
  //      le destructeur sur MODEL (et idem pour CSP).  Comme il n'y a
  //      pas de destructeur explicite pour Fpc_Solver et Fpc_Model,
  //      il semblerait que ce soit à nous de le faire.

  Fpc_Solver *solver = (Fpc_Solver *)p_solver;
  if (solver != NULL) {
    delete solver->model;
    delete solver->csp;
    delete solver;
  }
}

void printCSP(Fpc_Solver *solver) {
  vector <Fpc_Variable *> vars = solver->csp->variables;
  for (int i=0; i< vars.size(); i++) {
    vars[i]->display();
    std::cout << std::endl;
  }

  vector <Fpc_Constraint *> ctrs = solver->csp->constraints;
  for (int i=0; i< ctrs.size(); i++) {
    ctrs[i]->display();
    std::cout << std::endl;
  }
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibNext
 * Signature: (J)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_solver_fplib_FplibSolver_fplibNext
  (JNIEnv *, jclass, jlong solver)
{ 
  //printCSP((Fpc_Solver *)solver);
  return ((Fpc_Solver *)solver)->searchNext();
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibKB
 * Signature: (JIDD)Z
 */
JNIEXPORT jboolean JNICALL Java_solver_fplib_FplibSolver_fplibKB
(JNIEnv *, jclass, jlong solver, jint k, jdouble kB_percent, jdouble twoB_percent)
{
  //printCSP((Fpc_Solver *)solver);
  return ((Fpc_Solver *)solver)->kB(k, kB_percent, twoB_percent);
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplib2B
 * Signature: (JD)Z
 */
JNIEXPORT jboolean JNICALL Java_solver_fplib_FplibSolver_fplib2B
(JNIEnv *, jclass, jlong solver, jdouble twoB_percent)
{
    // vector <Fpc_Variable *> vars = ((Fpc_Solver *)solver)->csp->variables;
    // for (int i=0; i< vars.size(); i++) {
    //   vars[i]->display();
    //   std::cout << std::endl;
    // }

    return ((Fpc_Solver *)solver)->TwoB(twoB_percent);
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibCreateModel
 * Signature: (J)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibCreateModel
(JNIEnv *, jclass, jlong csp)
{  
  return (jlong)new Fpc_Model(*((Fpc_Csp *)csp));
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibCreateSolver
 * Signature: (J)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibCreateSolver
(JNIEnv *, jclass, jlong model)
{
  ((Fpc_Model *)model)->extract();
  return (jlong)new Fpc_Solver(*((Fpc_Model *)model));
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibPush
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_fplibPush
(JNIEnv *, jclass, jlong solver)
{
  ((Fpc_Solver *)solver)->push();
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibPop
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_fplibPop
  (JNIEnv *, jclass, jlong solver)
{
  ((Fpc_Solver *)solver)->back();
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibSetVarBounds
 * Signature: (JDD)V
 *
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_fplibSetVarBounds
(JNIEnv *jniEnv, jclass, jlong p_fplibVar, jdouble min, jdouble max)
{
  fp_number fp_min, fp_max;
  Fpc_Domain *domain = ((Fpc_Variable *)p_fplibVar)->domain;

  switch (domain->type) {
    case FPC_INT:
      fp_min.int_nb = (int)min;
      fp_max.int_nb = (int)max;
      break;
    case FPC_FLOAT:
      fp_min.float_nb = (float)min;
      fp_max.float_nb = (float)max;
      break;
    case FPC_DOUBLE:
      fp_min.double_nb = min;
      fp_max.double_nb = max;
      break;
    default:
      std::cerr << "Error (fplibSetVarBounds): unknown type (" << domain->type << ")!" << std::endl;
      fp_min.ldouble_nb = 0;
      fp_max.ldouble_nb = 0;
      break;
  }
  
  domain->setInfTo(&fp_min);
  domain->setSupTo(&fp_max);
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibAddBoundedVar
 * Signature: (JLjava/lang/String;IDD)J
 *
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibAddBoundedVar
(JNIEnv *jniEnv, jclass, jlong csp, jstring varName, jint varType, jdouble min, jdouble max)
{
  Fpc_Variable *var;
  char *var_name = allocateCharString(jniEnv, varName);
  map<string,Domain *>::iterator it_domain;
  map<string,ShaveInfo *>::iterator it_shaveInfo;

  switch(varType) 
  {
  case 1: //INT type
    //We do not check that the min and max double values are valid integers
    var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   min, 
			   max, 
			   FPC_INT);
    break;

  case 2: //FLOAT type
    //We do not check that the min and max double values are valid floats
    var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   min, 
			   max, 
			   FPC_FLOAT);
    break;
  case 3: //DOUBLE type
    if ((it_domain = FLUCTUAT_DOMAINS.find(string(var_name))) != FLUCTUAT_DOMAINS.end())
    {
      var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   it_domain->second->inf, 
			   it_domain->second->sup, 
			   FPC_DOUBLE);      
    }
    else {
      var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   min, 
			   max, 
			   FPC_DOUBLE);
    }

    if ((it_shaveInfo = SHAVED_VARS_INFO.find(string(var_name))) != SHAVED_VARS_INFO.end()) 
    {
      it_shaveInfo->second->var = var;
    }

    break;
  default: //Invalid type
    std::cerr << "Error (fplibAddBoundedVar): Invalid type ("
	      << varType
	      << ")!" 
	      << std::endl;
    var = NULL;
    break;
  }

 return (jlong)var;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibAddVar
 * Signature: (JLjava/lang/String;I)J
 *
 * varType represents the type of the variable in the Java source program. 
 * The mapping between values and types depends on the order of the types in 
 * the Java enumeration validation.util.Type (starting from 0).
 * See jni_common.h
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibAddVar
  (JNIEnv *jniEnv, jclass, jlong csp, jstring varName, jint varType)
{
  Fpc_Variable *var;
  char *var_name = allocateCharString(jniEnv, varName);
  map<string,Domain *>::iterator it_domain;
  map<string,ShaveInfo *>::iterator it_shaveInfo;

  switch(varType) 
  {
  case 1: //INT type
    var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   INT_MIN, 
			   INT_MAX, 
			   FPC_INT);
    break;
  case 2: //FLOAT type
    var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   -infinityf(), 
			   infinityf(), 
			   FPC_FLOAT);
    break;
  case 3: //DOUBLE type
    if ((it_domain = FLUCTUAT_DOMAINS.find(string(var_name))) != FLUCTUAT_DOMAINS.end())
    {
      var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   it_domain->second->inf, 
			   it_domain->second->sup, 
			   FPC_DOUBLE);      
    }
    else {
      var = new Fpc_Variable(*((Fpc_Csp *)csp), 
			   var_name, 
			   -infinity(), 
			   infinity(), 
			   FPC_DOUBLE);
    }

    if ((it_shaveInfo = SHAVED_VARS_INFO.find(string(var_name))) != SHAVED_VARS_INFO.end()) 
    {
      it_shaveInfo->second->var = var;
    }

    break;
  default: //Invalid type
    std::cerr << "Error (fplibAddVar): Invalid type ("
	      << varType
	      << ")!" 
	      << std::endl;
    var = NULL;
    break;
  }

  return (jlong)var;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibMkVar
 * Signature: (J)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibMkVar
  (JNIEnv *, jclass, jlong p_fplibVar)
{
  return (jlong)new Constraint(*(Fpc_Variable *)p_fplibVar);
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibAddCtr
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_fplibAddCtr
  (JNIEnv *, jclass, jlong model, jlong p_ctr)
{
  //((Constraint *)p_ctr)->display();
  //std::cout << std::endl;
  ((Fpc_Model *)model)->add(*(Constraint *)p_ctr);
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibInfValueInt
 * Signature: (J)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_fplib_FplibSolver_fplibInfValueInt
  (JNIEnv *, jclass, jlong p_var)
{
  return ((Fpc_Variable *)p_var)->domain->interval.int_I.inf;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibSupValueInt
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_solver_fplib_FplibSolver_fplibSupValueInt
  (JNIEnv *, jclass, jlong p_var)
{
  return ((Fpc_Variable *)p_var)->domain->interval.int_I.sup;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibInfValueFloat
 * Signature: (J)I
 */
extern "C"
JNIEXPORT jfloat JNICALL Java_solver_fplib_FplibSolver_fplibInfValueFloat
  (JNIEnv *, jclass, jlong p_var)
{
  return ((Fpc_Variable *)p_var)->domain->interval.float_I.inf;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibSupValueFloat
 * Signature: (J)I
 */
extern "C"
JNIEXPORT jfloat JNICALL Java_solver_fplib_FplibSolver_fplibSupValueFloat
  (JNIEnv *, jclass, jlong p_var)
{
  return ((Fpc_Variable *)p_var)->domain->interval.float_I.sup;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibInfValueDouble
 * Signature: (J)I
 */
extern "C"
JNIEXPORT jdouble JNICALL Java_solver_fplib_FplibSolver_fplibInfValueDouble
  (JNIEnv *, jclass, jlong p_var)
{
  return ((Fpc_Variable *)p_var)->domain->interval.double_I.inf;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibSupValueDouble
 * Signature: (J)I
 */
extern "C"
JNIEXPORT jdouble JNICALL Java_solver_fplib_FplibSolver_fplibSupValueDouble
  (JNIEnv *, jclass, jlong p_var)
{
  return ((Fpc_Variable *)p_var)->domain->interval.double_I.sup;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibVarDisplay
 * Signature: (J)V;
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_fplibVarDisplay
  (JNIEnv *, jclass, jlong p_var)
{
  ((Fpc_Variable *)p_var)->display();
  std::cout.flush();
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibMkOp
 * Signature: (IJJ)J
 *
 * opCode represents the value of an operator's code. 
 * The mapping between values and operators depends on the order of the 
 * operators in the Java enumeration validation.util.OpCode (starting from 0).
 * See jni_common.h
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibMkOp
  (JNIEnv *, jclass, jint opCode, jlong leftExpr, jlong rightExpr)
{
  Constraint *ctr;

  switch(opCode) 
  {
  case LT:
    ctr = &((Constraint *)leftExpr)->operator<(*(Constraint *)rightExpr);
    break;
  case LEQ:
    ctr = &((Constraint *)leftExpr)->operator<=(*(Constraint *)rightExpr);
    break;
  case EQU:
    ctr = &((Constraint *)leftExpr)->operator==(*(Constraint *)rightExpr);
    break; 
  case NEQ:
    ctr = &((Constraint *)leftExpr)->operator!=(*(Constraint *)rightExpr);
    break; 
  case GEQ:
    ctr = &((Constraint *)leftExpr)->operator>=(*(Constraint *)rightExpr);
    break; 
  case GT:
    ctr = &((Constraint *)leftExpr)->operator>(*(Constraint *)rightExpr);
    break;
  case ADD:
    ctr = &((Constraint *)leftExpr)->operator+(*(Constraint *)rightExpr);
    break; 
  case SUB:
    ctr = &((Constraint *)leftExpr)->operator-(*(Constraint *)rightExpr);
    break;
  case MUL:
    ctr = &((Constraint *)leftExpr)->operator*(*(Constraint *)rightExpr);
    break;
  case DIV:
    ctr = &((Constraint *)leftExpr)->operator/(*(Constraint *)rightExpr);
    break;
  default:
    std::cerr << "Error (fplibMkOp): Unknown operator (" 
	      << opCode 
	      << ")!" 
	      << std::endl;
    return NULL;
    break;
  }

  return (jlong)ctr;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibMkMethodCall
 * Signature: (Ljava/lang/String;[J)J
 *
 * We expect the analyzed Java program to use fully qualified method names.
 */
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibMkMethodCall
  (JNIEnv *jniEnv, jclass, jstring methodName, jlongArray parameters)
{
  Constraint *ctr;
  const char *method_name = jniEnv->GetStringUTFChars(methodName, 0);
  jlong *params = jniEnv->GetLongArrayElements(parameters, NULL);

  if (strcmp(method_name, "java.lang.Math.abs") == 0) {
    ctr = &fabs(*((Constraint *)params[0]));  
  } 
  else  if (strcmp(method_name, "java.lang.Math.sqrt") == 0) {
    ctr = &sqrt(*((Constraint *)params[0]));  
  } 
  else  if (strcmp(method_name, "java.lang.Math.cos") == 0) {
    ctr = &cos(*((Constraint *)params[0]));  
  } 
  else  if (strcmp(method_name, "java.lang.Math.sin") == 0) {
    ctr = &sin(*((Constraint *)params[0]));  
  } 
  else  if (strcmp(method_name, "java.lang.Math.tan") == 0) {
    ctr = &tan(*((Constraint *)params[0]));  
  } 
  else  if (strcmp(method_name, "java.lang.Math.acos") == 0) {
    ctr = &acos(*((Constraint *)params[0]));  
  } 
  else  if (strcmp(method_name, "java.lang.Math.exp") == 0) {
    ctr = &exp(*((Constraint *)params[0]));  
  } 
  else  if (strcmp(method_name, "java.lang.Math.pow") == 0) {
    ctr = &log(*((Constraint *)params[0]));  
    ctr = &ctr->operator*(*((Constraint *)params[1]));
    ctr = &exp(*ctr);
  } 
  else {
    std::cerr << "Error (fplibMkMethodCall): unknown method (" << method_name << ")!" << std::endl;
  }

  jniEnv->ReleaseLongArrayElements(parameters, params, 0);
  jniEnv->ReleaseStringUTFChars(methodName, method_name); 

  return (jlong)ctr;
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibMkAssign
 * Signature: (JJ)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibMkAssign
  (JNIEnv *, jclass, jlong p_var, jlong p_ctr) 
{
  return (jlong)&(((Fpc_Variable *)p_var)->operator=(*(Constraint *)p_ctr));
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibMkIntCst
 * Signature: (I)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibMkIntCst
  (JNIEnv *, jclass, jint value)
{
  return (jlong)new Constraint(value);
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibMkFloatCst
 * Signature: (F)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibMkFloatCst
  (JNIEnv *, jclass, jfloat value)
{
  return (jlong)new Constraint(value);
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibMkDoubleCst
 * Signature: (D)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fplib_FplibSolver_fplibMkDoubleCst
  (JNIEnv *, jclass, jdouble value)
{
  return (jlong)new Constraint(value);
}

/* ******************************************** */
/* ***             shaving                  *** */
/* ******************************************** */

void displayShavedDomains() {
  map<string, ShaveInfo *>::iterator it_shaveInfo;
  for (it_shaveInfo=SHAVED_VARS_INFO.begin(); 
       it_shaveInfo != SHAVED_VARS_INFO.end(); 
       it_shaveInfo++) 
  {
    std::cout << it_shaveInfo->first 
	      << ": ";
    it_shaveInfo->second->unionDomain.display();
    std::cout << std::endl;
  }
}

double computeSplitSize(double inf, double sup, long splitFactor) {
  double splitSize;
		
  if (inf == -infinity() || sup == infinity()) {
    if (sup == infinity()) {
      sup = MAXDOUBLE;
    }
    else if (sup == -infinity()) {
      sup = -MAXDOUBLE;
    }
    
    if (inf == infinity()) {
      inf = MAXDOUBLE;
    }
    else if (inf == -infinity()) {
      inf = -MAXDOUBLE;
    }
    splitSize = sup/splitFactor - inf/splitFactor;
  }
  else {
    splitSize = (sup - inf) / splitFactor;
  }
  return splitSize;
}

fp_number *splitDomain(Side side, Fpc_Variable *var, double splitSize) {
  fp_number *bound = new fp_number();

  if (side == LEFT) {
    double inf = var->domain->interval.double_I.inf;
    bound->double_nb = inf + splitSize;
    var->domain->setSupTo(bound);
  }
  else {
    double sup = var->domain->interval.double_I.sup;
    bound->double_nb = sup - splitSize;
    var->domain->setInfTo(bound);
  }

  return bound;
}

void updateShavedDomain(Side side, Fpc_Domain *shaved, fp_number *bound) {
  std::cout << "Domain is shaved!" << std::endl;
  
  if (side == LEFT) {
    bound->double_nb = nextafter(bound->double_nb, infinity());
    shaved->setInfTo(bound);
  }
  else {
    bound->double_nb = nextafter(bound->double_nb, -infinity());
    shaved->setSupTo(bound);
  }
}

bool shaveSide2B(Fpc_Solver *solver, Side side, Fpc_Variable *shavedVar, 
		 double splitSize, double twoBPercent) 
{
  solver->push();
  fp_number *newBound = splitDomain(side, shavedVar, splitSize);
  
  std::cout << "shaving domain: ";
  shavedVar->display();
  std::cout << std::endl;
  
  bool consistent = solver->TwoB(twoBPercent);

  solver->back();

  if (!consistent) {
    updateShavedDomain(side, shavedVar->domain, newBound);
  }

  return !consistent;
}

void doShavedDomainsUnion() {
  map<string, ShaveInfo *>::iterator it_shaveInfo;
  Fpc_Domain *pathDomain;
  Domain *unionDomain;
  Fpc_Variable *var;
  
  for (it_shaveInfo = SHAVED_VARS_INFO.begin();
       it_shaveInfo != SHAVED_VARS_INFO.end();
       it_shaveInfo++) 
  {
    unionDomain = &(it_shaveInfo->second->unionDomain);
    var = it_shaveInfo->second->var;
    if (var != NULL) { //Variable to shave exists in current path
      pathDomain = var->domain;
      if (pathDomain->interval.double_I.inf < unionDomain->inf)
	unionDomain->inf = pathDomain->interval.double_I.inf;
      if (pathDomain->interval.double_I.sup > unionDomain->sup)
	unionDomain->sup = pathDomain->interval.double_I.sup;
    }
  }
}


/*
 * Class:     solver_fplib_FplibSolver
 * Method:    fplibShave
 * Signature: (JDDD)Z
 */
JNIEXPORT jboolean JNICALL Java_solver_fplib_FplibSolver_fplibShave
(JNIEnv *, jclass, jlong p_solver, jdouble twoBPercent, 
 jdouble intervalMinSize, jdouble intervalMinRatio)
{
  queue<Fpc_Variable *> varQ;
  Fpc_Variable *shavedVar;
  bool shaved = false;
  double splitSize;
  long splitFactor;
  bool consistent = true;
  map<string, ShaveInfo *>::iterator it_shaveInfo;
  Fpc_Solver *solver = (Fpc_Solver *)p_solver;

  for (it_shaveInfo=SHAVED_VARS_INFO.begin(); 
       it_shaveInfo != SHAVED_VARS_INFO.end(); 
       it_shaveInfo++) 
  {
    if (it_shaveInfo->second->var != NULL) {
      varQ.push(it_shaveInfo->second->var);
    }
  }
    		
  while (!varQ.empty() && consistent) {
    shavedVar = varQ.front();
    varQ.pop();
			
    std::cout << "Candidate variable for shaving: ";
    shavedVar->display();
    std::cout << std::endl;
			
    splitFactor = SHAVED_VARS_INFO[string(shavedVar->name)]->splitFactor;
    splitSize = computeSplitSize(shavedVar->domain->interval.double_I.inf, 
				 shavedVar->domain->interval.double_I.sup,
				 splitFactor);
    if (splitSize > intervalMinSize && 1.0/splitFactor > intervalMinRatio) {
      shaved = shaveSide2B(solver, LEFT, shavedVar, splitSize, twoBPercent);
      shaved |= shaveSide2B(solver, RIGHT, shavedVar, splitSize, twoBPercent);

      std::cout << "Variable after shaving: ";
      shavedVar->display();
      std::cout << std::endl;

      if (shaved) {
 	consistent = solver->TwoB(twoBPercent);
      }
      else {
	//splitFactor could be increased faster to speed up shaving (but potentially loose in precision...)
	SHAVED_VARS_INFO[string(shavedVar->name)]->splitFactor *= 2;
	std::cout << "New split Factor=" 
		  << SHAVED_VARS_INFO[string(shavedVar->name)]->splitFactor 
		  << std::endl;
      }
      varQ.push(shavedVar);
    }
  }
		
  if (consistent) {
    doShavedDomainsUnion();
  }
				
  std::cout << "This path shaved domains: " << std::endl;
  for (it_shaveInfo=SHAVED_VARS_INFO.begin(); 
       it_shaveInfo != SHAVED_VARS_INFO.end(); 
       it_shaveInfo++) 
  {
    if (it_shaveInfo->second->var != NULL) {
      it_shaveInfo->second->var->display();
      std::cout << std::endl;
    }
  }
  std::cout << "Shaved domains after this path: " << std::endl;
  displayShavedDomains();

  //Reset splitFactor and pointers to fplib variables
  for (it_shaveInfo=SHAVED_VARS_INFO.begin(); 
       it_shaveInfo != SHAVED_VARS_INFO.end(); 
       it_shaveInfo++) 
  {
    it_shaveInfo->second->splitFactor = 2;
    it_shaveInfo->second->var = NULL;
  }

  return consistent;
}

double read_double(stringstream &line_stream) {
  string token;
  line_stream >> token;
  if (token.compare("Infinity") == 0)
    return infinity();
  else if (token.compare("-Infinity") == 0)
    return -infinity();
  else
    return strtod(token.c_str(), NULL);
}


/*
 * Class:     solver_fplib_FplibSolver
 * Method:    initShaving
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_initShaving
  (JNIEnv *jniEnv, jclass, jstring fluctuatFileName)
{
  const char *file_name = jniEnv->GetStringUTFChars(fluctuatFileName, 0);
  ifstream file(file_name);
  
  cout << scientific << setprecision(20);

  if (file.is_open()) {
    string line, token;
    stringstream line_stream;
    double inf, sup;
    
    while (file.good()) {
      getline(file, line);
      std::cout << line << std::endl;
      line_stream.str(line);
      line_stream.clear();
      line_stream >> token;
      if (token.compare("Var") == 0) {
	line_stream >> token;
	SHAVED_VARS_INFO.insert(pair<string, ShaveInfo *>(token, 
							new ShaveInfo()));
      } 
      else if (token.compare("Domain") == 0) {
	line_stream >> token;
	inf = read_double(line_stream);
	sup = read_double(line_stream);
	FLUCTUAT_DOMAINS.insert(pair<string, Domain *>(token,
						      new Domain(inf, sup)));
      }
    }
  }
  else {
    std::cerr << "Error: could not open file: " 
              << *file_name 
              << "!" 
              << std::endl;
  }

  map<string, ShaveInfo *>::iterator it;
  std::cout << "SHAVED_VARS_INFO:\n";
  for (it=SHAVED_VARS_INFO.begin(); it != SHAVED_VARS_INFO.end(); it++) {
    std::cout << it->first << " => ";
    (it->second)->display();
    std::cout << std::endl;
  }

  map<string, Domain *>::iterator it2;
  std::cout << "fluctuat domains:\n";
  for (it2=FLUCTUAT_DOMAINS.begin(); it2 != FLUCTUAT_DOMAINS.end(); it2++) {
    std::cout << it2->first << " => ";
    (it2->second)->display();
    std::cout << std::endl;
  }

  jniEnv->ReleaseStringUTFChars(fluctuatFileName, file_name); 
}

/*
 * Class:     solver_fplib_FplibSolver
 * Method:    displayShavedDomains
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_solver_fplib_FplibSolver_displayShavedDomains
  (JNIEnv *, jclass) 
{
  displayShavedDomains();
}


