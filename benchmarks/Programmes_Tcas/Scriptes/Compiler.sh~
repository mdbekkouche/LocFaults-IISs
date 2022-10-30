chemin_Programmes_Tcas=/home/mdbekkouche/These/Benchmarks_MCS-IIS/Programs_Benchmarks_MCS-IIS/Programmes_Tcas

#Afficher pour chaque programme Tcas son nom dans le fichier Programmes_Tcas.txt#
echo "Tcas" > $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt;
echo "TcasKO" >> $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt;
for (( i=2; i<=41; i++ ))
do
   echo "TcasKO$i" >> $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt;
done

#Compiler toutes les versions du programme Tcas.c, ainsi la version correcte#
while read line
do 
  gcc $chemin_Programmes_Tcas/$line/$line.c -o $chemin_Programmes_Tcas/$line/$line;
done < $chemin_Programmes_Tcas/Scriptes/Programmes_Tcas.txt;
