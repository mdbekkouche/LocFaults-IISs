class BSearch {

    /*@ requires 
      @ (\forall int k1; 
      @   (k1 >= 0 && k1 < tab.length - 1); 
      @   tab[k1] <= tab[k1+1]);
      @
      @ ensures
      @     (((\result == -1) ==> 
      @       (\forall int k2; (k2 >= 0 && k2 < tab.length); tab[k2] != x)) 
      @     && 
      @     ((\result != -1) ==> (tab[\result] == x)));
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
		gauche = milieu + 1;
	    }
	}

	return result;
    }

}
