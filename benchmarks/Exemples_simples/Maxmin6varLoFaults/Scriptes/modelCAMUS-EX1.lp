\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj: 0 maxmin_0_a_0#0 + 0 maxmin_0_b_0#1 + 0 maxmin_0_c_0#2 + 0 maxmin_0_d_0#3
      + 0 maxmin_0_e_0#4 + 0 maxmin_0_f_0#5 + 0 maxmin_0_max_0#6
      + 0 maxmin_0_min_0#7 + 0 maxmin_0_max_1#8 + 0 maxmin_0_min_1#9
      + 0 IloX10#10 + 0 IloX11#11 + 0 IloX12#12 + 0 IloX13#13 + 0 IloX14#14
      + 0 IloX15#15 + 0 IloX16#16 + 0 IloX17#17 + 0 IloX18#18 + 0 IloX19#19
      + 0 IloX20#20 + 0 maxmin_0_min_1#21 + 0 maxmin_0_min_1#22
      + 0 maxmin_0_min_1#23 + 0 maxmin_0_min_1#24 + 0 maxmin_0_max_1#25
      + 0 maxmin_0_min_1#26 + 0 maxmin_0_min_1#27 + 0 maxmin_0_min_1#28
      + 0 maxmin_0_min_1#29 + 0 maxmin_0_min_1#30 + 0 maxmin_0_max_1#31
      + 0 maxmin_0_min_1#32 + 0 maxmin_0_min_1#33 + 0 maxmin_0_min_1#34
      + 0 maxmin_0_min_1#35 + 0 maxmin_0_min_1#36 + 0 maxmin_0_max_1#37
      + 0 maxmin_0_min_1#38 + 0 maxmin_0_min_1#39 + 0 maxmin_0_min_1#40
      + 0 maxmin_0_min_1#41 + 0 maxmin_0_min_1#42 + 0 maxmin_0_max_1#43
      + 0 maxmin_0_min_1#44 + 0 maxmin_0_min_1#45 + 0 maxmin_0_min_1#46
      + 0 maxmin_0_min_1#47 + 0 maxmin_0_min_1#48 + 0 maxmin_0_max_1#49
      + 0 maxmin_0_min_1#50 + 0 maxmin_0_min_1#51 + 0 maxmin_0_min_1#52
      + 0 maxmin_0_min_1#53 + 0 IloX83#83 + 0 IloX84#84 + 0 IloX85#85
      + 0 IloX86#86 + 0 IloX90#90 + 0 IloX91#91 + 0 IloX92#92 + 0 IloX93#93
      + 0 IloX94#94 + 0 IloX95#95 + 0 IloX99#99 + 0 IloX100#100 + 0 IloX101#101
      + 0 IloX102#102 + 0 IloX103#103 + 0 IloX104#104 + 0 IloX105#105
      + 0 IloX106#106
