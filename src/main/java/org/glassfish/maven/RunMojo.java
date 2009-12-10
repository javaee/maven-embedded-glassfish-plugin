/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.maven;

import java.io.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.embedded.ContainerBuilder;
/**
 * @goal run
 */

public class RunMojo extends AbstractDeployMojo {

/**
 * @parameter expression="${app}"
 * @required
 */
    protected String app;

/**
 * @parameter expression="${cascade}"
 */
    Boolean cascade;
/**
 * @parameter expression="${dropTables}"
*/
     Boolean dropTables;


    public void execute() throws MojoExecutionException, MojoFailureException {

        File deployArchive = new File(app);
        if (!deployArchive.exists()) {
            throw new MojoExecutionException ("", new java.io.FileNotFoundException(app));
        }

        Server server = null;
        try {
            super.setClassPathProperty();

            server = Server.getServer(serverID);
            if (server == null) {
                server = Util.getServer(serverID, installRoot, instanceRoot, configFile, autoDelete);
                server.addContainer(getContainerBuilderType());
            }

            if (port != -1)
                server.createPort(port);

            server.start();
            EmbeddedDeployer deployer = server.getDeployer();
            DeployCommandParameters cmdParams = new DeployCommandParameters();
            configureDeployCommandParameters(cmdParams);

            UndeployCommandParameters undeployCommandParameters =
                    new UndeployCommandParameters();

            if (dropTables != null)
                undeployCommandParameters.droptables = dropTables;
            if (cascade != null)
                undeployCommandParameters.cascade = cascade;

            while(true) {
                String appName = deployer.deploy(deployArchive, cmdParams);
                System.out.println("Hit ENTER to redeploy, X to exit");
                String str = new BufferedReader(new InputStreamReader(System.in)).readLine();

                undeployCommandParameters.name = appName;
                deployer.undeploy(appName, undeployCommandParameters);

                if (str.equalsIgnoreCase("X"))
                    break;
            }
        } catch(Exception e) {
           throw new MojoExecutionException(e.getMessage(),e);
       } finally {
           if (server != null) {
            try {
                server.stop();
            } catch (Exception ex) {
            }
            }
       }
    }


}
