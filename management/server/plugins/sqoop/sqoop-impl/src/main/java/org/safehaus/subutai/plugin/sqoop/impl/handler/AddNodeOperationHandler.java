package org.safehaus.subutai.plugin.sqoop.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.sqoop.api.SetupType;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.CommandFactory;
import org.safehaus.subutai.plugin.sqoop.impl.CommandType;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;

import com.google.common.collect.Sets;


public class AddNodeOperationHandler extends AbstractOperationHandler<SqoopImpl>
{
    private final String hostname;


    public AddNodeOperationHandler( SqoopImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        productOperation = manager.getTracker().createProductOperation( SqoopConfig.PRODUCT_KEY,
                String.format( "Adding node %s to %s", ( hostname != null ? hostname : "" ), clusterName ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        SqoopConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        try
        {
            Agent agent;
            if ( config.getSetupType() == SetupType.OVER_HADOOP )
            {
                agent = setupHost( config );
            }
            else if ( config.getSetupType() == SetupType.WITH_HADOOP )
            {
                agent = addHost( config );
            }
            else
            {
                throw new ClusterSetupException( "No setup type" );
            }

            config.getNodes().add( agent );

            po.addLog( "Saving cluster info..." );
            manager.getPluginDao().saveInfo( SqoopConfig.PRODUCT_KEY, clusterName, config );
            po.addLog( "Saved cluster info" );

            po.addLogDone( null );
        }
        catch ( ClusterSetupException ex )
        {
            po.addLog( ex.getMessage() );
            po.addLogFailed( "Add worker node failed" );
        }
    }


    public Agent setupHost( SqoopConfig config ) throws ClusterSetupException
    {
        ProductOperation po = productOperation;

        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
        if ( agent == null )
        {
            throw new ClusterSetupException( "New node is not connected" );
        }

        //check if node is in the cluster
        if ( config.getNodes().contains( agent ) )
        {
            throw new ClusterSetupException( "Node already belongs to cluster" + clusterName );
        }

        po.addLog( "Checking prerequisites..." );

        //check installed subutai packages
        String s = CommandFactory.build( CommandType.LIST, null );
        Command checkInstalledCommand = manager.getCommandRunner().createCommand( new RequestBuilder( s ), Sets.newHashSet( agent )  );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            throw new ClusterSetupException( "Failed to check installed packages" );
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );
        boolean skipInstall = false;
        String hadoopPack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        if ( result.getStdOut().contains( CommandFactory.PACKAGE_NAME ) )
        {
            skipInstall = true;
            po.addLog( "Node already has Sqoop installed" );
        }
        else if ( !result.getStdOut().contains( hadoopPack ) )
        {
            throw new ClusterSetupException( "Node has no Hadoop installation" );
        }

        //install sqoop
        if ( !skipInstall )
        {
            po.addLog( "Installing Sqoop..." );
            s = CommandFactory.build( CommandType.INSTALL, null );
            Command installCommand = manager.getCommandRunner().createCommand( new RequestBuilder( s ).withTimeout( 180 ),  Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                po.addLog( "Installation succeeded" );
            }
            else
            {
                throw new ClusterSetupException( "Installation failed: " + installCommand.getAllErrors() );
            }
        }
        return agent;
    }


    public Agent addHost( SqoopConfig config )
    {

        return null;
    }
}