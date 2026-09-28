package minios.memory;

import minios.Process;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MemoryManagementTests {

    @Test
    void testMemoryAllocationSingle(){
        Process p = new Process(1, 0, new ArrayList<>(), 50);
        MemoryManagement memoryManagement = new MemoryManagement(100);
        assertTrue(memoryManagement.allocateMemory(p));
    }

    @Test
    void testMemoryAllocationDouble() {
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 50);
        MemoryManagement memoryManagement = new MemoryManagement(100);
        assertTrue(memoryManagement.allocateMemory(p1));
        assertTrue(memoryManagement.allocateMemory(p2));
    }

    @Test
    void testDeallocation() {
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 30);
        Process p3 = new Process(3, 0, new ArrayList<>(), 20);
        MemoryManagement memoryManagement = new MemoryManagement(100);

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
    void testFullMerge(){
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 50);
        MemoryManagement memoryManagement = new MemoryManagement(100);

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
    void testWaitQueue() {
        Process p1 = new Process(1, 0, new ArrayList<>(), 50);
        Process p2 = new Process(2, 0, new ArrayList<>(), 30);
        Process p3 = new Process(3, 0, new ArrayList<>(), 50);
        MemoryManagement memoryManagement = new MemoryManagement(100);

        //Allocates first two processes successfully, but third one fails due to lack of memory
        assertTrue(memoryManagement.allocateMemory(p1));
        assertTrue(memoryManagement.allocateMemory(p2));
        assertFalse(memoryManagement.allocateMemory(p3));

        //Tests that the wait queue was added to
        assertEquals(1,memoryManagement.waitingQueue.size());

        //Tests that the first process was deallocated and the processes from the wait queue was allocated
        assertTrue(memoryManagement.deallocateMemory(50));
        assertEquals(0,memoryManagement.waitingQueue.size());
    }


}
