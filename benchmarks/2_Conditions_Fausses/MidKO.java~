/*
 * find the median of three variables.
 */
class Foo{

     /*@ requires 
       @ ((a==2) && (b==1) && (c==3));
       @ ensures 
       @ (m==2);
       @*/
    static void max (int a, int b, int c) {
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
    }
}
