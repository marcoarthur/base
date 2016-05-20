package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.hub.share.dto.HeartbeatResponseDto;


public class HeartbeatProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Set<StateLinkProcessor> processors = new HashSet<>();

    private final HubManagerImpl hubManager;

    private final HubRestClient restClient;

    private final String path;


    public HeartbeatProcessor( HubManagerImpl hubManager, HubRestClient restClient, String peerId )
    {
        this.hubManager = hubManager;
        this.restClient = restClient;

        path = String.format( "/rest/v1.2/peers/%s/heartbeat", peerId );
    }


    public HeartbeatProcessor addProcessor( StateLinkProcessor processor )
    {
        processors.add( processor );

        return this;
    }


    @Override
    public void run()
    {
        try
        {
            sendHeartbeat();
        }
        catch ( Exception e )
        {
            log.error( "Error to process heartbeat: ", e );
        }
    }


    public void sendHeartbeat() throws Exception
    {
        if ( !hubManager.isRegistered() )
        {
            return;
        }

        try
        {
            RestResult<HeartbeatResponseDto> restResult = restClient.post( path, null, HeartbeatResponseDto.class );

            if ( !restResult.isSuccess() )
            {
                throw new Exception( restResult.getError() );
            }

            HeartbeatResponseDto dto = restResult.getEntity();

            processStateLinks( dto.getStateLinks() );
        }
        catch ( Exception e )
        {
            throw new Exception( e.getMessage(), e );
        }
    }


    private void processStateLinks( Set<String> stateLinks )
    {
        log.info( "stateLinks: {}", stateLinks );

        for ( StateLinkProcessor processor : processors )
        {
            try
            {
                processor.processStateLinks( stateLinks );
            }
            catch ( Exception e )
            {
                log.error( "Error to process state links: ", e );
            }
        }
    }
}