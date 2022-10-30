/*
 * find the median of three variables.
 */

    
      
       
     

      
    void mid (int a, int b, int c) {__CPROVER_assume((a==2) && (b==1) && (c==3));
	int m;
	m=c;
	if (b<c){
	    if (a<b){
		m=b;
	    }
	    else if (a<c){
		m=b; // error, the instruction should be m=a;
	    }          
	}
	else{
	    if (a>b){
		m=b;
	    }
	    else if (a>c){
		m=a;
	    }
	}	
     assert(m==2);
    }
