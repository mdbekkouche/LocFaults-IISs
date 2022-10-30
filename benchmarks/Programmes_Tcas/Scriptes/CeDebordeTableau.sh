chemin_Programmes_Tcas=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas

while read line0
do
 if [[ $line0 != "Tcas" ]]
 then
  
  echo -n $line0 >> $chemin_Programmes_Tcas/CeDebordeTableauChaqueversion.txt;
  echo -e  >> $chemin_Programmes_Tcas/CeDebordeTableauChaqueversion.txt;
   

        
        


  while read line
  do    
        
        p2=$(echo $line | cut -d":" -f1);
        p1=$(echo $line | cut -d":" -f2);
        

        Cur_Vertical_Sep=$(echo $p1 | cut -d" " -f1);
        High=$(echo $p1 | cut -d" " -f2);
        if [ $High=1 ]; then 
           High_Confidence=true
        else 
           High_Confidence=false
        fi
	Two=$(echo $p1 | cut -d" " -f3);
	if [ $Two == 1 ]; then 
	    Two_of_Three_Reports_Valid=true
	else 
	    Two_of_Three_Reports_Valid=false
	fi
	Own_Tracked_Alt=$(echo $p1 | cut -d" " -f4);
	Own_Tracked_Alt_Rate=$(echo $p1 | cut -d" " -f5);
	Other_Tracked_Alt=$(echo $p1 | cut -d" " -f6);
	Alt_Layer_Value=$(echo $p1 | cut -d" " -f7);
	Up_Separation=$(echo $p1 | cut -d" " -f8);
	Down_Separation=$(echo $p1 | cut -d" " -f9);
	Other_RAC=$(echo $p1 | cut -d" " -f10);
	Other_Capability=$(echo $p1 | cut -d" " -f11);
	Climb=$(echo $p1 | cut -d" " -f12);
	if [ $Climb == 1 ]; then 
	    Climb_Inhibit=true
	else 
	    Climb_Inhibit=false
	fi
        alt_sep=$($chemin_Programmes_Tcas/Tcas/Tcas $p1);

        if [[ $Alt_Layer_Value == 5 || $Alt_Layer_Value == -1 || $Alt_Layer_Value == -2 || $Alt_Layer_Value == 4 ]]
        then 
            
           echo -n $line";"Alt_Layer_Value=$Alt_Layer_Value >> $chemin_Programmes_Tcas/CeDebordeTableauChaqueversion.txt;
           echo -e  >> $chemin_Programmes_Tcas/CeDebordeTableauChaqueversion.txt;
        fi

      
  done < $chemin_Programmes_Tcas/$line0/CE_$line0.txt
 
 fi
done < $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt
