package org.safehaus.subutai.impl.hadoop.operation;

import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;

import java.util.HashSet;
import java.util.UUID;

/**
 * Created by daralbaev on 08.04.14.
 */
public class Deletion {

	private HadoopImpl parent;

	public Deletion(HadoopImpl parent) {
		this.parent = parent;
	}

	public UUID execute(final String clusterName) {
		final ProductOperation po
				= parent.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
				String.format("Destroying cluster %s", clusterName));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				HadoopClusterConfig hadoopClusterConfig = parent.getDbManager().getInfo( HadoopClusterConfig.PRODUCT_KEY, clusterName, HadoopClusterConfig.class);
				if ( hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
					return;
				}

				po.addLog("Updating db...");
				if (parent.getDbManager().deleteInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName())) {
					po.addLogDone("Cluster info deleted from DB\nDone");
				} else {
					po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
				}

				po.addLog("Destroying lxc containers...");

				try {
					parent.getLxcManager().destroyLxcs(new HashSet<>( hadoopClusterConfig.getAllNodes()));
					po.addLog("Lxc containers successfully destroyed");
				} catch (LxcDestroyException ex) {
					po.addLog(String.format("%s, skipping...", ex.getMessage()));
				}
			}
		});

		return po.getId();
	}
}
