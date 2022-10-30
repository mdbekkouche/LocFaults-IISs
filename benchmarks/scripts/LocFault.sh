chemin_Programmes_0_Conditions_Fausses=/home/clem/Bureau/LocFaults/benchmarks/0_Conditions_Fausses
chemin_CPBPV=/home/clem/Bureau/LocFaults/core/cpbpv


while read line
do
    mkdir -p $chemin_Programmes_0_Conditions_Fausses/Resultats/$line/$line"_CECPBPV";
    mkdir -p $chemin_Programmes_0_Conditions_Fausses/Resultats/$line/$line"_Faults";


    $chemin_CPBPV -solvers Z3 -writeCE $chemin_Programmes_0_Conditions_Fausses/Resultats/$line/$line"_CECPBPV"/CE_$prog".ce" $chemin_Programmes_0_Conditions_Fausses/$line".java"   -int_format 32
    $chemin_CPBPV -solvers CP_OPTIMIZER -locate $chemin_Programmes_0_Conditions_Fausses/Resultats/$line/$line"_CECPBPV"/CE_$prog".ce" -NbFaultyCond 0 $chemin_Programmes_0_Conditions_Fausses/$line".java" -int_format 32 > $chemin_Programmes_0_Conditions_Fausses/Resultats/$line/$line"_Faults"/Faults_$line".faults";


done < $chemin_Programmes_0_Conditions_Fausses/Scripts/Programmes_0_Conditions_Fausses.txt



