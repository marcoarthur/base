package org.safehaus.subutai.core.strategy.impl;

import org.safehaus.subutai.core.strategy.api.AbstractContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.*;

public class RoundRobinStrategy extends AbstractContainerPlacementStrategy {

	public static final String DEFAULT_NODE_TYPE = "default";

    @Override
    public String getId() {
        return "ROUND_ROBIN";
    }

    @Override
    public String getTitle() {
        return "Round Robin placement strategy";
    }

    @Override
	public void calculatePlacement(int nodesCount, Map<Agent, ServerMetric> serverMetrics, List<Criteria> criteria) {
		if (serverMetrics == null || serverMetrics.isEmpty()) return;

		List<Agent> ls = sortServers(serverMetrics);

		// distribute required nodes among servers in round-robin fashion
		Map<Agent, Integer> slots = new HashMap<Agent, Integer>();
		for (int i = 0; i < nodesCount; i++) {
			Agent best = ls.get(i % ls.size());
			if (slots.containsKey(best)) slots.put(best, slots.get(best) + 1);
			else slots.put(best, 1);
		}
		// add node distribution counts
		for (Map.Entry<Agent, Integer> e : slots.entrySet()) {
			addPlacementInfo(e.getKey(), DEFAULT_NODE_TYPE, e.getValue());
		}
	}

    protected List<Agent> sortServers(Map<Agent, ServerMetric> serverMetrics) {
		List<Agent> ls = new ArrayList(serverMetrics.keySet());
		Collections.sort(ls);
		return ls;
	}

}