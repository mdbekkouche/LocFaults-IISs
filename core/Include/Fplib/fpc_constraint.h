
/************************************************/
/* fpc_constraints.h :                          */
/*   floating point constraint solver           */
/*                                              */
/* vs 1/02/2005                                 */
/* copyright Claude Michel                      */
/*                                              */
/************************************************/


class Fpc_Constraint {
 public:
  int (*caller)(Fpc_Constraint *constraint, int *change, double percent);

  union {
    void (*binary_projection_s)(int precision, int rounding, short_interval *result, short_interval *first);
    void (*binary_projection_i)(int precision, int rounding, int_interval *result, int_interval *first);
    void (*binary_projection_ll)(int precision, int rounding, llong_interval *result, llong_interval *first);
    void (*binary_projection_us)(int precision, int rounding, ushort_interval *result, ushort_interval *first);
    void (*binary_projection_ui)(int precision, int rounding, uint_interval *result, uint_interval *first);
    void (*binary_projection_ull)(int precision, int rounding, ullong_interval *result, ullong_interval *first);

    void (*binary_projection_sf)(int precision, int rounding, short_interval *result, float_interval *first);
    void (*binary_projection_if)(int precision, int rounding, int_interval *result, float_interval *first);
    void (*binary_projection_llf)(int precision, int rounding, llong_interval *result, float_interval *first);
    void (*binary_projection_usf)(int precision, int rounding, ushort_interval *result, float_interval *first);
    void (*binary_projection_uif)(int precision, int rounding, uint_interval *result, float_interval *first);
    void (*binary_projection_ullf)(int precision, int rounding, ullong_interval *result, float_interval *first);

    void (*binary_projection_sd)(int precision, int rounding, short_interval *result, double_interval *first);
    void (*binary_projection_id)(int precision, int rounding, int_interval *result, double_interval *first);
    void (*binary_projection_lld)(int precision, int rounding, llong_interval *result, double_interval *first);
    void (*binary_projection_usd)(int precision, int rounding, ushort_interval *result, double_interval *first);
    void (*binary_projection_uid)(int precision, int rounding, uint_interval *result, double_interval *first);
    void (*binary_projection_ulld)(int precision, int rounding, ullong_interval *result, double_interval *first);

    void (*binary_projection_sl)(int precision, int rounding, short_interval *result, ldouble_interval *first);
    void (*binary_projection_il)(int precision, int rounding, int_interval *result, ldouble_interval *first);
    void (*binary_projection_ll_l)(int precision, int rounding, llong_interval *result, ldouble_interval *first);
    void (*binary_projection_usl)(int precision, int rounding, ushort_interval *result, ldouble_interval *first);
    void (*binary_projection_uil)(int precision, int rounding, uint_interval *result, ldouble_interval *first);
    void (*binary_projection_ulll)(int precision, int rounding, ullong_interval *result, ldouble_interval *first);

    void (*binary_projection_fs)(int precision, int rounding, float_interval *result, short_interval *first);
    void (*binary_projection_fi)(int precision, int rounding, float_interval *result, int_interval *first);
    void (*binary_projection_fll)(int precision, int rounding, float_interval *result, llong_interval *first);
    void (*binary_projection_fus)(int precision, int rounding, float_interval *result, ushort_interval *first);
    void (*binary_projection_fui)(int precision, int rounding, float_interval *result, uint_interval *first);
    void (*binary_projection_full)(int precision, int rounding, float_interval *result, ullong_interval *first);

    void (*binary_projection_ds)(int precision, int rounding, double_interval *result, short_interval *first);
    void (*binary_projection_di)(int precision, int rounding, double_interval *result, int_interval *first);
    void (*binary_projection_dll)(int precision, int rounding, double_interval *result, llong_interval *first);
    void (*binary_projection_dus)(int precision, int rounding, double_interval *result, ushort_interval *first);
    void (*binary_projection_dui)(int precision, int rounding, double_interval *result, uint_interval *first);
    void (*binary_projection_dull)(int precision, int rounding, double_interval *result, ullong_interval *first);

    void (*binary_projection_ls)(int precision, int rounding, ldouble_interval *result, short_interval *first);
    void (*binary_projection_li)(int precision, int rounding, ldouble_interval *result, int_interval *first);
    void (*binary_projection_l_ll)(int precision, int rounding, ldouble_interval *result, llong_interval *first);
    void (*binary_projection_lus)(int precision, int rounding, ldouble_interval *result, ushort_interval *first);
    void (*binary_projection_lui)(int precision, int rounding, ldouble_interval *result, uint_interval *first);
    void (*binary_projection_lull)(int precision, int rounding, ldouble_interval *result, ullong_interval *first);

    void (*binary_projection_f)(int precision, int rounding, float_interval *result, float_interval *first);
    void (*binary_projection_fd)(int precision, int rounding, float_interval *result, double_interval *first);
    void (*binary_projection_fl)(int precision, int rounding, float_interval *result, ldouble_interval *first);

    void (*binary_projection_d)(int precision, int rounding, double_interval *result, double_interval *first);
    void (*binary_projection_df)(int precision, int rounding, double_interval *result, float_interval *first);
    void (*binary_projection_dl)(int precision, int rounding, double_interval *result, ldouble_interval *first);

