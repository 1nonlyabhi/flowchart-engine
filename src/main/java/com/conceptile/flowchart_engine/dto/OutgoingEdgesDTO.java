package com.conceptile.flowchart_engine.dto;

import java.util.List;

public class OutgoingEdgesDTO {

  public List<String> outgoingEdges;

  public OutgoingEdgesDTO(List<String> connectedNodes) {
    this.outgoingEdges = connectedNodes;
  }

  public List<String> getOutgoingEdges() {
    return outgoingEdges;
  }

  public void setOutgoingEdges(List<String> outgoingEdges) {
    this.outgoingEdges = outgoingEdges;
  }
}