Subject To
 IloC0:  IloX55#55 - IloX56#56  = 0
 IloC1:  _1#54 - IloX55#55 >= 0
 IloC2:  IloX58#58 - IloX59#59  = 0
 IloC3:  _2#57 - IloX58#58 >= 0
 IloC4:  IloX61#61 - IloX62#62  = 0
 IloC5:  _3#60 - IloX61#61 >= 0
 IloC6:  IloX64#64 - IloX65#65  = 0
 IloC7:  _4#63 - IloX64#64 >= 0
 IloC8:  IloX67#67 - IloX68#68  = 0
 IloC9:  _5#66 - IloX67#67 >= 0
 IloC10: IloX70#70 - IloX71#71  = 0
 IloC11: _6#69 - IloX70#70 >= 0
 IloC12: IloX73#73 - IloX74#74  = 0
 IloC13: _7#72 - IloX73#73 >= 0
 IloC14: IloX76#76 - IloX77#77  = 0
 IloC15: _8#75 - IloX76#76 >= 0
 IloC16: IloX79#79 - IloX80#80  = 0
 IloC17: _64#78 - IloX79#79 >= 0
 IloC18: IloX82#82 - IloX87#87  = 0
 IloC19: _73#81 - IloX82#82 >= 0
 IloC20: IloX89#89 - IloX96#96  = 0
 IloC21: _74#88 - IloX89#89 >= 0
 IloC22: IloX98#98 - IloX107#107  = 0
 IloC23: _75#97 - IloX98#98 >= 0
 _1#0:      _1#54 = 1 <-> maxmin_0_a_0#0  = 1
 _2#1:      _2#57 = 1 <-> maxmin_0_b_0#1  = -3
 _3#2:      _3#60 = 1 <-> maxmin_0_c_0#2  = -4
 _4#3:      _4#63 = 1 <-> maxmin_0_d_0#3  = -2
 _5#4:      _5#66 = 1 <-> maxmin_0_e_0#4  = -1
 _6#5:      _6#69 = 1 <-> maxmin_0_f_0#5  = -2
 _7#6:      _7#72 = 1 <-> maxmin_0_max_0#6  = 0
 _8#7:      _8#75 = 1 <-> maxmin_0_min_0#7  = 0
 _64#8:     _64#78 = 1 <-> - maxmin_0_a_0#0 + maxmin_0_max_1#49  = 0
 IloI9#9:   IloX83#83 = 1 <-> maxmin_0_c_0#2 - maxmin_0_f_0#5 <= -1
 IloI10#10: IloX85#85 = 1 <-> maxmin_0_c_0#2 - maxmin_0_e_0#4 >= 1
 IloI11#11: IloX86#86 = 1 <-> maxmin_0_c_0#2 - maxmin_0_d_0#3 <= -1
 IloI12#12: IloX84#84 = 1 <-> IloX85#85 + IloX86#86 >= 2
 _73#13:    _73#81 = 1 <-> IloX83#83 + IloX84#84 >= 2
 IloI14#14: IloX94#94 = 1 <-> maxmin_0_b_0#1 - maxmin_0_c_0#2 <= 0
 IloI15#15: IloX95#95 = 1 <-> maxmin_0_b_0#1 - maxmin_0_d_0#3 >= 0
 IloI16#16: IloX92#92 = 1 <-> IloX94#94 + IloX95#95 >= 1
 IloI17#17: IloX93#93 = 1 <-> maxmin_0_b_0#1 - maxmin_0_e_0#4 >= 0
 IloI18#18: IloX90#90 = 1 <-> IloX92#92 + IloX93#93 >= 1
 IloI19#19: IloX91#91 = 1 <-> maxmin_0_b_0#1 - maxmin_0_f_0#5 >= 0
 _74#20:    _74#88 = 1 <-> IloX90#90 + IloX91#91 >= 1
 IloI21#21: IloX99#99 = 1 <-> maxmin_0_a_0#0 - maxmin_0_f_0#5 >= 1
 IloI22#22: IloX101#101 = 1 <-> maxmin_0_a_0#0 - maxmin_0_e_0#4 >= 1
 IloI23#23: IloX103#103 = 1 <-> maxmin_0_b_0#1 - maxmin_0_d_0#3 >= 1
 IloI24#24: IloX105#105 = 1 <-> maxmin_0_a_0#0 - maxmin_0_c_0#2 >= 1
 IloI25#25: IloX106#106 = 1 <-> maxmin_0_a_0#0 - maxmin_0_b_0#1 >= 1
 IloI26#26: IloX104#104 = 1 <-> IloX105#105 + IloX106#106 >= 2
 IloI27#27: IloX102#102 = 1 <-> IloX103#103 + IloX104#104 >= 2
 IloI28#28: IloX100#100 = 1 <-> IloX101#101 + IloX102#102 >= 2
 _75#29:    _75#97 = 1 <-> IloX99#99 + IloX100#100 >= 2
