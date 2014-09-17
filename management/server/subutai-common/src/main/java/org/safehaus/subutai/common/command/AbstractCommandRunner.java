package org.safehaus.subutai.common.command;


import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;


/**
 * Created by dilshat on 9/15/14.
 */
public abstract class AbstractCommandRunner implements CommandRunnerBase, ResponseListener {

    protected static final Logger LOG = Logger.getLogger( AbstractCommandRunner.class.getName() );
    protected ExpiringCache<UUID, CommandExecutor> commandExecutors;


    protected AbstractCommandRunner() {
        this.commandExecutors = new ExpiringCache<>();
    }


    public void dispose() {
        Map<UUID, CacheEntry<CommandExecutor>> entries = commandExecutors.getEntries();
        //shutdown all executors which are still there
        for ( Map.Entry<UUID, CacheEntry<CommandExecutor>> entry : entries.entrySet() ) {
            try {
                entry.getValue().getValue().getExecutor().shutdown();
            }
            catch ( Exception ignore ) {
            }
        }
        commandExecutors.dispose();
    }


    @Override
    public void onResponse( final Response response ) {
        if ( response != null && response.getUuid() != null && response.getTaskUuid() != null ) {
            final CommandExecutor commandExecutor = commandExecutors.get( response.getTaskUuid() );

            if ( commandExecutor != null ) {

                //process command response
                commandExecutor.getExecutor().execute( new Runnable() {

                    public void run() {
                        //obtain command lock
                        commandExecutor.getCommand().getUpdateLock();
                        try {
                            if ( commandExecutors.get( response.getTaskUuid() ) != null ) {

                                //append results to command
                                commandExecutor.getCommand().appendResult( response );

                                //call command callback
                                try {
                                    commandExecutor.getCallback().onResponse( response,
                                            commandExecutor.getCommand().getResults().get( response.getUuid() ),
                                            commandExecutor.getCommand() );
                                }
                                catch ( Exception e ) {
                                    LOG.log( Level.SEVERE, "Error in callback {0}", e );
                                }

                                //do cleanup on command completion or interruption by user
                                if ( commandExecutor.getCommand().hasCompleted() || commandExecutor.getCallback()
                                                                                                   .isStopped() ) {
                                    //remove command executor so that
                                    //if response comes from agent it is not processed by callback
                                    commandExecutors.remove( commandExecutor.getCommand().getCommandUUID() );
                                    //call this to notify all waiting threads that command completed
                                    commandExecutor.getCommand().notifyWaitingThreads();
                                    //shutdown command executor
                                    commandExecutor.getExecutor().shutdown();
                                }
                            }
                        }
                        finally {
                            commandExecutor.getCommand().releaseUpdateLock();
                        }
                    }
                } );
            }
        }
    }


    /**
     * Runs command on agents. Runs asynchronously for calling party. The supplied callback is triggered every time a
     * response is received from agent. Calling party may examine the command to see its status and results of each
     * agent.
     *
     * @param command - command to run
     * @param commandCallback - callback to trigger on every response
     */
    public abstract void runCommandAsync( final Command command, final CommandCallback commandCallback );


    /**
     * Runs command synchronously. Call returns after final response is received or stop() method is called from inside
     * a callback
     *
     * @param command - command to run
     * @param commandCallback - - callback to trigger on every response
     */
    public void runCommand( Command command, CommandCallback commandCallback ) {
        runCommandAsync( command, commandCallback );
        ( ( AbstractCommand ) command ).waitCompletion();
    }


    /**
     * Runs command asynchronously to the calling party. Result of command can be checked later using the associated
     * command object
     *
     * @param command - command to run
     */
    public void runCommandAsync( Command command ) {
        runCommandAsync( command, new CommandCallback() {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command ) {

            }
        } );
    }


    /**
     * Runs command synchronously. Call returns after final response is received
     *
     * @param command - command to run
     */
    public void runCommand( Command command ) {
        runCommandAsync( command, new CommandCallback() {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command ) {

            }
        } );
        ( ( AbstractCommand ) command ).waitCompletion();
    }
}