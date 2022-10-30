

/************************************************/
/* fpc_csp.h :                                  */
/*   floating point constraint solver           */
/*                                              */
/* vs 1/02/2005                                 */
/* copyright Claude Michel                      */
/*                                              */
/************************************************/


class Fpc_Csp {
 public:
  vector <Fpc_Variable *> variables;
  vector <Fpc_Constraint *> constraints;
  vector <Fpc_Domain *> domains;
  //  vector <Fpc_Domain *> domains_stack;
};
