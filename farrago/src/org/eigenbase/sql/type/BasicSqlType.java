/*
// $Id$
// Package org.eigenbase is a class library of database components.
// Copyright (C) 2004-2004 Disruptive Tech
// Copyright (C) 2004-2004 John V. Sichi.
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
package org.eigenbase.sql.type;

import org.eigenbase.reltype.*;
import org.eigenbase.sql.*;
import org.eigenbase.util.*;

import java.nio.charset.*;

/**
 * BasicSqlType represents a standard atomic SQL type (excluding
 * interval types).
 *
 * @author jhyde
 * @version $Id$
 */
public class BasicSqlType extends AbstractSqlType
{
    public static final int SCALE_NOT_SPECIFIED = Integer.MIN_VALUE;
    public static final int PRECISION_NOT_SPECIFIED = -1;
    private final int precision;
    private final int scale;
    private SqlCollation collation;
    private Charset charset;

    /**
     * Constructs a type with no parameters.
     * @param typeName Type name
     * @pre typeName.allowsNoPrecNoScale(false,false)
     */
    public BasicSqlType(SqlTypeName typeName)
    {
        super(typeName, false);
        Util.pre(
            typeName.allowsPrecScale(false, false),
            "typeName.allowsPrecScale(false,false), typeName="
            + typeName.name);
        this.precision = PRECISION_NOT_SPECIFIED;
        this.scale = SCALE_NOT_SPECIFIED;
        computeDigest();
    }

    /**
     * Constructs a type with precision/length but no scale.
     * @param typeName Type name
     * @pre typeName.allowsPrecNoScale(true,false)
     */
    public BasicSqlType(
        SqlTypeName typeName,
        int precision)
    {
        super(typeName, false);
        Util.pre(
            typeName.allowsPrecScale(true, false),
            "typeName.allowsPrecScale(true,false)");
        this.precision = precision;
        this.scale = SCALE_NOT_SPECIFIED;
        computeDigest();
    }

    /**
     * Constructs a type with precision/length and scale.
     * @param typeName Type name
     * @pre typeName.allowsPrecScale(true,true)
     */
    public BasicSqlType(
        SqlTypeName typeName,
        int precision,
        int scale)
    {
        super(typeName, false);
        Util.pre(
            typeName.allowsPrecScale(true, true),
            "typeName.allowsPrecScale(true,true)");
        this.precision = precision;
        this.scale = scale;
        computeDigest();
    }

    /**
     * Constructs a type with nullablity
     */
    BasicSqlType createWithNullability(boolean nullable)
    {
        BasicSqlType ret = null;
        try {
            ret = (BasicSqlType) this.clone();
        } catch (CloneNotSupportedException e) {
            throw Util.newInternal(e);
        }
        ret.isNullable = nullable;
        ret.computeDigest();
        return ret;
    }

    /**
     * Constructs a type with charset and collation
     * @pre SqlTypeUtil.inCharFamily(this)
     */
    BasicSqlType createWithCharsetAndCollation(
        Charset charset,
        SqlCollation collation)
    {
        Util.pre(SqlTypeUtil.inCharFamily(this) == true, "Not an chartype");
        BasicSqlType ret;
        try {
            ret = (BasicSqlType) this.clone();
        } catch (CloneNotSupportedException e) {
            throw Util.newInternal(e);
        }
        ret.charset = charset;
        ret.collation = collation;
        ret.computeDigest();
        return ret;
    }

    //implement RelDataType
    public int getPrecision()
    {
        if (precision == PRECISION_NOT_SPECIFIED) {
            switch(typeName.getOrdinal()) {
            case SqlTypeName.Boolean_ordinal:
                return 1;
            case SqlTypeName.Tinyint_ordinal:
                return 3;
            case SqlTypeName.Smallint_ordinal:
                return 5;
            case SqlTypeName.Integer_ordinal:
                return 10;
            case SqlTypeName.Bigint_ordinal:
                return 20;
            default:
                throw new AssertionError();
            }
        }
        return precision;
    }

    // implement RelDataType
    public int getScale()
    {
        assert(scale != SCALE_NOT_SPECIFIED);
        return scale;
    }

    public Charset getCharset()
        throws RuntimeException
    {
        return this.charset;
    }

    public SqlCollation getCollation()
        throws RuntimeException
    {
        return this.collation;
    }

    protected void generateTypeString(StringBuffer sb, boolean withDetail)
    {
        sb.append(typeName.getName());
        if (precision != PRECISION_NOT_SPECIFIED) {
            sb.append('(');
            sb.append(getPrecision());
            if (scale != SCALE_NOT_SPECIFIED) {
                sb.append(", ");
                sb.append(getScale());
            }
            sb.append(')');
        }
        if (!withDetail) {
            return;
        }
        if (charset != null) {
            sb.append(" CHARACTER SET \"");
            sb.append(charset.name());
            sb.append("\"");
        }
        if (collation != null) {
            sb.append(" COLLATE \"");
            sb.append(collation.getCollationName());
            sb.append("\"");
        }
    }
}

// End BasicSqlType.java