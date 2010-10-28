/**
 * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of Anchor Intelligence nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.fraudwall.util.net;

import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.exc.AnchorFatalError;

/**
 * Utilities for assisting with Java RMI (Remote Method Invocation).
 *
 * @author ryan
 */
public abstract class RMIUtils {

	private static final Log log = LogFactory.getLog(RMIUtils.class);

	/**
	 * Creates and returns a new RMI registry listening on
	 * port <code>registryPort</code>.
	 */
	public static Registry createRegistry(int registryPort) {
		try {
			log.info("Creating new registry on port " + registryPort);
			return LocateRegistry.createRegistry(registryPort);
		} catch (RemoteException e) {
			throw new AnchorFatalError("Unable to create registry on localhost:" + registryPort, e);
		}
	}

	/**
	 * Unexports the RMI registry specified via <code>registry</code>.
	 */
	public static void destroyRegistry(Registry registry) {
		try {
			UnicastRemoteObject.unexportObject(registry, true);
		} catch (NoSuchObjectException e) {
			throw new AnchorFatalError("Unable to unexport registry",e);
		}
	}


	/**
	 * Binds the name <code>serviceName</code> to the {@link Remote} object
	 * <code>remote</code> in the RMI registry listening on port <code>registryPort</code>.
	 */
	public static void bind(int registryPort, String serviceName, Remote remote) {
		try {
			Registry registry = getRegistry(registryPort);
			bind(registry, serviceName, remote);
		} catch (RemoteException e) {
			throw new AnchorFatalError("Unable to contact registry on port " + registryPort, e);
		}
	}

	/**
	 * Binds the name <code>serviceName</code> to the {@link Remote} object
	 * <code>remote</code> in the RMI registry specified via <code>registry</code>.
	 */
	public static void bind(Registry registry, String serviceName, Remote remote) {
		log.info("exporting Remote stub");
		try {
			Remote stub = UnicastRemoteObject.exportObject(remote, 0);
			try {
				log.info("binding RMI name: " + serviceName);
				registry.bind(serviceName, stub);
			} catch (AlreadyBoundException e) {
				if (log.isDebugEnabled())
					log.debug("already bound, re-binding");
				registry.rebind(serviceName, stub);
			}
		} catch (RemoteException e) {
			throw new AnchorFatalError("Unable bind to service name " + serviceName, e);
		}
	}

	/**
	 * Unbinds the name <code>serviceName</code> from the RMI registry
	 * listening on port <code>registryPort</code>.
	 */
	public static void unbind(int registryPort, String serviceName) {
		try {
			Registry registry = getRegistry(registryPort);
			unbind(registry,serviceName);
		} catch (RemoteException e) {
			throw new AnchorFatalError("Unable to contact registry on port " + registryPort, e);
		}
	}

	/**
	 * Unbinds the name <code>serviceName</code> from the RMI registry
	 * specified via <code>registry</code>.
	 */
	public static void unbind(Registry registry, String serviceName) {
		try {
			log.info("unbinding RMI name: " + serviceName);
			registry.unbind(serviceName);
		} catch (NotBoundException e) {
			throw new AnchorFatalError("Error communicating with remote producers", e);
		} catch (RemoteException e) {
			throw new AnchorFatalError("Error communicating with remote producers", e);
		}
	}

	public static Remote tryLookup(String remoteHostname, int remoteRegistryPort, String serviceName) {
		return lookup(remoteHostname, remoteRegistryPort, serviceName, false);
	}

	public static Remote lookup(String remoteHostname, int remoteRegistryPort, String serviceName, boolean fatal) {
		try {
			Registry registry = LocateRegistry.getRegistry(remoteHostname, remoteRegistryPort);
			try {
				return registry.lookup(serviceName);
			} catch (RemoteException e) {
				String msg = "Unable to lookup " + serviceName;
				handleRmiException(msg, remoteHostname, remoteRegistryPort, e, fatal);

			}
		} catch (NotBoundException e) {
			String msg = "Service " + serviceName + " not bound";
			handleRmiException(msg, remoteHostname, remoteRegistryPort, e, fatal);
		} catch (RemoteException e) {
			String msg = "Unable to contact registry";
			handleRmiException(msg, remoteHostname, remoteRegistryPort, e, fatal);
		}
		return null;
	}

	private static void handleRmiException(
		String message, String remoteHostname, int remotePort, Exception e, boolean fatal)
	{
		message += " on " + remoteHostname + ":"  + remotePort;
		if (fatal) {
			throw new AnchorFatalError(message, e);
		}
		log.info(message);
		log.info(e.getMessage());
		log.debug(e);
	}

	public static Registry getRegistry(int registryPort) throws RemoteException {
		log.info("getting RMI registry on localhost:" + registryPort);
		return LocateRegistry.getRegistry(null, registryPort);
	}

}
