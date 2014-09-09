package org.safehaus.subutai.cli.commands;


import java.util.List;

import org.safehaus.subutai.peer.api.Peer;
import org.safehaus.subutai.peer.api.PeerManager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command( scope = "peer", name = "ls" )
public class ListCommand extends OsgiCommandSupport {

    private PeerManager peerManager;


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager ) {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        List<Peer> list = peerManager.peers();
        System.out.println("Found " + list.size() + " registered peers");
        for ( Peer peer : list ) {
            System.out.println( peer.getId() + " " + peer.getIp() + " " + peer.getName() );
        }
        return null;
    }
}