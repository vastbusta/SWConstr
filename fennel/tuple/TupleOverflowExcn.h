/*
// $Id$
// Fennel is a relational database kernel.
// Copyright (C) 2004-2004 John V. Sichi.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

#ifndef Fennel_TupleOverflowExcn_Included
#define Fennel_TupleOverflowExcn_Included

#include "fennel/common/FennelExcn.h"

FENNEL_BEGIN_NAMESPACE

class TupleDescriptor;
class TupleData;
    
/**
 * Exception class to be thrown when an oversized tuple is encountered.
 *
 * @author John V. Sichi
 * @version $Id$
 */
class TupleOverflowExcn : public FennelExcn
{
public:
    /**
     * Constructs a new TupleOverflowExcn.
     *
     *<p>
     *
     * @param tupleDesc descriptor for the tuple
     *
     * @param tupleData data for the tuple
     *
     * @param cbActual actual number of bytes required to store tuple
     *
     * @param cbMax maximum number of bytes available to store tuple
     */
    explicit TupleOverflowExcn(
        TupleDescriptor const &tupleDesc,
        TupleData const &tupleData,
        uint cbActual,
        uint cbMax);
};

FENNEL_END_NAMESPACE

#endif

// End TupleOverflowExcn.h