\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj1: 0 tcas_0_Cur_Vertical_Sep_0#0 + 0 x4#1 + 0 tcas_0_High_Confidence_0#2
       + 0 x11#3 + 0 tcas_0_Two_of_Three_Reports_Valid_0#4
       + 0 tcas_0_Own_Tracked_Alt_0#5 + 0 tcas_0_Own_Tracked_Alt_Rate_0#6
       + 0 tcas_0_Other_Tracked_Alt_0#7 + 0 tcas_0_Alt_Layer_Value_0#8
       + 0 tcas_0_Up_Separation_0#9 + 0 tcas_0_Down_Separation_0#10
       + 0 tcas_0_Other_RAC_0#11 + 0 tcas_0_Other_Capability_0#12 + 0 x25#13
       + 0 tcas_0_Climb_Inhibit_0#14 + 0 tcas_0_Positive_RA_Alt_Thresh_0(0)#15
       + 0 tcas_0_Positive_RA_Alt_Thresh_0(1)#16
       + 0 tcas_0_Positive_RA_Alt_Thresh_0(2)#17
       + 0 tcas_0_Positive_RA_Alt_Thresh_0(3)#18 + 0 tcas_0_OLEV_0#19
       + 0 tcas_0_MAXALTDIFF_0#20 + 0 tcas_0_MINSEP_0#21
       + 0 tcas_0_NOZCROSS_0#22 + 0 tcas_0_NO_INTENT_0#23
       + 0 tcas_0_TCAS_TA_0#24 + 0 tcas_0_UNRESOLVED_0#25
       + 0 tcas_0_UPWARD_RA_0#26 + 0 tcas_0_DOWNWARD_RA_0#27 + 0 x45#28
       + 0 tcas_0_enabled_0#29 + 0 x51#30 + 0 tcas_0_tcas_equipped_0#31
       + 0 x57#32 + 0 tcas_0_intent_not_known_0#33 + 0 x63#34
       + 0 tcas_0_need_upward_RA_0#35 + 0 x69#36
       + 0 tcas_0_need_downward_RA_0#37 + 0 tcas_0_alt_sep_0#38 + 0 x76#39
       + 0 tcas_0_upward_preferred_0#40
       + 0 tcas_0_upward_crossing_situation_0#41 + 0 x83#42
       + 0 tcas_0_resultat_0#43 + 0 tcas_0_Inhibit_Biased_Climb_0#44
       + 0 tcas_0_OLEV_1#45 + 0 tcas_0_MAXALTDIFF_1#46 + 0 tcas_0_MINSEP_1#47
       + 0 tcas_0_NOZCROSS_1#48 + 0 tcas_0_NO_INTENT_1#49
       + 0 tcas_0_TCAS_TA_1#50 + 0 tcas_0_UNRESOLVED_1#51
       + 0 tcas_0_UPWARD_RA_1#52 + 0 tcas_0_DOWNWARD_RA_1#53
       + 0 tcas_0_Positive_RA_Alt_Thresh_1(0)#54
       + 0 tcas_0_Positive_RA_Alt_Thresh_1(1)#55
       + 0 tcas_0_Positive_RA_Alt_Thresh_1(2)#56
       + 0 tcas_0_Positive_RA_Alt_Thresh_1(3)#57
       + 0 tcas_0_Positive_RA_Alt_Thresh_2(1)#58
       + 0 tcas_0_Positive_RA_Alt_Thresh_2(0)#59
       + 0 tcas_0_Positive_RA_Alt_Thresh_2(2)#60
       + 0 tcas_0_Positive_RA_Alt_Thresh_2(3)#61
       + 0 tcas_0_Positive_RA_Alt_Thresh_3(2)#62
       + 0 tcas_0_Positive_RA_Alt_Thresh_3(0)#63
       + 0 tcas_0_Positive_RA_Alt_Thresh_3(1)#64
       + 0 tcas_0_Positive_RA_Alt_Thresh_3(3)#65
       + 0 tcas_0_Positive_RA_Alt_Thresh_4(3)#66
       + 0 tcas_0_Positive_RA_Alt_Thresh_4(0)#67
       + 0 tcas_0_Positive_RA_Alt_Thresh_4(1)#68
       + 0 tcas_0_Positive_RA_Alt_Thresh_4(2)#69 + 0 x139#70
       + 0 tcas_0_enabled_1#71 + 0 x145#72 + 0 x150#73
       + 0 tcas_0_tcas_equipped_1#74 + 0 x157#75
       + 0 tcas_0_intent_not_known_1#76 + 0 x161#77 + 0 tcas_0_alt_sep_1#78
       + 0 tcas_0_Inhibit_Biased_Climb_1#79 + 0 x168#80
       + 0 tcas_0_resultat_1#81 + 0 x179#82 + 0 tcas_0_need_upward_RA_1#83
       + 0 x183#84 + 0 tcas_0_Inhibit_Biased_Climb_2#85 + 0 x189#86
       + 0 tcas_0_resultat_2#87 + 0 x200#88 + 0 tcas_0_need_downward_RA_1#89
       + 0 x204#90
