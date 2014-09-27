package org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker;


import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class StartJobTrackerOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    public StartJobTrackerOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Starting JobTracker in %s", clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( hadoopClusterConfig.getJobTracker() == null )
        {
            productOperation.addLogFailed( String.format( "JobTracker on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( hadoopClusterConfig.getJobTracker().getHostname() );
        if ( node == null )
        {
            productOperation.addLogFailed( "JobTracker is not connected" );
            return;
        }

        Command startCommand = Commands.getJobTrackerCommand( node, "start" );
        manager.getCommandRunner().runCommand( startCommand );
        Command statusCommand = Commands.getJobTrackerCommand( node, "status" );
        manager.getCommandRunner().runCommand( statusCommand );

        AgentResult result = statusCommand.getResults().get( node.getUuid() );

        NodeState nodeState = NodeState.UNKNOWN;
        if ( statusCommand.hasCompleted() )
        {
            if ( result.getStdOut() != null && result.getStdOut().contains( "JobTracker" ) )
            {
                String[] array = result.getStdOut().split( "\n" );

                for ( String status : array )
                {
                    if ( status.contains( "JobTracker" ) )
                    {
                        String temp = status.replaceAll( "JobTracker is ", "" );
                        if ( temp.toLowerCase().contains( "not" ) )
                        {
                            nodeState = NodeState.STOPPED;
                        }
                        else
                        {
                            nodeState = NodeState.RUNNING;
                        }
                    }
                }
            }
        }

        if ( NodeState.RUNNING.equals( nodeState ) )
        {
            productOperation.addLogDone( String.format( "JobTracker on %s started", node.getHostname() ) );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Failed to start JobTracker %s. %s", node.getHostname(),
                    startCommand.getAllErrors() ) );
        }
    }
}