/*
 * sum of the n first integers
 * there is an error when n is greater than 1000
 */
class Sum {

	/*@ requires (n==20);
	  @ ensures (\result == 210);
	  @*/
	static int caller (int n) {
		int s = 0;
		int i = 1;
		int errorRank = 12;
		while (i <= n) {
			s = s + i;
			i = i + 1;		
			if (i>=errorRank) {
				s=1;
			}
		}
		return s;
	}


}
