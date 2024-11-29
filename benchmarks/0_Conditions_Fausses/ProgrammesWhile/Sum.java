class Sum {
 /*@ requires (n == 15);
   @ ensures (\result == (n*(n+1))/2);@*/
	static int Sum (int n) {
		int s = 0;
		int i = 0;
		while (i < n) {
			s = s + i;
			i = i + 1;			
		}
		return s;
	}


}
