
package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Fine {

    private int fineId;
    private int issueId;
    private BigDecimal fineAmount;
    private String paymentStatus;
    private LocalDate paymentDate;

    public Fine() {
    }

    public Fine(int fineId, int issueId, BigDecimal fineAmount,
                String paymentStatus, LocalDate paymentDate) {
        this.fineId = fineId;
        this.issueId = issueId;
        this.fineAmount = fineAmount;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    public int getFineId() { return fineId; }
    public void setFineId(int fineId) { this.fineId = fineId; }

    public int getIssueId() { return issueId; }
    public void setIssueId(int issueId) { this.issueId = issueId; }

    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}