Bounds
      tcas_0_Cur_Vertical_Sep_0#0 Free
 0 <= x4#1 <= 1
 0 <= tcas_0_High_Confidence_0#2 <= 1
 0 <= x11#3 <= 1
 0 <= tcas_0_Two_of_Three_Reports_Valid_0#4 <= 1
      tcas_0_Own_Tracked_Alt_0#5 Free
      tcas_0_Own_Tracked_Alt_Rate_0#6 Free
      tcas_0_Other_Tracked_Alt_0#7 Free
      tcas_0_Alt_Layer_Value_0#8 Free
      tcas_0_Up_Separation_0#9 Free
      tcas_0_Down_Separation_0#10 Free
      tcas_0_Other_RAC_0#11 Free
      tcas_0_Other_Capability_0#12 Free
 0 <= x25#13 <= 1
 0 <= tcas_0_Climb_Inhibit_0#14 <= 1
      tcas_0_Positive_RA_Alt_Thresh_0(0)#15 Free
      tcas_0_Positive_RA_Alt_Thresh_0(1)#16 Free
      tcas_0_Positive_RA_Alt_Thresh_0(2)#17 Free
      tcas_0_Positive_RA_Alt_Thresh_0(3)#18 Free
      tcas_0_OLEV_0#19 Free
      tcas_0_MAXALTDIFF_0#20 Free
      tcas_0_MINSEP_0#21 Free
      tcas_0_NOZCROSS_0#22 Free
      tcas_0_NO_INTENT_0#23 Free
      tcas_0_TCAS_TA_0#24 Free
      tcas_0_UNRESOLVED_0#25 Free
      tcas_0_UPWARD_RA_0#26 Free
      tcas_0_DOWNWARD_RA_0#27 Free
 0 <= x45#28 <= 1
 0 <= tcas_0_enabled_0#29 <= 1
 0 <= x51#30 <= 1
 0 <= tcas_0_tcas_equipped_0#31 <= 1
 0 <= x57#32 <= 1
 0 <= tcas_0_intent_not_known_0#33 <= 1
 0 <= x63#34 <= 1
 0 <= tcas_0_need_upward_RA_0#35 <= 1
 0 <= x69#36 <= 1
 0 <= tcas_0_need_downward_RA_0#37 <= 1
      tcas_0_alt_sep_0#38 Free
 0 <= x76#39 <= 1
 0 <= tcas_0_upward_preferred_0#40 <= 1
      tcas_0_upward_crossing_situation_0#41 Free
 0 <= x83#42 <= 1
 0 <= tcas_0_resultat_0#43 <= 1
      tcas_0_Inhibit_Biased_Climb_0#44 Free
      tcas_0_OLEV_1#45 Free
      tcas_0_MAXALTDIFF_1#46 Free
      tcas_0_MINSEP_1#47 Free
      tcas_0_NOZCROSS_1#48 Free
      tcas_0_NO_INTENT_1#49 Free
      tcas_0_TCAS_TA_1#50 Free
      tcas_0_UNRESOLVED_1#51 Free
      tcas_0_UPWARD_RA_1#52 Free
      tcas_0_DOWNWARD_RA_1#53 Free
      tcas_0_Positive_RA_Alt_Thresh_1(0)#54 Free
      tcas_0_Positive_RA_Alt_Thresh_1(1)#55 Free
      tcas_0_Positive_RA_Alt_Thresh_1(2)#56 Free
      tcas_0_Positive_RA_Alt_Thresh_1(3)#57 Free
      tcas_0_Positive_RA_Alt_Thresh_2(1)#58 Free
      tcas_0_Positive_RA_Alt_Thresh_2(0)#59 Free
      tcas_0_Positive_RA_Alt_Thresh_2(2)#60 Free
      tcas_0_Positive_RA_Alt_Thresh_2(3)#61 Free
      tcas_0_Positive_RA_Alt_Thresh_3(2)#62 Free
      tcas_0_Positive_RA_Alt_Thresh_3(0)#63 Free
      tcas_0_Positive_RA_Alt_Thresh_3(1)#64 Free
      tcas_0_Positive_RA_Alt_Thresh_3(3)#65 Free
      tcas_0_Positive_RA_Alt_Thresh_4(3)#66 Free
      tcas_0_Positive_RA_Alt_Thresh_4(0)#67 Free
      tcas_0_Positive_RA_Alt_Thresh_4(1)#68 Free
      tcas_0_Positive_RA_Alt_Thresh_4(2)#69 Free
 0 <= x139#70 <= 1
 0 <= tcas_0_enabled_1#71 <= 1
 0 <= x145#72 <= 1
 0 <= x150#73 <= 1
 0 <= tcas_0_tcas_equipped_1#74 <= 1
 0 <= x157#75 <= 1
 0 <= tcas_0_intent_not_known_1#76 <= 1
 0 <= x161#77 <= 1
      tcas_0_alt_sep_1#78 Free
      tcas_0_Inhibit_Biased_Climb_1#79 Free
 0 <= x168#80 <= 1
 0 <= tcas_0_resultat_1#81 <= 1
 0 <= x179#82 <= 1
 0 <= tcas_0_need_upward_RA_1#83 <= 1
 0 <= x183#84 <= 1
      tcas_0_Inhibit_Biased_Climb_2#85 Free
 0 <= x189#86 <= 1
 0 <= tcas_0_resultat_2#87 <= 1
 0 <= x200#88 <= 1
 0 <= tcas_0_need_downward_RA_1#89 <= 1
 0 <= x204#90 <= 1
