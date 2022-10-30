class array_int{

 /*@ requires 
   @ (\forall int k;(k >= 0 && k < a.length);a[k] == 0);
   @ ensures 
   @ (\forall int k;(k >= 0 && k < a.length);a[k] == k+1); 
   @*/
int array_int(int[] a){
  int[] b;
  int i;
  i=0;
  while(i<a.length){
    a[i]=i; // error in the assignment : should be "a[i]=i+1" 
    i=i+1;
  }
  return 0;
}
}
