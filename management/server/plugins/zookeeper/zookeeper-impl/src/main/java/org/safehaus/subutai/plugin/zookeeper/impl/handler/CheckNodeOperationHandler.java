package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;


/**
 * Handles check node status operation
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final String lxcHostname;


    public CheckNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Checking node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }
        if ( !config.getNodes().contains( node ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }
        productOperation.addLog( "Checking node..." );
        Command checkCommand = Commands.getStatusCommand( node );
        manager.getCommandRunner().runCommand( checkCommand );
        NodeState state = NodeState.UNKNOWN;
        if ( checkCommand.hasCompleted() )
        {
            AgentResult result = checkCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( "is Running" ) )
            {
                state = NodeState.RUNNING;
            }
            else if ( result.getStdOut().contains( "is NOT Running" ) )
            {
                state = NodeState.STOPPED;
            }
        }

        if ( NodeState.UNKNOWN.equals( state ) )
        {
            productOperation.addLogFailed(
                    String.format( "Failed to check status of %s, %s", lxcHostname, checkCommand.getAllErrors() ) );
        }
        else
        {
            productOperation.addLogDone( String.format( "Node %s is %s", lxcHostname, state ) );
        }
    }
}
