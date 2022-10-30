/*---------------------------------------------------------------------------------
 * 
 * Domains
 * -------
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

#ifndef _IBEX_DOMAIN_H
#define _IBEX_DOMAIN_H

#include "Interval.h"
#include "IntervalVector.h"
#include "IntervalMatrix.h"

namespace ibex {

/** \ingroup maingroup
 * \brief Dimension
 */
struct Dim {
  Dim(int dim1, int dim2, int dim3) : dim1(dim1), dim2(dim2), dim3(dim3) { }
  
  inline int size1() const { return dim1==0?1:dim1; }
  inline int size2() const { return dim2==0?1:dim2; }
  inline int size3() const { return dim3==0?1:dim3; }
  inline int size()  const { return size1()*size2()*size3(); }
  bool scalar() const { return dim1==0 && dim2==0 && dim3==0; }
 
  bool operator==(const Dim& d) const { 
    return dim1==d.dim1 && dim2==d.dim2 && dim3==d.dim3; 
  }

  pair<int, struct Dim> index_info(int base_num, int index1, int index2, int index3) const;

  int dim1, dim2, dim3; // dimensions: dim1 x dim2 x dim3
};

/** Compute dimensions of an indexed symbol */
Dim index_dim(const Dim& base_dim);

/** Compute the environment number of an indexed symbol and
 *  perform various checks (that the index is in the range, etc.). */
int index_num(const Dim& base_dim, int base_num, int index);


ostream& operator<<(ostream&, const struct Dim&);

/** \ingroup maingroup
 *
 * \brief Domain.
 */
/* class Domain { */
/*  public: */
/*   /** Builds an interval domain. */ 

/*   Domain(INTERVAL& x) : storage(interval), I(&x), dim(0,0,0) { } */
/** Builds an interval vector domain. */ 
/*   Domain(INTERVAL* x, const Dim& d) : storage(array), I(x), dim(d) { } */

/** Builds an interval matrix domain. */
/*   Domain(INTERVAL_MATRIX& m) : storage(matrix), M(&m), dim(0,RowDimension(m), ColDimension(m)) { } */

/** Builds an interval matrix array domain. */
/*   Domain(INTERVAL_MATRIX* m, int size) : storage(matrix_array), M(m), dim(size,RowDimension(m[0]), ColDimension(m[0])) { } */
  
/** warning : no check. */
/*   void write(const INTERVAL& x) { *I = x; } */

/** warning : no check. */
/*   void read(INTERVAL& x) const { x = *I; } */

/** warning : no check. */
/*   void write(const INTERVAL_MATRIX& x) { */
/*     if (storage==array) { */
/*       for (int i=0; i<dim.dim2; i++)  */
/* 	for (int j=0; j<dim.dim3; j++)  */
/* 	  x(i+1,j+1) = I[i*dim.dim3+j]; */
/*     } else */
/*       *M = x; */
/*   } */

/** warning : no check. */
/*   void read(INTERVAL_MATRIX& x) const { */
/*     if (storage==array) { */
/*       for (int i=0; i<dim.dim2; i++)  */
/* 	for (int j=0; j<dim.dim3; j++)  */
/* 	  I[i*dim.dim3+j] = x(i+1,j+1); */
/*     } else */
/*       x = *M; */
/*   } */

/** warning : no check. */
/*   void write(int index1, const INTERVAL& x) { */
/*     I[index1-1] = x; */
/*   } */

/** warning : no check. */
/*   void read(int index1, INTERVAL& x) const { */
/*     x = I[index1-1]; */
/*   } */

/** warning : no check. */
/*   void write(int index1, const INTERVAL_MATRIX& x) { */
/*     if (storage==array) { */
/*       for (int i=0; i<dim.dim2; i++)  */
/* 	for (int j=0; j<dim.dim3; j++)  */
/* 	  I[(index1-1)*dim.dim2 + i*dim.dim3 + j] = x(i+1,j+1); */
/*     } else */
/*       M[index1-1] = x; */
/*   } */


/** warning : no check. */
/*   void read(int index1, INTERVAL_MATRIX& x) const { */
/*     if (storage==array) { */
/*       for (int i=0; i<dim.dim2; i++)  */
/* 	for (int j=0; j<dim.dim3; j++)  */
/* 	  x(i+1,j+1) = I[(index1-1)*dim.dim2 + i*dim.dim3 + j]; */
/*     } else */
/*       x = M[index1-1]; */
/*   } */

/** warning : no check. */
/*   void write(int index1, int index2, const INTERVAL& x) { */
/*     if (storage==array) */
/*       I[(index1-1)*dim.dim3+index2-1] = x; */
/*     else  */
/*       (*M)(index1,index2) = x; */
/*   } */

/** warning : no check. */
/*   void read(int index1, int index2, INTERVAL& x) const { */
/*     if (storage==array) */
/*       x = I[(index1-1)*dim.dim3+index2-1]; */
/*     else  */
/*       x = (*M)(index1,index2); */
/*   } */

/** \precondition x must have one column. */
/*    * warning : no check. */
/*   void write(int index1, int index2, const INTERVAL_MATRIX& x) { */
/*     if (storage==array) */
/*       for (int j=0; j<dim.dim3; j++)  */
/* 	I[(index1-1)*dim.dim2 + (index2-1)*dim.dim3 + j] = x(j+1,1); */
/*     else  */
/*       SetRow(M[index1-1],index2,Col(x,1)); */
/*   } */

/** \precondition x must have one column. */
/*    * warning : no check. */
/*   void write(int index1, int index2, INTERVAL_MATRIX& x) const { */
/*     if (storage==array) */
/*       for (int j=0; j<dim.dim3; j++)  */
/* 	x(j+1,1) = I[(index1-1)*dim.dim2 + (index2-1)*dim.dim3 + j]; */
/*     else  */
/*       SetCol(x,1,Row(M[index1-1],index2)); */
/*   } */

/** warning : no check. */
/*   void write(int index1, int index2, int index3, const INTERVAL& x) { */
/*     if (storage==array) */
/*       I[(index1-1)*dim.dim2 + (index2-1)*dim.dim3 + index3-1] = x; */
/*     else */
/*       M[index1-1](index2,index3) = x; */
/*   } */

/** warning : no check. */
/*   void read(int index1, int index2, int index3, INTERVAL& x) const { */
/*     if (storage==array) */
/*       x =I[(index1-1)*dim.dim2 + (index2-1)*dim.dim3 + index3-1]; */
/*     else */
/*       x = M[index1-1](index2,index3); */
/*   } */

/*   Dim dim; */

/*   void free_mem() { */
/*     switch (storage) { */
/*     case interval : delete I; break; */
/*     case array : delete[] I; break; */
/*     case matrix : delete M; break; */
/*     case matrix_array : delete[] M; break; */
/*     } */
/*   } */

/*  private: */

/** Pointer to the domain (where a symbol takes values). */
/*   union { */
/*     INTERVAL* I;        // either a single interval, an array of intervals, */
/*     INTERVAL_MATRIX* M; // an interval matrix, or an array of interval matrices. */
/*     // INTERVAL_VECTOR* V; // (for the future) */
/*   }; */

/** For efficiency, matrix symbols may have their domains stored in a single "flat" interval 
    * array (see #Space). E.g., if x is a 3x3 matrix, then 9 intervals in an array are 
    * consecutively allocated for x. In this case, storage is "array". */
/*   enum { interval, array, matrix, matrix_array } storage; */
/* }; */

} // end namespace

#endif
