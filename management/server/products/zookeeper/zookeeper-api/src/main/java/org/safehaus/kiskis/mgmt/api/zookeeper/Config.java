/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.zookeeper;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class Config {

    public static final String PRODUCT_KEY = "Zookeeper";
    private String clusterName = "";
    private String zkName = "";
    private int numberOfNodes = 3;

    private Set<Agent> nodes;

    public String getZkName() {
        return zkName;
    }

    public void setZkName(String zkName) {
        this.zkName = zkName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", zkName=" + zkName + ", numberOfNodes=" + numberOfNodes + ", nodes=" + nodes + '}';
    }

}
