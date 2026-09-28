package minios.memory;

import minios.Process;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
