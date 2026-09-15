
package minios;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlgorithmTests {

    @Test
    void testFCFS() throws IOException {
        SchedulingAlgo algo = new FCFS();
        Kernel kernel = new Kernel(algo);
        Simulator sim = new Simulator(kernel, TraceParser.parseWorkload("test/minios/fcfs_test_workload.txt"));
        sim.run();
        assertEquals(16, kernel.calculateAverageWaitTime());
    }

    @Test
    void testSJF() throws IOException {
        SchedulingAlgo algo = new SJF();
        Kernel kernel = new Kernel(algo);
        Simulator sim = new Simulator(kernel, TraceParser.parseWorkload("test/minios/sjf_test_workload.txt"));
        sim.run();
        assertEquals(7.75, kernel.calculateAverageWaitTime());
    }

    @Test
    void testRR() throws IOException {
        SchedulingAlgo algo = new RR();
        Kernel kernel = new Kernel(algo);
        //Changed to five like example on the slides
        kernel.timeQuantum = 4;
        Simulator sim = new Simulator(kernel, TraceParser.parseWorkload("test/minios/rr_test_workload.txt"));
        sim.run();
        assertEquals(4.67, kernel.calculateAverageWaitTime(),0.01);
    }

}
