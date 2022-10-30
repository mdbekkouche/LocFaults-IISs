class BSearch {

   /*@ requires 
      @ (x==2959 && tab[0]==-2147480651 && tab[1]==-2147472932 && tab[2]==2940 && tab[3]==2950 && tab[4]==2954 && tab[5]==2955 && tab[6]==2956 && tab[7]==2959 && tab[8]==2960 && tab[9]==2960 );
      @ ensures
      @ (\result == 7);
      @*/
    int caller(int[] tab, int x) {
	int result = -1;
	int milieu = 0;
	int gauche = 0;
	int droite = tab.length - 1;
         
	while ((result == -1) && (gauche <= droite)) {
	    milieu = (gauche + droite) / 2;
	    if (tab[milieu] == x) {
		result = milieu;
	    }
	    else if (tab[milieu] > x) {
		droite = milieu - 1;
	    }
	    else {
		gauche = milieu; /*error: the instruction should be "gauche = milieu + 1" */
	    }
	}

	return result;
    }

}
