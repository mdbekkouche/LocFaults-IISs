
/************************************************/
/* fpc_solver.h :                               */
/*   floating point constraint solver           */
/*                                              */
/* vs 1/02/2005                                 */
/* copyright Claude Michel                      */
/*                                              */
/************************************************/



class Fpc_Solver {
 public:
  Fpc_Csp *csp;
  Fpc_Model *model;
  vector <Fpc_Variable *> enumerated_variables;

  vector <vector <Fpc_DomainStore *> *> stack;
  vector <vector <Fpc_DomainStore *> *> old_stores;
  vector <Fpc_DomainStore *> old_dstores;

  unsigned long long nb_splits, nb_solutions;

  Fpc_Solver(Fpc_Model &model);

  void push();
  void back();
  void restore();

  int TwoB(double percent);
  int TwoB();
  int ThreeBDomain(Fpc_Domain *domain);
  int kBDomain(int k, Fpc_Domain *domain, double kB_percent, double twoB_percent);
  int kBold(int k, double kB_percent, double twoB_percent);
  int kBBDomain(int k, Fpc_Domain *domain, double kBB_percent, double twoB_percent);
  int kBB(int k, double kBB_percent, double twoB_percent);
  int kBfixpoint(int k, double kB_percent, double twoB_percent);
  int kB(int k, double kB_percent, double twoB_percent);

  int searchEnumNext();
  int searchNext();
  int searchNext(double kB_percent, double TwoB_percent);
};
