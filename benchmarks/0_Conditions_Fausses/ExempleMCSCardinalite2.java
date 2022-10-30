class programme{

 /*@ ensures 
   @ ((z==0 || i!=0) && (v>=0) && (w>=0));
   @*/ 
void foo(int i){
  int v=5;
  int w=6;
  int z=i+(v+w); // error in the assignment : z=i+(v+w); instead of z=i*(v+w);
}
}
