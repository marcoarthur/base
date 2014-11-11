package org.safehaus.subutai.plugin.hadoop.impl;


import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterConfigurationInterface;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public class ClusterConfiguration implements ClusterConfigurationInterface
{

    private static final Logger LOG = Logger.getLogger( ClusterConfiguration.class.getName() );
    private TrackerOperation po;
    private HadoopImpl hadoopManager;


    public ClusterConfiguration( final TrackerOperation operation, final HadoopImpl cassandraManager )
    {
        this.po = operation;
        this.hadoopManager = cassandraManager;
    }


    public void configureCluster( ConfigBase configBase, Environment environment ) throws ClusterConfigurationException    {

        HadoopClusterConfig config = ( HadoopClusterConfig ) configBase;
        Commands commands = new Commands( config );

        ContainerHost namenode = environment.getContainerHostByUUID( config.getNameNode() );
        ContainerHost jobtracker = environment.getContainerHostByUUID( config.getJobTracker() );
        ContainerHost secondaryNameNode = environment.getContainerHostByUUID( config.getSecondaryNameNode() );
        po.addLog( String.format( "Configuring cluster: %s", configBase.getClusterName() ) );

        // Clear configuration files
        for ( ContainerHost containerHost : environment.getContainers() )
        {
            executeCommandOnContainer( containerHost, Commands.getClearMastersCommand() );
            executeCommandOnContainer( containerHost, Commands.getClearSlavesCommand() );
        }

        // Configure NameNode
        for ( ContainerHost containerHost : environment.getContainers() )
        {
            executeCommandOnContainer( containerHost, commands.getSetMastersCommand( namenode.getHostname(), jobtracker.getHostname() ) );
        }

        // Configure JobTracker
        executeCommandOnContainer( jobtracker, commands.getConfigureJobTrackerCommand( jobtracker.getHostname() ) );


        // Configure Secondary NameNode
        executeCommandOnContainer( namenode, commands.getConfigureSecondaryNameNodeCommand( secondaryNameNode.getHostname() ) );


        // Configure DataNodes
        for ( UUID uuid : config.getDataNodes() )
        {
            executeCommandOnContainer( namenode,
                    commands.getConfigureDataNodesCommand( environment.getContainerHostByUUID( uuid ).getHostname() ) );
        }

        // Configure TaskTrackers
        for ( UUID uuid : config.getTaskTrackers() )
        {
            executeCommandOnContainer( jobtracker, commands.getConfigureTaskTrackersCommand( environment.getContainerHostByUUID( uuid ).getHostname() ) );
        }

        // Format NameNode
        executeCommandOnContainer( namenode, Commands.getFormatNameNodeCommand() );


        // Start Hadoop cluster
        executeCommandOnContainer( namenode, Commands.getStartNameNodeCommand() );
        executeCommandOnContainer( jobtracker, Commands.getStartJobTrackerCommand() );


        po.addLog( "Configuration is finished !" );

        config.setEnvironmentId( environment.getId() );
        hadoopManager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, configBase.getClusterName(), configBase );
        po.addLogDone( "Hadoop cluster data saved into database" );
    }

    private void executeCommandOnContainer( ContainerHost containerHost, String command ){
        try
        {
            containerHost.execute( new RequestBuilder( command ) );
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
    }
}