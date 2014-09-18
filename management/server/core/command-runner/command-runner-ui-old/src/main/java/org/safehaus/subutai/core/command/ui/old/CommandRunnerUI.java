package org.safehaus.subutai.core.command.ui.old;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;


public class CommandRunnerUI implements PortalModule {

	public static final String MODULE_IMAGE = "terminal.png";
	public static final String MODULE_NAME = "Terminal Old";
	private CommandDispatcher commandRunner;
	private AgentManager agentManager;


	public void setCommandRunner(CommandDispatcher commandRunner) {
		this.commandRunner = commandRunner;
	}


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	public void init() {
	}


	public void destroy() {
	}


	@Override
	public String getId() {
		return CommandRunnerUI.MODULE_NAME;
	}


	@Override
	public String getName() {
		return CommandRunnerUI.MODULE_NAME;
	}


	@Override
	public File getImage() {
		return FileUtil.getFile(CommandRunnerUI.MODULE_IMAGE, this);
	}


	@Override
	public Component createComponent() {
		return new TerminalForm(commandRunner, agentManager);
	}

    @Override
    public Boolean isCorePlugin() {
        return true;
    }
}
