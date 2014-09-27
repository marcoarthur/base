package org.safehaus.subutai.core.command.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class CommandRunnerPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "terminal.png";
    public static final String MODULE_NAME = "Terminal";
    private CommandRunner commandRunner;
    private AgentManager agentManager;


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    @Override
    public String getId()
    {
        return CommandRunnerPortalModule.MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return CommandRunnerPortalModule.MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( CommandRunnerPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new TerminalComponent( commandRunner, agentManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}