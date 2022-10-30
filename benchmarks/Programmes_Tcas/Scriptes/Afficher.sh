chemin_Programmes_Tcas=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas


echo -n "" > $chemin_Programmes_Tcas/Resultats_LocFaults.txt
echo -n "" > $chemin_Programmes_Tcas/Resultats_BugAssist.txt

while read line
do
  if [[ $line == TcasKO28 ]] 
  then
    k=0;
    SumSusInstrs=0;
    timeLF=0;
    SumSusInstrs1=0;
    timeBA=0;
    nbok=0;
    nbok1=0;     
    for prog in `ls $chemin_Programmes_Tcas/$line/$line"_Specs"`; do
       k=$((k+1));

       p=$(echo $prog | cut -d"." -f1);
       
       NumSusLocFaults=0;
       while read line2
       do
          if [[ ${line2:0:40} =  "4. The number of suspicious instructions" ]] 
	  then
              SumSusInstrs=$(($SumSusInstrs+$(echo $line2 | cut -d":" -f2)*$(echo $line2 | cut -d":" -f2)));
              NumSusLocFaults=$(echo $line2 | cut -d":" -f2);
          fi     

       done < $chemin_Programmes_Tcas/$line/$line"_Faults"/Faults_$prog".faults";

       NumSusBugAssist=0;
       while read line3
       do
          if [[ ${line3:0:37} =  "The number of suspicious instructions" ]] 
	  then              
              SumSusInstrs1=$(($SumSusInstrs1+$(echo $line3 | cut -d":" -f2)*$(echo $line3 | cut -d":" -f2)));
              NumSusBugAssist=$(echo $line3 | cut -d":" -f2);
          fi
      
       done < $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";

              
       while read line4
       do
          if [[ ${line4:0:64} =  "2. Total elapsed time during DFS exploration and MCS calculation" ]] 
	  then
              timeLF=$(echo $timeLF+$(echo $line4 | cut -d":" -f2) | bc -l );
          fi     

       done < $chemin_Programmes_Tcas/$line/$line"_Faults"/Faults_$prog".faults";

 
       while read line5
       do
          if [[ ${line5:0:9} =  "User time" ]] 
	  then
              timeBA=$(echo $timeBA+$(echo $line5 | cut -d":" -f2) | bc -l );
          fi
      
       done < $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";
       
       
       
       #BugAssist

       ErrorsBugAssist="";
       while read line6
       do
          if [[ ${line6:0:24} =  "Suspicious instructions:" ]] 
	  then
              ErrorsBugAssist=$(echo $line6 | cut -d":" -f2);
          fi      
       done < $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";
       
       while read line61
       do  echo $ErrorsBugAssist;
           cpt=1;
           boolean=0;
           
           while [ $((NumSusBugAssist+1)) != $cpt ]
           do 
             if [[ $(echo $ErrorsBugAssist | cut -d"," -f$cpt) = $line61 ]]
             then
                boolean=1;
             fi
             cpt=$((cpt+1)); 
           done       
           if [[ $boolean = 1 ]]
           then 
              echo "Ok"; 
              nbok=$((nbok+1));
           else
              BugAssist=Faults_$prog;
           fi  
    
       done < $chemin_Programmes_Tcas/$line/erreurs.txt;
       
       
        
       #LocFaults       
            
       ErrorsLocFaults="";
       while read line7
       do
          if [[ ${line7:0:27} =  "3. Suspicious instructions:" ]] 
	  then
              ErrorsLocFaults=$(echo $line7 | cut -d":" -f2);
          fi      
       done < $chemin_Programmes_Tcas/$line/$line"_Faults"/Faults_$prog".faults";
    
       
       nberrors=0; 
       while read line71
       do  echo $ErrorsLocFaults;
           nberrors=$((nberrors+1));
           cpt=1;
           boolean=0;
           
           while [ $((NumSusLocFaults+1)) != $cpt ]
           do
             if [[ $(echo $ErrorsLocFaults | cut -d"," -f$cpt) = $line71 ]]
             then
               boolean=1; 
             fi
             cpt=$((cpt+1)); 
           done
           if [[ $boolean = 1 ]]
           then 
              echo "OK"; 
              nbok1=$((nbok1+1));
           else
              LocFaults=Faults_$prog;
           fi  

       done < $chemin_Programmes_Tcas/$line/erreurs.txt;  

       

     echo Faults_$prog".faults"
    done
    
    echo "LocFaults:"$nbok1";"$LocFaults;
    echo "BugAssist:"$nbok";"$BugAssist;
     

    echo $k;

    
    
    #echo -n $(echo $timeLF | cut -d"." -f1)","$(echo $timeLF | cut -d"." -f2) >> $chemin_Programmes_Tcas/Resultats_LocFaults.txt;
    #echo -e >> $chemin_Programmes_Tcas/Resultats_LocFaults.txt;
    #echo -n $SumSusInstrs >> $chemin_Programmes_Tcas/Resultats_LocFaults.txt;
    echo -n $nbok1 >> $chemin_Programmes_Tcas/Resultats_LocFaults.txt;
    #echo -n $k >> $chemin_Programmes_Tcas/Resultats_LocFaults.txt;   

    #echo -n $(echo $timeBA | cut -d"." -f1)","$(echo $timeBA | cut -d"." -f2) >> $chemin_Programmes_Tcas/Resultats_BugAssist.txt;
    #echo -e >> $chemin_Programmes_Tcas/Resultats_BugAssist.txt;
    echo -n $nbok >> $chemin_Programmes_Tcas/Resultats_BugAssist.txt;
    #echo -n $SumSusInstrs1 >> $chemin_Programmes_Tcas/Resultats_BugAssist.txt;
    #echo -n $k >> $chemin_Programmes_Tcas/Resultats_BugAssist.txt;

    echo -e  >> $chemin_Programmes_Tcas/Resultats_LocFaults.txt;
    echo -e  >> $chemin_Programmes_Tcas/Resultats_BugAssist.txt; 

  fi
done < $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt
