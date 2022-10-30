/*---------------------------------------------------------------------------------
 * 
 * Input-output
 * ---------------------
 *
 * Copyright (C) 2007-2009 Gilles Chabert
 * 
 * This file is part of IBEX.
 *
 * IBEX is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either 
 * version 2 of the License, or (at your option) any later version.
 *
 * IBEX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with IBEX; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301, USA 
 *
 --------------------------------------------------------------------------------*/

#ifndef __IBEX_IO_H
#define __IBEX_IO_H

#include <fstream>
#include "IbexExpr.h"
#include "IbexSpace.h"
#include "IbexPaving.h"

namespace ibex {

void read_file(const char* filename, Space& space, const ExtendedSymbol& symbol);

void print_var(const Space& space, const ExtendedSymbol&);

/* void plot2D(const char* filename, const Space& space, const ExtendedSymbol& x, const ExtendedSymbol& y, int color); */

/* class ScilabPlot2D { */
/*  public: */
/*    ScilabPlot2D(const char*); */
/*    ~ScilabPlot2D(); */
/*    void write_header(); */
/*    void update_frame(const INTERVAL& dx, const INTERVAL& dy); */
/*    void set_frame(); */
/*    void draw_box(const INTERVAL& x, const INTERVAL& y, int color); */
/*  private: */
/*   fstream fin; */
/*   REAL xmin,xmax,ymin,ymax; */
/*  }; */

/* class Paving2Scilab : public PavingVisitor, ScilabPlot2D { */
/*  public: */
/*   Paving2Scilab(const char* filename, const PavingNode&, int var1, int var2, int color_codes[]); */
/*  private: */
/*   virtual void visit(const PavingNode&); */
/*   virtual void visit(const ContractorNode&); */
/*   virtual void visit(const BisectorNode&); */
/*   int var1; */
/*   int var2; */
/*   int* color_codes;   */
/*   bool no_frame; */
/* }; */

struct RGB {
  public:
/*   RGB() : red(0), green(0), blue(0) { } */
/*   RGB(double red, double green, double blue) : red(red), green(green), blue(blue) { } */
  double red, green, blue;  
};

struct BoxColor {
  RGB color_in;  // color for the interior 
  RGB color_out;  // color for the border 
};


void plot2D(fstream& fin, const Space& space, const ExtendedSymbol& x, const ExtendedSymbol& y, const BoxColor& color);

class PavingPlot : public PavingVisitor {
 public:  
  PavingPlot(fstream& fin, const PavingNode&, int ctc, int var1, int var2, const BoxColor& color);

  PavingPlot(fstream& fin, const PavingNode&, int var1, int var2, const vector<BoxColor>& colors);
 private:
  virtual void visit(const PavingNode&);
  virtual void visit(const ContractorNode&);
  virtual void visit(const BisectorNode&);
  int var1;
  int var2;
  int ctc;
  bool compute_size;
  int size;
  fstream& fin;
};

} // end namespace

#endif
