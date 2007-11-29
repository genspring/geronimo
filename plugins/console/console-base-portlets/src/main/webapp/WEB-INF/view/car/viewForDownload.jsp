<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>

<h1>${plugin.name}</h1>

<table border="0">
  <tr>
    <th align="right" valign="top">Module ID:</th>
    <td>${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Group:</th>
    <td>${plugin.category}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Description:</th>
    <td>${plugin.description}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Author:</th>
    <td>${plugin.author}</td>
  </tr>
  <tr>
    <th align="right" valign="top">Web Site:</th>
    <td><a href="${plugin.url}">${plugin.url}</a></td>
  </tr>
  <c:forEach var="license" items="${plugin.license}">
      <tr>
        <th align="right" valign="top">License:</th>
        <td>${license.value}
          <c:choose>
              <c:when test="${license.osiApproved}">(Open Source)</c:when>
              <c:otherwise>(Proprietary)</c:otherwise>
          </c:choose>
        </td>
      </tr>
  </c:forEach>
    <tr>
    <th align="right" valign="top">Geronimo-Versions:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.geronimoVersion}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="geronimoVersion" items="${plugin.geronimoVersion}">
            <b>${geronimoVersion}</b>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
  <tr>
    <th align="right" valign="top">JVM Versions:</th>
    <td>
      <c:choose>
          <c:when test="${empty plugin.jvmVersion}">
            <i>Any</i>
          </c:when>
          <c:otherwise>
            <c:forEach var="jvmVersion" items="${plugin.jvmVersion}">
              ${jvmVersion}
            </c:forEach>
          </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">Dependencies:</th>
    <td>
      <c:forEach var="dependency" items="${plugin.dependency}">
        ${dependency.groupId}/${dependency.artifactId}/${dependency.version}/${dependency.type}<br />
      </c:forEach>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">Prerequisites:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.prerequisite}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="prereq" items="${plugin.prerequisite}">
            <b>${prereq.id.groupId}/${prereq.id.artifactId}/${prereq.id.version}/${prereq.id.type}</b> (${prereq.resourceType})<br/>
            ${prereq.description}
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">Obsoletes:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.obsoletes}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="module" items="${plugin.obsoletes}">
            ${module.groupId}/${module.artifactId}/${module.version}/${module.type}<br />
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">Installable:</th>
    <td>
    ${plugin.installable ? "<img alt='check' src='/console/images/checkmark._16_green.png' /> " : "<strong><font color='red'>X</font></strong> "}
    ${validation}
    </td>
  </tr>

</table>
<P>

<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>">
<table><tr>
<c:if test="${plugin.installable}">
<td valign="top">
    <input type="submit" value="Install" />
    <input type="hidden" name="configId" value="${configId}" />
    <input type="hidden" name="mode" value="viewForDownload-after" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
<td>
</c:if>
<td valign="top">
<input type="submit" value="Return" onclick="history.go(-1); return false;" />
</td></tr></table>
</form>