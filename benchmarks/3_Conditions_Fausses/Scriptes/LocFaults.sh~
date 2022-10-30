chemin_Programmes_3_Conditions_Fausses=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/3_Conditions_Fausses
chemin_Programmes_3_Conditions_FaussesBA=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS_BugAssist/3_Conditions_Fausses
chemin_CPBPV=/home/mdbekkouche/These/CBEL_ProjectV4/core/cpbpv
chemin_BugAssit=/home/mdbekkouche/These/Outils_Bug-Assist


while read line
do
    mkdir -p $chemin_Programmes_3_Conditions_Fausses/Resultats_05-08-2014/$line/$line"_CECPBPV";
    mkdir -p $chemin_Programmes_3_Conditions_Fausses/Resultats_05-08-2014/$line/$line"_Faults";
    mkdir -p $chemin_Programmes_3_Conditions_FaussesBA/Resultats_05-08-2014/$line/$line"_FaultsBA";
    

    $chemin_CPBPV -solvers Z3 -writeCE $chemin_Programmes_3_Conditions_Fausses/Resultats_05-08-2014/$line/$line"_CECPBPV"/CE_$prog".ce" $chemin_Programmes_3_Conditions_Fausses/$line".java"   -int_format 32
    $chemin_CPBPV -solvers CPLEX -locate $chemin_Programmes_3_Conditions_Fausses/Resultats_05-08-2014/$line/$line"_CECPBPV"/CE_$prog".ce" -NbFaultyCond 3 $chemin_Programmes_3_Conditions_Fausses/$line".java" -int_format 32 > $chemin_Programmes_3_Conditions_Fausses/Resultats_05-08-2014/$line/$line"_Faults"/Faults_$line".faults";

 
    /usr/bin/time -o  $chemin_Programmes_3_Conditions_FaussesBA/Resultats_05-08-2014/$line/$line"_FaultsBA"/Faults_$line".faults" $chemin_BugAssit/bugassist $chemin_Programmes_3_Conditions_FaussesBA/$line.c --function $line --ba --maxsat $chemin_BugAssit/msuncore_20090525/bin/msuncore > $chemin_Programmes_3_Conditions_FaussesBA/Resultats_05-08-2014/$line/$line"_FaultsBA"/Faults_$line".faults";



done < $chemin_Programmes_3_Conditions_Fausses/Scriptes/Programmes_3_Conditions_Fausses.txt
