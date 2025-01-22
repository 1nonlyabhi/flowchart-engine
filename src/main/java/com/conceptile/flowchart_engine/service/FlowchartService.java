package com.conceptile.flowchart_engine.service;

import com.conceptile.flowchart_engine.entity.Flowchart;
import com.conceptile.flowchart_engine.exception.FlowchartNameNotUniqueException;
import com.conceptile.flowchart_engine.exception.FlowchartNotFormattedException;
import com.conceptile.flowchart_engine.exception.FlowchartNotFoundException;
import com.conceptile.flowchart_engine.exception.NodeNotFoundException;
import com.conceptile.flowchart_engine.repository.FlowchartRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class FlowchartService {

  private final FlowchartRepository flowchartRepository;
  private final ObjectMapper objectMapper;

  public FlowchartService(FlowchartRepository flowchartRepository) {
    this.flowchartRepository = flowchartRepository;
    this.objectMapper = new ObjectMapper();
  }

  public Flowchart createFlowchart(String name, String graph, String start, String end) {
    try {
      Map<String, List<String>> graphMap = addMissingNodes(graph);
      String processedGraph = objectMapper.writeValueAsString(graphMap);

      Flowchart flowchart = new Flowchart();
      flowchart.setName(name);
      flowchart.setGraph(processedGraph);
      flowchart.setStartNode(start);
      flowchart.setEndNode(end);
      return flowchartRepository.save(flowchart);
    } catch (DataIntegrityViolationException e) {
      throw new FlowchartNameNotUniqueException("Flowchart name should be unique.");
    } catch (Exception e) {
      throw new FlowchartNotFormattedException("Invalid graph format", e);
    }
  }

  public Optional<Flowchart> getFlowchartById(Long id) {
    return flowchartRepository.findById(id);
  }

  public List<Flowchart> getAllFlowcharts() {
    return flowchartRepository.findAll();
  }

  public Flowchart updateFlowchart(Long id, String name, String graph, String start, String end) {
    return flowchartRepository
        .findById(id)
        .map(
            existingFlowchart -> {
              if (name != null) {
                existingFlowchart.setName(name);
              }
              if (graph != null) {
                try {
                  Map<String, List<String>> graphMap = addMissingNodes(graph);
                  String processedGraph = objectMapper.writeValueAsString(graphMap);
                  existingFlowchart.setGraph(processedGraph);
                } catch (Exception e) {
                  throw new FlowchartNotFormattedException("Invalid graph format", e);
                }
              }
              if (start != null) {
                existingFlowchart.setStartNode(start);
              }
              if (end == null || end.trim().isEmpty()) {
                existingFlowchart.setEndNode(null);
              } else {
                existingFlowchart.setEndNode(end);
              }
              return flowchartRepository.save(existingFlowchart);
            })
        .orElseThrow(
            () -> new FlowchartNotFoundException("Flowchart with id " + id + " not found"));
  }

  public boolean deleteFlowchart(Long id) {
    if (flowchartRepository.existsById(id)) {
      flowchartRepository.deleteById(id);
      return true;
    }
    return false;
  }

  // Fetch all outgoing edges for a given node
  public List<String> getOutgoingEdges(String node, String graph) {
    try {
      Map<String, List<String>> graphMap = objectMapper.readValue(graph, new TypeReference<>() {});
      if (!graphMap.containsKey(node)) {
        throw new NodeNotFoundException(
            "Node '" + node + "' is not part of the graph. Please provide a valid node.");
      }
      return graphMap.getOrDefault(node, Collections.emptyList());
    } catch (JsonProcessingException e) {
      throw new FlowchartNotFormattedException("Invalid graph format", e);
    }
  }

  // Validate the graph
  public boolean validateGraph(String graph, String head, String end) {
    try {
      Map<String, List<String>> graphMap = objectMapper.readValue(graph, new TypeReference<>() {});
      // Check for reachability from head
      Set<String> reachableFromHead = traverseGraph(head, graphMap);

      // Ensure every node is reachable from head
      if (!reachableFromHead.containsAll(graphMap.keySet())) {
        return false;
      }

      // If end is given, check if it is reachable from head
      if (end != null && !reachableFromHead.contains(end)) {
        return false;
      }

      // Check for dangling nodes
      for (List<String> edges : graphMap.values()) {
        for (String edge : edges) {
          if (!graphMap.containsKey(edge)) {
            return false;
          }
        }
      }

      return true;
    } catch (Exception e) {
      throw new FlowchartNotFormattedException("Invalid graph format", e);
    }
  }

  // Query all nodes connected to a specific node (directly or indirectly)
  public Set<String> getAllConnectedNodes(String node, String graph) {
    try {
      Map<String, List<String>> graphMap = objectMapper.readValue(graph, new TypeReference<>() {});
      if (!graphMap.containsKey(node)) {
        throw new NodeNotFoundException(
            "Node '" + node + "' is not part of the graph. Please provide a valid node.");
      }

      Set<String> connectedNodes = new HashSet<>();
      dfs(node, graphMap, connectedNodes);
      return connectedNodes;
    } catch (JsonProcessingException e) {
      throw new FlowchartNotFormattedException("Invalid graph format", e);
    }
  }

  private Map<String, List<String>> addMissingNodes(String graph) throws Exception {
    Map<String, List<String>> graphMap =
        objectMapper.readValue(graph, new TypeReference<Map<String, List<String>>>() {});
    Set<String> allNodes = new HashSet<>(graphMap.keySet());
    for (List<String> edges : graphMap.values()) {
      allNodes.addAll(edges);
    }
    for (String node : allNodes) {
      graphMap.putIfAbsent(node, new ArrayList<>());
    }
    return graphMap;
  }

  private Set<String> traverseGraph(String head, Map<String, List<String>> graphMap) {
    Set<String> reachableFromHead = new HashSet<>();
    dfs(head, graphMap, reachableFromHead);
    return reachableFromHead;
  }

  private void dfs(String node, Map<String, List<String>> graphMap, Set<String> visited) {
    if (node == null || visited.contains(node)) {
      return;
    }
    visited.add(node);
    for (String neighbor : graphMap.getOrDefault(node, Collections.emptyList())) {
      dfs(neighbor, graphMap, visited);
    }
  }
}
