package org.safehaus.subutai.core.peer.ui.forms;


import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerStatus;
import org.safehaus.subutai.core.peer.ui.PeerManagerPortalModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;


public class PeerRegisterForm extends CustomComponent
{

    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,
    "movingGuides":false,"snappingDistance":10} */
    private static final Logger LOG = LoggerFactory.getLogger( PeerRegisterForm.class.getName() );
    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @AutoGenerated
    private AbsoluteLayout mainLayout;
    @AutoGenerated
    private Table peersTable;
    @AutoGenerated
    private Button showPeersButton;
    @AutoGenerated
    private Button registerRequestButton;
    private Button registerViaRestButton;
    @AutoGenerated
    private Label ID;
    @AutoGenerated
    private TextField idTextField;
    @AutoGenerated
    private TextField ipTextField;
    @AutoGenerated
    private TextField servicePortTextField;

    private PeerManagerPortalModule peerManagerPortalModule;


    /**
     * The constructor should first build the main layout, set the composition root and then do any custom
     * initialization. <p/> The constructor will not be automatically regenerated by the visual editor.
     */
    public PeerRegisterForm( final PeerManagerPortalModule peerManagerPortalModule )
    {
        buildMainLayout();
        setCompositionRoot( mainLayout );

        // TODO add user code here
        this.peerManagerPortalModule = peerManagerPortalModule;
    }


    @AutoGenerated
    private AbsoluteLayout buildMainLayout()
    {
        // common part: create layout
        mainLayout = new AbsoluteLayout();
        mainLayout.setImmediate( false );
        mainLayout.setWidth( "100%" );
        mainLayout.setHeight( "100%" );

        // top-level component properties
        setWidth( "100.0%" );
        setHeight( "100.0%" );

        // peerRegisterLayout
        final AbsoluteLayout peerRegisterLayout = buildAbsoluteLayout_2();
        mainLayout.addComponent( peerRegisterLayout, "top:20.0px;right:0.0px;bottom:-20.0px;left:0.0px;" );

        return mainLayout;
    }


    @AutoGenerated
    private AbsoluteLayout buildAbsoluteLayout_2()
    {

        // common part: create layout
        AbsoluteLayout absoluteLayout = new AbsoluteLayout();
        absoluteLayout.setImmediate( false );
        absoluteLayout.setWidth( "100.0%" );
        absoluteLayout.setHeight( "100.0%" );

        // peerRegistration
        final Label peerRegistration = new Label();
        peerRegistration.setImmediate( false );
        peerRegistration.setWidth( "-1px" );
        peerRegistration.setHeight( "-1px" );
        peerRegistration.setValue( "Peer registration" );
        absoluteLayout.addComponent( peerRegistration, "top:0.0px;left:20.0px;" );

        // tags label
        final Label servicePort = new Label();
        servicePort.setImmediate( false );
        servicePort.setWidth( "-1px" );
        servicePort.setHeight( "-1px" );
        servicePort.setValue( "Service Port:" );
        absoluteLayout.addComponent( servicePort, "top:36.0px;left:20.0px;" );

        // servicePortTextField
        servicePortTextField = new TextField();
        servicePortTextField.setImmediate( false );
        servicePortTextField.setWidth( "-1px" );
        servicePortTextField.setHeight( "-1px" );
        servicePortTextField.setMaxLength( 256 );
        absoluteLayout.addComponent( servicePortTextField, "top:36.0px;left:150.0px;" );

        // IP
        final Label IP = new Label();
        IP.setImmediate( false );
        IP.setWidth( "-1px" );
        IP.setHeight( "-1px" );
        IP.setValue( "IP" );
        absoluteLayout.addComponent( IP, "top:80.0px;left:20.0px;" );

        // ipTextField
        ipTextField = new TextField();
        ipTextField.setImmediate( false );
        ipTextField.setWidth( "-1px" );
        ipTextField.setHeight( "-1px" );
        ipTextField.setMaxLength( 15 );
        absoluteLayout.addComponent( ipTextField, "top:80.0px;left:150.0px;" );

        // registerRequestButton
        registerRequestButton = createRegisterButton();
        absoluteLayout.addComponent( registerRequestButton, "top:160.0px;left:20.0px;" );
        registerRequestButton = createRegisterButton();

        // showPeersButton
        showPeersButton = createShowPeersButton();
        absoluteLayout.addComponent( showPeersButton, "top:234.0px;left:20.0px;" );

        // peersTable
        peersTable = new Table();
        peersTable.setCaption( "Peers" );
        peersTable.setImmediate( false );
        peersTable.setWidth( "800px" );
        peersTable.setHeight( "283px" );
        absoluteLayout.addComponent( peersTable, "top:294.0px;left:20.0px;" );

        return absoluteLayout;
    }


    private Button createShowPeersButton()
    {
        showPeersButton = new Button();
        showPeersButton.setCaption( "Show peers" );
        showPeersButton.setImmediate( false );
        showPeersButton.setWidth( "-1px" );
        showPeersButton.setHeight( "-1px" );

        showPeersButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                populateData();
                peersTable.refreshRowCache();
            }
        } );

        return showPeersButton;
    }


    private void populateData()
    {
        List<Peer> peers = peerManagerPortalModule.getPeerManager().peers();
        peersTable.removeAllItems();
        peersTable.addContainerProperty( "Name", String.class, null );
        peersTable.addContainerProperty( "IP", String.class, null );
        peersTable.addContainerProperty( "Status", PeerStatus.class, null );
        peersTable.addContainerProperty( "ActionsAdvanced", PeerManageActionsComponent.class, null );

        for ( final Peer peer : peers )
        {
            if ( peer == null || peer.getStatus() == null )
            {
                continue;
            }
            PeerManageActionsComponent.PeerManagerActionsListener listener =
                    new PeerManageActionsComponent.PeerManagerActionsListener()

                    {
                        @Override
                        public void OnPositiveButtonTrigger( final Peer peer )
                        {
                            switch ( peer.getStatus() )
                            {
                                case REQUESTED:
                                    peer.setStatus( PeerStatus.APPROVED );
                                    Peer selfPeer = getPeerJsonRepresentation( "127.0.0.1", "8181" );
                                    selfPeer.setStatus( PeerStatus.APPROVED );
                                    updatePeerOnAnother( selfPeer, peer.getIp(), "8181" );
                                    break;
                                case REGISTERED:
                                    peer.setStatus( PeerStatus.BLOCKED );
                                    updatePeerOnAnother( peer, peer.getIp(), "8181" );
                                    break;
                                case BLOCKED:
                                    peer.setStatus( PeerStatus.REGISTERED );
                                    updatePeerOnAnother( peer, peer.getIp(), "8181" );
                                    break;
                            }
                            Property property = peersTable.getItem( peer.getId() ).getItemProperty( "Status" );
                            property.setValue( peer.getStatus() );
                            peerManagerPortalModule.getPeerManager().update( peer );
                        }


                        @Override
                        public void OnNegativeButtonTrigger( final Peer peer )
                        {
                            Peer selfPeer = getPeerJsonRepresentation( "127.0.0.1", "8181" );
                            switch ( peer.getStatus() )
                            {
                                case REJECTED:
                                case APPROVED:
                                case BLOCKED:
                                case BLOCKED_PEER:
                                case REQUEST_SENT:
                                    peerManagerPortalModule.getPeerManager().unregister( peer.getId().toString() );
                                    peersTable.removeItem( peer.getId() );
                                    unregisterPeerFromAnother( selfPeer, peer.getIp(), "8181" );
                                    break;
                                case REQUESTED:
                                    peer.setStatus( PeerStatus.REJECTED );
                                    peerManagerPortalModule.getPeerManager().update( peer );
                                    Property property = peersTable.getItem( peer.getId() ).getItemProperty( "Status" );
                                    property.setValue( peer.getStatus() );
                                    selfPeer.setStatus( PeerStatus.BLOCKED_PEER );
                                    updatePeerOnAnother( selfPeer, peer.getIp(), "8181" );
                                    break;
                            }
                        }
                    };
            PeerManageActionsComponent component = new PeerManageActionsComponent( peer, listener );
            peersTable.addItem( new Object[] { peer.getName(), peer.getIp(), peer.getStatus(), component },
                    peer.getId() );
        }
    }


    private Peer getPeerJsonRepresentation( String ip, String servicePort )
    {
        String baseUrl = String.format( "http://%s:%s/cxf", ip, servicePort );
        try
        {
            WebClient client = WebClient.create( baseUrl );
            if ( servicePort.length() > 0 && ip.length() > 0 )
            {
                String peerJson = client.path( "peer/json" ).accept( MediaType.APPLICATION_JSON ).get( String.class );
                LOG.warn( peerJson );
                return GSON.fromJson( peerJson, Peer.class );
            }
            else
            {
                Notification.show( "Check form values" );
                return null;
            }
        }
        catch ( Exception e )
        {
            LOG.error( "PeerRegisterForm@createRegisterButton!clickListener" + e.getMessage(), e );
        }
        return null;
    }


    private void registerPeerToAnother( Peer peer, String ip, String servicePort )
    {
        String baseUrl = String.format( "http://%s:%s/cxf", ip, servicePort );
        WebClient client = WebClient.create( baseUrl );
        Response response =
                client.path( "peer/register" ).type( MediaType.TEXT_PLAIN ).accept( MediaType.APPLICATION_JSON )
                      .query( "peer", GSON.toJson( peer ) ).post( "" );
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            LOG.info( response.toString() );
            Notification.show( String.format( "Request sent to %s!", ip ) );
        }
        else
        {
            LOG.warn( "Response for registering peer: " + response.toString() );
        }
    }


    private void unregisterPeerFromAnother( Peer peer, String ip, String servicePort )
    {
        String baseUrl = String.format( "http://%s:%s/cxf", ip, servicePort );
        WebClient client = WebClient.create( baseUrl );
        Response response =
                client.path( "peer/unregister" ).type( MediaType.TEXT_PLAIN ).accept( MediaType.APPLICATION_JSON )
                      .query( "peerId", GSON.toJson( peer.getId().toString() ) ).delete();
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            LOG.info( response.toString() );
            Notification.show( String.format( "Request sent to %s!", ip ) );
        }
        else
        {
            LOG.warn( "Response for registering peer: " + response.toString() );
        }
    }


    private void updatePeerOnAnother( Peer peer, String ip, String servicePort )
    {
        String baseUrl = String.format( "http://%s:%s/cxf", ip, servicePort );
        WebClient client = WebClient.create( baseUrl );
        Response response =
                client.path( "peer/update" ).type( MediaType.TEXT_PLAIN ).accept( MediaType.APPLICATION_JSON )
                      .query( "peer", GSON.toJson( peer ) ).put( "" );
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            LOG.info( response.toString() );
            Notification.show( String.format( "Request sent to %s!", ip ) );
        }
        else
        {
            LOG.warn( "Response for registering peer: " + response.toString() );
        }
    }


    private Button createRegisterButton()
    {
        registerRequestButton = new Button();
        registerRequestButton.setCaption( "Register" );
        registerRequestButton.setImmediate( true );
        registerRequestButton.setWidth( "-1px" );
        registerRequestButton.setHeight( "-1px" );

        registerRequestButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                getUI().access( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String servicePort = servicePortTextField.getValue();
                        String ip = ipTextField.getValue();
                        LOG.warn( ip );

                        Peer remotePeer = getPeerJsonRepresentation( ip, servicePort );
                        remotePeer.setStatus( PeerStatus.REQUEST_SENT );
                        peerManagerPortalModule.getPeerManager().register( remotePeer );

                        Peer selfPeer = getPeerJsonRepresentation( "127.0.0.1", "8181" );
                        selfPeer.setStatus( PeerStatus.REQUESTED );

                        registerPeerToAnother( selfPeer, ip, servicePort );
                    }
                } );
            }
        } );

        return registerRequestButton;
    }
}