Binaries
 x4#1  x11#3  x25#13  x45#28  x51#30  x57#32  x63#34  x69#36  x76#39  x83#42 
 x139#70  x145#72  x150#73  x157#75  x161#77  x168#80  x179#82  x183#84 
 x189#86  x200#88  x204#90 
Generals
 tcas_0_Cur_Vertical_Sep_0#0  tcas_0_High_Confidence_0#2 
 tcas_0_Two_of_Three_Reports_Valid_0#4  tcas_0_Own_Tracked_Alt_0#5 
 tcas_0_Own_Tracked_Alt_Rate_0#6  tcas_0_Other_Tracked_Alt_0#7 
 tcas_0_Alt_Layer_Value_0#8  tcas_0_Up_Separation_0#9 
 tcas_0_Down_Separation_0#10  tcas_0_Other_RAC_0#11 
 tcas_0_Other_Capability_0#12  tcas_0_Climb_Inhibit_0#14 
 tcas_0_Positive_RA_Alt_Thresh_0(0)#15  tcas_0_Positive_RA_Alt_Thresh_0(1)#16 
 tcas_0_Positive_RA_Alt_Thresh_0(2)#17  tcas_0_Positive_RA_Alt_Thresh_0(3)#18 
 tcas_0_OLEV_0#19  tcas_0_MAXALTDIFF_0#20  tcas_0_MINSEP_0#21 
 tcas_0_NOZCROSS_0#22  tcas_0_NO_INTENT_0#23  tcas_0_TCAS_TA_0#24 
 tcas_0_UNRESOLVED_0#25  tcas_0_UPWARD_RA_0#26  tcas_0_DOWNWARD_RA_0#27 
 tcas_0_enabled_0#29  tcas_0_tcas_equipped_0#31  tcas_0_intent_not_known_0#33 
 tcas_0_need_upward_RA_0#35  tcas_0_need_downward_RA_0#37  tcas_0_alt_sep_0#38 
 tcas_0_upward_preferred_0#40  tcas_0_upward_crossing_situation_0#41 
 tcas_0_resultat_0#43  tcas_0_Inhibit_Biased_Climb_0#44  tcas_0_OLEV_1#45 
 tcas_0_MAXALTDIFF_1#46  tcas_0_MINSEP_1#47  tcas_0_NOZCROSS_1#48 
 tcas_0_NO_INTENT_1#49  tcas_0_TCAS_TA_1#50  tcas_0_UNRESOLVED_1#51 
 tcas_0_UPWARD_RA_1#52  tcas_0_DOWNWARD_RA_1#53 
 tcas_0_Positive_RA_Alt_Thresh_1(0)#54  tcas_0_Positive_RA_Alt_Thresh_1(1)#55 
 tcas_0_Positive_RA_Alt_Thresh_1(2)#56  tcas_0_Positive_RA_Alt_Thresh_1(3)#57 
 tcas_0_Positive_RA_Alt_Thresh_2(1)#58  tcas_0_Positive_RA_Alt_Thresh_2(0)#59 
 tcas_0_Positive_RA_Alt_Thresh_2(2)#60  tcas_0_Positive_RA_Alt_Thresh_2(3)#61 
 tcas_0_Positive_RA_Alt_Thresh_3(2)#62  tcas_0_Positive_RA_Alt_Thresh_3(0)#63 
 tcas_0_Positive_RA_Alt_Thresh_3(1)#64  tcas_0_Positive_RA_Alt_Thresh_3(3)#65 
 tcas_0_Positive_RA_Alt_Thresh_4(3)#66  tcas_0_Positive_RA_Alt_Thresh_4(0)#67 
 tcas_0_Positive_RA_Alt_Thresh_4(1)#68  tcas_0_Positive_RA_Alt_Thresh_4(2)#69 
 tcas_0_enabled_1#71  tcas_0_tcas_equipped_1#74  tcas_0_intent_not_known_1#76 
 tcas_0_alt_sep_1#78  tcas_0_Inhibit_Biased_Climb_1#79  tcas_0_resultat_1#81 
 tcas_0_need_upward_RA_1#83  tcas_0_Inhibit_Biased_Climb_2#85 
 tcas_0_resultat_2#87  tcas_0_need_downward_RA_1#89 
End
