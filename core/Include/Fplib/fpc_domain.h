
/************************************************/
/* fpc_domain.h :                               */
/*   floating point constraint solver           */
/*                                              */
/* vs 1/02/2005                                 */
/* copyright Claude Michel                      */
/*                                              */
/************************************************/



class Fpc_Domain {
 public:
  int type;
  Fpc_Variable *variable;
  vector <Fpc_Constraint *> constraints;
  int is_constant;
  double last_percent;

  Fpc_Domain *convert_to_short;
  Fpc_Domain *convert_to_int;
  Fpc_Domain *convert_to_llong;
  Fpc_Domain *convert_to_ushort;
  Fpc_Domain *convert_to_uint;
  Fpc_Domain *convert_to_ullong;
  Fpc_Domain *convert_to_float;
  Fpc_Domain *convert_to_double;
  Fpc_Domain *convert_to_ldouble;

  union {
    float_interval float_I;
    double_interval double_I;
    ldouble_interval ldouble_I;
    short_interval short_I;
    int_interval int_I;
    llong_interval llong_I;
    ushort_interval ushort_I;
    uint_interval uint_I;
    ullong_interval ullong_I;
  } interval;

  fp_number mid_inf, mid_sup, bound;

  Fpc_Domain(Fpc_Csp &csp, int type);
  Fpc_Domain(Fpc_Csp &csp, float inf, float sup, int type);
  Fpc_Domain(Fpc_Csp &csp, double inf, double sup, int type);
  Fpc_Domain(Fpc_Csp &csp, long double inf, long double sup, int type);
  Fpc_Domain(Fpc_Csp &csp, short inf, short sup, int type);
  Fpc_Domain(Fpc_Csp &csp, int inf, int sup, int type);
  Fpc_Domain(Fpc_Csp &csp, long long inf, long long sup, int type);
  Fpc_Domain(Fpc_Csp &csp, unsigned short inf, unsigned short sup, int type);
  Fpc_Domain(Fpc_Csp &csp, unsigned int inf, unsigned int sup, int type);
  Fpc_Domain(Fpc_Csp &csp, unsigned long long inf, unsigned long long sup, int type);

  int is_number();
  int is_empty();

  void setSupToInf();
  void setInfToSup();
  int setInfToMiddle();
  int setSupToMiddle();

  void setToInf(fp_number *bound);
  void setToSup(fp_number *bound);
  void setInfTo(fp_number *bound);
  void setSupTo(fp_number *bound);
  int computeMid(fp_number *mid_inf, fp_number *mid_sup);

  long double getSize();

  Fpc_DomainStore *save();

  void display();
};

class Fpc_DomainStore {
 public:
  Fpc_Domain *domain;
  int type;
  double last_percent;

  union {
    short_interval short_I;
    int_interval int_I;
    llong_interval llong_I;
    ushort_interval ushort_I;
    uint_interval uint_I;
    ullong_interval ullong_I;
    float_interval float_I;
    double_interval double_I;
    ldouble_interval ldouble_I;
  } interval;

  void save(Fpc_Domain *domain);
  void saveInf(Fpc_Domain *domain);
  void saveSup(Fpc_Domain *domain);
  void restore();
  void restoreInf();
  void restoreSup();
  void restoreInfFromSup();
  void restoreSupFromInf();
};

