package com.conceptile.flowchart_engine.controller;

import com.conceptile.flowchart_engine.dto.ConnectedNodesDTO;
import com.conceptile.flowchart_engine.dto.OutgoingEdgesDTO;
import com.conceptile.flowchart_engine.dto.ValidFlowchartDTO;
import com.conceptile.flowchart_engine.entity.Flowchart;
import com.conceptile.flowchart_engine.exception.FlowchartNameNotUniqueException;
import com.conceptile.flowchart_engine.exception.FlowchartNotFormattedException;
import com.conceptile.flowchart_engine.exception.FlowchartNotFoundException;
import com.conceptile.flowchart_engine.exception.NodeNotFoundException;
import com.conceptile.flowchart_engine.service.FlowchartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/flowcharts")
public class FlowchartController {

  private final FlowchartService flowchartService;

  public FlowchartController(FlowchartService flowchartService) {
    this.flowchartService = flowchartService;
  }

  @Operation(
      summary = "Create a new flowchart",
      description =
          "Create a flowchart with name, graph, and start/end nodes. If a node is referenced in an edge but not listed in the graph, it is added with an empty list of edges.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successfully created flowchart"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
      })
  @PostMapping
  public ResponseEntity<Flowchart> createFlowchart(@RequestBody Flowchart flowchartRequest) {
    if (StringUtils.isEmpty(flowchartRequest.getName())
        || StringUtils.isEmpty(flowchartRequest.getGraph())
        || StringUtils.isEmpty(flowchartRequest.getStartNode())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "name, graph, start field can't be null or empty.");
    }
    try {
      Flowchart flowchart =
          flowchartService.createFlowchart(
              flowchartRequest.getName(),
              flowchartRequest.getGraph(),
              flowchartRequest.getStartNode(),
              flowchartRequest.getEndNode());
      return new ResponseEntity<>(flowchart, HttpStatus.CREATED);
    } catch (FlowchartNameNotUniqueException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @Operation(summary = "Get flowchart by ID", description = "Fetch a flowchart by its unique ID.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Flowchart found"),
        @ApiResponse(responseCode = "404", description = "Flowchart not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<Flowchart> getFlowchartById(@PathVariable Long id) {
    Optional<Flowchart> flowchart = flowchartService.getFlowchartById(id);
    if (flowchart.isEmpty())
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Flowchart doesn't exist with id: " + id);
    return ResponseEntity.ok(flowchart.get());
  }

  @Operation(summary = "Get all flowcharts", description = "Retrieve a list of all flowcharts.")
  @ApiResponse(responseCode = "200", description = "List of flowcharts")
  @GetMapping
  public ResponseEntity<List<Flowchart>> getAllFlowcharts() {
    List<Flowchart> flowcharts = flowchartService.getAllFlowcharts();
    return ResponseEntity.ok(flowcharts);
  }

  @Operation(
      summary = "Update an existing flowchart",
      description = "Update the details of a flowchart by its ID.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated flowchart"),
        @ApiResponse(responseCode = "404", description = "Flowchart not found"),
        @ApiResponse(responseCode = "400", description = "Invalid flowchart format")
      })
  @PutMapping("/{id}")
  public ResponseEntity<Flowchart> updateFlowchart(
      @PathVariable Long id, @RequestBody Flowchart flowchartRequest) {
    try {
      Flowchart updatedFlowchart =
          flowchartService.updateFlowchart(
              id,
              flowchartRequest.getName(),
              flowchartRequest.getGraph(),
              flowchartRequest.getStartNode(),
              flowchartRequest.getEndNode());
      return ResponseEntity.ok(updatedFlowchart);
    } catch (FlowchartNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    } catch (FlowchartNotFormattedException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  @Operation(summary = "Delete flowchart by ID", description = "Delete a flowchart by its ID.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Flowchart not found")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFlowchart(@PathVariable Long id) {
    if (flowchartService.deleteFlowchart(id)) {
      return ResponseEntity.noContent().build();
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flowchart id is invalid.");
    }
  }

  @Operation(
      summary = "Get outgoing edges for a node",
      description = "Fetch the outgoing edges for a specified node in a flowchart.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched outgoing edges"),
        @ApiResponse(responseCode = "404", description = "Flowchart or Node not found"),
        @ApiResponse(responseCode = "400", description = "Bad request due to invalid input")
      })
  @GetMapping("/edges/outgoing/{id}/node/{node}")
  public ResponseEntity<OutgoingEdgesDTO> getOutgoingEdges(
      @PathVariable Long id, @PathVariable String node) {
    try {
      Optional<Flowchart> flowchart = flowchartService.getFlowchartById(id);
      if (flowchart.isEmpty())
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "FlowChart with Id " + id + " is not present.");
      List<String> outgoingEdges =
          flowchartService.getOutgoingEdges(node, flowchart.get().getGraph());
      return ResponseEntity.ok(new OutgoingEdgesDTO(outgoingEdges));
    } catch (NodeNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (FlowchartNotFormattedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @Operation(
      summary = "Validate flowchart graph",
      description =
          """
        Validates the structure of a flowchart graph, ensuring that it meets several key criteria for correctness and integrity:
        1. Complete Reachability Validation: It ensures that every node in the graph is reachable from the `head`. If any node is unreachable, the validation fails.
        2. End Node Validation: If an `end` node is provided, the method confirms that the `end` node is reachable from the `head`. If not, the graph is considered invalid.
        3. Dangling Edge Detection: The method checks for dangling edges — references to nodes that do not exist in the graph. If such references are found, the graph is invalid.
    """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully validated graph"),
        @ApiResponse(responseCode = "404", description = "Flowchart not found")
      })
  @GetMapping("/validate/{id}")
  public ResponseEntity<ValidFlowchartDTO> validateGraph(@PathVariable Long id) {
    try {
      Optional<Flowchart> flowchart = flowchartService.getFlowchartById(id);
      if (flowchart.isEmpty())
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "FlowChart with Id " + id + " is not present.");
      boolean isValid =
          flowchartService.validateGraph(
              flowchart.get().getGraph(),
              flowchart.get().getStartNode(),
              flowchart.get().getEndNode());
      return ResponseEntity.ok(new ValidFlowchartDTO(isValid));
    } catch (FlowchartNotFormattedException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid flowChart or graph structure: " + e.getMessage());
    }
  }

  @Operation(
      summary = "Get all connected nodes",
      description = "Fetch all connected nodes to a specified node in a flowchart.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched connected nodes"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request due to invalid node or flowchart data"),
        @ApiResponse(responseCode = "404", description = "Flowchart not found")
      })
  @GetMapping("/connectedNodes/{id}/node/{node}")
  public ResponseEntity<ConnectedNodesDTO> getConnectedNodes(
      @PathVariable Long id, @PathVariable String node) {
    try {
      Optional<Flowchart> flowchart = flowchartService.getFlowchartById(id);
      if (flowchart.isEmpty())
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "FlowChart with Id " + id + " is not present.");
      Set<String> outgoingEdges =
          flowchartService.getAllConnectedNodes(node, flowchart.get().getGraph());
      return ResponseEntity.ok(new ConnectedNodesDTO(outgoingEdges));
    } catch (NodeNotFoundException | FlowchartNotFormattedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }
}
