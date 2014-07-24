package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.FileUtil;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.base.Strings;


/**
 * This is a zk cluster setup strategy.
 */
public class ZookeeperSetupStrategy implements ClusterSetupStrategy {

    public static final String TEMPLATE_NAME = "zookeeper";
    private final ZookeeperClusterConfig config;
    private final ContainerManager containerManager;
    private final CommandRunner commandRunner;
    private final ProductOperation po;


    public ZookeeperSetupStrategy( final ZookeeperClusterConfig config, ProductOperation po,
                                   ContainerManager containerManager, CommandRunner commandRunner ) {
        this.config = config;
        this.po = po;
        this.containerManager = containerManager;
        this.commandRunner = commandRunner;
    }


    public static PlacementStrategyENUM getNodePlacementStrategy() {
        return PlacementStrategyENUM.ROUND_ROBIN;
    }


    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException {

        try {
            po.addLog( String.format( "Creating %d lxc containers...", config.getNumberOfNodes() ) );
            Set<Agent> agents = containerManager
                    .clone( TEMPLATE_NAME, config.getNumberOfNodes(), null, getNodePlacementStrategy() );
            config.setNodes( agents );

            po.addLog( "Lxc containers created successfully\nConfiguring cluster..." );

            Command configureClusterCommand = Commands.getConfigureClusterCommand( config.getNodes(),
                    ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                    prepareConfiguration( config.getNodes() ), ConfigParams.CONFIG_FILE_PATH.getParamValue() );

            commandRunner.runCommand( configureClusterCommand );

            if ( configureClusterCommand.hasSucceeded() ) {

                po.addLog( String.format( "Cluster configured\nStarting %s...", ZookeeperClusterConfig.PRODUCT_KEY ) );
                //start all nodes
                Command startCommand = Commands.getStartCommand( config.getNodes() );
                final AtomicInteger count = new AtomicInteger();
                commandRunner.runCommand( startCommand, new CommandCallback() {
                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command ) {
                        if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                            if ( count.incrementAndGet() == config.getNodes().size() ) {
                                stop();
                            }
                        }
                    }
                } );

                if ( count.get() == config.getNodes().size() ) {
                    po.addLog( String.format( "Starting %s succeeded\nDone", ZookeeperClusterConfig.PRODUCT_KEY ) );
                }
                else {
                    po.addLog( String.format( "Starting %s failed, %s, skipping...", ZookeeperClusterConfig.PRODUCT_KEY,
                            startCommand.getAllErrors() ) );
                }
            }
            else {
                throw new ClusterSetupException(
                        String.format( "Failed to configure cluster, %s", configureClusterCommand.getAllErrors() ) );
            }
        }
        catch ( LxcCreateException ex ) {
            throw new ClusterSetupException( ex.getMessage() );
        }
        catch ( ClusterConfigurationException e ) {
            throw new ClusterSetupException( e.getMessage() );
        }

        return config;
    }


    //temporary workaround until we get full configuration injection working
    public static String prepareConfiguration( Set<Agent> nodes ) throws ClusterConfigurationException {
        String zooCfgFile = FileUtil.getContent( "conf/zoo.cfg", ZookeeperSetupStrategy.class );

        if ( Strings.isNullOrEmpty( zooCfgFile ) ) {
            throw new ClusterConfigurationException( "Zoo.cfg resource is missing" );
        }

        zooCfgFile = zooCfgFile
                .replace( "$" + ConfigParams.DATA_DIR.getPlaceHolder(), ConfigParams.DATA_DIR.getParamValue() );

        /*
        1=zookeeper1:2888:3888
        2=zookeeper2:2888:3888
        3=zookeeper3:2888:3888
         */

        StringBuilder serversBuilder = new StringBuilder();
        int id = 0;
        for ( Agent agent : nodes ) {
            serversBuilder.append( ++id ).append( "=" ).append( agent.getHostname() )
                          .append( ConfigParams.PORTS.getParamValue() ).append( "\n" );
        }

        zooCfgFile = zooCfgFile.replace( "$" + ConfigParams.SERVERS.getPlaceHolder(), serversBuilder.toString() );


        return zooCfgFile;
    }
}
