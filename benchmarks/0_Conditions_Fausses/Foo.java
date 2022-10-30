class programme{

 /*@ ensures 
   @ (c >= d+e);
   @*/ 
void foo(int a,int b){
  int c,d,e,f;
  if (a>=0){
    c=a;
    d=a;
    e=b;
  }
  else{
    c=b; /* error */
    d=1;
    e=-a;
    if (a>b){
      f=b+e+a;
      d=d+4;
    }
    else{
      f=e;
    }
  }
  c=c+d+e; /* error */
}
}
