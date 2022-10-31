# LocFaults: Help tool for locating bugs in a JAVA program

## Get started

- Clone github repository:
```
git clone https://github.com/mdbekkouche/LocFaultsTool.git
```

- Run the following commands to locate errors in the TritypeKO program:
```
cd path/to/LocFaultsTool/
```
```
./cpbpv -solvers CPLEX -locate path/to/LocFaultsTool/benchmarks/0_Conditions_Fausses/Resultats/TritypeKO/TritypeKO_CECPBPV/CE_.ce -NbFaultyCond 0 path/to/LocFaultsTool/benchmarks/0_Conditions_Fausses/TritypeKO.java -int_format 32
```
`CPLEX` is he constraint solver used to calculate the MCSs.

`path/to/LocFaultsTool/benchmarks/0_Conditions_Fausses/Resultats/TritypeKO/TritypeKO_CECPBPV/CE_.ce` is the counterexample from which error localization is performed.

Localization is done with at most `0` deviated conditions.  

`path/to/LocFaultsTool/benchmarks/0_Conditions_Fausses/TritypeKO.java` 
indicates the path to the program that does not conform to its specification in which we seek to identify the suspicious instructions.

We use integers coded on `32` bits.

- Run the following command to locate errors in the TritypeKO5 program with atmost two conditions deviated from the counterexample path:
```
./cpbpv -solvers CPLEX -locate path/to/LocFaultsTool/benchmarks/2_Conditions_Fausses/Resultats/TritypeKO5/TritypeKO5_CECPBPV/CE_.ce -NbFaultyCond 2 path/to/LocFaultsTool/benchmarks/2_Conditions_Fausses/TritypeKO5.java -int_format 32
```
