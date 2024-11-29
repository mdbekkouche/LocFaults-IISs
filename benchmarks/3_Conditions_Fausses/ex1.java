class Foo{

     /*@ requires 
       @ ((a==7));
       @ ensures 
       @ (x==6);
       @*/
    void ex1 (int a) {
    	int x, j, z;
	x = 0;
	j = 5;
	a = a - 10; // Corrected version of the line (original was a = a - 10)
	if (a > j) {
	x = x + 1;
	} else {
	z = 0;
	}
	x = x + j;	
    }
}
