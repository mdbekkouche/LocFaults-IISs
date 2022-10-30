class prim{
  
  /*@ requires 
   @ (nodecount==4)
   @ && (Source[0]==3 && Source[1]==2 && Source[2]==1 && Source[3]==0 )
   @ && (Dest[0]==0 && Dest[1]==3 && Dest[2]==2 && Dest[3]==1 )
   @ && (Weight[0]==4 && Weight[1]==3 && Weight[2]==2 && Weight[3]==1 )
   @ && (\forall int k;(k >= 0 && k < ResultNodes.length);ResultNodes[k] == 0)
   @ && (\forall int k;(k >= 0 && k < ResultEdges.length);ResultEdges[k] == 0)
   @ && (\forall int k;(k >= 0 && k < visited.length);visited[k] == 0);
   @ ensures 
   @ (k==3); 
   @*/

int prim(int[] ResultNodes,int[] ResultEdges,int[] Source,int[] Dest,int[] Weight,int[] visited,int nodecount,int k){
  int INFINITY = 899;
  int edgecount = 4;

  int i,j,k1,h, sourceflag, destflag, min;
   
  ResultNodes[0]=0;

  i=1;
  while(i < nodecount){
    ResultNodes[i] = INFINITY;
    i=i+1;
  }

  i=0;
  while(i < nodecount-1){
    ResultEdges[i] = INFINITY;
    i=i+1;
  }

  k=0;  

  while( k!= nodecount-1) 
    {

      
      min=0;
      i=0;
      while(i< edgecount){
        visited[i]= Weight[i];
        i=i+1;
      }

      k1=0;
      while(k1< edgecount)
        {
          h=0;
          while(h < edgecount)
            {

              if( visited[h]< visited[min] ){
                min = h;
              }
              h=h+1;
            }

          sourceflag=0;
          j=0;
          while(j < nodecount)
            {
              if(  Source[min]== ResultNodes[j]){
                sourceflag=1;
              }
             j=j+1;
            }

          destflag=1;
          j=0;
          while(j < nodecount)
            {
              if(  Dest[min]== ResultNodes[j]){
                  destflag=0;
                }
              j=j+1;
            }
          if( sourceflag==0 && destflag==0)
            {
              visited[min]=889;
            }
         k1=k1+1;
        }
      ResultEdges[k]=min;
      ResultNodes[k+1]=Dest[min];
      Weight[min]= INFINITY;
      k=k+1;

    }
  return 0;
}
}
