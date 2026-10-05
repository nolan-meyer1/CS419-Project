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
    public boolean allocateMemory(Process process) {
        int requiredPages = (int) Math.ceil((double) process.memorySize / pageSize);

        //Checks if there is enough free frames
        int freeFrames = 0;
        for(boolean frame : frameTable){
            if(frame){
                freeFrames++;
            }
        }

        if(freeFrames < requiredPages){
            return false; // Not enough free frames
        }

        //TODO: Implement the actual allocation logic here, updating the frameTable and processPageTables accordingly.
        return true;
    }

    @Override
    public boolean deallocateMemory(int startAddress) {
        return false;
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
