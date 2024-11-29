# Default compilation target is Linux x86 32 bits.
# To compile to Linux x86 64 bits (e.g. Crabe.polytech),
# use 'make x86_64'.
#
# Modify JDK_DIR as needed.

.SUFFIXES: .java .class

PWD:=`pwd`

JDK_DIR:=/usr/lib/jvm/java-19-openjdk-amd64
JDK_INC_DIRS:=$(JDK_DIR)/include $(JDK_DIR)/include/linux 
JAVAC:=$(JDK_DIR)/bin/javac
JAVAH:=$(JDK_DIR)/bin/javah
JAVADOC:=$(JDK_DIR)/bin/javadoc

CC:=gcc
CXX:=g++

ifeq ($(MAKECMDGOALS),x86_64)
  CFLAGS:=-fPIC -m64
else
  CFLAGS:=-m32
endif

JAVA_SRC_DIR:=java/src
JAVA_CLS_DIR:=java/classes

JNI_SRC_DIR:=$(JAVA_SRC_DIR)/JNI
JNI_LIB_DIR:=$(JAVA_CLS_DIR)/JNI

DOC_DIR:=Doc


.PHONY: all x86_64 dirs java_classes common_32_64 jni_32 jni_64 jni_z3 jni_fplib jni_glpk clean doc


all: common_32_64 jni_32
	@echo "Successfully built cpbpv."

x86_64: common_32_64 jni_64
	@echo "Successfully built cpbpv."

common_32_64: dirs java_classes


######################## Target directories

dirs: $(JAVA_CLS_DIR) $(JNI_LIB_DIR)
	@echo "Directories created."

$(JAVA_CLS_DIR):
	mkdir $(JAVA_CLS_DIR)

$(JNI_LIB_DIR):
	mkdir $(JNI_LIB_DIR)


######################## Java sources

JAVA_SOLVER_LIBS:=lib/CP/ILOG.CP.jar:lib/cplex/cplex.jar

JAVA_PARSER_LIBS:=lib/java2CFG/*

JAVA_PKGS:=$(shell find . -path *.svn -prune -o -type d -print | sed -e s+.*/java/src/++ -e s+/+.+g)

