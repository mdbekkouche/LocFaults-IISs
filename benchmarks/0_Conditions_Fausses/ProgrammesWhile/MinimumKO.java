/* The minimum in an array of n integers
 */

class Minimum {

	/*@ requires (a[0]==19 && a[1]==18 && a[2]==17 && a[3]==16 && a[4]==15 && a[5]==14 && a[6]==13 && a[7]==12 && a[8]==11 && a[9]==10 && a[10]==9 && a[11]==8 && a[12]==7 && a[13]==6 && a[14]==5 && a[15]==4 && a[16]==3 && a[17]==2 && a[18]==1 && a[19]==0); 
          @ ensures (min<=a[0] && min<=a[1] && min<=a[2] && min<=a[3] && min<=a[4] && min<=a[5] && min<=a[6] && min<=a[7] && min<=a[8] && min<=a[9] && min<=a[10] && min<=a[11] && min<=a[12] && min<=a[13] && min<=a[14] && min<=a[15] && min<=a[16] && min<=a[17] && min<=a[18] && min<=a[19]); 
	  @*/
	static int Minimum (int[] a) {
		int min=a[0];
		int i = 1;
		while (i<a.length-1) { /*error, the condition should be (i<a.length)*/
                     if (a[i]<=min){
                        min=a[i];
                     }
		     i = i+1;
		}
		return min;
	}
}
