#ifndef JNI_IBEX_H
#define JNI_IBEX_H

#include <vector>

#include "IbexEnv.h"
#include "IbexSpace.h"
#include "IbexContractor.h"

/*
 * Deletes all the vector's elements and empties the vector.
 * Defined in jni_IbexSolver but must be visible to the 
 * JNI IbexExpressionVisitor implementation.
 */
template<class T> void empty_vector(vector<T>& vec);

#endif
