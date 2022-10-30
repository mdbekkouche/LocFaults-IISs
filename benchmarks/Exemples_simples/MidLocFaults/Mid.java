/*
 * find the median of three variables.
 */
class mid{

     
     
     
      
       
    static void mid (int a, int b, int c) {
	int m;
	m=c;
	if (b<c){
	    if (a<b){
		m=b;
	    }
	    else if (a<c){
		m=a;  
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
    }
}
