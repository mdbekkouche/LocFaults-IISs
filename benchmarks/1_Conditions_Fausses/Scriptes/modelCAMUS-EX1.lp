\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj: 0 caller_0_i_0#0 + 0 caller_0_j_0#1 + 0 caller_0_k_0#2
      + 0 caller_0_trityp_0#3 + 0 caller_0_res_0#4 + 0 IloX5#5 + 0 IloX6#6
      + 0 IloX7#7 + 0 IloX8#8 + 0 IloX9#9 + 0 IloX10#10
      + 0 caller_0_trityp_1#11 + 0 caller_0_trityp_2#12 + 0 IloX13#13
      + 0 IloX14#14 + 0 IloX15#15 + 0 IloX16#16 + 0 IloX17#17 + 0 IloX18#18
      + 0 IloX19#19 + 0 IloX20#20 + 0 caller_0_trityp_3#21
      + 0 caller_0_trityp_4#22 + 0 IloX23#23 + 0 IloX24#24 + 0 IloX25#25
      + 0 IloX26#26 + 0 IloX27#27 + 0 IloX28#28 + 0 IloX29#29 + 0 IloX30#30
      + 0 IloX31#31 + 0 IloX32#32 + 0 IloX63#63
Subject To
 IloC0:  IloX34#34 - IloX35#35  = 0
 IloC1:  _1#33 - IloX34#34 >= 0
 IloC2:  IloX35#35  = 1
 IloC3:  IloX37#37 - IloX38#38  = 0
 IloC4:  _2#36 - IloX37#37 >= 0
 IloC5:  IloX38#38  = 1
 IloC6:  IloX40#40 - IloX41#41  = 0
 IloC7:  _3#39 - IloX40#40 >= 0
 IloC8:  IloX41#41  = 1
 IloC9:  IloX43#43 - IloX44#44  = 0
 IloC10: _4#42 - IloX43#43 >= 0
 IloC11: IloX46#46 - IloX47#47  = 0
 IloC12: _5#45 - IloX46#46 >= 0
 IloC13: IloX49#49 - IloX50#50  = 0
 IloC14: _7#48 - IloX49#49 >= 0
 IloC15: IloX52#52 - IloX53#53  = 0
 IloC16: _8#51 - IloX52#52 >= 0
 IloC17: IloX55#55 - IloX56#56  = 0
 IloC18: _10#54 - IloX55#55 >= 0
 IloC19: IloX58#58 - IloX59#59  = 0
 IloC20: _11#57 - IloX58#58 >= 0
 IloC21: IloX62#62 + IloX64#64  = 1
 IloC22: IloX61#61 - IloX65#65  = 0
 IloC23: _13#60 - IloX61#61 >= 0
 IloC24: IloX65#65  = 1
 _1#0:      _1#33 = 1 <-> caller_0_i_0#0  = 1
 _2#1:      _2#36 = 1 <-> caller_0_j_0#1  = 2
 _3#2:      _3#39 = 1 <-> caller_0_k_0#2  = 1
 _4#3:      _4#42 = 1 <-> caller_0_trityp_0#3  = 0
 _5#4:      _5#45 = 1 <-> caller_0_res_0#4  = 0
 _7#5:      _7#48 = 1 <-> caller_0_trityp_1#11  = 0
 _8#6:      _8#51 = 1 <-> - caller_0_trityp_1#11 + caller_0_trityp_2#12  = 0
 _10#7:     _10#54 = 1 <-> - caller_0_trityp_2#12 + caller_0_trityp_3#21  = 2
 _11#8:     _11#57 = 1 <-> - caller_0_trityp_3#21 + caller_0_trityp_4#22  = 0
 IloI9#9:   IloX64#64 = 1 <-> caller_0_trityp_4#22  = 2
 IloI10#10: IloX63#63 = 1 <-> - caller_0_k_0#2 + caller_0_i_0#0
            + caller_0_j_0#1 <= 0
 _13#11:    _13#60 = 1 <-> IloX62#62 + IloX63#63 >= 1
