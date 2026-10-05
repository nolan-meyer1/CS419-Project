package minios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagingAllocation implements MemoryManagement {

    private final int pageSize;
    private final int totalMemory;
    private final int totalFrames;
    private boolean[] frameTable; //false means that is is allocated true means that is free
    private HashMap<Process,ArrayList> pageTable;



    PagingAllocation(int pageSize, int totalMemory) {
        this.pageSize = pageSize;
        this.totalMemory = totalMemory;
        this.totalFrames = totalMemory / pageSize;
        this.frameTable = new boolean[totalFrames];
        pageTable = new HashMap<>();

        for(int i = 0; i < totalFrames; i++){
            frameTable[i] = true; // Initialize all frames as free
        }
    }


    @Override
    public boolean allocateMemory(Process process) {
        int requiredPages = (int) Math.ceil((double) process.memorySize / pageSize);

        //Checks if there is enough free frames
        if(freeFrames() < requiredPages){
            return false;
        }else {

            //TODO: Verify that this is correct
            ArrayList<Integer> processPageTable = new ArrayList<>();
            for (int i = 0; i < totalFrames && processPageTable.size() < requiredPages; i++) {
                if (frameTable[i]) { // If the frame is free
                    frameTable[i] = false; // Mark it as allocated
                    processPageTable.add(i); // Add to the process's page table
                }
            }

            //Adds the page table to the process
            process.pageTable = processPageTable;
            pageTable.put(process, processPageTable);
            return true;
        }
    }

    @Override
    public boolean deallocateMemory(int processId) {
        //TODO: Loop through the hashmap and find the process with the given start address, then free its frames and remove it from hashmap. Make test case pass
        return false;
    }

    //Calculate free frames
    private int freeFrames(){
        int freeFrames = 0;
        for(boolean frame : frameTable){
            if(frame){
                freeFrames++;
            }
        }
        return freeFrames;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public int getTotalFrames() {
        return totalFrames;
    }
}
