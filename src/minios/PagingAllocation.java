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



    PagingAllocation(int pageSize, int totalMemory) {
        this.pageSize = pageSize;
        this.totalMemory = totalMemory;
        this.totalFrames = totalMemory / pageSize;
        this.frameTable = new boolean[totalFrames];

        for(int i = 0; i < totalFrames; i++){
            frameTable[i] = true; // Initialize all frames as free
        }
    }


    @Override
    public boolean allocate(Process process) {
        int requiredPages = (int) Math.ceil((double) process.memorySize / pageSize);

        //Checks if there is enough free frames
        if(freeFrames() < requiredPages){
            return false;
        }

        //Creates new page table for process
        process.pageTable = new int[requiredPages];

        int pageIndex = 0;

        //Finds free frames and assigns them to the process
        for (int i = 0; i < totalFrames && pageIndex < requiredPages; i++){

            if (frameTable[i]){
                frameTable[i] = false;

                //Stores which frame this page was assigned to
                process.pageTable[pageIndex] = i;

                pageIndex++;
            }
        }
        return true;
    }

    @Override
    public boolean release(Process process) {
        //TODO: Implement deallocation



        return false;
    }

    //Calculate free frames
    protected int freeFrames(){
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
