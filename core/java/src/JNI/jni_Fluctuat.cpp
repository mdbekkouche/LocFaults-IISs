#include "jni_Fluctuat.h"
#include "jni_common.h"

#include "DebugInterface.h"

#include <iostream>

/* Global variables storing information on debug status */
const char *POINT, *SOURCE;
int LINE;

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
 * Class:     solver_fluctuat_Fluctuat
 * Method:    createDebugInterface
 * Signature: ()J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fluctuat_Fluctuat_createDebugInterface
(JNIEnv *, jclass) 
{
  return (jlong)createDebugInterface();
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugAddSourceFile
 * Signature: (JLjava/lang/String;)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_solver_fluctuat_Fluctuat_debugAddSourceFile
  (JNIEnv *jniEnv, jclass, jlong fluctuat, jstring cppFile)
{
  // TODO: If debugAddSourceFile copies the file name, we should free
  //       the cpp_file buffer here.
  char *cpp_file = allocateCharString(jniEnv, cppFile);
  return debugAddSourceFile((DebugInterface*)fluctuat, cpp_file);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugSetResourceFile
 * Signature: (JLjava/lang/String;)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_solver_fluctuat_Fluctuat_debugSetResourceFile
(JNIEnv *jniEnv, jclass, jlong fluctuat, jstring rcFile) 
{
  // TODO: If debugSetResourceFile copies the file name, we should free
  //       the rc_file buffer here.
  char *rc_file = allocateCharString(jniEnv, rcFile);
  return debugSetResourceFile((DebugInterface*)fluctuat, rc_file);  
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugAddFunctionBreakPoint
 * Signature: (JLjava/lang/String;J)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_solver_fluctuat_Fluctuat_debugAddFunctionBreakPoint
(JNIEnv *jniEnv, jclass, jlong fluctuat, jstring fctName, jlong cond) 
{
  // TODO: If debugAddFunctionBreakPoint copies the file name, we should free
  //       the fct buffer here.
  char *fct = allocateCharString(jniEnv, fctName);
  return debugAddFunctionBreakPoint((DebugInterface*)fluctuat, 
				    fct, 
				    (BreakCondition*)cond);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugAddBreakPoint
 * Signature: (JLjava/lang/String;IJ)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_solver_fluctuat_Fluctuat_debugAddBreakPoint
(JNIEnv *jniEnv, jclass, jlong fluctuat, jstring fileName, jint line, jlong cond) 
{
  char *file_name = allocateCharString(jniEnv, fileName);
  return debugAddBreakPoint((DebugInterface*)fluctuat, 
			    file_name, 
			    line,
			    (BreakCondition*)cond);  
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugDelBreakPoints
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_solver_fluctuat_Fluctuat_debugDelBreakPoints
(JNIEnv *, jclass, jlong fluctuat)
{
  return debugDelBreakPoints((DebugInterface*)fluctuat);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugAnalyze
 * Signature: (JLjava/lang/String;JJ)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_fluctuat_Fluctuat_debugAnalyze
(JNIEnv *jniEnv, jclass, jlong fluctuat, jstring fctName, jlong stopFct, 
 jlong memory) 
{
  // TODO: If debugAnalyze copies the file name, we should free
  //       the fct buffer here.
  char *fct = allocateCharString(jniEnv, fctName);
  return debugAnalyze((DebugInterface*)fluctuat, 
		      fct, 
		      (DebugStopFunction) stopFct,
		      (void *)memory);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugContinue
 * Signature: (JI)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_fluctuat_Fluctuat_debugContinue
  (JNIEnv *, jclass, jlong fluctuat, jint count)
{
  //There is a "bug" in Fluctuat v. 3.873 that POINT must be reset to NULL
  //before calling debugContinue
  POINT = NULL;
  return debugContinue((DebugInterface*)fluctuat, count, &POINT, &SOURCE, &LINE);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugNext
 * Signature: (JI)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_fluctuat_Fluctuat_debugNext
  (JNIEnv *, jclass, jlong fluctuat, jint count)
{
  //There is a "bug" in Fluctuat v. 3.873 that POINT must be reset to NULL
  //before calling debugNext
  POINT = NULL;
  return debugNext((DebugInterface*)fluctuat, count, &POINT, &SOURCE, &LINE);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugStep
 * Signature: (JI)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_fluctuat_Fluctuat_debugStep
(JNIEnv *, jclass, jlong fluctuat, jint count) 
{
  //There is a "bug" in Fluctuat v. 3.873 that POINT must be reset to NULL
  //before calling debugStep
  POINT = NULL;
  return debugStep((DebugInterface*)fluctuat, count, &POINT, &SOURCE, &LINE);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugStepInstruction
 * Signature: (JI)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_fluctuat_Fluctuat_debugStepInstruction
(JNIEnv *, jclass, jlong fluctuat, jint count) 
{
  //There is a "bug" in Fluctuat v. 3.873 that POINT must be reset to NULL
  //before calling debugStepInstruction
  POINT = NULL;
  return debugStepInstruction((DebugInterface*)fluctuat, 
			      count, 
			      &POINT, 
			      &SOURCE, 
			      &LINE);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    getLineStatus
 * Signature: ()I
 */
extern "C"
JNIEXPORT jint JNICALL Java_solver_fluctuat_Fluctuat_getLineStatus
(JNIEnv *, jclass)
{
  return LINE;
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    getSourceStatus
 * Signature: ()Ljava/lang/String;
 */
extern "C"
JNIEXPORT jstring JNICALL Java_solver_fluctuat_Fluctuat_getSourceStatus
(JNIEnv *jniEnv, jclass)
{
  return jniEnv->NewStringUTF(SOURCE);
}


/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    getPointStatus
 * Signature: ()Ljava/lang/String;
 */
extern "C"
JNIEXPORT jstring JNICALL Java_solver_fluctuat_Fluctuat_getPointStatus
(JNIEnv *jniEnv, jclass) 
{
  return jniEnv->NewStringUTF(POINT);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugGetVariable
 * Signature: (JLjava/lang/String;)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_solver_fluctuat_Fluctuat_debugGetVariable
(JNIEnv *jniEnv, jclass, jlong fluctuat, jstring varName) 
{
  // TODO: If debugGetVariable copies the var name or do not store it, 
  //       we should free the var_name buffer here.
  char *var_name = allocateCharString(jniEnv, varName);
  return (jlong)debugGetVariable((DebugInterface*)fluctuat, var_name);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugFromCurrentRetrieveResult
 * Signature: (JJ[D)Z
 */
JNIEXPORT jboolean JNICALL Java_solver_fluctuat_Fluctuat_debugFromCurrentRetrieveResult
(JNIEnv *jniEnv, jclass, jlong fluctuat, jlong var, jdoubleArray bounds) 
{
  DebugResult var_bounds;
  int ret = debugFromCurrentRetrieveResult((DebugInterface*)fluctuat, 
					   (InternVariable*)var,
					   &var_bounds);
  jdouble array_var_bounds[6] = {
    var_bounds.minDomain,
    var_bounds.maxDomain,
    var_bounds.minFloatDomain,
    var_bounds.maxFloatDomain,
    var_bounds.minError,
    var_bounds.maxError 
  };
  jniEnv->SetDoubleArrayRegion(bounds, 0, 6, array_var_bounds);
  return ret;
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugFromCurrentSetResult
 * Signature: (JJ[D)Z
 */
JNIEXPORT jboolean JNICALL Java_solver_fluctuat_Fluctuat_debugFromCurrentSetResult
(JNIEnv *jniEnv, jclass, jlong fluctuat, jlong var, jdoubleArray bounds) 
{
  DebugResult var_bounds;
  double *array_var_bounds;

  array_var_bounds = jniEnv->GetDoubleArrayElements(bounds, NULL);
  var_bounds.minDomain = array_var_bounds[0];
  var_bounds.maxDomain = array_var_bounds[1];
  var_bounds.minFloatDomain = array_var_bounds[2];
  var_bounds.maxFloatDomain = array_var_bounds[3];
  var_bounds.minError = array_var_bounds[4];
  var_bounds.maxError = array_var_bounds[5]; 
  jniEnv->ReleaseDoubleArrayElements(bounds, array_var_bounds, 0);

  return debugFromCurrentSetResult((DebugInterface*)fluctuat, 
				   (InternVariable*)var,
				   &var_bounds);
}

/*
 * Class:     solver_fluctuat_Fluctuat
 * Method:    debugClearAnalyzer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_solver_fluctuat_Fluctuat_debugClearAnalyzer
(JNIEnv *, jclass, jlong fluctuat)
{
  debugClearAnalyzer((DebugInterface*)fluctuat);
}
