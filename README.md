# Flowchart Engine

## Overview

This is a simple system implemented in Spring Boot and Java that allows for the management of flowcharts. The system supports basic CRUD operations for flowcharts, which are represented as directed graphs consisting of nodes and edges. The system also includes functionality for validating flowchart graphs, retrieving specific nodes' connected edges, and documenting APIs using Swagger.

## Features

- **Basic CRUD Operations**:
  1. **Create Flowchart**: Create a new flowchart with a unique ID, nodes, and edges.
  2. **Fetch Flowchart**: Fetch details of a flowchart by its ID, including its nodes and edges.
  3. **Update Flowchart**: Add or remove nodes and edges in an existing flowchart.
  4. **Delete Flowchart**: Delete an existing flowchart by its ID.

- **Other Features**:
  1. **Validate Graph**: Ensure the graph is valid based on reachability, dangling edges, and end node validations.
  2. **Fetch Outgoing Edges**: Retrieve all outgoing edges for a specific node.
  3. **Swagger API Documentation**: Basic API documentation for easy access to the flowchart-related endpoints.
  4. **Find Connected Nodes**: Query all nodes connected to a specific node.

---

## API Endpoints

### 1. **Create Flowchart**  
**POST** `/api/flowcharts`

- **Description**: Create a new flowchart with a name, graph, and start/end nodes. If a node is referenced in an edge but not listed in the graph, it is added with an empty list of edges.
  
- **Request Body**:
  ```json
  {
    "name": "flowchart1",
    "graph": {
      "f": ["g", "i"],
      "g": ["h"],
      "i": ["g", "k"],
      "j": ["i"],
      "k": []
    },
    "startNode": "f"
  }
  ```

- **Response**:
  ```json
  {
    "id": 1,
    "name": "flowchart1",
    "startNode": "f",
    "endNode": null,
    "graph": {
      "f": ["g", "i"],
      "g": ["h"],
      "h": [],
      "i": ["g", "k"],
      "j": ["i"],
      "k": []
    }
  }
  ```

---

### 2. **Get Flowchart by ID**  
**GET** `/api/flowcharts/{id}`

- **Description**: Fetch the details of a flowchart by its unique ID, including nodes and edges.
  
- **Response**:
  ```json
  {
    "id": 1,
    "name": "flowchart1",
    "startNode": "f",
    "endNode": null,
    "graph": {
      "f": ["g", "i"],
      "g": ["h"],
      "h": [],
      "i": ["g", "k"],
      "j": ["i"],
      "k": []
    }
  }
  ```

---

### 3. **Update Flowchart**  
**PUT** `/api/flowcharts/{id}`

- **Description**: Update the details of an existing flowchart by its ID. This includes adding/removing nodes and edges.

- **Request Body**:
  ```json
  {
    "name": "updatedFlowchart",
    "graph": {
      "f": ["g", "i"],
      "g": ["h", "i"],
      "h": [],
      "i": ["g", "k"],
      "j": ["i"],
      "k": []
    },
    "startNode": "f",
    "endNode": "k"
  }
  ```

- **Response**:
  ```json
  {
    "id": 1,
    "name": "updatedFlowchart",
    "startNode": "f",
    "endNode": "k",
    "graph": {
      "f": ["g", "i"],
      "g": ["h", "i"],
      "h": [],
      "i": ["g", "k"],
      "j": ["i"],
      "k": []
    }
  }
  ```

---

### 4. **Delete Flowchart**  
**DELETE** `/api/flowcharts/{id}`

- **Description**: Delete a flowchart by its ID.

- **Response**:  
  - **200 OK**: Successfully deleted the flowchart.
  - **404 Not Found**: Flowchart with the specified ID does not exist.

---

### 5. **Validate Flowchart Graph**  
**GET** `/api/flowcharts/validate/{id}`

- **Description**: Validates the structure of a flowchart graph ensuring:
  1. **Complete Reachability**: Every node in the graph should be reachable from the start node.
  2. **End Node Validation**: If an end node is provided, it should be reachable from the start node.
  3. **Dangling Edge Detection**: Ensures there are no references to nodes that do not exist in the graph.
  
- **Response**:
  ```json
  {
    "valid": true
  }
  ```

---

### 6. **Get Outgoing Edges for a Node**  
**GET** `/api/flowcharts/edges/outgoing/{id}/node/{node}`

- **Description**: Fetch the outgoing edges for a specified node in a flowchart.
  
- **Response**:
  ```json
  {
    "outgoingEdges": ["g", "i"]
  }
  ```

---

### 7. **Get All Connected Nodes**  
**GET** `/api/flowcharts/connectedNodes/{id}/node/{node}`

- **Description**: Fetch all nodes that are directly or indirectly connected to a specified node.

- **Response**:
  ```json
  {
    "connectedNodes": ["g", "i", "h", "k"]
  }
  ```

---

## Entity Structure

- **Flowchart**:
  - **id**: The unique identifier of the flowchart.
  - **name**: The unique name of the flowchart.
  - **startNode**: The start node of the flowchart.
  - **endNode**: (Optional) The end node of the flowchart.
  - **graph**: A serialized graph representing nodes and edges in a directed format.

---

## Technologies

- **Spring Boot**: Framework for building the RESTful service.
- **JPA/Hibernate**: Used for managing database entities and flowchart storage.
- **Swagger/OpenAPI**: For API documentation.

---

## Running the Application

1. Clone the repository: [link](https://github.com/1nonlyabhi/flowchart-engine)
2. Build the application with Maven.
   ```bash
   mvn clean install
   ```
3. Run the application using the Spring Boot runner:
   ```bash
   ./mvnw spring-boot:run
   ```
4. The application will be accessible at `http://localhost:8080`.
5. Use Swagger to explore the available API endpoints.

---

## Conclusion

This system provides a complete solution to manage flowcharts, handle directed graphs, and supports validation checks and dynamic querying for connected nodes. The inclusion of Swagger ensures that API documentation is easily accessible for developers integrating with the system.

# Alternate Approch:

There could be an alternative approach to implement the flowchart system, where the **Node** and **Edge** are treated as separate entities and then associated with the **Flowchart** entity. This approach allows more flexibility in managing nodes and edges independently before associating them with a specific flowchart.

Here’s the entity structure in a tree-like format:

```
Flowchart
│
├── id (Long)
├── name (String)
├── nodes (List<Node>)
└── edges (List<Edge>)

Node
│
├── id (Long)
├── name (String)
└── flowchart (Many-to-One → Flowchart)

Edge
│
├── id (Long)
├── fromNodeId (Long)
├── toNodeId (Long)
├── name (String, optional)
└── flowchart (Many-to-One → Flowchart)
```
