class BubbleSort { 
 /*@ requires (tab[0] == 9 && tab[1] == 8 && tab[2] == 7 && tab[3] == 6 && tab[4] == 5 && tab[5] == 4 && tab[6] == 3 && tab[7] == 2 && tab[8] == 1 && tab[9] == 8);
   @ ensures (\forall int k1; (k1 >= 0 && k1 < tab.length - 1); tab[k1] <= tab[k1+1]);*/
    void bubbleSort (int[] tab) {
	int i = 0;
	int j = tab.length-1; /*error : the instruction should be j = tab.length */
	int aux = 0;
	int fini = 0;
	while (fini == 0) {
	    fini = 1;
	    i = 1;
	    while (i < j) {
            /*@ assert (i > 0 && i < tab.length); */
		if (tab[i-1] > tab[i]) {
		    aux = tab[i-1];
		    tab[i-1] = tab[i];
		    tab[i] = aux;
		    fini = 0;	
		}
		i = i + 1;			
	    }
	    j = j - 1;
	}
	return;
    }
    
}
