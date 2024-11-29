class ex1 {
	
	int fct2(int i, int j) {
      		int r;
      		if (i<10 && j<10) {
			r = i - j; // r = i + j
			if (r > 0) {
				return 1;
			} else {
				return -1;
			} 
		} else {
			return -2;
		}
      	}
    /*@ requires
      @	(a==2) && (b==1) && (c==3);
      @ ensures 
      @ (res==1); 
      @*/
	void fct(int a, int b, int c) {
		int m, res;
		res=0;
		m=c;
		if (b<c){
		    if (a<b){
			res=fct2(a,c);
		    }
		    else if (a<c){
			res=fct2(b,c);
		    }          
		}
		else{
		    if (a>b){
			res=fct2(a,c);
		    }
		    else if (a>c){
			res=fct2(b,c);
		    }
		}
		if (res==0) {
			res=fct2(a,b);
		}
	}
}
