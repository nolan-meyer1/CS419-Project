package minios;

import org.junit.jupiter.api.Disabled;
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
        assertTrue(memoryManagement.deallocateMemory(p2));
        assertTrue(memoryManagement.deallocateMemory(p3));

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
        assertTrue(memoryManagement.deallocateMemory(p1));
        assertTrue(memoryManagement.deallocateMemory(p2));

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
    void testPagingDeallocation(){
        PagingAllocation memoryManagment = new PagingAllocation(10, 40);

        Process p1 = new Process(1, 0 , new ArrayList<>(), 20);
        Process p2 = new Process(2, 0 , new ArrayList<>(), 30);

        // p1 needs 2 pages, so it should allocate successfully
        assertTrue(memoryManagment.allocateMemory(p1));

        // p2 needs 3 pages, but only 2 frames are left
        assertFalse(memoryManagment.allocateMemory(p2));

        // Free p1's 2 frames
        assertTrue(memoryManagment.deallocateMemory(p1));

        // Now all 4 frames are free again, so p2 should fit
        assertTrue(memoryManagment.allocateMemory(p2));
    }

    @Test
    void testPagingAllocation(){
        Process p1 = new Process(1, 0, new ArrayList<>(), 70);
        Process p2 = new Process(2, 0, new ArrayList<>(), 40);
        PagingAllocation pagingAllocation = new PagingAllocation(10,100);
        Kernel kernel = new Kernel(new SJF(),pagingAllocation);

        //Second process should be put in the memory wait queue since there is not enough memory for it
        kernel.admitProcess(p1);
        kernel.admitProcess(p2);

        //Tests that the page table for p1 has the correct number of pages (7 pages for 70 memory size with page size of 10)
        assertEquals(7, p1.pageTable.length);

        //Tests that the frames allocated to p1 are marked as used in the frame table
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
        assertEquals(6, pagingAllocation.freeFrames());
    }

}
