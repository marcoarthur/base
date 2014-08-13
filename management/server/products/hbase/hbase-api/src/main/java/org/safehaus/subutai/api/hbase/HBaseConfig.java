/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.hbase;


import java.util.Set;
import java.util.UUID;

import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.subutai.shared.protocol.ConfigBase;


/**
 * @author dilshat
 */
public class HBaseConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "HBase";
    private int numberOfNodes = 4;
    private UUID uuid;
    private String master;
    private Set<String> region;
    private Set<String> quorum;
    private String backupMasters;
    private String domainInfo;
    private Set<String> nodes;
    private String clusterName = "";
    private String hadoopNameNode;


    public HBaseConfig() {
        this.uuid = UUID.fromString( UUIDGenerator.getInstance().generateTimeBasedUUID().toString() );
    }


    public static String getProductKey() {
        return PRODUCT_KEY;
    }


    public String getHadoopNameNode() {
        return hadoopNameNode;
    }


    public void setHadoopNameNode( String hadoopNameNode ) {
        this.hadoopNameNode = hadoopNameNode;
    }


    public UUID getUuid() {
        return uuid;
    }


    public void setUuid( UUID uuid ) {
        this.uuid = uuid;
    }


    public void reset() {
        this.master = null;
        this.region = null;
        this.quorum = null;
        this.backupMasters = null;
        this.domainInfo = "";
        this.clusterName = "";
    }


    public String getMaster() {
        return master;
    }


    public void setMaster( String master ) {
        this.master = master;
    }


    public Set<String> getRegion() {
        return region;
    }


    public void setRegion( Set<String> region ) {
        this.region = region;
    }


    public Set<String> getQuorum() {
        return quorum;
    }


    public void setQuorum( Set<String> quorum ) {
        this.quorum = quorum;
    }


    public String getBackupMasters() {
        return backupMasters;
    }


    public void setBackupMasters( String backupMasters ) {
        this.backupMasters = backupMasters;
    }


    public String getDomainInfo() {
        return domainInfo;
    }


    public void setDomainInfo( String domainInfo ) {
        this.domainInfo = domainInfo;
    }


    public Set<String> getNodes() {
        return nodes;
    }


    public void setNodes( Set<String> nodes ) {
        this.nodes = nodes;
    }


    public int getNumberOfNodes() {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes ) {
        this.numberOfNodes = numberOfNodes;
    }


    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName( String clusterName ) {
        this.clusterName = clusterName;
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }


    @Override
    public String toString() {
        return "HBaseConfig{" +
                "numberOfNodes=" + numberOfNodes +
                ", uuid=" + uuid +
                ", master='" + master + '\'' +
                ", region=" + region +
                ", quorum=" + quorum +
                ", backupMasters='" + backupMasters + '\'' +
                ", domainInfo='" + domainInfo + '\'' +
                ", nodes=" + nodes +
                ", clusterName='" + clusterName + '\'' +
                ", hadoopNameNode='" + hadoopNameNode + '\'' +
                '}';
    }
}
