
package com.rest.models;

import com.rest.models.enums.DisputeStatus;
import java.time.LocalDateTime;

public class Dispute {

    private int disputeId;
    private String transactionReferenceId;
    private int raisedByUserId;
    private String reason;
    private DisputeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int reviewedByAdminId;

    public Dispute(int disputeId, String transactionReferenceId,
                   int raisedByUserId, String reason) {
        this.disputeId = disputeId;
        this.transactionReferenceId = transactionReferenceId;
        this.raisedByUserId = raisedByUserId;
        this.reason = reason;
        this.status = DisputeStatus.RAISED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.reviewedByAdminId = 0;
    }

    public int getDisputeId() {
        return disputeId;
    }

    public String getTransactionReferenceId() {
        return transactionReferenceId;
    }

    public int getRaisedByUserId() {
        return raisedByUserId;
    }

    public String getReason() {
        return reason;
    }

    public DisputeStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getReviewedByAdminId() {
        return reviewedByAdminId;
    }

    public void review(DisputeStatus status, int adminId) {
        if (status != DisputeStatus.APPROVED
                && status != DisputeStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Review status must be APPROVED or REJECTED");
        }

        if (this.status != DisputeStatus.RAISED
                && this.status != DisputeStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "This dispute has already been resolved");
        }

        this.status = status;
        this.reviewedByAdminId = adminId;
        this.updatedAt = LocalDateTime.now();
    }

    public void markUnderReview() {
        if (status != DisputeStatus.RAISED) {
            throw new IllegalStateException(
                    "Only a newly raised dispute can enter review");
        }

        status = DisputeStatus.UNDER_REVIEW;
        updatedAt = LocalDateTime.now();
    }
}
