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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ page import="javax.portlet.RenderRequest"%>
<portlet:defineObjects/>

<%
        String childs = (String)renderRequest.getPortletSession().getAttribute("jndiTree");     
        renderRequest.getPortletSession().removeAttribute("jndiTree");
%>

<script>
        dojo.require("dojo.widget.*");
        dojo.require("dojo.widget.TreeV3");
        dojo.require("dojo.widget.TreeNodeV3");
        dojo.require("dojo.widget.TreeBasicControllerV3");
        dojo.require("dojo.widget.TreeSelectorV3");
        dojo.require("dojo.widget.TreeEmphasizeOnSelect");
        dojo.require("dojo.widget.TreeToggleOnSelect");
        
        dojo.hostenv.writeIncludes();

        dojo.addOnLoad(function(){

            var tree = dojo.widget.manager.getWidgetById('tree');

            var treeNodes = <%= childs %>;          

            tree.setChildren(treeNodes);

        });



var lastFound = '';
var doCheck = false;;
function check(curr,last)
{
        var cr=curr.split(".");
        var ls=last.split(".");
        for(var i =0;i<cr.length;i++)
                if(parseInt(cr[i]) < parseInt(ls[i]))
                        return false;
        return true;
}
function findNext(children, str)
{
        for(var child = 0;child < children.length;child++)
        {
                if(doCheck)
                {
                        if(!check(children[child].widgetId,lastFound))
                                continue;
                        else
                                if(children[child].widgetId == lastFound)doCheck = false;
                }
                if(children[child].title.indexOf(str) != -1 && children[child].widgetId != lastFound && !doCheck)
                        return children[child].widgetId;
                if(children[child].children){
                        var ret=findNext(children[child].children,str);
                        if(ret)
                                return ret;
                }
        }       
        return ;
}

function select(node)
{
        var nodes = node.split(".");
        var curr ="";
        for(nod in nodes)
        {
                if(curr == "")curr=curr+nodes[nod];
                else curr=curr+"."+nodes[nod];
                if(dojo.widget.byId(curr).state != "LOADED" && dojo.widget.byId(curr).children.length != 0)
                        dojo.widget.byId(curr).setChildren(dojo.widget.byId(curr).children);
                dojo.widget.byId('controller').expandToLevel(dojo.widget.byId(curr),1);
        }
        dojo.widget.byId('selector').select(dojo.widget.byId(node));
}

function findInSelected(selectedNodes)
{
        var search = document.getElementById("searchText").value;
        if(search == "")
        {
                alert('No Text to search');
                return;
        }
        var v= findNext(selectedNodes,search);
        if(v)
        {
                dojo.widget.byId('selector').deselectAll();
                select(v);
                document.getElementById("findNext").disabled=false;
                lastFound =  v;
                doCheck = true;
        }
        else{
                alert('Nothing found');
                lastFound =  '';
                doCheck = false;
        }
}
function textChange(){
        document.getElementById("findNext").disabled=true;
        lastFound = '';
        doCheck = false;
}

var lastSearchOn = '';
function search(){
        document.body.style.cursor = "wait";
        lastFound = '';
        doCheck = false;
        lastSearchOn = [];
        var nodes;
        if(document.getElementById("inSelected").checked){
                nodes = dojo.widget.byId('selector').selectedNodes;
                if(nodes == '')
                {
                        alert('No Node selected to search');
                }
                else{
                        for(obj in nodes)
                                lastSearchOn.push(nodes[obj]);  
                        findInSelected(nodes);
                }
        }
        else{ 
                nodes = dojo.widget.byId('tree').children;
                lastSearchOn = dojo.widget.byId('tree').children;
                findInSelected(nodes);
        }
        document.body.style.cursor = '';
}
function searchContinue(){
        document.body.style.cursor = "wait";
        findInSelected(lastSearchOn);
        document.body.style.cursor = '';
}

</script>

<TABLE cellpadding="1" cellspacing="1" border="1">
<tr><td>
<b>Search Text:</b> <input type="text" id="searchText" onChange="javascript:textChange()"/>
<input type="button" value="Find" onClick="javascript:search()"/>
<input type="button" id="findNext" value="Find Next" onClick="javascript:searchContinue()" disabled=true />
Search only selected:<input type="checkbox" id="inSelected" onChange="javascript:textChange()"/>
</td></tr>
</table>
<br/>


<div dojoType="TreeBasicControllerV3" widgetId="controller"></div>      
<div dojoType="TreeSelectorV3" widgetId="selector"></div>       
<div dojoType="TreeEmphasizeOnSelect" selector="selector"></div>
<div dojoType="TreeToggleOnSelect" selector="selector" controller="controller"></div>
<div dojoType="TreeDocIconExtension"  widgetId="iconcontroller" templateCssPath="${pageContext.request.contextPath}/TreeDocIcon.css"></div>
<div dojoType="TreeV3" listeners="controller;selector;iconcontroller" widgetId='tree' allowedMulti='false'></div>
