class tcastest
{
     /*@
       @ ensures
       @ (((Up_Separation >= Positive_RA_Alt_Thresh) && (Down_Separation < Positive_RA_Alt_Thresh)) ==> (alt_sep != DOWNWARD_RA));
      @*/
   
     /*@
       @ ensures
       @ (((Up_Separation < Positive_RA_Alt_Thresh) && (Down_Separation >= Positive_RA_Alt_Thresh)) ==> (alt_sep != UPWARD_RA));
      @*/
      

    int tcas(int Cur_Vertical_Sep,boolean High_Confidence,boolean Two_of_Three_Reports_Valid,int Own_Tracked_Alt,int Own_Tracked_Alt_Rate,int Other_Tracked_Alt,int Positive_RA_Alt_Thresh,int Up_Separation,int Down_Separation,int Other_RAC,int Other_Capability,boolean Climb_Inhibit)
    {
        int OLEV;
	int MAXALTDIFF;
	int MINSEP;
	int NOZCROSS;
	int NO_INTENT;
	int TCAS_TA;
	int UNRESOLVED;
	int UPWARD_RA;
	int DOWNWARD_RA; 
        boolean enabled;
	boolean tcas_equipped;
	boolean intent_not_known;
	boolean need_upward_RA;
	boolean need_downward_RA;
	int alt_sep;
	boolean upward_preferred;
	int upward_crossing_situation;
	boolean resultat;
        boolean resultat1;                 
        int Inhibit_Biased_Climb;
	int upward_crossing_situation;
        int res;
        OLEV = 600;
	MAXALTDIFF = 600;
	MINSEP = 300;
	NOZCROSS = 100;
	NO_INTENT = 0;
	TCAS_TA = 1;
	UNRESOLVED = 0;
	UPWARD_RA = 1;
	DOWNWARD_RA = 2;
        enabled = (High_Confidence) && (Own_Tracked_Alt_Rate <= OLEV) && (Cur_Vertical_Sep > MAXALTDIFF);
	tcas_equipped = (Other_Capability == TCAS_TA);
	intent_not_known = ((Two_of_Three_Reports_Valid) && (Other_RAC == NO_INTENT));
	alt_sep = UNRESOLVED;
        if ((enabled) && (((tcas_equipped) && (intent_not_known)) || !(tcas_equipped)))
	    { 
		if (Climb_Inhibit){
		    Inhibit_Biased_Climb=Up_Separation + NOZCROSS;
		}
		else{
		    Inhibit_Biased_Climb=Up_Separation;
		}
		upward_preferred = (Inhibit_Biased_Climb > Down_Separation);
		
		if (upward_preferred)
		    {  
			resultat = (!(Own_Tracked_Alt < Other_Tracked_Alt) || ((Own_Tracked_Alt < Other_Tracked_Alt) && !(Down_Separation > Positive_RA_Alt_Thresh))); /* opertor mutation */
		    }
		else
		    {  
			resultat = (Other_Tracked_Alt < Own_Tracked_Alt) && (Cur_Vertical_Sep >= MINSEP) && (Up_Separation >= Positive_RA_Alt_Thresh);
		    }  
		
		need_upward_RA= (resultat) && (Own_Tracked_Alt < Other_Tracked_Alt);
		if (Climb_Inhibit){
		    res=Up_Separation + NOZCROSS; 
		}
		else{
		    res=Up_Separation;
		} 
		upward_preferred = (res > Down_Separation);
		if (upward_preferred)
		    {
			resultat1 = (Own_Tracked_Alt < Other_Tracked_Alt) && (Cur_Vertical_Sep >= MINSEP) && (Down_Separation >= Positive_RA_Alt_Thresh);
		    }
		else
		    {
			resultat1 = !(Other_Tracked_Alt < Own_Tracked_Alt) || (((Other_Tracked_Alt < Own_Tracked_Alt)) && (Up_Separation >= Positive_RA_Alt_Thresh));
		    } 
		
		need_downward_RA=(resultat1) && (Other_Tracked_Alt < Own_Tracked_Alt);
		if ((need_upward_RA)  && (need_downward_RA)){
		    //         unreachable: requires Own_Below_Threat and Own_Above_Threat
		    //           to both be true - that requires Own_Tracked_Alt < Other_Tracked_Alt
		    //           and Other_Tracked_Alt < Own_Tracked_Alt, which isn't possible 
		    alt_sep = UNRESOLVED; 
                }
		else if (need_upward_RA){
		    alt_sep = UPWARD_RA; 
		}
		else if (need_downward_RA)
		    {alt_sep = DOWNWARD_RA;}
		else 
		    {alt_sep = UNRESOLVED;}
	    }
	
	return alt_sep;
    }
}
