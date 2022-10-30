class array_int{

 /*@ requires 
   @(\forall int k;(k >= 0 && k < a.length);a[k] == 0);
   @ ensures 
   @ (a[18] == 19); 
   @*/
int ArrayInitKO(int[] a){
  int i;
  i=0;
  while(i<a.length){
    a[i]=0;  // error in the assignment : should be "a[i]=i+1"
    i=i+1;
  }
  return 0;
}
}
