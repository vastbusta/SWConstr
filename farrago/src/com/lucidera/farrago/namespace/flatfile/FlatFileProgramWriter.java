/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005-2005 LucidEra, Inc.
// Copyright (C) 2005-2005 The Eigenbase Project
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.lucidera.farrago.namespace.flatfile;

import com.disruptivetech.farrago.calc.*;

import org.eigenbase.rel.*;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.*;
import org.eigenbase.sql.type.*;


/**
 * Constructs a Calculator program for translating text from a flat file into
 * typed data. It is assumed that the text has already been processed for
 * quoting and escape characters.
 *
 * @author jpham
 * @version $Id$
 */
public class FlatFileProgramWriter
{

    //~ Static fields/initializers ---------------------------------------------

    public static final int FLAT_FILE_MAX_NON_CHAR_VALUE_LEN = 255;

    //~ Instance fields --------------------------------------------------------

    RexBuilder rexBuilder;
    RelDataTypeFactory typeFactory;
    RelNode rel;

    //~ Constructors -----------------------------------------------------------

    public FlatFileProgramWriter(
        RelNode rel)
    {
        this.rel = rel;
        rexBuilder = rel.getCluster().getRexBuilder();
        typeFactory = rexBuilder.getTypeFactory();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Given the description of the expected data types, generates a program for
     * converting text into typed data.
     *
     * <p>First this method infers the description of text columns required to
     * read the exptected data values. Then it constructs the casts necessary to
     * perform data conversion. Date conversions may require special functions.
     */
    public RexProgram getProgram(RelDataType rowType)
    {
        assert (rowType.isStruct());
        RelDataTypeField [] targetTypes = rowType.getFields();

        // infer source text types
        RelDataType [] sourceTypes = new RelDataType[targetTypes.length];
        String [] sourceNames = new String[targetTypes.length];
        for (int i = 0; i < targetTypes.length; i++) {
            RelDataType targetType = targetTypes[i].getType();
            sourceTypes[i] = getTextType(targetType);
            sourceNames[i] = targetTypes[i].getName();
        }
        RelDataType inputRowType =
            typeFactory.createStructType(sourceTypes, sourceNames);

        // construct rex program
        RexProgramBuilder programBuilder =
            new RexProgramBuilder(inputRowType, rexBuilder);
        for (int i = 0; i < targetTypes.length; i++) {
            RelDataType targetType = targetTypes[i].getType();

            // TODO: call dedicated functions for conversion of dates
            programBuilder.addProject(
                rexBuilder.makeCast(
                    targetType,
                    programBuilder.makeInputRef(i)),
                sourceNames[i]);
        }
        RexProgram program = programBuilder.getProgram();
        return program;
    }

    /**
     * Relies on a {@link RexToCalcTranslator} to convert a Rex program into a
     * Fennel calculator program.
     */
    protected String toFennelProgram(RexProgram program)
    {
        // translate to a fennel calc program
        RexToCalcTranslator translator =
            new RexToCalcTranslator(rexBuilder, rel);
        return translator.generateProgram(
                program.getInputRowType(),
                program);
    }

    /**
     * Converts a SQL type into a type that can be used by a Fennel
     * FlatFileExecStream to read files.
     */
    private RelDataType getTextType(RelDataType sqlType)
    {
        int length = FLAT_FILE_MAX_NON_CHAR_VALUE_LEN;
        switch (sqlType.getSqlTypeName().getOrdinal()) {
        case SqlTypeName.Char_ordinal:
        case SqlTypeName.Varchar_ordinal:
            length = sqlType.getPrecision();
            break;
        case SqlTypeName.Bigint_ordinal:
        case SqlTypeName.Boolean_ordinal:
        case SqlTypeName.Date_ordinal:
        case SqlTypeName.Decimal_ordinal:
        case SqlTypeName.Double_ordinal:
        case SqlTypeName.Float_ordinal:
        case SqlTypeName.Integer_ordinal:
        case SqlTypeName.Real_ordinal:
        case SqlTypeName.Smallint_ordinal:
        case SqlTypeName.Time_ordinal:
        case SqlTypeName.Timestamp_ordinal:
        case SqlTypeName.Tinyint_ordinal:
            break;
        case SqlTypeName.Binary_ordinal:
        case SqlTypeName.IntervalDayTime_ordinal:
        case SqlTypeName.IntervalYearMonth_ordinal:
        case SqlTypeName.Multiset_ordinal:
        case SqlTypeName.Null_ordinal:
        case SqlTypeName.Row_ordinal:
        case SqlTypeName.Structured_ordinal:
        case SqlTypeName.Symbol_ordinal:
        case SqlTypeName.Varbinary_ordinal:
        default:

            // unsupported for flat files
            assert (false) : "Type is unsupported for flat files: "
                + sqlType.getSqlTypeName();
        }
        RelDataType type =
            typeFactory.createSqlType(SqlTypeName.Varchar, length);
        return typeFactory.createTypeWithNullability(type, true);
    }
}

// End FlatFileProgramWriter.java