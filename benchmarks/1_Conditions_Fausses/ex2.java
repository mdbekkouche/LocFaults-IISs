class ex1 {
    /*@ requires
      @	(a==5) && (b==5);
      @ ensures 
      @ (r==a+b) && (res==1); 
      @*/
	void fct (int a, int b) {
		int r, res;
		if (a<10 && b<10) {
		r = a - b;
		if (r > 0) {
			res = 1;
		} else if (r == 0) {
			res = 0;
		} else {
			res = -1;
		} 
		} else {
			r = -1;
		}
	}
}
