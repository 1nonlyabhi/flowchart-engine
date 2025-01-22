package com.conceptile.flowchart_engine.dto;

public class ValidFlowchartDTO {
  private boolean valid;

  public ValidFlowchartDTO(boolean valid) {
    this.valid = valid;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }
}
