package io.subutai.core.executor.impl;


import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.OutputRedirection;
import io.subutai.common.command.Request;
import io.subutai.common.command.RequestType;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.BrokerException;
import io.subutai.core.broker.api.Topic;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandProcessorTest
{
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final String RESPONSE_JSON = String.format(
            " { response: {" + "      \"type\":\"EXECUTE_RESPONSE\"," + "      \"id\":\"%s\","
                    + "      \"commandId\":\"%s\"," + "      \"pid\":123," + "      \"responseNumber\":2,"
                    + "      \"stdOut\":\"output\"," + "      \"stdErr\":\"err\"," + "      \"exitCode\" : 0" + "  } }",
            HOST_ID.toString(), COMMAND_ID.toString() );

    @Mock
    Broker broker;
    @Mock
    HostRegistry hostRegistry;
    @Mock
    ExpiringCache commands;
    @Mock
    CommandProcess process;
    @Mock
    ResourceHostInfo resourceHostInfo;
    @Mock
    ContainerHostInfo containerHostInfo;
    @Mock
    Request request;
    @Mock
    CommandCallback callback;
    @Mock
    User user;
    @Mock
    Session session;
    @Mock
    IdentityManager identityManager;


    CommandProcessor commandProcessor;


    @Before
    public void setUp() throws Exception
    {
        commandProcessor = spy( new CommandProcessor( broker, hostRegistry, identityManager ) );
        commandProcessor.commands = commands;
        doThrow( new HostDisconnectedException( "" ) ).when( hostRegistry ).getResourceHostInfoById( HOST_ID );
        when( hostRegistry.getContainerHostInfoById( HOST_ID ) ).thenReturn( containerHostInfo );
        when( hostRegistry.getResourceHostByContainerHost( containerHostInfo ) ).thenReturn( resourceHostInfo );
        when( request.getId() ).thenReturn( HOST_ID );
        when( request.getCommandId() ).thenReturn( COMMAND_ID );
        doReturn( session ).when( commandProcessor ).getActiveSession();
        doReturn( RESPONSE_JSON ).when( commandProcessor ).encrypt( anyString() );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {

            new CommandProcessor( null, hostRegistry, identityManager );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {

            new CommandProcessor( broker, null, identityManager );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {

            new CommandProcessor( broker, hostRegistry, null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testGetTopic() throws Exception
    {

        assertEquals( Topic.RESPONSE_TOPIC, commandProcessor.getTopic() );
    }


    @Test
    public void testRemove() throws Exception
    {

        commandProcessor.remove( COMMAND_ID );

        verify( commands ).remove( COMMAND_ID );
    }


    @Test
    public void testGetTargetHost() throws Exception
    {
        when( containerHostInfo.getState() ).thenReturn( ContainerHostState.RUNNING );

        ResourceHostInfo targetHost = commandProcessor.getTargetHost( HOST_ID );

        assertEquals( resourceHostInfo, targetHost );
    }


    @Test
    public void testOnMessage() throws Exception
    {
        byte[] message = RESPONSE_JSON.getBytes( "UTF-8" );

        commandProcessor.onMessage( message );


        when( commands.get( COMMAND_ID ) ).thenReturn( process );

        commandProcessor.onMessage( message );

        verify( process ).processResponse( isA( ResponseImpl.class ) );


        RuntimeException exception = mock( RuntimeException.class );
        doThrow( exception ).when( commands ).get( COMMAND_ID );

        commandProcessor.onMessage( message );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetResult() throws Exception
    {
        try
        {
            commandProcessor.getResult( COMMAND_ID );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        when( commands.get( COMMAND_ID ) ).thenReturn( process );

        commandProcessor.getResult( COMMAND_ID );

        verify( process ).waitResult();
    }


    @Test
    public void testExecute() throws Exception
    {

        try
        {
            commandProcessor.execute( request, callback );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
        Request request1 = new Request()
        {
            @Override
            public RequestType getType()
            {
                return null;
            }


            @Override
            public String getId()
            {
                return HOST_ID;
            }


            @Override
            public UUID getCommandId()
            {
                return COMMAND_ID;
            }


            @Override
            public String getWorkingDirectory()
            {
                return null;
            }


            @Override
            public String getCommand()
            {
                return null;
            }


            @Override
            public List<String> getArgs()
            {
                return null;
            }


            @Override
            public Map<String, String> getEnvironment()
            {
                return null;
            }


            @Override
            public OutputRedirection getStdOut()
            {
                return null;
            }


            @Override
            public OutputRedirection getStdErr()
            {
                return null;
            }


            @Override
            public String getRunAs()
            {
                return null;
            }


            @Override
            public Integer getTimeout()
            {
                return null;
            }


            @Override
            public Integer isDaemon()
            {
                return null;
            }


            @Override
            public Set<String> getConfigPoints()
            {
                return null;
            }


            @Override
            public Integer getPid()
            {
                return null;
            }
        };

        when( resourceHostInfo.getId() ).thenReturn( HOST_ID );
        when( containerHostInfo.getState() ).thenReturn( ContainerHostState.RUNNING );
        try
        {
            commandProcessor.execute( request, callback );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }


        when( commands.put( eq( COMMAND_ID ), any( CommandProcess.class ), anyInt(),
                any( CommandProcessExpiryCallback.class ) ) ).thenReturn( true );

        commandProcessor.execute( request1, callback );

        verify( broker ).sendTextMessage( eq( HOST_ID.toString() ), anyString() );

        doThrow( new BrokerException( "" ) ).when( broker ).sendTextMessage( eq( HOST_ID.toString() ), anyString() );

        try
        {
            commandProcessor.execute( request1, callback );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        doThrow( new HostDisconnectedException( "" ) ).when( hostRegistry )
                                                      .getResourceHostByContainerHost( containerHostInfo );
        try
        {
            commandProcessor.execute( request, callback );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
    }
}
