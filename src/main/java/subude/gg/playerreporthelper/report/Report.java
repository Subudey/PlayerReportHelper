package subude.gg.playerreporthelper.report;

public class Report {
    private final int id;

    private final String reporter;

    private final String target;

    private final String reason;

    private final String date;

    private final String status;

    private final String handledBy;

    private final String handledDate;

    private final long responseTime;

    public Report(int id, String reporter, String target, String reason, String date, String status, String handledBy, String handledDate, long responseTime) {
        this.id = id;
        this.reporter = reporter;
        this.target = target;
        this.reason = reason;
        this.date = date;
        this.status = status;
        this.handledBy = handledBy;
        this.handledDate = handledDate;
        this.responseTime = responseTime;
    }

    public int getId() {
        return this.id;
    }

    public String getReporter() {
        return this.reporter;
    }

    public String getTarget() {
        return this.target;
    }

    public String getReason() {
        return this.reason;
    }

    public String getDate() {
        return this.date;
    }

    public String getStatus() {
        return this.status;
    }

    public String getHandledBy() {
        return this.handledBy;
    }

    public String getHandledDate() {
        return this.handledDate;
    }

    public long getResponseTime() {
        return this.responseTime;
    }
}
