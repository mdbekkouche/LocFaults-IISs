
/************************************************/
/* fpc_variable.h :                             */
/*   floating point constraint solver           */
/*                                              */
/* vs 1/02/2005                                 */
/* copyright Claude Michel                      */
/*                                              */
/************************************************/


class Fpc_Constraint;

class Fpc_Variable {
 public:
  char *name;
  Fpc_Domain *domain;
  vector <Fpc_Constraint *> constraints;

  Fpc_Variable(Fpc_Csp &csp, char *name);
  Fpc_Variable(Fpc_Csp &csp, char *name, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, Fpc_Domain &domain);
  Fpc_Variable(Fpc_Csp &csp, char *name, float inf, float sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, double inf, double sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, long double inf, long double sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, short inf, short sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, int inf, int sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, long long inf, long long sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, unsigned short inf, unsigned short sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, unsigned int inf, unsigned int sup, int type);
  Fpc_Variable(Fpc_Csp &csp, char *name, unsigned long long inf, unsigned long long sup, int type);

  Constraint &operator=(Constraint &expr);
  Constraint &operator=(Fpc_Variable &variable);
  Constraint &operator=(float value);
  Constraint &operator=(double value);
  Constraint &operator=(long double value);

  void display();
};