JAVA_SRCS:=$(wildcard $(addsuffix /*.java,$(addprefix $(JAVA_SRC_DIR)/,$(subst .,/,$(JAVA_PKGS)))))

JAVA_CLSS:=$(subst $(JAVA_SRC_DIR),$(JAVA_CLS_DIR),$(JAVA_SRCS:.java=.class))

JFLAGS:=-d $(JAVA_CLS_DIR) -sourcepath .:$(JAVA_SRC_DIR) -cp .:$(JAVA_CLS_DIR):$(JAVA_SOLVER_LIBS):$(JAVA_PARSER_LIBS)

java_classes: $(JAVA_CLSS)
	@echo "Java classes compiled."

$(JAVA_CLS_DIR)/%.class: $(JAVA_SRC_DIR)/%.java  
	$(JAVAC) $(JFLAGS) $<


######################## JNI sources

JNI_JHFLAGS:=-force -classpath $(JAVA_CLS_DIR)
JNI_CFLAGS:=$(addprefix -I,$(JDK_INC_DIRS))

jni_32: jni_z3 jni_fplib jni_glpk jni_fluctuat
	@echo "JNI files compiled."

jni_64: jni_z3  jni_fluctuat           #jni_fplib
	@echo "JNI files compiled."


###################### z3

Z3_LIB_DIR:=lib/Z3
Z3_INC_DIR:=Include/Z3
ifeq ($(MAKECMDGOALS),x86_64)
  Z3_LIBS:=$(Z3_LIB_DIR)/x86_64/libz3.a -lgmp
else
  Z3_LIBS:=-Wl,-rpath,$(realpath $(Z3_LIB_DIR)/x86_32) -L$(Z3_LIB_DIR)/x86_32 -lz3-gmp
endif

jni_z3: $(JNI_LIB_DIR)/libjni_z3.so
	@echo "JNI z3 files compiled."

$(JNI_LIB_DIR)/libjni_z3.so: $(JNI_SRC_DIR)/jni_Z3Solver.o
	@echo "Generating JNI z3 library..."
	$(CXX) -shared $(CFLAGS) -o $@ -Wl,-soname,$(notdir $@) $^ $(Z3_LIBS)

$(JNI_SRC_DIR)/jni_Z3Solver.o: $(JNI_SRC_DIR)/jni_common.h $(JNI_SRC_DIR)/jni_Z3Solver.h $(JNI_SRC_DIR)/jni_Z3Solver.cpp 
	$(CXX) $(CFLAGS) -I$(Z3_INC_DIR) $(JNI_CFLAGS) -c -o $@ $(JNI_SRC_DIR)/jni_Z3Solver.cpp 

$(JNI_SRC_DIR)/jni_Z3Solver.h: $(JAVA_SRC_DIR)/solver/z3/Z3Solver.java 
	$(JAVAH) $(JNI_JHFLAGS) -o $@ solver.z3.Z3Solver 

###################### fplib

ifeq ($(MAKECMDGOALS),x86_64)
  FPLIB_LIB_DIR:=lib/Fplib/x86_64
else
  FPLIB_LIB_DIR:=lib/Fplib/x86_32
endif
FPLIB_INC_DIR:=Include/Fplib
FPLIB_LIBS:=-Wl,-rpath,$(realpath $(FPLIB_LIB_DIR)) -L$(FPLIB_LIB_DIR) -lfpc -lfpi

jni_fplib: $(JNI_LIB_DIR)/libjni_fplib.so $(JNI_LIB_DIR)/libjni_fplibSolver.so
	@echo "JNI fplib files compiled."

$(JNI_LIB_DIR)/libjni_fplibSolver.so: $(JNI_SRC_DIR)/jni_FplibSolver.o
	@echo "Generating JNI fplibSolver library..."
	$(CXX) -shared $(CFLAGS) -o $@ -Wl,-soname,$(notdir $@) $^ $(FPLIB_LIBS)

$(JNI_SRC_DIR)/jni_FplibSolver.o: $(JNI_SRC_DIR)/jni_common.h $(JNI_SRC_DIR)/jni_FplibSolver.h $(JNI_SRC_DIR)/jni_FplibSolver.cpp 
	$(CXX) $(CFLAGS) -I$(FPLIB_INC_DIR) $(JNI_CFLAGS) -c -o $@ $(JNI_SRC_DIR)/jni_FplibSolver.cpp 

$(JNI_SRC_DIR)/jni_FplibSolver.h: $(JAVA_SRC_DIR)/solver/fplib/FplibSolver.java 
	$(JAVAH) $(JNI_JHFLAGS) -o $@ solver.fplib.FplibSolver 

$(JNI_LIB_DIR)/libjni_fplib.so: $(JNI_SRC_DIR)/jni_Fplib.o
	@echo "Generating JNI fplib library..."
	$(CXX) -shared $(CFLAGS) -o $@ -Wl,-soname,$(notdir $@) $^ $(FPLIB_LIBS)

$(JNI_SRC_DIR)/jni_Fplib.o: $(JNI_SRC_DIR)/jni_common.h $(JNI_SRC_DIR)/jni_Fplib.h $(JNI_SRC_DIR)/jni_Fplib.cpp 
	$(CXX) $(CFLAGS) -I$(FPLIB_INC_DIR) $(JNI_CFLAGS) -c -o $@ $(JNI_SRC_DIR)/jni_Fplib.cpp 

$(JNI_SRC_DIR)/jni_Fplib.h: $(JAVA_SRC_DIR)/solver/fplib/cfg/Fplib.java 
	$(JAVAH) $(JNI_JHFLAGS) -o $@ solver.fplib.cfg.Fplib


###################### glpk

GLPK_LIB_DIR:=lib/GLPK
GLPK_INC_DIR:=Include/GLPK
GLPK_LIBS:=-L$(GLPK_LIB_DIR) -lglpk-gmp -lm

jni_glpk: $(JNI_LIB_DIR)/libjni_glpk.so
	@echo "JNI GLPK files compiled."

$(JNI_LIB_DIR)/libjni_glpk.so: $(JNI_SRC_DIR)/jni_GlpkSolver.o
	@echo "Generating JNI GLPK library..."
	$(CXX) -shared $(CFLAGS) -o $@ -Wl,-soname,$(notdir $@) $^ $(GLPK_LIBS)

$(JNI_SRC_DIR)/jni_GlpkSolver.o: $(JNI_SRC_DIR)/jni_common.h $(JNI_SRC_DIR)/jni_GlpkSolver.h $(JNI_SRC_DIR)/jni_GlpkSolver.cpp 
	$(CXX) $(CFLAGS) -I$(GLPK_INC_DIR) $(JNI_CFLAGS) -c -o $@ $(JNI_SRC_DIR)/jni_GlpkSolver.cpp 

$(JNI_SRC_DIR)/jni_GlpkSolver.h: $(JAVA_SRC_DIR)/solver/glpk/GlpkSolver.java 
	$(JAVAH) $(JNI_JHFLAGS) -o $@ solver.glpk.GlpkSolver 


###################### fluctuat

FLUCTUAT_LIB_DIR:=$(PWD)/lib/Fluctuat
FLUCTUAT_INC_DIR:=Include/Fluctuat
ifeq ($(MAKECMDGOALS),x86_64)
  FLUCTUAT_LIBS:=-Wl,-rpath,$(realpath $(FLUCTUAT_LIB_DIR)/x86_64) $(FLUCTUAT_LIB_DIR)/x86_64/debug_analyzer.so 
else
  FLUCTUAT_LIBS:=-Wl,-rpath,$(realpath $(FLUCTUAT_LIB_DIR)/x86_32) $(FLUCTUAT_LIB_DIR)/x86_32/debug_analyzer.so
endif

jni_fluctuat: $(JNI_LIB_DIR)/libjni_Fluctuat.so
	@echo "JNI Fluctuat files compiled."

$(JNI_LIB_DIR)/libjni_Fluctuat.so: $(JNI_SRC_DIR)/jni_Fluctuat.o
	@echo "Generating JNI Fluctuat library..."
	$(CC) -shared $(CFLAGS) -o $@ -Wl,-soname,$(notdir $@) $^ $(FLUCTUAT_LIBS)

$(JNI_SRC_DIR)/jni_Fluctuat.o: $(JNI_SRC_DIR)/jni_common.h $(JNI_SRC_DIR)/jni_Fluctuat.h $(JNI_SRC_DIR)/jni_Fluctuat.cpp
	$(CXX) $(CFLAGS) -I$(FLUCTUAT_INC_DIR) $(JNI_CFLAGS) -c -o $@ $(JNI_SRC_DIR)/jni_Fluctuat.cpp

$(JNI_SRC_DIR)/jni_Fluctuat.h: $(JAVA_SRC_DIR)/solver/fluctuat/Fluctuat.java 
	$(JAVAH) $(JNI_JHFLAGS) -o $@ solver.fluctuat.Fluctuat 

######################## clean

clean:
	@echo "Deleting JNI files..."
	-@rm -rf $(JNI_LIB_DIR)/* 
	-@rm -f $(JNI_SRC_DIR)/jni_Z3Solver.[oh]
	-@rm -f $(JNI_SRC_DIR)/jni_FplibSolver.[oh]
	-@rm -f $(JNI_SRC_DIR)/jni_GlpkSolver.[oh]
	-@rm -f $(JNI_SRC_DIR)/jni_Fluctuat.[oh]
	@echo "Deleting class files..."
	-@rm -rf $(JAVA_CLS_DIR)/*

######################## doc

doc: $(DOC_DIR)
	$(JAVADOC) -sourcepath $(JAVA_SRC_DIR) -d $(DOC_DIR) $(wordlist 1,$(words $(JAVA_PKGS)),$(JAVA_PKGS))

$(DOC_DIR):
	mkdir $(DOC_DIR)