    void (*binary_projection_l)(int precision, int rounding, ldouble_interval *result, ldouble_interval *first);
    void (*binary_projection_lf)(int precision, int rounding, ldouble_interval *result, float_interval *first);
    void (*binary_projection_ld)(int precision, int rounding, ldouble_interval *result, double_interval *first);

    void (*ternary_projection_f)(int precision, int rounding, 
				 float_interval *result, float_interval *first, float_interval *second); 
    void (*ternary_projection_d)(int precision, int rounding, 
				 double_interval *result, double_interval *first, double_interval *second); 
    void (*ternary_projection_l)(int precision, int rounding, 
				 ldouble_interval *result, ldouble_interval *first, ldouble_interval *second); 

    void (*oneontwo_projection_l)(int precision, int rounding, 
				  ldouble_interval *first_result, ldouble_interval *second_result, ldouble_interval *first); 

  } projection;

  int precision;
  int rounding;
  Fpc_Domain *result;
  Fpc_Domain *first_result;
  Fpc_Domain *second_result;
  Fpc_Domain *first_parameter;
  Fpc_Domain *second_parameter;


  char *description;

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_s)(int precision, int rounding, short_interval *result, short_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_i)(int precision, int rounding, int_interval *result, int_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ll)(int precision, int rounding, llong_interval *result, llong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_us)(int precision, int rounding, ushort_interval *result, ushort_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ui)(int precision, int rounding, uint_interval *result, uint_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ull)(int precision, int rounding, ullong_interval *result, ullong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_sf)(int precision, int rounding, short_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_if)(int precision, int rounding, int_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_llf)(int precision, int rounding, llong_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_usf)(int precision, int rounding, ushort_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_uif)(int precision, int rounding, uint_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ullf)(int precision, int rounding, ullong_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_sd)(int precision, int rounding, short_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_id)(int precision, int rounding, int_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_lld)(int precision, int rounding, llong_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_usd)(int precision, int rounding, ushort_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_uid)(int precision, int rounding, uint_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ulld)(int precision, int rounding, ullong_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_sd)(int precision, int rounding, short_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_id)(int precision, int rounding, int_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_lld)(int precision, int rounding, llong_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_usd)(int precision, int rounding, ushort_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_uid)(int precision, int rounding, uint_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ulld)(int precision, int rounding, ullong_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_fs)(int precision, int rounding, float_interval *result, short_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_fi)(int precision, int rounding, float_interval *result, int_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_fll)(int precision, int rounding, float_interval *result, llong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_fus)(int precision, int rounding, float_interval *result, ushort_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_fui)(int precision, int rounding, float_interval *result, uint_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_full)(int precision, int rounding, float_interval *result, ullong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);


  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ds)(int precision, int rounding, double_interval *result, short_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_di)(int precision, int rounding, double_interval *result, int_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_dll)(int precision, int rounding, double_interval *result, llong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_dus)(int precision, int rounding, double_interval *result, ushort_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_dui)(int precision, int rounding, double_interval *result, uint_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_dull)(int precision, int rounding, double_interval *result, ullong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);


  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ls)(int precision, int rounding, ldouble_interval *result, short_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_li)(int precision, int rounding, ldouble_interval *result, int_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_l_ll)(int precision, int rounding, ldouble_interval *result, llong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_lus)(int precision, int rounding, ldouble_interval *result, ushort_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_lui)(int precision, int rounding, ldouble_interval *result, uint_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_lull)(int precision, int rounding, ldouble_interval *result, ullong_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_f)(int precision, int rounding, float_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_fd)(int precision, int rounding, float_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_fl)(int precision, int rounding, float_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_d)(int precision, int rounding, double_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_df)(int precision, int rounding, double_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_dl)(int precision, int rounding, double_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_l)(int precision, int rounding, ldouble_interval *result, ldouble_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_lf)(int precision, int rounding, ldouble_interval *result, float_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*binary_projection_ld)(int precision, int rounding, ldouble_interval *result, double_interval *first),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter);

  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*ternary_projection_f)(int precision, int rounding, 
					      float_interval *result, float_interval *first, float_interval *second),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter,
		 Fpc_Domain &second_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*ternary_projection_d)(int precision, int rounding, 
					      double_interval *result, double_interval *first, double_interval *second),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter,
		 Fpc_Domain &second_parameter);
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*ternary_projection_l)(int precision, int rounding, 
					      ldouble_interval *result, ldouble_interval *first, ldouble_interval *second),
                 int precision,
                 int rounding,
		 Fpc_Domain &result,
		 Fpc_Domain &first_parameter,
		 Fpc_Domain &second_parameter);
  
  
  Fpc_Constraint(Fpc_Csp &csp, 
		 void (*oneontwo_projection_l)(int precision, int rounding, 
					       ldouble_interval *first_result, ldouble_interval *second_result, 
					       ldouble_interval *first),
		 int precision,
		 int rounding,
		 Fpc_Domain &first_result,
		 Fpc_Domain &second_result,
		 Fpc_Domain &first_parameter,
		 char dummy);
  
  int call(int *change, double percent);

  void display();
			  
};

