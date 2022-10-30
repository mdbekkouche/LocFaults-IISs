chemin_Programmes_2_Conditions_Fausses_Bf=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/2_Conditions_Fausses/BfKO2
chemin_Programmes_2_Conditions_FaussesBA_Bf=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS_BugAssist/2_Conditions_Fausses/BfKO2
chemin_CPBPV=/home/mdbekkouche/These/CBEL_ProjectV2/core/cpbpv
chemin_BugAssit=/home/mdbekkouche/These/Outils_Bug-Assist


k=5;

while read line
do
    mkdir -p $chemin_Programmes_2_Conditions_Fausses_Bf/Resultats/$line/$line"_CECPBPV";
    mkdir -p $chemin_Programmes_2_Conditions_Fausses_Bf/Resultats/$line/$line"_Faults";
    mkdir -p $chemin_Programmes_2_Conditions_FaussesBA_Bf/Resultats/$line/$line"_FaultsBA";
    

    $chemin_CPBPV -solvers Z3 -writeCE $chemin_Programmes_2_Conditions_Fausses_Bf/Resultats/$line/$line"_CECPBPV"/CE_$prog".ce" $chemin_Programmes_2_Conditions_Fausses_Bf/$line".java" -arrays_length $k -max_unfoldings $k  -int_format 32
    $chemin_CPBPV -solvers CPLEX -locate $chemin_Programmes_2_Conditions_Fausses_Bf/Resultats/$line/$line"_CECPBPV"/CE_$prog".ce" -NbFaultyCond 2 $chemin_Programmes_2_Conditions_Fausses_Bf/$line".java" -arrays_length $k -max_unfoldings $k  -int_format 32 > $chemin_Programmes_2_Conditions_Fausses_Bf/Resultats/$line/$line"_Faults"/Faults_$line".faults";


    #/usr/bin/time -o  $chemin_Programmes_2_Conditions_FaussesBA_Bf/Resultats/$line/$line"_FaultsBA"/Faults_$line".faults" $chemin_BugAssit/bugassist $chemin_Programmes_2_Conditions_FaussesBA_Bf/$line.c --function $line --unwind $((k+1)) --ba --maxsat $chemin_BugAssit/msuncore_20090525/bin/msuncore > $chemin_Programmes_2_Conditions_FaussesBA_Bf/Resultats/$line/$line"_FaultsBA"/Faults_$line".faults";

((k++));

done < $chemin_Programmes_2_Conditions_Fausses_Bf/Scriptes/Programmes_2_Conditions_Fausses_Bf.txt
