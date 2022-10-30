\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj: 0 caller_0_i_0#0 + 0 caller_0_j_0#1 + 0 caller_0_k_0#2
      + 0 caller_0_trityp_0#3 + 0 caller_0_res_0#4 + 0 caller_0_trityp_1#5
      + 0 caller_0_trityp_2#6 + 0 caller_0_trityp_3#7 + 0 caller_0_trityp_4#8
      + 0 IloX9#9 + 0 IloX10#10 + 0 IloX11#11 + 0 IloX12#12 + 0 IloX13#13
      + 0 IloX14#14 + 0 IloX15#15 + 0 IloX16#16 + 0 IloX17#17 + 0 IloX18#18
      + 0 IloX19#19 + 0 caller_0_res_1#20 + 0 caller_0_trityp_5#21
      + 0 caller_0_Result_0#22 + 0 caller_0_trityp_2#23
      + 0 caller_0_trityp_3#24 + 0 caller_0_trityp_4#25
Subject To
 IloC0:  IloX27#27 - IloX28#28  = 0
 IloC1:  _1#26 - IloX27#27 >= 0
 IloC2:  IloX28#28  = 1
 IloC3:  IloX30#30 - IloX31#31  = 0
 IloC4:  _2#29 - IloX30#30 >= 0
 IloC5:  IloX31#31  = 1
 IloC6:  IloX33#33 - IloX34#34  = 0
 IloC7:  _3#32 - IloX33#33 >= 0
 IloC8:  IloX34#34  = 1
 IloC9:  IloX36#36 - IloX37#37  = 0
 IloC10: _4#35 - IloX36#36 >= 0
 IloC11: IloX39#39 - IloX40#40  = 0
 IloC12: _5#38 - IloX39#39 >= 0
 IloC13: IloX42#42 - IloX43#43  = 0
 IloC14: _6#41 - IloX42#42 >= 0
 IloC15: IloX45#45 - IloX46#46  = 0
 IloC16: _15#44 - IloX45#45 >= 0
 IloC17: IloX48#48 - IloX49#49  = 0
 IloC18: _16#47 - IloX48#48 >= 0
 IloC19: IloX51#51 - IloX52#52  = 0
 IloC20: _17#50 - IloX51#51 >= 0
 IloC21: IloX54#54 - IloX55#55  = 0
 IloC22: _18#53 - IloX54#54 >= 0
 IloC23: IloX55#55  = 1
 IloC24: IloX57#57 - IloX58#58  = 0
 IloC25: _19#56 - IloX57#57 >= 0
 IloC26: IloX58#58  = 1
 _1#0:   _1#26 = 1 <-> caller_0_i_0#0  = 1
 _2#1:   _2#29 = 1 <-> caller_0_j_0#1  = 2
 _3#2:   _3#32 = 1 <-> caller_0_k_0#2  = 1
 _4#3:   _4#35 = 1 <-> caller_0_trityp_0#3  = 0
 _5#4:   _5#38 = 1 <-> caller_0_res_0#4  = 0
 _6#5:   _6#41 = 1 <-> caller_0_trityp_1#5  = 0
 _15#6:  _15#44 = 1 <-> - caller_0_trityp_1#5 + caller_0_trityp_2#23  = 0
 _16#7:  _16#47 = 1 <-> - caller_0_trityp_2#23 + caller_0_trityp_3#24  = 2
 _17#8:  _17#50 = 1 <-> - caller_0_trityp_3#24 + caller_0_trityp_4#25  = 3
 _18#9:  _18#53 = 1 <-> caller_0_j_0#1 - caller_0_k_0#2  = 0
 _19#10: _19#56 = 1 <-> caller_0_trityp_4#25 <= 3
Bounds
      caller_0_i_0#0 Free
      caller_0_j_0#1 Free
      caller_0_k_0#2 Free
      caller_0_trityp_0#3 Free
      caller_0_res_0#4 Free
      caller_0_trityp_1#5 Free
      caller_0_trityp_2#6 Free
      caller_0_trityp_3#7 Free
      caller_0_trityp_4#8 Free
 0 <= IloX9#9 <= 1
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
      caller_0_res_1#20 Free
      caller_0_trityp_5#21 Free
      caller_0_Result_0#22 Free
      caller_0_trityp_2#23 Free
      caller_0_trityp_3#24 Free
      caller_0_trityp_4#25 Free
 0 <= _1#26 <= 1
 0 <= IloX27#27 <= 1
 0 <= IloX28#28 <= 1
 0 <= _2#29 <= 1
 0 <= IloX30#30 <= 1
 0 <= IloX31#31 <= 1
 0 <= _3#32 <= 1
 0 <= IloX33#33 <= 1
 0 <= IloX34#34 <= 1
 0 <= _4#35 <= 1
 0 <= IloX36#36 <= 1
 0 <= IloX37#37 <= 1
 0 <= _5#38 <= 1
 0 <= IloX39#39 <= 1
 0 <= IloX40#40 <= 1
 0 <= _6#41 <= 1
 0 <= IloX42#42 <= 1
 0 <= IloX43#43 <= 1
 0 <= _15#44 <= 1
 0 <= IloX45#45 <= 1
 0 <= IloX46#46 <= 1
 0 <= _16#47 <= 1
 0 <= IloX48#48 <= 1
 0 <= IloX49#49 <= 1
 0 <= _17#50 <= 1
 0 <= IloX51#51 <= 1
 0 <= IloX52#52 <= 1
 0 <= _18#53 <= 1
 0 <= IloX54#54 <= 1
 0 <= IloX55#55 <= 1
 0 <= _19#56 <= 1
 0 <= IloX57#57 <= 1
 0 <= IloX58#58 <= 1
Binaries
 IloX9#9  IloX10#10  IloX11#11  IloX12#12  IloX13#13  IloX14#14  IloX15#15 
 IloX16#16  IloX17#17  IloX18#18  IloX19#19  _1#26  IloX27#27  IloX28#28 
 _2#29  IloX30#30  IloX31#31  _3#32  IloX33#33  IloX34#34  _4#35  IloX36#36 
 IloX37#37  _5#38  IloX39#39  IloX40#40  _6#41  IloX42#42  IloX43#43  _15#44 
 IloX45#45  IloX46#46  _16#47  IloX48#48  IloX49#49  _17#50  IloX51#51 
 IloX52#52  _18#53  IloX54#54  IloX55#55  _19#56  IloX57#57  IloX58#58 
Generals
 caller_0_i_0#0  caller_0_j_0#1  caller_0_k_0#2  caller_0_trityp_0#3 
 caller_0_res_0#4  caller_0_trityp_1#5  caller_0_trityp_2#6 
 caller_0_trityp_3#7  caller_0_trityp_4#8  caller_0_res_1#20 
 caller_0_trityp_5#21  caller_0_Result_0#22  caller_0_trityp_2#23 
 caller_0_trityp_3#24  caller_0_trityp_4#25 
End