Bounds
      maxmin_0_a_0#0 Free
      maxmin_0_b_0#1 Free
      maxmin_0_c_0#2 Free
      maxmin_0_d_0#3 Free
      maxmin_0_e_0#4 Free
      maxmin_0_f_0#5 Free
      maxmin_0_max_0#6 Free
      maxmin_0_min_0#7 Free
      maxmin_0_max_1#8 Free
      maxmin_0_min_1#9 Free
 0 <= IloX10#10 <= 1
 0 <= IloX11#11 <= 1
 0 <= IloX12#12 <= 1
 0 <= IloX13#13 <= 1
 0 <= IloX14#14 <= 1
 0 <= IloX15#15 <= 1
 0 <= IloX16#16 <= 1
 0 <= IloX17#17 <= 1
 0 <= IloX18#18 <= 1
 0 <= IloX19#19 <= 1
 0 <= IloX20#20 <= 1
      maxmin_0_min_1#21 Free
      maxmin_0_min_1#22 Free
      maxmin_0_min_1#23 Free
      maxmin_0_min_1#24 Free
      maxmin_0_max_1#25 Free
      maxmin_0_min_1#26 Free
      maxmin_0_min_1#27 Free
      maxmin_0_min_1#28 Free
      maxmin_0_min_1#29 Free
      maxmin_0_min_1#30 Free
      maxmin_0_max_1#31 Free
      maxmin_0_min_1#32 Free
      maxmin_0_min_1#33 Free
      maxmin_0_min_1#34 Free
      maxmin_0_min_1#35 Free
      maxmin_0_min_1#36 Free
      maxmin_0_max_1#37 Free
      maxmin_0_min_1#38 Free
      maxmin_0_min_1#39 Free
      maxmin_0_min_1#40 Free
      maxmin_0_min_1#41 Free
      maxmin_0_min_1#42 Free
      maxmin_0_max_1#43 Free
      maxmin_0_min_1#44 Free
      maxmin_0_min_1#45 Free
      maxmin_0_min_1#46 Free
      maxmin_0_min_1#47 Free
      maxmin_0_min_1#48 Free
      maxmin_0_max_1#49 Free
      maxmin_0_min_1#50 Free
      maxmin_0_min_1#51 Free
      maxmin_0_min_1#52 Free
      maxmin_0_min_1#53 Free
 0 <= _1#54 <= 1
 0 <= IloX55#55 <= 1
 0 <= IloX56#56 <= 1
 0 <= _2#57 <= 1
 0 <= IloX58#58 <= 1
 0 <= IloX59#59 <= 1
 0 <= _3#60 <= 1
 0 <= IloX61#61 <= 1
 0 <= IloX62#62 <= 1
 0 <= _4#63 <= 1
 0 <= IloX64#64 <= 1
 0 <= IloX65#65 <= 1
 0 <= _5#66 <= 1
 0 <= IloX67#67 <= 1
 0 <= IloX68#68 <= 1
 0 <= _6#69 <= 1
 0 <= IloX70#70 <= 1
 0 <= IloX71#71 <= 1
 0 <= _7#72 <= 1
 0 <= IloX73#73 <= 1
 0 <= IloX74#74 <= 1
 0 <= _8#75 <= 1
 0 <= IloX76#76 <= 1
 0 <= IloX77#77 <= 1
 0 <= _64#78 <= 1
 0 <= IloX79#79 <= 1
 0 <= IloX80#80 <= 1
 0 <= _73#81 <= 1
 0 <= IloX82#82 <= 1
 0 <= IloX83#83 <= 1
 0 <= IloX84#84 <= 1
 0 <= IloX85#85 <= 1
 0 <= IloX86#86 <= 1
 0 <= IloX87#87 <= 1
 0 <= _74#88 <= 1
 0 <= IloX89#89 <= 1
 0 <= IloX90#90 <= 1
 0 <= IloX91#91 <= 1
 0 <= IloX92#92 <= 1
 0 <= IloX93#93 <= 1
 0 <= IloX94#94 <= 1
 0 <= IloX95#95 <= 1
 0 <= IloX96#96 <= 1
 0 <= _75#97 <= 1
 0 <= IloX98#98 <= 1
 0 <= IloX99#99 <= 1
 0 <= IloX100#100 <= 1
 0 <= IloX101#101 <= 1
 0 <= IloX102#102 <= 1
 0 <= IloX103#103 <= 1
 0 <= IloX104#104 <= 1
 0 <= IloX105#105 <= 1
 0 <= IloX106#106 <= 1
 0 <= IloX107#107 <= 1
Binaries
 IloX10#10  IloX11#11  IloX12#12  IloX13#13  IloX14#14  IloX15#15  IloX16#16 
 IloX17#17  IloX18#18  IloX19#19  IloX20#20  _1#54  IloX55#55  IloX56#56 
 _2#57  IloX58#58  IloX59#59  _3#60  IloX61#61  IloX62#62  _4#63  IloX64#64 
 IloX65#65  _5#66  IloX67#67  IloX68#68  _6#69  IloX70#70  IloX71#71  _7#72 
 IloX73#73  IloX74#74  _8#75  IloX76#76  IloX77#77  _64#78  IloX79#79 
 IloX80#80  _73#81  IloX82#82  IloX83#83  IloX84#84  IloX85#85  IloX86#86 
 IloX87#87  _74#88  IloX89#89  IloX90#90  IloX91#91  IloX92#92  IloX93#93 
 IloX94#94  IloX95#95  IloX96#96  _75#97  IloX98#98  IloX99#99  IloX100#100 
 IloX101#101  IloX102#102  IloX103#103  IloX104#104  IloX105#105  IloX106#106 
 IloX107#107 
Generals
 maxmin_0_a_0#0  maxmin_0_b_0#1  maxmin_0_c_0#2  maxmin_0_d_0#3 
 maxmin_0_e_0#4  maxmin_0_f_0#5  maxmin_0_max_0#6  maxmin_0_min_0#7 
 maxmin_0_max_1#8  maxmin_0_min_1#9  maxmin_0_min_1#21  maxmin_0_min_1#22 
 maxmin_0_min_1#23  maxmin_0_min_1#24  maxmin_0_max_1#25  maxmin_0_min_1#26 
 maxmin_0_min_1#27  maxmin_0_min_1#28  maxmin_0_min_1#29  maxmin_0_min_1#30 
 maxmin_0_max_1#31  maxmin_0_min_1#32  maxmin_0_min_1#33  maxmin_0_min_1#34 
 maxmin_0_min_1#35  maxmin_0_min_1#36  maxmin_0_max_1#37  maxmin_0_min_1#38 
 maxmin_0_min_1#39  maxmin_0_min_1#40  maxmin_0_min_1#41  maxmin_0_min_1#42 
 maxmin_0_max_1#43  maxmin_0_min_1#44  maxmin_0_min_1#45  maxmin_0_min_1#46 
 maxmin_0_min_1#47  maxmin_0_min_1#48  maxmin_0_max_1#49  maxmin_0_min_1#50 
 maxmin_0_min_1#51  maxmin_0_min_1#52  maxmin_0_min_1#53 
End
