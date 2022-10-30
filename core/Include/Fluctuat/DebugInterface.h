#ifndef DEBUG_INTERFACEH
#define DEBUG_INTERFACEH

#ifdef DEBUG_EXPORT_DLL
#define DEBUG_DLL_IMPORT __declspec(dllexport)
#else
#define DEBUG_DLL_IMPORT
/* #define DEBUG_DLL_IMPORT __declspec(dllimport) */
#endif

#ifdef _MSC_VER
#pragma pack(push,4)
#endif

#ifdef __cplusplus
extern "C" {
#endif

typedef struct _DebugInterface DebugInterface;
typedef struct _InternVariable InternVariable;

typedef int DebugVerdict; /* enum { DVFail, DVOk } */

DEBUG_DLL_IMPORT DebugInterface* createDebugInterface();

DEBUG_DLL_IMPORT void debugSetInclude(DebugInterface* fluctuat, const char* path);
DEBUG_DLL_IMPORT DebugVerdict debugAddSourceFile(DebugInterface* fluctuat, const char* fileName);
DEBUG_DLL_IMPORT DebugVerdict debugSetResourceFile(DebugInterface* fluctuat, const char* fileName);
DEBUG_DLL_IMPORT DebugVerdict debugSetLookAtLocalVariable(DebugInterface* fluctuat, unsigned int ident, const char* variableName, int uLineSrc, const char* fileSrc);
DEBUG_DLL_IMPORT DebugVerdict debugSetLookAtLocalVariableAtEndFunction(DebugInterface* fluctuat, unsigned int ident, const char* variableName, const char* functionName);
DEBUG_DLL_IMPORT void debugDontLookAtGlobalVariables(DebugInterface* fluctuat);
DEBUG_DLL_IMPORT void debugDontLookAtMainVariables(DebugInterface* fluctuat);

enum TypeBreakCondition { TBreakExact, TBreakInterval, TBreakBottom, TBreakTop };
typedef struct _BreakCondition {
   const char* variableName;
   enum TypeBreakCondition typeBreak;
   double minFloat;
   double maxFloat;
} 
#ifdef __GNUC__
__attribute__((aligned(4), packed))
#endif
BreakCondition;

DEBUG_DLL_IMPORT DebugVerdict debugAddBreakPoint(DebugInterface* fluctuat, const char* fileName, int line, BreakCondition* condition);
DEBUG_DLL_IMPORT DebugVerdict debugDelBreakPoints(DebugInterface* fluctuat);
DEBUG_DLL_IMPORT DebugVerdict debugAddBreakPointOnCurrent(DebugInterface* fluctuat, BreakCondition* condition);
DEBUG_DLL_IMPORT DebugVerdict debugAddFunctionBreakPoint(DebugInterface* fluctuat, const char* function, BreakCondition* condition);

typedef int DebugExecution; /* enum { DEFail, DEFinish, DECurrent }; */
typedef int (*DebugStopFunction)(void* stopCallBackMemory);

DEBUG_DLL_IMPORT DebugExecution debugAnalyze(DebugInterface* fluctuat, const char* entryFunction, DebugStopFunction stopCallBack, void* stopCallBackMemory);
DEBUG_DLL_IMPORT DebugExecution debugAnalyzeMain(DebugInterface* fluctuat, DebugStopFunction stopCallBack, void* stopCallBackMemory);
// DEBUG_DLL_IMPORT DebugExecution debugExecuteCommand(DebugInterface* fluctuat, const char* command, const char** result, const char** fileSource, int* lineSource);
DEBUG_DLL_IMPORT DebugVerdict debugGetFunctionPosition(DebugInterface* fluctuat, const char** point, const char** file, int* line);

DEBUG_DLL_IMPORT DebugExecution debugNext(DebugInterface* fluctuat, int count, const char** point, const char** fileSource, int* lineSource);
DEBUG_DLL_IMPORT DebugExecution debugStep(DebugInterface* fluctuat, int count, const char** point, const char** fileSource, int* lineSource);
DEBUG_DLL_IMPORT DebugExecution debugStepInstruction(DebugInterface* fluctuat, int count, const char** point, const char** fileSource, int* lineSource);
DEBUG_DLL_IMPORT DebugExecution debugFinish(DebugInterface* fluctuat, int count, const char** fileSource, const char** point, int* lineSource);
DEBUG_DLL_IMPORT DebugExecution debugContinue(DebugInterface* fluctuat, int count, const char** point, const char** fileSource, int* lineSource);

typedef struct _DebugResult {
   double minDomain;
   double maxDomain;
   double minFloatDomain;
   double maxFloatDomain;
   double minError;
   double maxError;
}
#ifdef __GNUC__
__attribute__((aligned(4), packed))
#endif
DebugResult;

typedef struct _DebugViewResult {
   const char* typedVar;
   double min;
   double max;
   double minFloat;
   double maxFloat;
   double minError;
   double maxError;
   double minRelativeError;
   double maxRelativeError;
   double minHighOrderError;
   double maxHighOrderError;
}
#ifdef __GNUC__
__attribute__((aligned(4), packed))
#endif
DebugViewResult;

typedef struct _DebugErrorOrigin {
   char valid;
   int line;
   const char* filename;
   const char* funName;
   double maxCoefficient;
   double minCoefficient;
}
#ifdef __GNUC__
__attribute__((aligned(4), packed))
#endif
DebugErrorOrigin;

DEBUG_DLL_IMPORT DebugVerdict debugPrintVariable(DebugInterface* fluctuat, const char* variableName, const char** result);
DEBUG_DLL_IMPORT DebugVerdict debugDPrintVariable(DebugInterface* fluctuat, const char* variableName, const char** result);
DEBUG_DLL_IMPORT DebugVerdict debugViewVariable(DebugInterface* fluctuat, const char* variableName, DebugViewResult* result, DebugErrorOrigin* errorOrigin);
DEBUG_DLL_IMPORT DebugVerdict debugViewNextOrigin(DebugInterface* fluctuat, DebugErrorOrigin* errorOrigin);
DEBUG_DLL_IMPORT int debugGetCountInstructions(DebugInterface* fluctuat);
DEBUG_DLL_IMPORT InternVariable* debugGetVariable(DebugInterface* fluctuat, const char* variableName);
DEBUG_DLL_IMPORT DebugVerdict debugFromCurrentRetrieveResult(DebugInterface* fluctuat, InternVariable* variable, DebugResult* result);
DEBUG_DLL_IMPORT DebugVerdict debugFromCurrentSetResult(DebugInterface* fluctuat, InternVariable* variable, DebugResult* result);
DEBUG_DLL_IMPORT DebugVerdict debugFromCurrentSetInterval(DebugInterface* fluctuat, InternVariable* variable, DebugResult* result);

typedef struct _DebugVariable {
   const char* name;
   int lineSource;
   const char* fileSource;
   int lineDeclaration;
   const char* fileDeclaration;
   int lineLastAssign;
   const char* fileLastAssign;
   unsigned int ident;

   double minDomain;
   double maxDomain;
   double minFloatDomain;
   double maxFloatDomain;
   double minError;
   double maxError;
   double accuracy;
}
#ifdef __GNUC__
__attribute__((aligned(4), packed))
#endif
DebugVariable;

DEBUG_DLL_IMPORT int debugRetrieveVariableAndResult(DebugInterface* fluctuat, DebugVariable* variable);
DEBUG_DLL_IMPORT void freeDebugInterface(DebugInterface* fluctuat);
DEBUG_DLL_IMPORT void debugClearAnalyzer(DebugInterface* debug);

#ifdef __cplusplus
}
#endif

#ifdef _MSC_VER
#pragma pack(pop)
#endif

#endif

