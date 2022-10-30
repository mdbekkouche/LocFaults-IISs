
/************************************************/
/* fpc.h :                                      */
/*   floating point constraint solver           */
/*                                              */
/* vs 1/02/2005                                 */
/* copyright Claude Michel                      */
/*                                              */
/************************************************/




#ifndef _FPC_H
#define _FPC_H

using namespace std;

#include <stdio.h>
#include <fpi.h>

#include <vector>
#include <queue>

using namespace std;

extern int do_trace;
extern int use_new_constraints;

#define FPC_FLOAT   1
#define FPC_DOUBLE  2
#define FPC_LDOUBLE 3
#define FPC_SHORT   4
#define FPC_INT     5
#define FPC_LLONG   6
#define FPC_USHORT  7
#define FPC_UINT    8
#define FPC_ULLONG  9


#define C_variable     1
#define C_float        2
#define C_double       3
#define C_ldouble      4
#define C_short        5
#define C_int          6
#define C_llong        7
#define C_ushort       8
#define C_uint         9
#define C_ullong      10

#define C_roundup     20
#define C_rounddown   21
#define C_roundnear   22
#define C_roundzero   23

#define C_float_precision     25
#define C_double_precision    26
#define C_ldouble_precision   27
#define C_any_precision       28

#define C_set         50

#define C_neg        100

#define C_add        200
#define C_sub        201
#define C_mult       202
#define C_div        203

#define C_equal      300
#define C_lessthan   301
#define C_morethan   302
#define C_lesseq     303
#define C_moreeq     304
#define C_noteq      305

#define C_sqrtf      0x1000
#define C_sqrt       0x2000
#define C_sqrtl      0x3000

#define C_xxf        0x1001
#define C_xx         0x2001
#define C_xxl        0x3001

#define C_fabsf      0x1002
#define C_fabs       0x2002
#define C_fabsl      0x3002

#define C_sinf       0x1003
#define C_sin        0x2003
#define C_sinl       0x3003

#define C_cosf       0x1004
#define C_cos        0x2004
#define C_cosl       0x3004

#define C_tanf       0x1005
#define C_tan        0x2005
#define C_tanl       0x3005

#define C_asinf      0x1006
#define C_asin       0x2006
#define C_asinl      0x3006

#define C_acosf      0x1007
#define C_acos       0x2007
#define C_acosl      0x3007

#define C_atanf      0x1008
#define C_atan       0x2008
#define C_atanl      0x3008

#define C_expf       0x1009
#define C_exp        0x2009
#define C_expl       0x3009

#define C_logf       0x100A
#define C_log        0x200A
#define C_logl       0x300A

#define C_cbrtf      0x100B
#define C_cbrt       0x200B
#define C_cbrtl      0x300B




class Fpc_Domain;
class Fpc_DomainStore;
class Fpc_Variable;
class Fpc_Constraint;
class Fpc_Csp;
class Constraint;

#include <fpc_constraint.h>
#include <fpc_domain.h>
#include <fpc_variable.h>
#include <fpc_csp.h>

#include <constraint.h>
#include <fpc_solver.h>

#endif
