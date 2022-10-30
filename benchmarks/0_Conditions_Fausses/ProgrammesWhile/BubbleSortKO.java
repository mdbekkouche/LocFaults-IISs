class BubbleSort {

    /*@ requires 
      @ (tab[0]==2 && tab[1]==1 && tab[2]==0);
      @
      @ ensures
      @ (tab[0]==0 && tab[1]==1 && tab[2]==2); 
      @*/ 

    void bubbleSort (int[] tab) {
	int i = 0;
	int j = tab.length - 1;
	int aux = 0;
	int fini = 0;
	while (fini == 0) {
	    fini = 1;
	    i = 1; // error in the assignment : should be "i = 0;" 
	    while (i < j) {
		if (tab[i] > tab[i+1]) {
		    aux = tab[i];
		    tab[i] = tab[i+1];
		    tab[i+1] = aux;
		    fini = 0;				
		}
		i = i + 1;			
	    }
	    j = j - 1;
	}
	
    }
    
}
