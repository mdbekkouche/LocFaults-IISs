class BubbleSort { 
 /*@ requires (tab[0] == -24186 && tab[1] == -25049 && tab[2] == -25049 && tab[3] == -25049 && tab[4] == -25049 && tab[5] == -25049 && tab[6] == -25049 && tab[7] == -25049 && tab[8] == -25049 && tab[9] == -25049 && tab[10] == -31452 && tab[11] == -31453 && tab[12] == -31454 && tab[13] == -31455 && tab[14] == -31456 && tab[15] == -31457 && tab[16] == -31458 && tab[17] == -31459 && tab[18] == -31460 && tab[19] == -31461 && tab[20] == -31462 && tab[21] == -24882);
   @ ensures (tab[0] >= tab[1] && tab[1] >= tab[2] && tab[2] >= tab[3] && tab[3] >= tab[4] && tab[4] >= tab[5] && tab[5] >= tab[6] && tab[6] >= tab[7] && tab[7] >= tab[8] && tab[8] >= tab[9] && tab[9] >= tab[10] && tab[10] >= tab[11] && tab[11] >= tab[12] && tab[12] >= tab[13] && tab[13] >= tab[14] && tab[14] >= tab[15] && tab[15] >= tab[16] && tab[16] >= tab[17] && tab[17] >= tab[18] && tab[18] >= tab[19] && tab[19] >= tab[20] && tab[20] >= tab[21]);*/
    void bubbleSort (int[] tab) {
	int i = 0;
	int j = tab.length - 1; /*error : the instruction should be j = tab.length */
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
