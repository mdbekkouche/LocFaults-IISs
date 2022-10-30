class minmax {
    
    /*@ ensures 
      @ (least <= most);
      @*/

    public static void MinmaxKO(int in1,int in2,int in3){
	int least;
        int most;
	least = in1;
	most = in1;
	if (most < in2){
	    most = in2;
	}
	if (most < in3){
	    most = in3;
	}
	if (least > in2){ 
	    most = in2;  // error in the assignment : most = in2 instead of least = in2
	}
	if (least > in3){ 
	    least = in3; 
	}
    }
}
