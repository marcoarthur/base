package org.safehaus.subutai.plugin.shark.impl;


import com.google.common.base.Preconditions;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.shark.api.SetupType;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.handler.ActualizeMasterIpOperationHandler;
import org.safehaus.subutai.plugin.shark.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.shark.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.shark.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.shark.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.spark.api.Spark;


public class SharkImpl implements Shark
{

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Spark sparkManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;


    public SharkImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
                      Spark sparkManager )
    {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.sparkManager = sparkManager;

        Commands.init( commandRunner );
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public Spark getSparkManager()
    {
        return sparkManager;
    }


    public DbManager getDbManager()
    {
        return dbManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    @Override
    public UUID installCluster( final SharkClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<SharkClusterConfig> getClusters()
    {
        return dbManager.getInfo( SharkClusterConfig.PRODUCT_KEY, SharkClusterConfig.class );
    }


    @Override
    public SharkClusterConfig getCluster( String clusterName )
    {
        return dbManager.getInfo( SharkClusterConfig.PRODUCT_KEY, clusterName, SharkClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID actualizeMasterIP( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new ActualizeMasterIpOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( ProductOperation po, SharkClusterConfig config,
                                                         Environment environment )
    {
        if ( config.getSetupType() == SetupType.OVER_SPARK )
        {
            return new SetupStrategyOverSpark( this, config, po );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP_SPARK )
        {
            SetupStrategyWithHadoopSpark s = new SetupStrategyWithHadoopSpark( this, config, po );
            s.setEnvironment( environment );
            return s;
        }
        return null;
    }


}

