package minios;

import java.util.List;

public class SJF implements SchedulingAlgo {
    @Override
    public void addProcess(List<Process> readyQueue, Process p) {
        readyQueue.add(p);
    }

    @Override
    public Process selectNextProcess(List<Process> readyQueue) {
        int minBurstLength = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0; i < readyQueue.size();  i++) {
            Process p = readyQueue.get(i);
            int burstLength = calculateBurstLength(p);

            if (burstLength < minBurstLength){
                minBurstLength = burstLength;
                minIndex = i;
            }
        }

        if(minIndex == -1) {
            return null;
        }

        return readyQueue.remove(minIndex);
    }

    private int calculateBurstLength(Process p) {
        int burstLength = 0;

        for (Instruction inst : p.code){
            if(inst.type == Instruction.OpType.CPU) {
                burstLength += inst.duration;
            }
        }
        return burstLength;
    }
}
