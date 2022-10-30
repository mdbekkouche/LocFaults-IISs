chemin_Programmes=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Exemples_simples/Maxmin6varLoFaults
chemin_ProgrammesBA=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Exemples_simples/Maxmin6varBugAssist
chemin_CPBPV=/home/mdbekkouche/These/CBEL_Project/core/cpbpv
chemin_BugAssit=/home/mdbekkouche/These/Outils_Bug-Assist

while read line
do
    mkdir -p $chemin_Programmes/Resultats/$line/$line"_CECPBPV";
    mkdir -p $chemin_Programmes/Resultats/$line/$line"_Faults";
    mkdir -p $chemin_ProgrammesBA/Resultats/$line/$line"_FaultsBA";
    
    
    $chemin_CPBPV -solvers Z3 -writeCE $chemin_Programmes/Resultats/$line/$line"_CECPBPV"/CE_$prog".ce" $chemin_Programmes/$line".java"  -int_format 32
    
    for (( i=0; i<=4; i++ ))
    do
    $chemin_CPBPV -solvers CPLEX -locate $chemin_Programmes/Resultats/$line/$line"_CECPBPV"/CE_$prog".ce" -NbFaultyCond $i $chemin_Programmes/$line".java" -int_format 32 > $chemin_Programmes/Resultats/$line/$line"_Faults"/Faults_$line".faults"$i"conditionfausses";
    done
 
    /usr/bin/time -o  $chemin_ProgrammesBA/Resultats/$line/$line"_FaultsBA"/Faults_$line".faults" $chemin_BugAssit/bugassist $chemin_ProgrammesBA/$line.c --function maxmin  --ba --maxsat $chemin_BugAssit/msuncore_20090525/bin/msuncore > $chemin_ProgrammesBA/Resultats/$line/$line"_FaultsBA"/Faults_$line".faults";


done < $chemin_Programmes/Scriptes/Programmes.txt
