package minios;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MemoryManagementTests {

    @Test
    void testMemoryAllocationSingleContiguous(){
        Process p = new Process(1, 0, new ArrayList<>(), 50);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);
        assertTrue(memoryManagement.allocateMemory(p));
    }

    @Test
    void testMemoryAllocationDoubleContiguous() {
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 50);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);
        assertTrue(memoryManagement.allocateMemory(p1));
        assertTrue(memoryManagement.allocateMemory(p2));
    }

    @Test
    void testDeallocatioContiguous() {
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 30);
        Process p3 = new Process(3, 0, new ArrayList<>(), 20);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);

        assertTrue(memoryManagement.allocateMemory(p1));
        assertTrue(memoryManagement.allocateMemory(p2));
        assertTrue(memoryManagement.allocateMemory(p3));

        //Tests that both are deallocated successfully
        assertTrue(memoryManagement.deallocateMemory(50));
        assertTrue(memoryManagement.deallocateMemory(80));

        //Checks that free memory is merged back together
        assertEquals(50,memoryManagement.head.getSize());
        assertEquals(50,memoryManagement.head.getNext().getSize());
    }

    @Test
    void testFullMergeContiguous(){
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 50);
        ContiguousAllocation memoryManagement = new ContiguousAllocation(100);

        assertTrue(memoryManagement.allocateMemory(p1));
        assertTrue(memoryManagement.allocateMemory(p2));

        //Tests that both are deallocated successfully
        assertTrue(memoryManagement.deallocateMemory(0));
        assertTrue(memoryManagement.deallocateMemory(50));

        //Tests that the head how has the full 100, and that there is no next block
        assertEquals(100,memoryManagement.head.getSize());
        assertEquals(null,memoryManagement.head.getNext());

    }

    @Test
    void testMemoryWaitQueue() {

        Process p1 = new Process(1, 0, new ArrayList<>(), 70);
        Process p2 = new Process(2, 0, new ArrayList<>(), 40);
        Kernel kernel = new Kernel(new SJF(),new ContiguousAllocation(100));

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
    void testPagingAllocation(){
        Process p1 = new Process(1, 0, new ArrayList<>(), 70);
        Process p2 = new Process(2, 0, new ArrayList<>(), 40);
        Kernel kernel = new Kernel(new SJF(),new PagingAllocation(10,100));

        //Second process should be put in the memory wait queue since there is not enough memory for it
        kernel.admitProcess(p1);
        kernel.admitProcess(p2);

        assertEquals(1, kernel.memoryWaitQueue.size());

        kernel.onClockTick(1);

        assertEquals(0, kernel.memoryWaitQueue.size());
    }


}
