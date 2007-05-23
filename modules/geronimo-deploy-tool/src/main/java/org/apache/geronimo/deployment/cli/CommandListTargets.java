/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.cli;

import java.io.PrintWriter;

import javax.enterprise.deploy.spi.Target;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;

/**
 * The CLI deployer logic to list targets.
 *
 * @version $Rev$ $Date$
 */
public class CommandListTargets extends AbstractCommand {

    public void execute(PrintWriter out, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        Target[] list = connection.getDeploymentManager().getTargets();
        if ((list == null) || (list.length == 0)) {
            out.println("No available targets.");
        } else {
            out.println("Available Targets:");
            for (int i = 0; i < list.length; i++) {
                Target target = list[i];
                out.println("  "+target.getName());
            }
        }
    }
}
