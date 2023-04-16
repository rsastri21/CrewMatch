package com.lux.crewmatch;

import com.lux.crewmatch.entities.Assignment;
import com.lux.crewmatch.services.AssignmentComparator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.ast.Assign;

import java.util.Objects;
import java.util.PriorityQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest
class CrewMatchApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void assignmentTest() {
        // Create dummy assignments
        Assignment assignment1 = new Assignment(null, null, 0, 0.65);
        Assignment assignment2 = new Assignment(null, null, 0, 0.47);

        // Test Priority Queue
        PriorityQueue<Assignment> pq = new PriorityQueue<>(new AssignmentComparator());
        pq.add(assignment1);
        pq.add(assignment2);

        assertThat(Objects.requireNonNull(pq.poll()).getWeight(), closeTo(0.65, 0.1));
    }

}
