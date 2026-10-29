package minios;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MemoryManagementTests {

    @Test
    void testMemoryAllocationSingleContiguous(){
        Process p = new Process(1, 0, new ArrayList<>(), 50);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);
        assertTrue(memoryManagement.allocate(p));
    }

    @Test
    void testMemoryAllocationDoubleContiguous() {
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 50);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);
        assertTrue(memoryManagement.allocate(p1));
        assertTrue(memoryManagement.allocate(p2));
    }

    @Test
    void testDeallocatioContiguous() {
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 30);
        Process p3 = new Process(3, 0, new ArrayList<>(), 20);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);

        assertTrue(memoryManagement.allocate(p1));
        assertTrue(memoryManagement.allocate(p2));
        assertTrue(memoryManagement.allocate(p3));

        //Tests that both are deallocated successfully
        assertTrue(memoryManagement.release(p2));
        assertTrue(memoryManagement.release(p3));

        //Checks that free memory is merged back together
        assertEquals(50,memoryManagement.head.getSize());
        assertEquals(50,memoryManagement.head.getNext().getSize());
    }

    @Test
    void testFullMergeContiguous(){
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 50);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);

        assertTrue(memoryManagement.allocate(p1));
        assertTrue(memoryManagement.allocate(p2));

        //Tests that both are deallocated successfully
        assertTrue(memoryManagement.release(p1));
        assertTrue(memoryManagement.release(p2));

        //Tests that the head how has the full 100, and that there is no next block
        assertEquals(100,memoryManagement.head.getSize());
        assertEquals(null,memoryManagement.head.getNext());

    }

    @Test
    void testMemoryWaitQueue() {

        Process p1 = new Process(1, 0, new ArrayList<>(), 70);
        Process p2 = new Process(2, 0, new ArrayList<>(), 40);
        Kernel kernel = new Kernel(new SJF(),new ContiguousAllocation(100),100);

        //Second process should be put in the memory wait queue since there is not enough memory for it
        kernel.admitProcess(p1);
        kernel.admitProcess(p2);

        assertEquals(1, kernel.memoryWaitQueue.size());

        kernel.onClockTick(1);

        assertEquals(0, kernel.memoryWaitQueue.size());

    }

    @Test
    void testFrameCalculation(){
        PagingAllocation pagingAllocation = new PagingAllocation(10, 100);
        assertEquals(10, pagingAllocation.getTotalFrames());
    }

    @Test
    void testPageSize(){
        PagingAllocation pagingAllocation = new PagingAllocation(10, 100);
        assertEquals(10, pagingAllocation.getPageSize());
    }

    @Test
    void testPagingDeallocation(){
        PagingAllocation memoryManagment = new PagingAllocation(10, 40);

        Process p1 = new Process(1, 0 , new ArrayList<>(), 20);
        Process p2 = new Process(2, 0 , new ArrayList<>(), 30);

        // p1 needs 2 pages, so it should allocate successfully
        assertTrue(memoryManagment.allocate(p1));

        // p2 needs 3 pages, but only 2 frames are left
        assertFalse(memoryManagment.allocate(p2));

        // Free p1's 2 frames
        assertTrue(memoryManagment.release(p1));

        // Now all 4 frames are free again, so p2 should fit
        assertTrue(memoryManagment.allocate(p2));
    }

    @Test
    void testPagingAllocation(){
        Process p1 = new Process(1, 0, new ArrayList<>(), 70);
        Process p2 = new Process(2, 0, new ArrayList<>(), 40);
        PagingAllocation pagingAllocation = new PagingAllocation(10,100);
        Kernel kernel = new Kernel(new SJF(),pagingAllocation,100);

        //Second process should be put in the memory wait queue since there is not enough memory for it
        kernel.admitProcess(p1);
        kernel.admitProcess(p2);

        //Tests that the page table for p1 has the correct number of pages (7 pages for 70 memory size with page size of 10)
        assertEquals(7, p1.pageTable.length);
        assertEquals(3, pagingAllocation.freeFrames());

        //Process 2 is added to wait queue since there are not enough free frames for it
        assertEquals(1, kernel.memoryWaitQueue.size());

        //Advances clock
        kernel.onClockTick(1);

        //Tests that process 2 is allocated memory and removed from the wait queue
        assertEquals(0, kernel.memoryWaitQueue.size());

        //tests that the frames allocated to p2 are marked as used in the frame table
        assertEquals(4, p2.pageTable.length);
    }

    @Test
    void pagingAllocationTranslateAddress(){
        PagingAllocation pagingAllocation = new PagingAllocation(10, 100);
        Process p1 = new Process(1, 0, new ArrayList<>(), 70);

        //Allocate memory for p1
        assertTrue(pagingAllocation.allocate(p1));

        //Translate a virtual address to a physical address
        int virtualAddress = 25; // This is in page 2 (0-indexed), offset 5
        int physicalAddress = pagingAllocation.translateAddress(p1, virtualAddress);

        // The expected physical address should be the frame number * page size + offset
        int expectedFrameNumber = p1.pageTable[2]; // Page 2's frame number
        int expectedPhysicalAddress = expectedFrameNumber * pagingAllocation.getPageSize() + 5;

        assertEquals(expectedPhysicalAddress, physicalAddress);
    }

    @Test
    void testStoreAndLoadPaging() {

        // Create a small instruction list that stores a value
        // and then loads from the same virtual address
        List<Instruction> instructions = new ArrayList<>();

        instructions.add(new Instruction(Instruction.OpType.STORE, 7, 123));

        instructions.add(new Instruction(Instruction.OpType.LOAD, 7, 0));

        // Create a process with enough memory for virtual address 7
        Process p = new Process(1, 0, instructions, 20);

        // Use paging so address translation is already implemented
        Kernel kernel = new Kernel(new FCFS(), new PagingAllocation(10, 100), 100);

        // Capture console output so we can verify STORE and LOAD behavior
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            // Admit the process and execute both instructions
            kernel.admitProcess(p, 0);
            kernel.onClockTick(0);
            kernel.onClockTick(1);

        } finally {
            // Restore normal console output after the test
            System.setOut(originalOut);
        }

        String console = output.toString();

        // Verify the value was stored and later loaded correctly
        assertTrue(console.contains("stores value 123"));
        assertTrue(console.contains("loads value 123"));
    }

    @Test
    void testInvalidStoreTerminatesProcessPaging() {

        // Virtual address 10 is invalid for a process with memory size 10
        // because valid addresses are 0 through 9
        List<Instruction> instructions = new ArrayList<>();

        instructions.add(new Instruction(Instruction.OpType.STORE, 10, 999));

        Process p = new Process(1, 0, instructions, 10);

        // Use paging to test invalid address handling
        Kernel kernel = new Kernel(new FCFS(), new PagingAllocation(10, 100), 100);

        // Capture console output to verify that a warning is printed
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            // Admit the process and execute the invalid STORE
            kernel.admitProcess(p, 0);
            kernel.onClockTick(0);

        } finally {
            System.setOut(originalOut);
        }

        String console = output.toString();

        // Verify that the invalid STORE produced the expected warning
        assertTrue(
                console.contains("WARNING: Process 1 attempted invalid instruction STORE 10 999"));

        // An invalid memory access should immediately terminate the process
        assertEquals(Process.State.TERMINATED, p.state);
    }

    @Test
    void testInvalidLoadTerminatesProcessPaging() {

        // Virtual address 15 is invalid for a process with memory size 10
        List<Instruction> instructions = new ArrayList<>();

        instructions.add(new Instruction(Instruction.OpType.LOAD, 15, 0));

        Process p = new Process(1, 0, instructions, 10);

        // Use paging to test invalid address handling
        Kernel kernel = new Kernel(new FCFS(), new PagingAllocation(10, 100), 100);

        // Capture console output to verify that a warning is printed
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            // Admit the process and execute the invalid LOAD
            kernel.admitProcess(p, 0);
            kernel.onClockTick(0);

        } finally {
            System.setOut(originalOut);
        }

        String console = output.toString();

        // Verify that the invalid LOAD produced the expected warning
        assertTrue(console.contains("WARNING: Process 1 attempted invalid instruction LOAD 15"));

        // An invalid memory access should immediately terminate the process
        assertEquals(Process.State.TERMINATED, p.state);
    }
}
