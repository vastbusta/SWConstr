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

#include "fennel/common/CommonPreamble.h"
#include "fennel/ftrs/BTreeSearchUniqueExecStream.h"
#include "fennel/btree/BTreeReader.h"
#include "fennel/exec/ExecStreamBufAccessor.h"

FENNEL_BEGIN_CPPFILE("$Id$");

ExecStreamResult BTreeSearchUniqueExecStream::execute(
    ExecStreamQuantum const &quantum)
{
    ExecStreamResult rc = precheckConduitInput();
    if (rc != EXECRC_YIELD) {
        return rc;
    }
    
    uint nTuples = 0;
    assert(quantum.nTuplesMax > 0);

    // outer loop
    for (;;) {

        if (!innerSearchLoop()) {
            return EXECRC_BUF_UNDERFLOW;
        }
        
        if (nTuples >= quantum.nTuplesMax) {
            return EXECRC_QUANTUM_EXPIRED;
        }
        
        if (pOutAccessor->produceTuple(tupleData)) {
            ++nTuples;
        } else {
            return EXECRC_BUF_OVERFLOW;
        }
        
        pReader->endSearch();
        pInAccessor->consumeTuple();
    }
}

FENNEL_END_CPPFILE("$Id$");

// End BTreeSearchUniqueExecStream.cpp