/*
// $Id$
// Package org.eigenbase is a class library of database components.
// Copyright (C) 2004-2004 Disruptive Tech
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
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
package org.eigenbase.sql;

import org.eigenbase.util.NlsString;
import org.eigenbase.sql.type.SqlTypeName;
import org.eigenbase.sql.parser.ParserPosition;

/**
 * A character string literal.
 *
 * <p>Its {@link #value} field is an {@link NlsString} and
 * {@link #typeName} is {@link SqlTypeName#Char}.
 *
 * @author wael
 * @version $Id$
 */
public class SqlCharStringLiteral extends SqlAbstractStringLiteral
{
    protected SqlCharStringLiteral(
        NlsString val,
        ParserPosition pos)
    {
        super(val, SqlTypeName.Char, pos);
    }

    // override clone because NlsString is mutable (yuck!)
    public Object clone() {
        return new SqlCharStringLiteral(
            (NlsString) ((NlsString) value).clone(),
            getParserPosition());
    }

    /** @return the underlying NlsString */
    public NlsString getNlsString()
    {
        return (NlsString) value;
    }

    /** @return the collation */
    public SqlCollation getCollation()
    {
        return getNlsString().getCollation();
    }

    /** Sets the collation.
     * (Convenient for the sql parser to do this after construction)
     */
    public void setCollation(SqlCollation collation)
    {
        getNlsString().setCollation(collation);
    }

    public void unparse(
        SqlWriter writer,
        int leftPrec,
        int rightPrec)
    {
        if (false) {
            String stringValue = ((NlsString) value).getValue();
            writer.print(writer.dialect.quoteStringLiteral(stringValue));
        }
        assert value instanceof NlsString;
        writer.print(value.toString());
    }

    protected SqlAbstractStringLiteral concat1(SqlLiteral [] lits)
    {
        NlsString [] args = new NlsString[lits.length];
        for (int i = 0; i < lits.length; i++) {
            args[i] = ((SqlCharStringLiteral) lits[i]).getNlsString();
        }
        return new SqlCharStringLiteral(
            NlsString.concat(args),
            lits[0].getParserPosition());
    }
}

// End SqlCharStringLiteral.java