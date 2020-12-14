package com.nukkitx.digraph;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class DiGraph {
    private final Map<String, DiGraphNode> nodes = new LinkedHashMap<>();
    private final TreeMap<String, DiGraphEdge> edges = new TreeMap<>();
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, DiGraphNode> getNodes() {
        return nodes;
    }

    public DiGraphNode getNode(String id) {
        return nodes.get(id);
    }

    public void setNode(String id, DiGraphNode node) {
        this.nodes.put(id, node);
    }

    public TreeMap<String, DiGraphEdge> getEdges() {
        return edges;
    }

    public DiGraphEdge getEdge(String id) {
        return this.edges.get(id);
    }

    public void setEdge(String id, DiGraphEdge edge) {
        this.edges.put(id, edge);
    }
}
