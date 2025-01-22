package com.conceptile.flowchart_engine.dto;

import java.util.Set;

public class ConnectedNodesDTO {

  public Set<String> connectedNodes;

  public ConnectedNodesDTO(Set<String> connectedNodes) {
    this.connectedNodes = connectedNodes;
  }

  public Set<String> getConnectedNodes() {
    return connectedNodes;
  }

  public void setConnectedNodes(Set<String> connectedNodes) {
    this.connectedNodes = connectedNodes;
  }
}
