package koeko.ResultsManagement;

public class MedalsInstructions {
    private Long bronzeTime;
    private Double bronzeScore;
    private Long silverTime;
    private Double silverScore;
    private Long goldTime;
    private Double goldScore;

    public Boolean parseInstructions(String instructions) {
        if (instructions != null) {
            String[] medals = instructions.split(";");
            if (medals.length >= 3) {
                for (int i = 0; i < 3; i++) {
                    if (medals[i].split(":").length == 2) {
                        if (medals[i].split(":")[1].split("/").length == 2) {
                            if (i == 0) {
                                bronzeTime = Long.valueOf(medals[i].split(":")[1].split("/")[0]);
                                bronzeScore = Double.valueOf(medals[i].split(":")[1].split("/")[1]);
                            } else if (i == 1) {
                                silverTime = Long.valueOf(medals[i].split(":")[1].split("/")[0]);
                                silverScore = Double.valueOf(medals[i].split(":")[1].split("/")[1]);
                            } else {
                                goldTime = Long.valueOf(medals[i].split(":")[1].split("/")[0]);
                                goldScore = Double.valueOf(medals[i].split(":")[1].split("/")[1]);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Long getBronzeTime() {
        return bronzeTime;
    }

    public void setBronzeTime(Long bronzeTime) {
        this.bronzeTime = bronzeTime;
    }

    public Double getBronzeScore() {
        return bronzeScore;
    }

    public void setBronzeScore(Double bronzeScore) {
        this.bronzeScore = bronzeScore;
    }

    public Long getSilverTime() {
        return silverTime;
    }

    public void setSilverTime(Long silverTime) {
        this.silverTime = silverTime;
    }

    public Double getSilverScore() {
        return silverScore;
    }

    public void setSilverScore(Double silverScore) {
        this.silverScore = silverScore;
    }

    public Long getGoldTime() {
        return goldTime;
    }

    public void setGoldTime(Long goldTime) {
        this.goldTime = goldTime;
    }

    public Double getGoldScore() {
        return goldScore;
    }

    public void setGoldScore(Double goldScore) {
        this.goldScore = goldScore;
    }
}
