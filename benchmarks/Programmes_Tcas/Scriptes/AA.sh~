chemin_Programmes_Tcas=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas


echo -n "" > /home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas/Scriptes/AA.txt

while read line
do
  if [[ $line == TcasKO37 ]] 
  then
    for prog in `ls $chemin_Programmes_Tcas/$line/$line"_Specs"`; do
     echo "<li><a href=./Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas/$line/$line"_Faults"/Faults_$prog.faults>Faults_$prog</a></li>" >> /home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas/Scriptes/AA.txt
    done
  fi
done < $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt
