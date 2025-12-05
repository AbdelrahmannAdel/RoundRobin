public class GanttEntry {
    
    int pid;        // which process ran
    int start;      // start time of slice
    int end;        // end time of slice
    
    public GanttEntry(int pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }

}
