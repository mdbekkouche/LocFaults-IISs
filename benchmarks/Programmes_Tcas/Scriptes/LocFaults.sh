chemin_Programmes_Tcas=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas
chemin_CPBPV=/home/mdbekkouche/These/CBEL_ProjectV4/core/cpbpv
chemin_BugAssit=/home/mdbekkouche/These/Outils_Bug-Assist

while read line
do
  if [[ $line != Tcas ]] 
  then
    mkdir -p $chemin_Programmes_Tcas/$line/$line"_CECPBPV";
    mkdir -p $chemin_Programmes_Tcas/$line/$line"_Faults";
    mkdir -p $chemin_Programmes_Tcas/$line/$line"_FaultsBA";
    k=0;
    for prog in `ls $chemin_Programmes_Tcas/$line/$line"_Specs"`; do
     k=$((k+1));
    #$chemin_CPBPV -solvers Z3 -writeCE $chemin_Programmes_Tcas/$line/$line"_CECPBPV"/CE_$prog".ce" $chemin_Programmes_Tcas/$line/$line"_Specs"/$prog -arrays_length 4  -int_format 32
    $chemin_CPBPV -solvers CPLEX -locate $chemin_Programmes_Tcas/$line/$line"_CECPBPV"/CE_$prog".ce" -NbFaultyCond 1 $chemin_Programmes_Tcas/$line/$line"_Specs"/$prog -arrays_length 4  -int_format 32 > $chemin_Programmes_Tcas/$line/$line"_Faults"/Faults_$prog".faults";

     p=$(echo $prog | cut -d"." -f1);
 
    /usr/bin/time -o  $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults" $chemin_BugAssit/bugassist $chemin_Programmes_Tcas/$line/$line"_SpecsC"/$p.c --function $line --ba --maxsat $chemin_BugAssit/msuncore_20090525/bin/msuncore > $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";

    echo -e  >> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";     

    echo -n "Suspicious instructions:" >> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";
    
    cptt=0;
    while read line2 
    do
	if [[ ${line2:0:2} =  "< " ]] 
	then
        ((cptt++));
            if [[ $cptt = 1 ]]
            then
		echo -n "$(echo $line2 | cut -d":" -f3)">> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";
	    else
		echo -n ",$(echo $line2 | cut -d":" -f3)">> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";
	    fi    
	fi  
    done < $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";

    echo -e  >> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults"; 

    echo -n "The number of suspicious instructions:" >> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults"; 

    echo -e $cptt >> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";    

    echo -n "User time:" >> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";

    while read line3
    do 
	if [[ $(echo $line3 | grep -c "user") = 1 ]]
	then
	    echo "$(echo "$(echo $line3 | cut -d" " -f1)" | cut -d"u" -f1)" >> $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";
	fi
    done < $chemin_Programmes_Tcas/$line/$line"_FaultsBA"/Faults_$p".faults";

     
   

     echo Faults_$prog".faults"
    done
    echo $k;
  fi
done < $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt
