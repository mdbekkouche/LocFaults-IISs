class HeronKO3 {

	/*@ requires 
          @ (i ==1 && j == 2 && k ==1);
	  @ ensures
	  @ (res == -1);
	  @*/

	static int caller (int i, int j, int k) {
		int trityp = 0;
		int res = 0;
		int s = (i+j+k)/2;
		if (i == 0 || j == 0 || k == 0) {
			trityp = 4;
			res = 0;
		}
		else {
			trityp = 0;
			if (i == j) {
				trityp = trityp + 1;
			}
			if (i == k) {
				trityp = trityp + 2;
			}
			if (j == k) {
				trityp = trityp + 3;
			}
			if (trityp == 0) {
				if ((i+j) <= k || ((j+k) <= i || (i+k) <= j)) {
					trityp = 4;
					res = -1;
				}
				else {
				    trityp = 1; // any triangle
				    res = s*(s-i)*(s-j)*(s-k);
				}			
			}
			else {
			    if (trityp > 3) { // equilateral
				trityp = 3;
				res = (3*i*i*i*i)/16;
				}
				else {
				    if (trityp == 1 && (i+j) > k) { // isosceles
						trityp = 2;
						// i==j
						res = s*(s-i)*(s-i)*(s-k);

					}
					else {
                                                // error in the condition : (i+j) > k instead of (i+k) > j
						if (trityp == 2 && (i+j) > k) {
							trityp = 2;
							// i==k
						        res = s*(s-i)*(s-j)*(s-i); 
						}
						else {
							if (trityp == 3 && (j+k) > i) {
								trityp = 2;
								// j==k
								res = s*(s-i)*(s-j)*(s-j);
							}
							else {
								trityp = 4;
								res = -1;

							}
						}
					}
				}
			}
		}
		return res;
	}

}
