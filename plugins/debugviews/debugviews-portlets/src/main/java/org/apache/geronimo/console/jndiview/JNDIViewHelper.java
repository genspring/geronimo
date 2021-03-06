package org.apache.geronimo.console.jndiview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.geronimo.console.util.Tree;
import org.apache.geronimo.console.util.TreeEntry;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.java.RootContext;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RemoteProxy
public class JNDIViewHelper {

    private static final Logger log = LoggerFactory.getLogger(JNDIViewHelper.class);
    
    private static final String NO_CHILD = "none";
    
    private static final String NOT_LEAF_TYPE = "not_leaf";
    
    private static final String NORMAL_TYPE = "normal";

    private void buildEJBModule(Kernel kernel, List arryList, Hashtable entApp) throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "EJBModule");
        Set setEnt = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {
            AbstractName gb = (AbstractName) iterator.next();
            TreeEntry ejbModule = new TreeEntry(gb.getNameProperty("name"), NORMAL_TYPE);

            if (gb.getNameProperty("J2EEApplication") == null || gb.getNameProperty("J2EEApplication").equals("null")) {
                TreeEntry treeEnt = (TreeEntry) entApp.get("EJBModule");
                treeEnt.addChild(ejbModule);
            } else {
                TreeEntry treeEnt = (TreeEntry) entApp.get(gb.getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findEntry("EJBModule");
                treeEnt.addChild(ejbModule);
            }
            Map queryEnt = new HashMap();
            TreeEntry entityBean = new TreeEntry("EntityBeans", NOT_LEAF_TYPE);
            ejbModule.addChild(entityBean);
            queryEnt.put("j2eeType", "EntityBean");
            queryEnt.put("EJBModule", gb.getNameProperty("name"));
            queryEnt.put("J2EEApplication", gb.getNameProperty("J2EEApplication"));
            Set setEntBean = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, queryEnt));

            Iterator iterEntBean = setEntBean.iterator();

            while (iterEntBean.hasNext()) {
                AbstractName gbEntBean = (AbstractName) iterEntBean.next();
                TreeEntry beanNode = new TreeEntry(gbEntBean.getNameProperty("name"), NORMAL_TYPE);
                entityBean.addChild(beanNode);
                Context jndi = (Context) kernel.getAttribute(gbEntBean, "componentContext");
                buildContext(beanNode, jndi, "java:comp");
            }

            queryEnt = new HashMap();
            TreeEntry sessionBean = new TreeEntry("SessionBeans", NOT_LEAF_TYPE);
            ejbModule.addChild(sessionBean);
            queryEnt.put("j2eeType", "StatelessSessionBean");
            queryEnt.put("EJBModule", gb.getNameProperty("name"));
            queryEnt.put("J2EEApplication", gb.getNameProperty("J2EEApplication"));
            Set setSessionBean = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, queryEnt));

            Iterator iterSessionBean = setSessionBean.iterator();

            while (iterSessionBean.hasNext()) {
                AbstractName gbSessionBean = (AbstractName) iterSessionBean.next();
                TreeEntry beanNode = new TreeEntry(gbSessionBean.getNameProperty("name"), NORMAL_TYPE);
                sessionBean.addChild(beanNode);
                Context jndi = (Context) kernel.getAttribute(gbSessionBean, "componentContext");
                buildContext(beanNode, jndi, "java:comp");
            }

            queryEnt = new HashMap();
            queryEnt.put("j2eeType", "StatefullSessionBean");
            queryEnt.put("EJBModule", gb.getNameProperty("name"));
            queryEnt.put("J2EEApplication", gb.getNameProperty("J2EEApplication"));
            setSessionBean = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, queryEnt));

            iterSessionBean = setSessionBean.iterator();

            while (iterSessionBean.hasNext()) {
                AbstractName gbSessionBean = (AbstractName) iterSessionBean.next();
                TreeEntry beanNode = new TreeEntry(gbSessionBean.getNameProperty("name"),NORMAL_TYPE);
                sessionBean.addChild(beanNode);
                Context jndi = (Context) kernel.getAttribute(gbSessionBean, "componentContext");
                buildContext(beanNode, jndi, "java:comp");
            }
        }
    }

    private void buildWebModule(Kernel kernel, List arryList, Hashtable entApp) throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "WebModule");
        Set setEnt = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {

            AbstractName gb = (AbstractName) iterator.next();
            TreeEntry webModule = new TreeEntry(gb.getNameProperty("name"), NORMAL_TYPE);

            if (gb.getNameProperty("J2EEApplication") == null || gb.getNameProperty("J2EEApplication").equals("null")) {
                TreeEntry treeEnt = (TreeEntry) entApp.get("WebModule");
                treeEnt.addChild(webModule);
            } else {
                TreeEntry treeEnt = (TreeEntry) entApp.get(gb.getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findEntry("WebModule");
                treeEnt.addChild(webModule);
            }

            Map map = (Map) kernel.getAttribute(gb, "componentContext");
            String[] servlets = (String[]) kernel.getAttribute(gb, "servlets");

            TreeEntry servletsNode = null;
            TreeEntry jspNode = null;

            for (int i = 0; i < servlets.length; i++) {
                String servlet = servlets[i];
                servlet = servlet.substring(servlet.indexOf("name=") + 5);
                if (servlet.indexOf(",") != -1)
                    servlet = servlet.substring(0, servlet.indexOf(","));
                if (!servlet.equals("jsp") && servlet.startsWith("jsp.")) {
                    if (servletsNode == null) {
                        servletsNode = new TreeEntry("Servlets", NOT_LEAF_TYPE);
                        webModule.addChild(servletsNode);
                    }
                    if (jspNode == null) {
                        jspNode = new TreeEntry("JSP", NOT_LEAF_TYPE);
                        servletsNode.addChild(jspNode);
                    }
                    jspNode.addChild(new TreeEntry(servlet.substring(4), NORMAL_TYPE));
                } else if (!servlet.equals("jsp")) {
                    if (servletsNode == null) {
                        servletsNode = new TreeEntry("Servlets", NOT_LEAF_TYPE);
                        webModule.addChild(servletsNode);
                    }
                    servletsNode.addChild(new TreeEntry(servlet, NORMAL_TYPE));
                }
            }
            Iterator contexts = map.keySet().iterator();
            while (contexts.hasNext())
                webModule.addChild(new TreeEntry("java:comp/" + contexts.next(), NORMAL_TYPE));
        }
    }

    private void buildResourceModule(Kernel kernel, List arryList, Hashtable entApp) throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "ResourceAdapterModule");
        Set setEnt = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {
            AbstractName gb = (AbstractName) iterator.next();
            String resourceModule = gb.getNameProperty("name");
            if (gb.getNameProperty("J2EEApplication") == null || gb.getNameProperty("J2EEApplication").equals("null")) {
                TreeEntry treeEnt = (TreeEntry) entApp.get("ResourceAdapterModule");
                treeEnt.addChild(new TreeEntry(resourceModule, NORMAL_TYPE));
            } else {
                TreeEntry treeEnt = (TreeEntry) entApp.get(gb.getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findEntry("ResourceAdapterModule");
                treeEnt.addChild(new TreeEntry(resourceModule, NORMAL_TYPE));
            }
        }
    }

    private void buildAppClientModule(Kernel kernel, List arryList, Hashtable entApp) throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "AppClientModule");
        Set setEnt = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {
            AbstractName gb = (AbstractName) iterator.next();
            String appClienteModule = gb.getNameProperty("name");
            if (gb.getNameProperty("J2EEApplication") == null || gb.getNameProperty("J2EEApplication").equals("null")) {
                TreeEntry treeEnt = (TreeEntry) entApp.get("AppClientModule");
                treeEnt.addChild(new TreeEntry(appClienteModule, NORMAL_TYPE));
            } else {
                TreeEntry treeEnt = (TreeEntry) entApp.get(gb.getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findEntry("AppClientModule");
                treeEnt.addChild(new TreeEntry(appClienteModule, NORMAL_TYPE));
            }
        }
    }

    public void buildContext(TreeEntry node, Context compCtx, String nodeCurr) {
        Context oldCtx = RootContext.getComponentContext();
        RootContext.setComponentContext(compCtx);
        try {
            InitialContext ctx = new InitialContext();
            buildContextSub(node, (Context) ctx.lookup("java:comp"), nodeCurr);
        } catch (Exception e) {
            log.warn("Error looking up java:comp context", e);
        } finally {
            RootContext.setComponentContext(oldCtx);
        }
    }

    private void buildContextSub(TreeEntry node, Context ctx, String nodeCurr) {
        try {
            NamingEnumeration enumName = ctx.list("");
            while (enumName.hasMoreElements()) {
                NameClassPair pair = (NameClassPair) enumName.next();
                Object obj = null;
                try {
                    obj = ctx.lookup(pair.getName());
                } catch (NamingException e) {
                    // ignore.... not a context
                }
                if (obj instanceof Context) {
                    buildContextSub(node, (Context) obj, nodeCurr + "/" + pair.getName());
                } else {
                    node.addChild(new TreeEntry(nodeCurr + "/" + pair.getName(), NORMAL_TYPE));
                }
            }
        } catch (Exception e) {
            log.warn("Error listing context", e);
        }
    }

    private void buildGlobal(TreeEntry tree, Context context, String parent) throws Exception {
        if (parent == null)
            parent = "";
        if (!parent.equals(""))
            parent = parent + "/";
        javax.naming.NamingEnumeration enum1 = context.list("");
        while (enum1.hasMoreElements()) {
            javax.naming.NameClassPair pair = (javax.naming.NameClassPair) enum1.next();
            Object obj = null;
            try {
                obj = context.lookup(pair.getName());
            } catch (NamingException e) {
                // ignore.... it wasn't a context
            }
            if (obj instanceof Context) {
                buildGlobal(tree, (Context) obj, parent + pair.getName());
            } else {
                tree.addChild(new TreeEntry(parent + pair.getName(), NORMAL_TYPE));
            }
        }
    }

    @RemoteMethod
    public Tree getTrees() throws Exception {
        Tree jndiTree = new Tree(null, "name");

        List arryList = new ArrayList();
        Hashtable entApp = new Hashtable();

        TreeEntry treeGlobal = new TreeEntry("Global Context", NOT_LEAF_TYPE);
        jndiTree.addItem(treeGlobal);
        buildGlobal(treeGlobal, org.apache.xbean.naming.global.GlobalContextManager.getGlobalContext(), "");

        TreeEntry tree = new TreeEntry("Enterprise Applications", NOT_LEAF_TYPE);
        jndiTree.addItem(tree);

        TreeEntry treeMod = new TreeEntry("EJBModule", NOT_LEAF_TYPE);
        entApp.put("EJBModule", treeMod);
        jndiTree.addItem(treeMod);

        treeMod = new TreeEntry("WebModule", NOT_LEAF_TYPE);
        entApp.put("WebModule", treeMod);
        jndiTree.addItem(treeMod);

        treeMod = new TreeEntry("ResourceAdapterModule", NOT_LEAF_TYPE);
        entApp.put("ResourceAdapterModule", treeMod);
        jndiTree.addItem(treeMod);

        treeMod = new TreeEntry("AppClientModule", NOT_LEAF_TYPE);
        entApp.put("AppClientModule", treeMod);
        jndiTree.addItem(treeMod);

        org.apache.geronimo.kernel.Kernel kernel = org.apache.geronimo.kernel.KernelRegistry.getSingleKernel();

        Set setEnt = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, Collections.EMPTY_MAP,
                org.apache.geronimo.j2ee.management.impl.J2EEApplicationImpl.class.getName()));
        Iterator iterator = setEnt.iterator();
        while (iterator.hasNext()) {
            org.apache.geronimo.gbean.AbstractName gb = (org.apache.geronimo.gbean.AbstractName) iterator.next();
            TreeEntry curr = new TreeEntry(gb.getNameProperty("name"), NORMAL_TYPE);
            tree.addChild(curr);
            entApp.put(gb.getNameProperty("name"), curr);

            TreeEntry temp = new TreeEntry("EJBModule", NOT_LEAF_TYPE);
            curr.addChild(temp);

            temp = new TreeEntry("WebModule", NOT_LEAF_TYPE);
            curr.addChild(temp);

            temp = new TreeEntry("ResourceAdapterModule", NOT_LEAF_TYPE);
            curr.addChild(temp);

            temp = new TreeEntry("AppClientModule", NOT_LEAF_TYPE);
            curr.addChild(temp);
        }

        buildEJBModule(kernel, jndiTree.getItems(), entApp);
        buildWebModule(kernel, jndiTree.getItems(), entApp);
        buildResourceModule(kernel, jndiTree.getItems(), entApp);
        buildAppClientModule(kernel, jndiTree.getItems(), entApp);

        check_no_child(jndiTree.getItems());
        return jndiTree;
    }

    private static void check_no_child(List<TreeEntry> list) {
        List<TreeEntry> children;
        for (TreeEntry entry : list) {
            children = entry.getChildren();
            if (children.size() > 0)
                check_no_child(children);
            else if (entry.getType().equals(NOT_LEAF_TYPE))
                children.add(new TreeEntry(NO_CHILD, NORMAL_TYPE));
        }
    }
}
