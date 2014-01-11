/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class Step2 extends Panel {

    public Step2(final Wizard wizard) {

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard<br>"
                + " 1) <font color=\"#f14c1a\"><strong>Config Servers and Routers</strong></font><br>"
                + " 2) Replica Set Configurations");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

        final TextField clusterNameTxtFld = new TextField("Enter cluster name");
        clusterNameTxtFld.setInputPrompt("Cluster name");
        clusterNameTxtFld.setRequired(true);
        clusterNameTxtFld.setMaxLength(20);
        clusterNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setClusterName(event.getProperty().getValue().toString().trim());
            }
        });

        mainContent.addComponent(clusterNameTxtFld);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as config servers<br>"
                + "(Recommended 3 nodes, choose 1 or 3 nodes)</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        mainContent.addComponent(configServersLabel);

        final TwinColSelect routersColSel = new TwinColSelect("", new ArrayList<Agent>());
        final TwinColSelect configServersColSel = new TwinColSelect("", new ArrayList<Agent>());

        configServersColSel.setItemCaptionPropertyId("hostname");
        configServersColSel.setRows(7);
        configServersColSel.setNullSelectionAllowed(true);
        configServersColSel.setMultiSelect(true);
        configServersColSel.setImmediate(true);
        configServersColSel.setLeftColumnCaption("Available Nodes");
        configServersColSel.setRightColumnCaption("Config Servers");
        configServersColSel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        configServersColSel.setRequired(true);
        configServersColSel.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                wizard.getConfig().setConfigServers(agentList);
                //clean 
                wizard.getConfig().getRouterServers().removeAll(wizard.getConfig().getConfigServers());
                wizard.getConfig().getDataNodes().removeAll(wizard.getConfig().getConfigServers());
            }
        });

        mainContent.addComponent(configServersColSel);

        Label routersLabel = new Label("<strong>Choose hosts that will act as routers<br>"
                + "(Provide at least 2 servers)</strong>");
        routersLabel.setContentMode(Label.CONTENT_XHTML);
        mainContent.addComponent(routersLabel);

        routersColSel.setItemCaptionPropertyId("hostname");
        routersColSel.setRows(7);
        routersColSel.setNullSelectionAllowed(true);
        routersColSel.setMultiSelect(true);
        routersColSel.setImmediate(true);
        routersColSel.setLeftColumnCaption("Available Nodes");
        routersColSel.setRightColumnCaption("Routers");
        routersColSel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        routersColSel.setRequired(true);
        routersColSel.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                wizard.getConfig().setRouterServers(agentList);
                //clean 
                wizard.getConfig().getConfigServers().removeAll(wizard.getConfig().getRouterServers());
                wizard.getConfig().getDataNodes().removeAll(wizard.getConfig().getRouterServers());
            }
        });

        mainContent.addComponent(routersColSel);

        grid.addComponent(mainContent, 3, 0, 9, 9);
        grid.setComponentAlignment(mainContent, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                wizard.getConfig().setClusterName(clusterNameTxtFld.getValue().toString().trim());
                wizard.getConfig().setConfigServers((Set<Agent>) configServersColSel.getValue());
                wizard.getConfig().setRouterServers((Set<Agent>) routersColSel.getValue());

                if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Please provide cluster name");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getConfigServers())) {
                    show("Please add config servers");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getRouterServers())) {
                    show("Please add routers");
                } else if (wizard.getDbManager().getMongoClusterInfo(wizard.getConfig().getClusterName()) != null) {
                    show(MessageFormat.format("Cluster with name {0} already exists", wizard.getConfig().getClusterName()));
                } else if (wizard.getConfig().getConfigServers().size() != 1
                        && wizard.getConfig().getConfigServers().size() != 3) {
                    show("Please, select 1 or 3 nodes as config servers");
                } else {
                    wizard.next();
                }
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        content.addComponent(grid);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);
        content.addComponent(buttons);

        addComponent(content);

        routersColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, wizard.getConfig().getSelectedAgents()));
        configServersColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, wizard.getConfig().getSelectedAgents()));

        //set values if this is a second visit
        clusterNameTxtFld.setValue(wizard.getConfig().getClusterName());

        configServersColSel.setValue(wizard.getConfig().getConfigServers());
        routersColSel.setValue(wizard.getConfig().getRouterServers());
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
