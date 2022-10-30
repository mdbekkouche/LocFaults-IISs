chemin_Programmes_Tcas=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas

while read line0
do
 if [[ $line0 != "Tcas" ]]
 then
 
  while read line
  do    
        mkdir -p $chemin_Programmes_Tcas/$line0/$line0"_Specs";
        mkdir -p $chemin_Programmes_Tcas/$line0/$line0"_SpecsC";

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
        
        if [[ $Alt_Layer_Value != 5 && $Alt_Layer_Value != -1 && $Alt_Layer_Value != -2 && $Alt_Layer_Value != 4 ]]
        then  

         
        tail -n+4 $chemin_Programmes_Tcas/$line0/$line0.java > $chemin_Programmes_Tcas/$line0/copie
        tail -n+0  $chemin_Programmes_Tcas/$line0/copie > $chemin_Programmes_Tcas/$line0/$line0"_Specs"/$line0"_$p2.java"
        
        sed -i "1i\class tcastest\n{\n\n/*@ requires\n@ (Cur_Vertical_Sep == $Cur_Vertical_Sep)\n@ && (High_Confidence == $High_Confidence)\n@ && (Two_of_Three_Reports_Valid == $Two_of_Three_Reports_Valid)\n@ && (Own_Tracked_Alt == $Own_Tracked_Alt)\n@ && (Own_Tracked_Alt_Rate == $Own_Tracked_Alt_Rate)\n@ && (Other_Tracked_Alt == $Other_Tracked_Alt)\n@ && (Alt_Layer_Value == $Alt_Layer_Value)\n@ && (Up_Separation == $Up_Separation)\n@ && (Down_Separation == $Down_Separation)\n@ && (Other_RAC == $Other_RAC)\n@ && (Other_Capability == $Other_Capability)\n@ && (Climb_Inhibit == $Climb_Inhibit)\n@ && (Positive_RA_Alt_Thresh[0] == 0)\n@ && (Positive_RA_Alt_Thresh[1] == 0)\n@ && (Positive_RA_Alt_Thresh[2] == 0)\n@ && (Positive_RA_Alt_Thresh[3] == 0)\n@ && (Positive_RA_Alt_Thresh[4] == 0);\n@ ensures\n@ (alt_sep == $alt_sep);\n@*/" $chemin_Programmes_Tcas/$line0/$line0"_Specs"/$line0"_$p2.java"
        
       echo -e "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nvoid $line0(int Cur_Vertical_Sep,int High_Confidence,int Two_of_Three_Reports_Valid,int Own_Tracked_Alt,int Own_Tracked_Alt_Rate,int Other_Tracked_Alt,int Alt_Layer_Value,int Up_Separation,int Down_Separation,int Other_RAC,int Other_Capability,int Climb_Inhibit)
	{ __CPROVER_assume((Cur_Vertical_Sep==$Cur_Vertical_Sep) &&(High_Confidence==$High_Confidence)&&(Two_of_Three_Reports_Valid==$Two_of_Three_Reports_Valid) && (Own_Tracked_Alt == $Own_Tracked_Alt)&&(Own_Tracked_Alt_Rate == $Own_Tracked_Alt_Rate)&&(Other_Tracked_Alt == $Other_Tracked_Alt)&&(Alt_Layer_Value == $Alt_Layer_Value)&&(Up_Separation == $Up_Separation)&&(Down_Separation == $Down_Separation)&&(Other_RAC == $Other_RAC)&&(Other_Capability == $Other_Capability)&&(Climb_Inhibit == $Climb_Inhibit));int Positive_RA_Alt_Thresh[4];" > $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c";
  

       tail -n+28 $chemin_Programmes_Tcas/$line0/$line0"_Specs"/$line0"_$p2.java" >> $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c";
 
       head -n+116 $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c" > $chemin_Programmes_Tcas/$line0/copie;
   
       cat $chemin_Programmes_Tcas/$line0/copie > $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c";

       echo -e "assert(alt_sep == $alt_sep);\n}" >> $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c";

       sed -i -e "s/true/1/g" $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c";

       sed -i -e "s/false/0/g" $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c";
  
       sed -i -e "s/boolean/int/g" $chemin_Programmes_Tcas/$line0/$line0"_SpecsC"/$line0"_$p2.c";
   
       fi

      
  done < $chemin_Programmes_Tcas/$line0/CE_$line0.txt
 
 fi
done < $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt
