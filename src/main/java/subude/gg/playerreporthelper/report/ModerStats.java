package subude.gg.playerreporthelper.report;

public class ModerStats {
    private final String name;

    private final int accepted;

    private final int denied;

    private final long averageResponse;

    public ModerStats(String name, int accepted, int denied, long averageResponse) {
        this.name = name;
        this.accepted = accepted;
        this.denied = denied;
        this.averageResponse = averageResponse;
    }

    public String getName() {
        return this.name;
    }

    public int getAccepted() {
        return this.accepted;
    }

    public int getDenied() {
        return this.denied;
    }

    public long getAverageResponse() {
        return this.averageResponse;
    }
}
