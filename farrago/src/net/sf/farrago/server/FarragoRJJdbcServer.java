/*
// Farrago is a relational database management system.
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
package net.sf.farrago.server;

import org.objectweb.rmijdbc.RJDriverServer;
import org.objectweb.rmijdbc.RJJdbcServer;

import java.rmi.RemoteException;

/**
 * The main class for Farrago's RMI/JDBC Server.
 *
 * @author Tim Leung
 * @version $Id$
 */
class FarragoRJJdbcServer extends RJJdbcServer
{
    public static void main(String[] args) {
        try {
            Class.forName("org.objectweb.rmijdbc.RJDriverServer_Stub");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Can't find stub!");
            System.exit(0);
        }

        verboseMode = Boolean.valueOf(
            System.getProperty("RmiJdbc.verbose", "true")).booleanValue();

        processArgs(args);

        printMsg("Starting RmiJdbc Server !");
        initServer(new RJJdbcServer());

    }

    protected RJDriverServer buildDriverServer() throws RemoteException {
        return new FarragoRJDriverServer(admpasswd_);
    }
}
