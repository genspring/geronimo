/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.commands;

import org.apache.geronimo.gshell.command.CommandExecutor
import org.apache.geronimo.gshell.command.IO

import org.apache.geronimo.testsupport.TestSupport

/**
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
class RemoteServerControlCommandTest extends GroovyTestCase {

    def command
    def executedCommand
    
    protected void setUp() {
		def testSupport = new GroovyTestSupport()
        
        File configurationFile = testSupport.resolveFile('src/test/resources/etc/server-configuration.xml')

        command = new RemoteServerControlCommand([io: new IO(), configurationFileName: configurationFile.absolutePath])
        command.executor = { executedCommand = it } as CommandExecutor
    }
    
	void testConfigurationFileDoesNotExistThrowsISE() {
	    shouldFail(IllegalStateException.class) {
	        command.configurationFileName = 'doesNotExist'
	        command.doExecute()
	    }
	}

	void testServerIsUndefinedThrowsIAE() {
	    shouldFail(IllegalArgumentException.class) {
	        command.serverName = 'undefined'
	        command.doExecute()
	    }
	}

	void testServerIsDefinedWithUndefinedHostThrowsIAE() {
	    shouldFail(IllegalArgumentException.class) {
	        command.serverName = 'serverWithUndefinedHost'
	        command.doExecute()
	    }
	}
	
	void testControlIsUndefinedThrowsISE() {
	    shouldFail(IllegalStateException.class) {
	        command.serverName = 'defaultServer'
	        command.control = 'undefined'
	        command.doExecute()
	    }
	}
	
	void testRemoteLoginCmdIsUndefinedThrowsISE() {
	    shouldFail(IllegalStateException.class) {
	        command.serverName = 'serverWithUndefinedRemoteLoginCmd'
	        command.doExecute()
	    }
	}
	
	void testRemoteControlOK() {
        command.serverName = 'defaultServer'
        command.control = 'start'
        command.doExecute()
        
        assert 'rshCommand start-server' == executedCommand
	}
	
}