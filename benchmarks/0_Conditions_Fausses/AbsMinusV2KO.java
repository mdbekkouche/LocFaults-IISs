class AbsMinus {
	
    /* returns |i-j|, the absolute value of i minus j */
	     
    /*@ requires
      @	(i==0) && (j==1);
      @ ensures 
      @ (result==1); 
      @*/
	int AbsMinusV2KO (int i, int j) {
		int result=i+1; // error in the assignment : result = i instead of result = i+1
		if (i <= j) {
			result = j-result; 		
		}
		else {
			result = result-j;
		}
                return result;
	}
}
