/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.templateregistry;


import java.util.List;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;
import org.safehaus.subutai.shared.protocol.Disposable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 *
 */
public class TemplateRegistryForm extends CustomComponent implements Disposable {


    private final AgentManager agentManager;
    private final TemplateRegistryManager registryManager;
    private HierarchicalContainer container;
    private Tree templateTree;


    public TemplateRegistryForm( AgentManager agentManager, TemplateRegistryManager registryManager ) {
        setHeight( 100, Unit.PERCENTAGE );

        this.agentManager = agentManager;
        this.registryManager = registryManager;

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );

        container = new HierarchicalContainer();
        container.addContainerProperty( "value", Template.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        templateTree = new Tree( "Templates" );
        templateTree.setContainerDataSource( container );
        templateTree.setItemIconPropertyId( "icon" );
        templateTree.setImmediate( true );
        templateTree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator() {

            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId ) {
                String description = "";

                Item item = templateTree.getItem( itemId );
                if ( item != null ) {

                    Template template = ( Template ) item.getItemProperty( "value" ).getValue();
                    if ( template != null ) {
                        description = "Name: " + template.getTemplateName() + "<br>" + "Parent: " + template
                                .getParentTemplateName() + "<br>" + "Arch: " + template.getLxcArch() + "<br>"
                                + "Utsname: " + template.getLxcUtsname() + "<br>" + "Cfg Path: " + template
                                .getSubutaiConfigPath();
                    }
                }

                return description;
            }
        } );

        templateTree.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                Item item = templateTree.getItem( event.getProperty().getValue() );

                if ( item != null ) {

                    Notification.show( item.toString() );
                }
            }
        } );

        fillTemplateTree();

        horizontalSplit.setFirstComponent( templateTree );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();
        TabSheet commandsSheet = new TabSheet();
        commandsSheet.setStyleName( Runo.TABSHEET_SMALL );
        commandsSheet.setSizeFull();

        //        commandsSheet.addTab(new Cloner(lxcManager, agentTree), "Clone");
        //        commandsSheet.addTab(manager, managerTabCaption);
        //        commandsSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
        //            @Override
        //            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        //                TabSheet tabsheet = event.getTabSheet();
        //                String caption = tabsheet.getTab(event.getTabSheet().getSelectedTab()).getCaption();
        //                if (caption.equals(managerTabCaption)) {
        //                    manager.getLxcInfo();
        //                }
        //            }
        //        });
        verticalLayout.addComponent( commandsSheet );

        horizontalSplit.setSecondComponent( verticalLayout );
        setCompositionRoot( horizontalSplit );
    }


    private void fillTemplateTree() {
        container.removeAllItems();
        addChildren( registryManager.getTemplateTree(), Template.getMasterTemplate() );
    }


    private void addChildren( TemplateTree tree, Template currentTemplate ) {
        Item templateItem = container.addItem( currentTemplate.getTemplateName() );
        templateItem.getItemProperty( "value" ).setValue( currentTemplate );
        templateTree.setItemCaption( currentTemplate.getTemplateName(), currentTemplate.getTemplateName() );

        Template parent = tree.getParentTemplate( currentTemplate );
        if ( parent != null ) {
            container.setParent( currentTemplate.getTemplateName(), parent.getTemplateName() );
        }

        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( children == null || children.isEmpty() ) {
            container.setChildrenAllowed( currentTemplate.getTemplateName(), false );
        }
        else {
            container.setChildrenAllowed( currentTemplate.getTemplateName(), true );
            for ( Template child : children ) {

                addChildren( tree, child );
            }

            templateTree.expandItem( currentTemplate.getTemplateName() );
        }
    }


    public void dispose() {

    }
}
