

/************************************************/
/* constraint.h :                               */
/*   floating point constraint solver           */
/*                                              */
/* vs 1/02/2005                                 */
/* copyright Claude Michel                      */
/*                                              */
/************************************************/


class Constraint;

//====================== Begin automatic generation ========================
















Constraint &operator+(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator+(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator+(Fpc_Variable &variable_left, float value_right);

Constraint &operator+(Fpc_Variable &variable_left, double value_right);

Constraint &operator+(Fpc_Variable &variable_left, long double value_right);

Constraint &operator+(float value_left, Constraint &constraint_right);

Constraint &operator+(float value_left, Fpc_Variable &variable_right);

Constraint &operator+(double value_left, Constraint &constraint_right);

Constraint &operator+(double value_left, Fpc_Variable &variable_right);

Constraint &operator+(long double value_left, Constraint &constraint_right);

Constraint &operator+(long double value_left, Fpc_Variable &variable_right);

 











Constraint &operator-(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator-(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator-(Fpc_Variable &variable_left, float value_right);

Constraint &operator-(Fpc_Variable &variable_left, double value_right);

Constraint &operator-(Fpc_Variable &variable_left, long double value_right);

Constraint &operator-(float value_left, Constraint &constraint_right);

Constraint &operator-(float value_left, Fpc_Variable &variable_right);

Constraint &operator-(double value_left, Constraint &constraint_right);

Constraint &operator-(double value_left, Fpc_Variable &variable_right);

Constraint &operator-(long double value_left, Constraint &constraint_right);

Constraint &operator-(long double value_left, Fpc_Variable &variable_right);

 











Constraint &operator*(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator*(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator*(Fpc_Variable &variable_left, float value_right);

Constraint &operator*(Fpc_Variable &variable_left, double value_right);

Constraint &operator*(Fpc_Variable &variable_left, long double value_right);

Constraint &operator*(float value_left, Constraint &constraint_right);

Constraint &operator*(float value_left, Fpc_Variable &variable_right);

Constraint &operator*(double value_left, Constraint &constraint_right);

Constraint &operator*(double value_left, Fpc_Variable &variable_right);

Constraint &operator*(long double value_left, Constraint &constraint_right);

Constraint &operator*(long double value_left, Fpc_Variable &variable_right);

 











Constraint &operator/(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator/(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator/(Fpc_Variable &variable_left, float value_right);

Constraint &operator/(Fpc_Variable &variable_left, double value_right);

Constraint &operator/(Fpc_Variable &variable_left, long double value_right);

Constraint &operator/(float value_left, Constraint &constraint_right);

Constraint &operator/(float value_left, Fpc_Variable &variable_right);

Constraint &operator/(double value_left, Constraint &constraint_right);

Constraint &operator/(double value_left, Fpc_Variable &variable_right);

Constraint &operator/(long double value_left, Constraint &constraint_right);

Constraint &operator/(long double value_left, Fpc_Variable &variable_right);

 






Constraint &operator==(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator==(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator==(Fpc_Variable &variable_left, float value_right);

Constraint &operator==(Fpc_Variable &variable_left, double value_right);

Constraint &operator==(Fpc_Variable &variable_left, long double value_right);

Constraint &operator==(Fpc_Variable &variable_left, short value_right);

Constraint &operator==(Fpc_Variable &variable_left, int value_right);

Constraint &operator==(Fpc_Variable &variable_left, long long value_right);

Constraint &operator==(Fpc_Variable &variable_left, unsigned short value_right);

Constraint &operator==(Fpc_Variable &variable_left, unsigned int value_right);

Constraint &operator==(Fpc_Variable &variable_left, unsigned long long value_right);

Constraint &operator==(float value_left, Constraint &constraint_right);

Constraint &operator==(float value_left, Fpc_Variable &variable_right);

Constraint &operator==(double value_left, Constraint &constraint_right);

Constraint &operator==(double value_left, Fpc_Variable &variable_right);

Constraint &operator==(long double value_left, Constraint &constraint_right);

Constraint &operator==(long double value_left, Fpc_Variable &variable_right);

Constraint &operator==(short value_left, Constraint &constraint_right);

Constraint &operator==(short value_left, Fpc_Variable &variable_right);

Constraint &operator==(int value_left, Constraint &constraint_right);

Constraint &operator==(int value_left, Fpc_Variable &variable_right);

Constraint &operator==(long long value_left, Constraint &constraint_right);

Constraint &operator==(long long value_left, Fpc_Variable &variable_right);

Constraint &operator==(unsigned short value_left, Constraint &constraint_right);

Constraint &operator==(unsigned short value_left, Fpc_Variable &variable_right);

Constraint &operator==(unsigned int value_left, Constraint &constraint_right);

Constraint &operator==(unsigned int value_left, Fpc_Variable &variable_right);

Constraint &operator==(unsigned long long value_left, Constraint &constraint_right);

Constraint &operator==(unsigned long long value_left, Fpc_Variable &variable_right);

 





Constraint &operator<(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator<(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator<(Fpc_Variable &variable_left, float value_right);

Constraint &operator<(Fpc_Variable &variable_left, double value_right);

Constraint &operator<(Fpc_Variable &variable_left, long double value_right);

Constraint &operator<(Fpc_Variable &variable_left, short value_right);

Constraint &operator<(Fpc_Variable &variable_left, int value_right);

Constraint &operator<(Fpc_Variable &variable_left, long long value_right);

Constraint &operator<(Fpc_Variable &variable_left, unsigned short value_right);

Constraint &operator<(Fpc_Variable &variable_left, unsigned int value_right);

Constraint &operator<(Fpc_Variable &variable_left, unsigned long long value_right);

Constraint &operator<(float value_left, Constraint &constraint_right);

Constraint &operator<(float value_left, Fpc_Variable &variable_right);

Constraint &operator<(double value_left, Constraint &constraint_right);

Constraint &operator<(double value_left, Fpc_Variable &variable_right);

Constraint &operator<(long double value_left, Constraint &constraint_right);

Constraint &operator<(long double value_left, Fpc_Variable &variable_right);

Constraint &operator<(short value_left, Constraint &constraint_right);

Constraint &operator<(short value_left, Fpc_Variable &variable_right);

Constraint &operator<(int value_left, Constraint &constraint_right);

Constraint &operator<(int value_left, Fpc_Variable &variable_right);

Constraint &operator<(long long value_left, Constraint &constraint_right);

Constraint &operator<(long long value_left, Fpc_Variable &variable_right);

Constraint &operator<(unsigned short value_left, Constraint &constraint_right);

Constraint &operator<(unsigned short value_left, Fpc_Variable &variable_right);

Constraint &operator<(unsigned int value_left, Constraint &constraint_right);

Constraint &operator<(unsigned int value_left, Fpc_Variable &variable_right);

Constraint &operator<(unsigned long long value_left, Constraint &constraint_right);

Constraint &operator<(unsigned long long value_left, Fpc_Variable &variable_right);

 





Constraint &operator>(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator>(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator>(Fpc_Variable &variable_left, float value_right);

Constraint &operator>(Fpc_Variable &variable_left, double value_right);

Constraint &operator>(Fpc_Variable &variable_left, long double value_right);

Constraint &operator>(Fpc_Variable &variable_left, short value_right);

Constraint &operator>(Fpc_Variable &variable_left, int value_right);

Constraint &operator>(Fpc_Variable &variable_left, long long value_right);

Constraint &operator>(Fpc_Variable &variable_left, unsigned short value_right);

Constraint &operator>(Fpc_Variable &variable_left, unsigned int value_right);

Constraint &operator>(Fpc_Variable &variable_left, unsigned long long value_right);

Constraint &operator>(float value_left, Constraint &constraint_right);

Constraint &operator>(float value_left, Fpc_Variable &variable_right);

Constraint &operator>(double value_left, Constraint &constraint_right);

Constraint &operator>(double value_left, Fpc_Variable &variable_right);

Constraint &operator>(long double value_left, Constraint &constraint_right);

Constraint &operator>(long double value_left, Fpc_Variable &variable_right);

Constraint &operator>(short value_left, Constraint &constraint_right);

Constraint &operator>(short value_left, Fpc_Variable &variable_right);

Constraint &operator>(int value_left, Constraint &constraint_right);

Constraint &operator>(int value_left, Fpc_Variable &variable_right);

Constraint &operator>(long long value_left, Constraint &constraint_right);

Constraint &operator>(long long value_left, Fpc_Variable &variable_right);

Constraint &operator>(unsigned short value_left, Constraint &constraint_right);

Constraint &operator>(unsigned short value_left, Fpc_Variable &variable_right);

Constraint &operator>(unsigned int value_left, Constraint &constraint_right);

Constraint &operator>(unsigned int value_left, Fpc_Variable &variable_right);

Constraint &operator>(unsigned long long value_left, Constraint &constraint_right);

Constraint &operator>(unsigned long long value_left, Fpc_Variable &variable_right);

 





Constraint &operator<=(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator<=(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator<=(Fpc_Variable &variable_left, float value_right);

Constraint &operator<=(Fpc_Variable &variable_left, double value_right);

Constraint &operator<=(Fpc_Variable &variable_left, long double value_right);

Constraint &operator<=(Fpc_Variable &variable_left, short value_right);

Constraint &operator<=(Fpc_Variable &variable_left, int value_right);

Constraint &operator<=(Fpc_Variable &variable_left, long long value_right);

Constraint &operator<=(Fpc_Variable &variable_left, unsigned short value_right);

Constraint &operator<=(Fpc_Variable &variable_left, unsigned int value_right);

Constraint &operator<=(Fpc_Variable &variable_left, unsigned long long value_right);

Constraint &operator<=(float value_left, Constraint &constraint_right);

Constraint &operator<=(float value_left, Fpc_Variable &variable_right);

Constraint &operator<=(double value_left, Constraint &constraint_right);

Constraint &operator<=(double value_left, Fpc_Variable &variable_right);

Constraint &operator<=(long double value_left, Constraint &constraint_right);

Constraint &operator<=(long double value_left, Fpc_Variable &variable_right);

Constraint &operator<=(short value_left, Constraint &constraint_right);

Constraint &operator<=(short value_left, Fpc_Variable &variable_right);

Constraint &operator<=(int value_left, Constraint &constraint_right);

Constraint &operator<=(int value_left, Fpc_Variable &variable_right);

Constraint &operator<=(long long value_left, Constraint &constraint_right);

Constraint &operator<=(long long value_left, Fpc_Variable &variable_right);

Constraint &operator<=(unsigned short value_left, Constraint &constraint_right);

Constraint &operator<=(unsigned short value_left, Fpc_Variable &variable_right);

Constraint &operator<=(unsigned int value_left, Constraint &constraint_right);

Constraint &operator<=(unsigned int value_left, Fpc_Variable &variable_right);

Constraint &operator<=(unsigned long long value_left, Constraint &constraint_right);

Constraint &operator<=(unsigned long long value_left, Fpc_Variable &variable_right);

 





Constraint &operator>=(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator>=(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator>=(Fpc_Variable &variable_left, float value_right);

Constraint &operator>=(Fpc_Variable &variable_left, double value_right);

Constraint &operator>=(Fpc_Variable &variable_left, long double value_right);

Constraint &operator>=(Fpc_Variable &variable_left, short value_right);

Constraint &operator>=(Fpc_Variable &variable_left, int value_right);

Constraint &operator>=(Fpc_Variable &variable_left, long long value_right);

Constraint &operator>=(Fpc_Variable &variable_left, unsigned short value_right);

Constraint &operator>=(Fpc_Variable &variable_left, unsigned int value_right);

Constraint &operator>=(Fpc_Variable &variable_left, unsigned long long value_right);

Constraint &operator>=(float value_left, Constraint &constraint_right);

Constraint &operator>=(float value_left, Fpc_Variable &variable_right);

Constraint &operator>=(double value_left, Constraint &constraint_right);

Constraint &operator>=(double value_left, Fpc_Variable &variable_right);

Constraint &operator>=(long double value_left, Constraint &constraint_right);

Constraint &operator>=(long double value_left, Fpc_Variable &variable_right);

Constraint &operator>=(short value_left, Constraint &constraint_right);

Constraint &operator>=(short value_left, Fpc_Variable &variable_right);

Constraint &operator>=(int value_left, Constraint &constraint_right);

Constraint &operator>=(int value_left, Fpc_Variable &variable_right);

Constraint &operator>=(long long value_left, Constraint &constraint_right);

Constraint &operator>=(long long value_left, Fpc_Variable &variable_right);

Constraint &operator>=(unsigned short value_left, Constraint &constraint_right);

Constraint &operator>=(unsigned short value_left, Fpc_Variable &variable_right);

Constraint &operator>=(unsigned int value_left, Constraint &constraint_right);

Constraint &operator>=(unsigned int value_left, Fpc_Variable &variable_right);

Constraint &operator>=(unsigned long long value_left, Constraint &constraint_right);

Constraint &operator>=(unsigned long long value_left, Fpc_Variable &variable_right);

 





Constraint &operator!=(Fpc_Variable &variable_left, Constraint &constraint_right);

Constraint &operator!=(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

Constraint &operator!=(Fpc_Variable &variable_left, float value_right);

Constraint &operator!=(Fpc_Variable &variable_left, double value_right);

Constraint &operator!=(Fpc_Variable &variable_left, long double value_right);

Constraint &operator!=(Fpc_Variable &variable_left, short value_right);

Constraint &operator!=(Fpc_Variable &variable_left, int value_right);

Constraint &operator!=(Fpc_Variable &variable_left, long long value_right);

Constraint &operator!=(Fpc_Variable &variable_left, unsigned short value_right);

Constraint &operator!=(Fpc_Variable &variable_left, unsigned int value_right);

Constraint &operator!=(Fpc_Variable &variable_left, unsigned long long value_right);

Constraint &operator!=(float value_left, Constraint &constraint_right);

Constraint &operator!=(float value_left, Fpc_Variable &variable_right);

Constraint &operator!=(double value_left, Constraint &constraint_right);

Constraint &operator!=(double value_left, Fpc_Variable &variable_right);

Constraint &operator!=(long double value_left, Constraint &constraint_right);

Constraint &operator!=(long double value_left, Fpc_Variable &variable_right);

Constraint &operator!=(short value_left, Constraint &constraint_right);

Constraint &operator!=(short value_left, Fpc_Variable &variable_right);

Constraint &operator!=(int value_left, Constraint &constraint_right);

Constraint &operator!=(int value_left, Fpc_Variable &variable_right);

Constraint &operator!=(long long value_left, Constraint &constraint_right);

Constraint &operator!=(long long value_left, Fpc_Variable &variable_right);

Constraint &operator!=(unsigned short value_left, Constraint &constraint_right);

Constraint &operator!=(unsigned short value_left, Fpc_Variable &variable_right);

Constraint &operator!=(unsigned int value_left, Constraint &constraint_right);

Constraint &operator!=(unsigned int value_left, Fpc_Variable &variable_right);

Constraint &operator!=(unsigned long long value_left, Constraint &constraint_right);

Constraint &operator!=(unsigned long long value_left, Fpc_Variable &variable_right);

 

Constraint &operator-(Constraint &constraint);

Constraint &operator-(Fpc_Variable &variable);

 

Constraint &sqrtf(Constraint &constraint);

Constraint &sqrtf(Fpc_Variable &variable);

 
Constraint &sqrt(Constraint &constraint);

Constraint &sqrt(Fpc_Variable &variable);

 
Constraint &sqrtl(Constraint &constraint);

Constraint &sqrtl(Fpc_Variable &variable);

 
Constraint &xxf(Constraint &constraint);

Constraint &xxf(Fpc_Variable &variable);

 
Constraint &xx(Constraint &constraint);

Constraint &xx(Fpc_Variable &variable);

 
Constraint &xxl(Constraint &constraint);

Constraint &xxl(Fpc_Variable &variable);

 
Constraint &fabsf(Constraint &constraint);

Constraint &fabsf(Fpc_Variable &variable);

 
Constraint &fabs(Constraint &constraint);

Constraint &fabs(Fpc_Variable &variable);

 
Constraint &fabsl(Constraint &constraint);

Constraint &fabsl(Fpc_Variable &variable);

 
Constraint &sinf(Constraint &constraint);

Constraint &sinf(Fpc_Variable &variable);

 
Constraint &sin(Constraint &constraint);

Constraint &sin(Fpc_Variable &variable);

 
Constraint &sinl(Constraint &constraint);

Constraint &sinl(Fpc_Variable &variable);

 
Constraint &cosf(Constraint &constraint);

Constraint &cosf(Fpc_Variable &variable);

 
Constraint &cos(Constraint &constraint);

Constraint &cos(Fpc_Variable &variable);

 
Constraint &cosl(Constraint &constraint);

Constraint &cosl(Fpc_Variable &variable);

 
Constraint &tanf(Constraint &constraint);

Constraint &tanf(Fpc_Variable &variable);

 
Constraint &tan(Constraint &constraint);

Constraint &tan(Fpc_Variable &variable);

 
Constraint &tanl(Constraint &constraint);

Constraint &tanl(Fpc_Variable &variable);

 
Constraint &asinf(Constraint &constraint);

Constraint &asinf(Fpc_Variable &variable);

 
Constraint &asin(Constraint &constraint);

Constraint &asin(Fpc_Variable &variable);

 
Constraint &asinl(Constraint &constraint);

Constraint &asinl(Fpc_Variable &variable);

 
Constraint &acosf(Constraint &constraint);

Constraint &acosf(Fpc_Variable &variable);

 
Constraint &acos(Constraint &constraint);

Constraint &acos(Fpc_Variable &variable);

 
Constraint &acosl(Constraint &constraint);

Constraint &acosl(Fpc_Variable &variable);

 
Constraint &atanf(Constraint &constraint);

Constraint &atanf(Fpc_Variable &variable);

 
Constraint &atan(Constraint &constraint);

Constraint &atan(Fpc_Variable &variable);

 
Constraint &atanl(Constraint &constraint);

Constraint &atanl(Fpc_Variable &variable);

 
Constraint &logf(Constraint &constraint);

Constraint &logf(Fpc_Variable &variable);

 
Constraint &log(Constraint &constraint);

Constraint &log(Fpc_Variable &variable);

 
Constraint &logl(Constraint &constraint);

Constraint &logl(Fpc_Variable &variable);

 
Constraint &expf(Constraint &constraint);

Constraint &expf(Fpc_Variable &variable);

 
Constraint &exp(Constraint &constraint);

Constraint &exp(Fpc_Variable &variable);

 
Constraint &expl(Constraint &constraint);

Constraint &expl(Fpc_Variable &variable);

 
Constraint &cbrtf(Constraint &constraint);

Constraint &cbrtf(Fpc_Variable &variable);

 
Constraint &cbrt(Constraint &constraint);

Constraint &cbrt(Fpc_Variable &variable);

 
Constraint &cbrtl(Constraint &constraint);

Constraint &cbrtl(Fpc_Variable &variable);

 

//====================== End automatic generation ========================


class Constraint {
 public:
  int type;

  Fpc_Variable *variable;
  Fpc_Domain *domain;
  Constraint *left;
  Constraint *right;

  short svalue;
  int ivalue;
  long long llvalue;
  unsigned short usvalue;
  unsigned int uivalue;
  unsigned long long ullvalue;
  float fvalue;
  double dvalue;
  long double lvalue;

  static int current_rounding_mode;
  int rounding_mode;
  static int current_precision_mode;
  int precision_mode;

  Constraint() {};

  Constraint(Fpc_Variable &variable);

  Constraint(short constant);
  Constraint(int constant);
  Constraint(long long constant);
  Constraint(unsigned short constant);
  Constraint(unsigned int constant);
  Constraint(unsigned long long constant);
  Constraint(float constant);
  Constraint(double constant);
  Constraint(long double constant);
  Constraint(int type, Constraint *left, Constraint *right);

  //====================== Begin automatic generation ========================





  Constraint &operator+(Constraint &constraint);

  Constraint &operator+(Fpc_Variable &variable);

  Constraint &operator+(short value);

  Constraint &operator+(int value);

  Constraint &operator+(long long value);

  Constraint &operator+(unsigned short value);

  Constraint &operator+(unsigned int value);

  Constraint &operator+(unsigned long long value);

  Constraint &operator+(float value);

  Constraint &operator+(double value);

  Constraint &operator+(long double value);

  friend Constraint &operator+(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator+(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator+(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator+(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator+(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator+(float value_left, Constraint &constraint_right);

  friend Constraint &operator+(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator+(double value_left, Constraint &constraint_right);

  friend Constraint &operator+(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator+(long double value_left, Constraint &constraint_right);

  friend Constraint &operator+(long double value_left, Fpc_Variable &variable_right);

 
  Constraint &operator-(Constraint &constraint);

  Constraint &operator-(Fpc_Variable &variable);

  Constraint &operator-(short value);

  Constraint &operator-(int value);

  Constraint &operator-(long long value);

  Constraint &operator-(unsigned short value);

  Constraint &operator-(unsigned int value);

  Constraint &operator-(unsigned long long value);

  Constraint &operator-(float value);

  Constraint &operator-(double value);

  Constraint &operator-(long double value);

  friend Constraint &operator-(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator-(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator-(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator-(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator-(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator-(float value_left, Constraint &constraint_right);

  friend Constraint &operator-(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator-(double value_left, Constraint &constraint_right);

  friend Constraint &operator-(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator-(long double value_left, Constraint &constraint_right);

  friend Constraint &operator-(long double value_left, Fpc_Variable &variable_right);

 
  Constraint &operator*(Constraint &constraint);

  Constraint &operator*(Fpc_Variable &variable);

  Constraint &operator*(short value);

  Constraint &operator*(int value);

  Constraint &operator*(long long value);

  Constraint &operator*(unsigned short value);

  Constraint &operator*(unsigned int value);

  Constraint &operator*(unsigned long long value);

  Constraint &operator*(float value);

  Constraint &operator*(double value);

  Constraint &operator*(long double value);

  friend Constraint &operator*(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator*(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator*(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator*(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator*(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator*(float value_left, Constraint &constraint_right);

  friend Constraint &operator*(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator*(double value_left, Constraint &constraint_right);

  friend Constraint &operator*(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator*(long double value_left, Constraint &constraint_right);

  friend Constraint &operator*(long double value_left, Fpc_Variable &variable_right);

 
  Constraint &operator/(Constraint &constraint);

  Constraint &operator/(Fpc_Variable &variable);

  Constraint &operator/(short value);

  Constraint &operator/(int value);

  Constraint &operator/(long long value);

  Constraint &operator/(unsigned short value);

  Constraint &operator/(unsigned int value);

  Constraint &operator/(unsigned long long value);

  Constraint &operator/(float value);

  Constraint &operator/(double value);

  Constraint &operator/(long double value);

  friend Constraint &operator/(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator/(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator/(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator/(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator/(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator/(float value_left, Constraint &constraint_right);

  friend Constraint &operator/(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator/(double value_left, Constraint &constraint_right);

  friend Constraint &operator/(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator/(long double value_left, Constraint &constraint_right);

  friend Constraint &operator/(long double value_left, Fpc_Variable &variable_right);

 

  Constraint &operator==(Constraint &constraint);

  Constraint &operator==(Fpc_Variable &variable);

  Constraint &operator==(float value);

  Constraint &operator==(double value);

  Constraint &operator==(long double value);

  friend Constraint &operator==(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, short value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, int value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, long long value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, unsigned short value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, unsigned int value_right);

  friend Constraint &operator==(Fpc_Variable &variable_left, unsigned long long value_right);

  friend Constraint &operator==(float value_left, Constraint &constraint_right);

  friend Constraint &operator==(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(double value_left, Constraint &constraint_right);

  friend Constraint &operator==(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(long double value_left, Constraint &constraint_right);

  friend Constraint &operator==(long double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(short value_left, Constraint &constraint_right);

  friend Constraint &operator==(short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(int value_left, Constraint &constraint_right);

  friend Constraint &operator==(int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(long long value_left, Constraint &constraint_right);

  friend Constraint &operator==(long long value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(unsigned short value_left, Constraint &constraint_right);

  friend Constraint &operator==(unsigned short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(unsigned int value_left, Constraint &constraint_right);

  friend Constraint &operator==(unsigned int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator==(unsigned long long value_left, Constraint &constraint_right);

  friend Constraint &operator==(unsigned long long value_left, Fpc_Variable &variable_right);

 
  Constraint &operator<(Constraint &constraint);

  Constraint &operator<(Fpc_Variable &variable);

  Constraint &operator<(float value);

  Constraint &operator<(double value);

  Constraint &operator<(long double value);

  friend Constraint &operator<(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, short value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, int value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, long long value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, unsigned short value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, unsigned int value_right);

  friend Constraint &operator<(Fpc_Variable &variable_left, unsigned long long value_right);

  friend Constraint &operator<(float value_left, Constraint &constraint_right);

  friend Constraint &operator<(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(double value_left, Constraint &constraint_right);

  friend Constraint &operator<(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(long double value_left, Constraint &constraint_right);

  friend Constraint &operator<(long double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(short value_left, Constraint &constraint_right);

  friend Constraint &operator<(short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(int value_left, Constraint &constraint_right);

  friend Constraint &operator<(int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(long long value_left, Constraint &constraint_right);

  friend Constraint &operator<(long long value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(unsigned short value_left, Constraint &constraint_right);

  friend Constraint &operator<(unsigned short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(unsigned int value_left, Constraint &constraint_right);

  friend Constraint &operator<(unsigned int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<(unsigned long long value_left, Constraint &constraint_right);

  friend Constraint &operator<(unsigned long long value_left, Fpc_Variable &variable_right);

 
  Constraint &operator>(Constraint &constraint);

  Constraint &operator>(Fpc_Variable &variable);

  Constraint &operator>(float value);

  Constraint &operator>(double value);

  Constraint &operator>(long double value);

  friend Constraint &operator>(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, short value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, int value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, long long value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, unsigned short value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, unsigned int value_right);

  friend Constraint &operator>(Fpc_Variable &variable_left, unsigned long long value_right);

  friend Constraint &operator>(float value_left, Constraint &constraint_right);

  friend Constraint &operator>(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(double value_left, Constraint &constraint_right);

  friend Constraint &operator>(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(long double value_left, Constraint &constraint_right);

  friend Constraint &operator>(long double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(short value_left, Constraint &constraint_right);

  friend Constraint &operator>(short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(int value_left, Constraint &constraint_right);

  friend Constraint &operator>(int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(long long value_left, Constraint &constraint_right);

  friend Constraint &operator>(long long value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(unsigned short value_left, Constraint &constraint_right);

  friend Constraint &operator>(unsigned short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(unsigned int value_left, Constraint &constraint_right);

  friend Constraint &operator>(unsigned int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>(unsigned long long value_left, Constraint &constraint_right);

  friend Constraint &operator>(unsigned long long value_left, Fpc_Variable &variable_right);

 
  Constraint &operator<=(Constraint &constraint);

  Constraint &operator<=(Fpc_Variable &variable);

  Constraint &operator<=(float value);

  Constraint &operator<=(double value);

  Constraint &operator<=(long double value);

  friend Constraint &operator<=(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, short value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, int value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, long long value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, unsigned short value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, unsigned int value_right);

  friend Constraint &operator<=(Fpc_Variable &variable_left, unsigned long long value_right);

  friend Constraint &operator<=(float value_left, Constraint &constraint_right);

  friend Constraint &operator<=(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(double value_left, Constraint &constraint_right);

  friend Constraint &operator<=(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(long double value_left, Constraint &constraint_right);

  friend Constraint &operator<=(long double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(short value_left, Constraint &constraint_right);

  friend Constraint &operator<=(short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(int value_left, Constraint &constraint_right);

  friend Constraint &operator<=(int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(long long value_left, Constraint &constraint_right);

  friend Constraint &operator<=(long long value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(unsigned short value_left, Constraint &constraint_right);

  friend Constraint &operator<=(unsigned short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(unsigned int value_left, Constraint &constraint_right);

  friend Constraint &operator<=(unsigned int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator<=(unsigned long long value_left, Constraint &constraint_right);

  friend Constraint &operator<=(unsigned long long value_left, Fpc_Variable &variable_right);

 
  Constraint &operator>=(Constraint &constraint);

  Constraint &operator>=(Fpc_Variable &variable);

  Constraint &operator>=(float value);

  Constraint &operator>=(double value);

  Constraint &operator>=(long double value);

  friend Constraint &operator>=(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, short value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, int value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, long long value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, unsigned short value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, unsigned int value_right);

  friend Constraint &operator>=(Fpc_Variable &variable_left, unsigned long long value_right);

  friend Constraint &operator>=(float value_left, Constraint &constraint_right);

  friend Constraint &operator>=(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(double value_left, Constraint &constraint_right);

  friend Constraint &operator>=(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(long double value_left, Constraint &constraint_right);

  friend Constraint &operator>=(long double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(short value_left, Constraint &constraint_right);

  friend Constraint &operator>=(short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(int value_left, Constraint &constraint_right);

  friend Constraint &operator>=(int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(long long value_left, Constraint &constraint_right);

  friend Constraint &operator>=(long long value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(unsigned short value_left, Constraint &constraint_right);

  friend Constraint &operator>=(unsigned short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(unsigned int value_left, Constraint &constraint_right);

  friend Constraint &operator>=(unsigned int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator>=(unsigned long long value_left, Constraint &constraint_right);

  friend Constraint &operator>=(unsigned long long value_left, Fpc_Variable &variable_right);

 
  Constraint &operator!=(Constraint &constraint);

  Constraint &operator!=(Fpc_Variable &variable);

  Constraint &operator!=(float value);

  Constraint &operator!=(double value);

  Constraint &operator!=(long double value);

  friend Constraint &operator!=(Fpc_Variable &variable_left, Constraint &constraint_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, float value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, double value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, long double value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, short value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, int value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, long long value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, unsigned short value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, unsigned int value_right);

  friend Constraint &operator!=(Fpc_Variable &variable_left, unsigned long long value_right);

  friend Constraint &operator!=(float value_left, Constraint &constraint_right);

  friend Constraint &operator!=(float value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(double value_left, Constraint &constraint_right);

  friend Constraint &operator!=(double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(long double value_left, Constraint &constraint_right);

  friend Constraint &operator!=(long double value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(short value_left, Constraint &constraint_right);

  friend Constraint &operator!=(short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(int value_left, Constraint &constraint_right);

  friend Constraint &operator!=(int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(long long value_left, Constraint &constraint_right);

  friend Constraint &operator!=(long long value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(unsigned short value_left, Constraint &constraint_right);

  friend Constraint &operator!=(unsigned short value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(unsigned int value_left, Constraint &constraint_right);

  friend Constraint &operator!=(unsigned int value_left, Fpc_Variable &variable_right);

  friend Constraint &operator!=(unsigned long long value_left, Constraint &constraint_right);

  friend Constraint &operator!=(unsigned long long value_left, Fpc_Variable &variable_right);

 

  friend Constraint &operator-(Constraint &constraint);

  friend Constraint &operator-(Fpc_Variable &variable);

 

  friend Constraint &sqrtf(Constraint &constraint);

  friend Constraint &sqrtf(Fpc_Variable &variable);

 
  friend Constraint &sqrt(Constraint &constraint);

  friend Constraint &sqrt(Fpc_Variable &variable);

 
  friend Constraint &sqrtl(Constraint &constraint);

  friend Constraint &sqrtl(Fpc_Variable &variable);

 
  friend Constraint &xxf(Constraint &constraint);

  friend Constraint &xxf(Fpc_Variable &variable);

 
  friend Constraint &xx(Constraint &constraint);

  friend Constraint &xx(Fpc_Variable &variable);

 
  friend Constraint &xxl(Constraint &constraint);

  friend Constraint &xxl(Fpc_Variable &variable);

 
  friend Constraint &fabsf(Constraint &constraint);

  friend Constraint &fabsf(Fpc_Variable &variable);

 
  friend Constraint &fabs(Constraint &constraint);

  friend Constraint &fabs(Fpc_Variable &variable);

 
  friend Constraint &fabsl(Constraint &constraint);

  friend Constraint &fabsl(Fpc_Variable &variable);

 
  friend Constraint &sinf(Constraint &constraint);

  friend Constraint &sinf(Fpc_Variable &variable);

 
  friend Constraint &sin(Constraint &constraint);

  friend Constraint &sin(Fpc_Variable &variable);

 
  friend Constraint &sinl(Constraint &constraint);

  friend Constraint &sinl(Fpc_Variable &variable);

 
  friend Constraint &cosf(Constraint &constraint);

  friend Constraint &cosf(Fpc_Variable &variable);

 
  friend Constraint &cos(Constraint &constraint);

  friend Constraint &cos(Fpc_Variable &variable);

 
  friend Constraint &cosl(Constraint &constraint);

  friend Constraint &cosl(Fpc_Variable &variable);

 
  friend Constraint &tanf(Constraint &constraint);

  friend Constraint &tanf(Fpc_Variable &variable);

 
  friend Constraint &tan(Constraint &constraint);

  friend Constraint &tan(Fpc_Variable &variable);

 
  friend Constraint &tanl(Constraint &constraint);

  friend Constraint &tanl(Fpc_Variable &variable);

 
  friend Constraint &asinf(Constraint &constraint);

  friend Constraint &asinf(Fpc_Variable &variable);

 
  friend Constraint &asin(Constraint &constraint);

  friend Constraint &asin(Fpc_Variable &variable);

 
  friend Constraint &asinl(Constraint &constraint);

  friend Constraint &asinl(Fpc_Variable &variable);

 
  friend Constraint &acosf(Constraint &constraint);

  friend Constraint &acosf(Fpc_Variable &variable);

 
  friend Constraint &acos(Constraint &constraint);

  friend Constraint &acos(Fpc_Variable &variable);

 
  friend Constraint &acosl(Constraint &constraint);

  friend Constraint &acosl(Fpc_Variable &variable);

 
  friend Constraint &atanf(Constraint &constraint);

  friend Constraint &atanf(Fpc_Variable &variable);

 
  friend Constraint &atan(Constraint &constraint);

  friend Constraint &atan(Fpc_Variable &variable);

 
  friend Constraint &atanl(Constraint &constraint);

  friend Constraint &atanl(Fpc_Variable &variable);

 
  friend Constraint &logf(Constraint &constraint);

  friend Constraint &logf(Fpc_Variable &variable);

 
  friend Constraint &log(Constraint &constraint);

  friend Constraint &log(Fpc_Variable &variable);

 
  friend Constraint &logl(Constraint &constraint);

  friend Constraint &logl(Fpc_Variable &variable);

 
  friend Constraint &expf(Constraint &constraint);

  friend Constraint &expf(Fpc_Variable &variable);

 
  friend Constraint &exp(Constraint &constraint);

  friend Constraint &exp(Fpc_Variable &variable);

 
  friend Constraint &expl(Constraint &constraint);

  friend Constraint &expl(Fpc_Variable &variable);

 
  friend Constraint &cbrtf(Constraint &constraint);

  friend Constraint &cbrtf(Fpc_Variable &variable);

 
  friend Constraint &cbrt(Constraint &constraint);

  friend Constraint &cbrt(Fpc_Variable &variable);

 
  friend Constraint &cbrtl(Constraint &constraint);

  friend Constraint &cbrtl(Fpc_Variable &variable);

 

  //===================== End automatic generation ====================

  void display_binary(char *name);
  void display();
};


Constraint &Const(float value);
Constraint &Const(double value);
Constraint &Const(long double value);

extern void SetRoundUp();
extern void SetRoundDown();
extern void SetRoundNear();
extern void SetRoundZero();

extern void SetFloatPrecision();
extern void SetDoublePrecision();
extern void SetLDoublePrecision();

class Fpc_Model {
 public:
  Fpc_Csp *csp;
  vector <Constraint *> constraints;

  Fpc_Model(Fpc_Csp &csp);

  void add(Constraint &constraint);

  Fpc_Domain *introduce_convertion_to(int type, Fpc_Domain *from, int rounding_mode);
  Fpc_Domain *introduce_convertion_from(int type, Fpc_Domain *to, int rounding_mode);
  Fpc_Domain *extracting_ternary(Constraint *constraint);
  int type_arbitration(int type1, int type2);
  int compare_type_arbitration(int type1, int type2);
  Fpc_Domain *extracting_compare(Constraint *constraint);
  Fpc_Domain *extracting_unary_ops(Constraint *constraint);
  Fpc_Domain *extracting_unary_function(Constraint *constraint);
  Fpc_Domain *extracting_unary_sfunction(Constraint *constraint);
  Fpc_Domain *extracting_trigo_function(Constraint *constraint);
  Fpc_Domain *extracting(Constraint *constraint);
  int extract(void);
};