Bounds
      caller_0_i_0#0 Free
      caller_0_j_0#1 Free
      caller_0_k_0#2 Free
      caller_0_trityp_0#3 Free
      caller_0_res_0#4 Free
 0 <= IloX5#5 <= 1
 0 <= IloX6#6 <= 1
 0 <= IloX7#7 <= 1
 0 <= IloX8#8 <= 1
 0 <= IloX9#9 <= 1
 0 <= IloX10#10 <= 1
      caller_0_trityp_1#11 Free
      caller_0_trityp_2#12 Free
 0 <= IloX13#13 <= 1
 0 <= IloX14#14 <= 1
 0 <= IloX15#15 <= 1
 0 <= IloX16#16 <= 1
 0 <= IloX17#17 <= 1
 0 <= IloX18#18 <= 1
 0 <= IloX19#19 <= 1
 0 <= IloX20#20 <= 1
      caller_0_trityp_3#21 Free
      caller_0_trityp_4#22 Free
 0 <= IloX23#23 <= 1
 0 <= IloX24#24 <= 1
 0 <= IloX25#25 <= 1
 0 <= IloX26#26 <= 1
 0 <= IloX27#27 <= 1
 0 <= IloX28#28 <= 1
 0 <= IloX29#29 <= 1
 0 <= IloX30#30 <= 1
 0 <= IloX31#31 <= 1
 0 <= IloX32#32 <= 1
 0 <= _1#33 <= 1
 0 <= IloX34#34 <= 1
 0 <= IloX35#35 <= 1
 0 <= _2#36 <= 1
 0 <= IloX37#37 <= 1
 0 <= IloX38#38 <= 1
 0 <= _3#39 <= 1
 0 <= IloX40#40 <= 1
 0 <= IloX41#41 <= 1
 0 <= _4#42 <= 1
 0 <= IloX43#43 <= 1
 0 <= IloX44#44 <= 1
 0 <= _5#45 <= 1
 0 <= IloX46#46 <= 1
 0 <= IloX47#47 <= 1
 0 <= _7#48 <= 1
 0 <= IloX49#49 <= 1
 0 <= IloX50#50 <= 1
 0 <= _8#51 <= 1
 0 <= IloX52#52 <= 1
 0 <= IloX53#53 <= 1
 0 <= _10#54 <= 1
 0 <= IloX55#55 <= 1
 0 <= IloX56#56 <= 1
 0 <= _11#57 <= 1
 0 <= IloX58#58 <= 1
 0 <= IloX59#59 <= 1
 0 <= _13#60 <= 1
 0 <= IloX61#61 <= 1
 0 <= IloX62#62 <= 1
 0 <= IloX63#63 <= 1
 0 <= IloX64#64 <= 1
 0 <= IloX65#65 <= 1
Binaries
 IloX5#5  IloX6#6  IloX7#7  IloX8#8  IloX9#9  IloX10#10  IloX13#13  IloX14#14 
 IloX15#15  IloX16#16  IloX17#17  IloX18#18  IloX19#19  IloX20#20  IloX23#23 
 IloX24#24  IloX25#25  IloX26#26  IloX27#27  IloX28#28  IloX29#29  IloX30#30 
 IloX31#31  IloX32#32  _1#33  IloX34#34  IloX35#35  _2#36  IloX37#37 
 IloX38#38  _3#39  IloX40#40  IloX41#41  _4#42  IloX43#43  IloX44#44  _5#45 
 IloX46#46  IloX47#47  _7#48  IloX49#49  IloX50#50  _8#51  IloX52#52 
 IloX53#53  _10#54  IloX55#55  IloX56#56  _11#57  IloX58#58  IloX59#59  _13#60 
 IloX61#61  IloX62#62  IloX63#63  IloX64#64  IloX65#65 
Generals
 caller_0_i_0#0  caller_0_j_0#1  caller_0_k_0#2  caller_0_trityp_0#3 
 caller_0_res_0#4  caller_0_trityp_1#11  caller_0_trityp_2#12 
 caller_0_trityp_3#21  caller_0_trityp_4#22 
End
