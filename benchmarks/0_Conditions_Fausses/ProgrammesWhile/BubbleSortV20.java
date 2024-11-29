class BubbleSort { 
 /*@ requires (tab[0] == 23 && tab[1] == 22 && tab[2] == 21 && tab[3] == 20 && tab[4] == 19 && tab[5] == 18 && tab[6] == 17 && tab[7] == 16 && tab[8] == 15 && tab[9] == 14 && tab[10] == 13 && tab[11] == 12 && tab[12] == 11 && tab[13] == 10 && tab[14] == 9 && tab[15] == 8 && tab[16] == 7 && tab[17] == 6 && tab[18] == 5 && tab[19] == 4 && tab[20] == 3 && tab[21] == 2 && tab[22] == 1 && tab[23] == 22);
   @ ensures (tab[0] >= tab[1] && tab[1] >= tab[2] && tab[2] >= tab[3] && tab[3] >= tab[4] && tab[4] >= tab[5] && tab[5] >= tab[6] && tab[6] >= tab[7] && tab[7] >= tab[8] && tab[8] >= tab[9] && tab[9] >= tab[10] && tab[10] >= tab[11] && tab[11] >= tab[12] && tab[12] >= tab[13] && tab[13] >= tab[14] && tab[14] >= tab[15] && tab[15] >= tab[16] && tab[16] >= tab[17] && tab[17] >= tab[18] && tab[18] >= tab[19] && tab[19] >= tab[20] && tab[20] >= tab[21] && tab[21] >= tab[22] && tab[22] >= tab[23]);*/
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
