package org.safehaus.subutai.plugin.cassandra.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Cassandra extends ApiBase<CassandraClusterConfig>
{

    UUID startCluster( String clusterName );

    UUID checkCluster( String clusterName );

    UUID stopCluster( String clusterName );

    UUID startService( String clusterName, String lxchostname );

    UUID stopService( String clusterName, String lxchostname );

    UUID statusService( String clusterName, String lxchostname );

    UUID addNode( String clusterName, String lxchostname, String nodetype );

    UUID destroyNode( String clusterName, String lxchostname, String nodetype );

    UUID checkNode( String clustername, String lxchostname );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, CassandraClusterConfig config,
                                                         ProductOperation po );

    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( CassandraClusterConfig config );
}