/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2006-2006 The Eigenbase Project
// Copyright (C) 2006-2006 Disruptive Tech
// Copyright (C) 2006-2006 LucidEra, Inc.
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
package net.sf.farrago.server;

import java.io.*;

import java.rmi.*;
import java.rmi.registry.*;

import java.util.*;

import net.sf.farrago.db.*;
import net.sf.farrago.fem.config.*;
import net.sf.farrago.jdbc.engine.*;
import net.sf.farrago.release.*;
import net.sf.farrago.resource.*;
import net.sf.farrago.session.*;
import net.sf.farrago.util.*;


/**
 * FarragoAbstractServer is a common base for implementations of Farrago network
 * servers.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public abstract class FarragoAbstractServer
{

    //~ Static fields/initializers ---------------------------------------------

    protected static Registry rmiRegistry;

    //~ Instance fields --------------------------------------------------------

    protected final PrintWriter pw;

    protected int rmiRegistryPort;

    protected int singleListenerPort;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FarragoServer instance, with console output to System.out.
     * This constructor can be used to embed a FarragoServer inside of another
     * container such as a J2EE app server.
     */
    protected FarragoAbstractServer()
    {
        this(new PrintWriter(System.out, true));
    }

    /**
     * Creates a new FarragoServer instance, with redirected console output.
     * This constructor can be used to embed a FarragoAbstractServer inside of
     * another container such as a J2EE app server.
     *
     * @param pw receives console output
     */
    protected FarragoAbstractServer(PrintWriter pw)
    {
        this.pw = pw;
    }

    //~ Methods ----------------------------------------------------------------

    protected void configureNetwork(
        FarragoReleaseProperties releaseProps,
        FemFarragoConfig config)
    {
        rmiRegistryPort = config.getServerRmiRegistryPort();

        singleListenerPort = config.getServerSingleListenerPort();

        if (rmiRegistryPort == -1) {
            rmiRegistryPort = releaseProps.jdbcUrlPortDefault.get();
        }
    }

    /**
     * Starts the network.
     *
     * @param jdbcDriver the JDBC driver which will be served to remote clients
     *
     * @return network port on which server is configured to listen
     */
    protected abstract int startNetwork(FarragoJdbcServerDriver jdbcDriver)
        throws Exception;

    /**
     * Stops the network. Default implementation is to call unbindRegistry, but
     * subclasses can override.
     */
    protected void stopNetwork()
    {
        // REVIEW jvs 4-June-2006:  For VJDBC, this causes spurious
        // errors to be traced on shutdown, because VJDBC registers
        // a ShutdownThread to unbind itself.  Any way to squelch those?
        unbindRegistry();
    }

    /**
     * Locates the RMI registry. RMI-based servers should use this during
     * startNetwork after creating a registry.
     */
    protected void locateRmiRegistry()
    {
        if (rmiRegistry == null) {
            // This is the first server instance in this JVM, so
            // look up the RMI registry just created.
            try {
                rmiRegistry = LocateRegistry.getRegistry(rmiRegistryPort);
            } catch (Throwable ex) {
                // TODO:  handle this better
            }
        }
    }

    /**
     * Unbinds all items remaining in the RMI registry. RMI-based servers should
     * use this in stopNetwork.
     */
    protected void unbindRegistry()
    {
        if (rmiRegistry == null) {
            return;
        }

        try {
            String [] names = rmiRegistry.list();
            for (int i = 0; i < names.length; ++i) {
                rmiRegistry.unbind(names[i]);
            }
        } catch (Exception ex) {
            // TODO:  handle this better
            ex.printStackTrace();
        }
    }

    /**
     * Starts the server.
     *
     * @param jdbcDriver the JDBC driver which will be served to remote clients
     */
    public void start(FarragoJdbcServerDriver jdbcDriver)
        throws Exception
    {
        FarragoResource res = FarragoResource.instance();
        FarragoReleaseProperties releaseProps =
            FarragoReleaseProperties.instance();
        pw.println(
            res.ServerProductName.str(
                releaseProps.productName.get()));
        pw.println(res.ServerLoadingDatabase.str());

        // Load the session factory
        FarragoSessionFactory sessionFactory = jdbcDriver.newSessionFactory();

        // Load the database instance
        FarragoDatabase db = FarragoDbSingleton.pinReference(sessionFactory);

        FemFarragoConfig config = db.getSystemRepos().getCurrentConfig();

        configureNetwork(
            releaseProps,
            config);

        pw.println(res.ServerStartingNetwork.str());

        boolean success = false;
        try {
            int port = startNetwork(jdbcDriver);

            pw.println(
                res.ServerListening.str(new Integer(port)));
            success = true;
        } finally {
            if (!success) {
                pw.println(res.ServerNetworkStartFailed.str());
                stopHard();
            }
        }
    }

    /**
     * Stops the server if there are no sessions.
     *
     * @return whether server was stopped
     */
    public boolean stopSoft()
    {
        FarragoResource res = FarragoResource.instance();
        pw.println(res.ServerShuttingDown.str());

        // NOTE:  use groundReferences=1 in shutdownConditional
        // to account for our baseline reference
        if (FarragoDbSingleton.shutdownConditional(getGroundReferences())) {
            pw.println(res.ServerShutdownComplete.str());

            // TODO: should find a way to prevent new messages BEFORE shutdown
            stopNetwork();
            return true;
        } else {
            pw.println(res.ServerSessionsExist.str());
            return false;
        }
    }

    /**
     * Returns the number of ground references for this server. Ground
     * references are references pinned at startup time. For the base
     * implementation of FarragoServer this is always 1. Farrago extensions,
     * especially those that initialize resources via {@link
     * FarragoSessionFactory#specializedInitialization(
     * FarragoAllocationOwner)}, may need to alter this value.
     *
     * @return the number of ground references for this server
     */
    protected int getGroundReferences()
    {
        return 1;
    }

    /**
     * Stops the server, killing any sessions.
     */
    public void stopHard()
    {
        stopNetwork();
        FarragoResource res = FarragoResource.instance();
        pw.println(res.ServerShuttingDown.str());
        FarragoDbSingleton.shutdown();
        pw.println(res.ServerShutdownComplete.str());
    }

    /**
     * @return redirected console output
     */
    public PrintWriter getPrintWriter()
    {
        return pw;
    }

    /**
     * Implements console interaction from stdin after the server has
     * successfully started.
     */
    public void runConsole()
    {
        FarragoResource res = FarragoResource.instance();

        // TODO:  install signal handlers also
        InputStreamReader inReader = new InputStreamReader(System.in);
        LineNumberReader lineReader = new LineNumberReader(inReader);
        for (;;) {
            String cmd;
            try {
                cmd = lineReader.readLine();
            } catch (IOException ex) {
                break;
            }
            if (cmd == null) {
                // interpret end-of-stream as meaning we are supposed to
                // run forever as a daemon
                return;
            }
            if (cmd.equals("!quit")) {
                if (stopSoft()) {
                    break;
                }
            } else if (cmd.equals("!kill")) {
                stopHard();
                break;
            } else {
                pw.println(res.ServerBadCommand.str(cmd));
            }
        }
        System.exit(0);
    }
}

// End FarragoAbstractServer.java