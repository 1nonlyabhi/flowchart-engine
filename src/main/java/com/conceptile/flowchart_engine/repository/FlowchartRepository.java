package com.conceptile.flowchart_engine.repository;

import com.conceptile.flowchart_engine.entity.Flowchart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowchartRepository extends JpaRepository<Flowchart, Long> {

  Optional<Flowchart> findByName(String name);
}
