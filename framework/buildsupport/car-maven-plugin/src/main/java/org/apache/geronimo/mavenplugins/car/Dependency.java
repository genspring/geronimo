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


package org.apache.geronimo.mavenplugins.car;

import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.system.plugin.model.DependencyType;

/**
 * @version $Rev$ $Date$
 */
public class Dependency extends ModuleId {

    /**
     * @parameter
     */
    private Boolean start;


    public Boolean isStart() {
        if (start == null) {
            return Boolean.TRUE;
        }
        return start;
    }

    public void setStart(Boolean start) {
        this.start = start;
    }

    public DependencyType toDependencyType() {
        DependencyType dependency = new DependencyType();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setType(type);
        dependency.setStart(start);
        return dependency;
    }

    public org.apache.geronimo.kernel.repository.Dependency toKernelDependency() {
        org.apache.geronimo.kernel.repository.Artifact artifact = new org.apache.geronimo.kernel.repository.Artifact(groupId, artifactId, version, type);
        ImportType importType = getImport() == null? ImportType.ALL: ImportType.getByName(getImport());
        return new org.apache.geronimo.kernel.repository.Dependency(artifact, importType);
    }

    public String toString() {
        return groupId + ":" + artifactId + ":" + version + ":" + type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dependency that = (Dependency) o;
        if (!super.equals(that)) return false;
        return isStart().equals(that.isStart());
    }
    //rely on super.hashcode();

    public static Dependency newDependency(DependencyType dependencyType) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(dependencyType.getGroupId());
        dependency.setArtifactId(dependencyType.getArtifactId());
        dependency.setVersion(dependencyType.getVersion());
        dependency.setType(dependencyType.getType());
        dependency.setStart(dependencyType.isStart());
        dependency.setImport(ImportType.ALL.toString());
        return dependency;
    }
}
