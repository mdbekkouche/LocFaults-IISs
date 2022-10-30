/*
 * Basic Interval Arithmetic Subroutines Rounding Control
 *
 * This is the C version adapted from the assembly version fpRound.s which is
 * not position independent and would prevent from building the JNI dynamic library 
 * on AMD64 architectures (such as Crabe).
 */

#include <fenv.h>

void BiasRoundUp() 
{
  fesetround(FE_UPWARD);
}

void BiasRoundDown() 
{
  fesetround(FE_DOWNWARD);
}

void BiasRoundNear() 
{
  fesetround(FE_TONEAREST);
}
