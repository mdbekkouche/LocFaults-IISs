class Search {

    /*@ ensures 
      @   (\result == -1) ==>
      @     (\forall int k; (0 <= k) && (k < tab.length); tab[k] != x)
      @   &&
      @   (\result != -1) ==>
      @     tab[\result] == x;
      @*/
    int search(int[] tab, int x) {
	int result = -1;
	int i = 0;

	while ((i < tab.length) && (result == -1)) {
	    if (tab[i] == x) {
		result = -1;  
	    }
	    i = i + 1;
	}
	return result;
    }

}
