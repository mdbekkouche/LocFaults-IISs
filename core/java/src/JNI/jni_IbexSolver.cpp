#include "jni_IbexSolver.h"

#include <iostream>
#include <vector>

#include "jni_Ibex.h"

#include "IbexToken.h"
#include "IbexConstraint.h"
#include "IbexPaver.h"
#include "IbexEnv.h"
#include "IbexExpr.h"
#include "IbexDomain.h"
#include "IbexSpace.h"
#include "IbexContractor.h"
#include "IbexBisector.h"
#include "IbexPropagation.h"
#include "IbexHC4Revise.h"
#include "IbexSequence.h"
#include "Interval.h"



/* We define integer max and min values. We are interested in Java 'int' integers, 
 * which are 32-bit integers. 
 */
static const int INT_MAX_VALUE = 0x7FFFFFFF;
static const int INT_MIN_VALUE = 0x80000000;

/*
 * Deletes all the vector's elements and empties the vector.
 */
template<class T>
void empty_vector(vector<T>& vec) 
{ 
	while (!vec.empty()) 
	{
		delete vec.back();
		vec.pop_back(); 
	}
}

/*
 * Creates the natural contractor (i.e. HC4Revise managed) for the
 * given constraint.
 */
ibex::Contractor * create_natural_ctc(ibex::Constraint const &ctr, 
				      ibex::Space &space)
{
  return new ibex::HC4Revise(ctr, space);
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexCreateEnv
 * Signature: ()J
 *
 * This method should always be called first. It creates the paver environment 
 * where to store variables and constraints.
 */
extern "C" 
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexCreateEnv
  (JNIEnv *, jclass)
{
  return (jlong)new ibex::Env();
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexCreateSpace
 * Signature: (J)J
 *
 * This method should be called after initialization and before
 * creation of the paver space.
 */
extern "C" 
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexCreateSpaceFactory
(JNIEnv *, jclass, jlong env)
{
  return (jlong)new ibex::SpaceFactory(*(ibex::Env *)env);
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexCreateSpace
 * Signature: (J)J
 *
 * This method should be called after the creation of the space factory.
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexCreateSpace
(JNIEnv *, jclass, jlong space_factory)
{
  return (jlong)((ibex::SpaceFactory *)space_factory)->build_space();
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexCreateCTCVector
 * Signature: ()J
 *
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexCreateCTCVector
  (JNIEnv *, jclass)
{
  return (jlong)new vector< const ibex::Contractor * >();
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexRelease
 * Signature: (JJJJJ)V
 *
 * Releases the memory resources allocated for the paver.
 */
extern "C" 
JNIEXPORT void JNICALL Java_solver_ibex_IbexSolver_ibexRelease(
   JNIEnv *, 
   jclass, 
   jlong ctc_vector,
   jlong paver,
   jlong space, 
   jlong space_factory, 
   jlong env)
{
  vector< const ibex::Contractor * > *p_ctc_vector = 
    (vector< const ibex::Contractor * > *)ctc_vector;
  if (p_ctc_vector)
    empty_vector(*p_ctc_vector);
  delete (ibex::Paver *)paver;
  delete (ibex::Space *)space;
  delete (ibex::SpaceFactory *)space_factory;
  delete (ibex::Env *)env;
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexExplore
 * Signature: (JJ)V
 *
 * This method explores the search space for solutions to the CSP.
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_ibex_IbexSolver_ibexExplore
(JNIEnv *, jclass, jlong space, jlong p_paver)
{
  ibex::Space *paver_space = (ibex::Space *)space;
  ibex::Paver *paver = (ibex::Paver *)p_paver;
  //paver->select(0);
  //paver->solver_mode = true;
  //paver->trace=true;
  //paver->restart();
  //paver->next_box();
  paver->explore();

  //Now we print information about solutions
  paver->report();
  int n_ctc = 0; //The number of the contractor of interest
  for (int j=0; j<paver->nb_boxes(n_ctc); j++) {
    for (int k=1; k<=paver_space->nb_var(); k++) {
      cout << "[" << Inf(paver->box(n_ctc,j)(k)) << "," << Sup(paver->box(n_ctc,j)(k)) << "]";
    }
    cout << endl;
  }
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexAddVar
 * Signature: (JLjava/lang/String;)V
 *
 * Adds a variable in the environment. At this level, we do not keep track of its type, and hence domain: 
 * the domain information will be added through the paver space factory. 
 */
extern "C" 
JNIEXPORT void JNICALL Java_solver_ibex_IbexSolver_ibexAddVar
  (JNIEnv *jniEnv, jclass, jlong env, jstring varName)
{
	char const *var_name = jniEnv->GetStringUTFChars(varName, 0);
	//Ibex symbols are defined over three dimensions. 
	//Talking about a single variable is talking about a 
        //one item three dimensional array!
	((ibex::Env *)env)->add_symbol(var_name, ibex::Dim(0,0,0));
	jniEnv->ReleaseStringUTFChars(varName, var_name); 
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexSetVarDomain
 * Signature: (JLjava/lang/String;I)V
 *
 * Adds domain information to the variables present in the environment.
 * This method requires the space factory has been created.
 * varType represents the type of the variable in the Java source
 * program. The mapping between values and types depends on the order
 * of the types in the Java enumeration validation.util.Type (starting
 * from 0).
 *
 */
extern "C" 
JNIEXPORT void JNICALL Java_solver_ibex_IbexSolver_ibexSetVarDomain
  (JNIEnv *jniEnv, jclass, jlong space_factory, jstring varName, jint varType)
{
  const char *var_name = jniEnv->GetStringUTFChars(varName, 0); 
  ibex::SpaceFactory *paver_space_factory = 
    (ibex::SpaceFactory *)space_factory;
  switch(varType) {
  case 1: //INT type => is assumed to be a variable for the moment
    paver_space_factory->set_entity(var_name, 
				    ibex::IBEX_VAR, 
				    INTERVAL(INT_MIN_VALUE, INT_MAX_VALUE));
    break;
  case 2: //FLOAT type => is assumed to be a variable for the moment
    paver_space_factory->set_entity(var_name, 
				    ibex::IBEX_VAR, 
				    INTERVAL(BiasNegInf, BiasPosInf));
    break;
  case 3: //DOUBLE type => is assumed to be a variable for the moment
    paver_space_factory->set_entity(var_name, 
				    ibex::IBEX_VAR, 
				    INTERVAL(BiasNegInf, BiasPosInf));
    break;
  default: //Invalid type
    cerr << "Error (ibexSetVarDomain): Invalid type!" << endl;
    break;
  }
  jniEnv->ReleaseStringUTFChars(varName, var_name); 
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexCreateContractors
 * Signature: (JJD)J
 * 
 * Creates a paver instance.
 * The paver space and environment should exist before calling this method.
 * All the contractors are handled through a Propagation contractor 
 * (a kind of AC3 propagation algorithm).
 *
 * A Precision contractor is also added whose ceil is defined by the precision parameter.
 * The bisection algorithm is provided by a RoundRobin whose precision is the same as the 
 * Precision contractor.
 * 
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexCreatePaver
  (JNIEnv *, jclass, jlong jspace, jlong p_ctc_vector, jdouble precision)
{
  vector< const ibex::Contractor * > &ctc_vector = 
    *(vector< const ibex::Contractor * > *)p_ctc_vector;
  ibex::Space *paver_space = (ibex::Space *)jspace;
  
  //Adds a precision contractor to the list of contractors
  ctc_vector.push_back(new ibex::Precision(*paver_space, precision));
  
  //Creates a Propagation contractor to handle all the constraints' contractors
  vector< const ibex::Contractor * > propagation_ctc_vector;
  propagation_ctc_vector.push_back(new ibex::Propagation(ctc_vector, *paver_space));

  //The Propagation constructor copies the given list of contractors
  //so we can empty ctc_vector here, but we could as well wait it is
  //done in ibexRelease...
  empty_vector(ctc_vector);

  //Now we have all we need to create the paver with its RoundRobin bisector.
  ibex::Paver *paver = new ibex::Paver(*paver_space, 
				       propagation_ctc_vector, 
				       ibex::RoundRobin(*paver_space, 
							precision));

  //The Paver constructor copies the given list of contractors so we
  //empty propagation_ctc_vector here
  empty_vector(propagation_ctc_vector);

  return (jlong)paver;
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexNewCmpOp
 * Signature: (JJIJJ)V
 *
 * Creates a new ibex contractor from a comparison expression and adds
 * it to the current list of contractors. All the contractors are
 * HC4Revise contractors.

 */
extern "C"
JNIEXPORT void JNICALL Java_solver_ibex_IbexSolver_ibexNewCmpOp
  (JNIEnv *, 
   jclass,
   jlong space,
   jlong p_ctc_vector, 
   jint opCode, 
   jlong leftExpr, 
   jlong rightExpr)
{
  ibex::Expr const *zero_expr;
  ibex::Contractor const *ctc;
  ibex::Space *paver_space = (ibex::Space *)space;
  vector< const ibex::Contractor * > &ctc_vector = 
    *(vector< const ibex::Contractor * > *)p_ctc_vector;

  switch(opCode) {
  case ibex::LT   :  
  case ibex::LEQ  : 
  case ibex::GEQ  :   
  case ibex::GT   : //Inequality CmpOpType 
    zero_expr = &ibex::BinOpExpr::new_(*(ibex::Expr *)leftExpr, 
				       ibex::SUB, 
				       *(ibex::Expr *)rightExpr);
    ctc = create_natural_ctc(ibex::Inequality::new_(*zero_expr, 
						    (ibex::CmpOpType)opCode), 
			     *paver_space);
    ctc_vector.push_back(ctc);
    break;	  
    //case ibex::M_EQU:
  case ibex::EQU  : //Equality CmpOpType
    zero_expr = &ibex::BinOpExpr::new_(*(ibex::Expr *)leftExpr, 
				       ibex::SUB, 
				       *(ibex::Expr *)rightExpr);
    ctc = create_natural_ctc(ibex::Equality::new_(*zero_expr), *paver_space);
    ctc_vector.push_back(ctc);
    break;
  default:
    cerr << "Error (ibexNewCmpOp): Unknown op code: " << opCode << " !" << endl;
    break;
  }
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexNewUnion
 * Signature: (JJ)V
 *
 * Creates a disjunction sequence contractor from the last two
 * contractors added to the list of contractors ctc_vector.
 */
extern "C"
JNIEXPORT void JNICALL Java_solver_ibex_IbexSolver_ibexNewUnion
  (JNIEnv *, jclass, jlong space, jlong p_ctc_vector)
{
  vector< const ibex::Contractor * > ctc_union;
  ibex::Space *paver_space = (ibex::Space *)space;
  vector< const ibex::Contractor * > &ctc_vector = 
    *(vector< const ibex::Contractor * > *)p_ctc_vector;

  //Moves the last two contractors from ctc_vector to a new vector
  ctc_union.push_back(ctc_vector.back());
  ctc_vector.pop_back();
  ctc_union.push_back(ctc_vector.back());
  ctc_vector.pop_back();

  //Creates the two contractors' union and adds it to the
  //current list of contractors
  ctc_vector.push_back(new ibex::Sequence(ctc_union, *paver_space, false));

  //The Sequence constructor has copied the contractors so we
  //can delete them in ctc_union
  empty_vector(ctc_union);
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexNewCst
 * Signature: (JD)J
 *
 * Creates a new constant with the given value and returns a pointer
 * to it cast into a 'long int'.
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexNewCst
  (JNIEnv *, jclass, jlong env, jdouble value)
{
  return (jlong)&ibex::Constant::new_scalar(*(ibex::Env *)env, 
					    INTERVAL(value, value));
}

/*
  * Class:     solver_ibex_IbexSolver
  * Method:    ibexGetSymbol
  * Signature: (JLjava/lang/String;)J
  * 
  * Returns a pointer (cast into an int) to the ibex symbol of the
  * given name.
  */
 extern "C"
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexGetSymbol
  (JNIEnv *jniEnv, jclass, jlong env, jstring varName)
 {
   char const *var_name = jniEnv->GetStringUTFChars(varName, 0); 
   jlong symbol = (jlong)&((ibex::Env *)env)->symbol_expr(var_name);
   jniEnv->ReleaseStringUTFChars(varName, var_name);
   return symbol;
 }

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexGetLB
 * Signature: (JLjava/lang/String;)D
 *
 */
extern "C"
JNIEXPORT jdouble JNICALL Java_solver_ibex_IbexSolver_ibexGetLB
  (JNIEnv *jniEnv, jclass, jlong space, jstring varName)
{
  char const *var_name = jniEnv->GetStringUTFChars(varName, 0); 
  jdouble val = (jdouble)Inf(((ibex::Space *)space)->domain(var_name));
  jniEnv->ReleaseStringUTFChars(varName, var_name);
  return val;
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexGetUB
 * Signature: (J)D
 *
 */
extern "C"
JNIEXPORT jdouble JNICALL Java_solver_ibex_IbexSolver_ibexGetUB
  (JNIEnv *jniEnv, jclass, jlong space, jstring varName)
{
  char const *var_name = jniEnv->GetStringUTFChars(varName, 0); 
  jdouble val = (jdouble)Sup(((ibex::Space *)space)->domain(var_name));
  jniEnv->ReleaseStringUTFChars(varName, var_name);
  return val;
}

/*
 * Class:     solver_ibex_IbexSolver
 * Method:    ibexNewOp
 * Signature: (IJJ)J
 *
 * Creates a new ibex expression and returns it as a pointer cast into
 * a 'long int'.
 * Most of the IBEX opCodes are unused in Java programs.
 * Still, they are included as comments for future extensions.
 *
 * The two long int's leftExpr and rightExpr are actually pointers to
 * ibex::Expr objects.
 *
 * Returns a pointer to the newly built operator expression.
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_ibex_IbexSolver_ibexNewOp
  (JNIEnv *, jclass, jint opCode, jlong leftExpr, jlong rightExpr)
{
  ibex::Expr const *res_expr;
  
  switch(opCode) {
  case ibex::ADD    : 
  case ibex::SUB    :
  case ibex::MUL    :
  case ibex::DIV    :
    //case ibex::MIN    :
    //case ibex::MAX    :
    //case ibex::ARCTAN2:
    //case ibex::M_ADD  :
    //case ibex::M_SUB  :
    //case ibex::M_MUL  :
    //case ibex::M_SCAL :
    //case ibex::M_VEC  :
    //case ibex::V_DOT  : //BinOpType
    res_expr = &ibex::BinOpExpr::new_(*(ibex::Expr *)leftExpr, 
				      (ibex::BinOpType)opCode, 
				      *(ibex::Expr *)rightExpr);
    break;
    //case ibex::SIGN   :
  case ibex::MINUS  :
    //case ibex::SQR    :
    //case ibex::SQRT   :
    //case ibex::LOG    :
    //case ibex::EXP    :
    //case ibex::COS    :
    //case ibex::SIN    :
    //case ibex::TAN    :
    //case ibex::ARCCOS :
    //case ibex::ARCSIN :
    //case ibex::ARCTAN :
    //case ibex::COSH   :
    //case ibex::SINH   :
    //case ibex::TANH   :
    //case ibex::ARCCOSH:
    //case ibex::ARCSINH:
    //case ibex::ARCTANH:
    //case ibex::ABS    :
    //case ibex::M_MINUS:
    //case ibex::M_TRANS: //UnaOpType
    res_expr = &ibex::UnaOpExpr::new_((ibex::UnaOpType)opCode, 
				      *(ibex::Expr *)leftExpr);
    break;
  default:
    cerr << "Error (ibexNewOp): Unknown op code: " << opCode << " !" << endl;
    break;
  }
  
  return (jlong)res_expr;
}

