/* The minimum in an array of n integers
 */

class Minimum {

	/*@ requires (a[0]==3 && a[1]==2 && a[2]==1 && a[3]==0); 
          @ ensures (min<=a[0] && min<=a[1] && min<=a[2] && min<=a[3]); 
	  @*/
	static int Minimum (int[] a) {
		int min=a[0];
		int i = 1;
		while (i<a.length) {
                     if (a[i]<=min){
                        min=a[i];
                     }
		     i = i+1;
		}
		return min;
	}
}
