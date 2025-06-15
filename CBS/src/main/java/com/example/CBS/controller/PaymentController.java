//Handles payment initiation and simulates webhook callbacks.
package com.example.CBS.controller;

import com.example.CBS.model.Payment;
import com.example.CBS.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{paymentId}/initiate")
    @PreAuthorize("hasRole('RIDER')") // Only the rider can initiate payment
    public ResponseEntity<String> initiatePayment(@PathVariable Long paymentId) {
        // In a real scenario, you'd verify if the current user owns this payment/booking
        // For simplicity, we assume frontend provides correct paymentId.

        String redirectUrl = paymentService.initiatePaymentWithGateway(paymentId);
        return ResponseEntity.ok("Payment initiated. Redirect to: " + redirectUrl);
    }

    /**
     * This endpoint simulates a webhook/callback from a payment gateway (e.g., PayPal, Stripe).
     * It should typically NOT be called directly by the frontend. The payment gateway
     * calls this URL when a payment's status changes.
     * In production, this endpoint would need to be highly secured (e.g., IP whitelisting, signature verification).
     */
    @PostMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(
            @RequestParam("transactionId") String transactionId,
            @RequestParam("status") String status,
            @RequestParam(value = "payerId", required = false) String payerId) { // Payer ID might be optional
        try {
            Payment updatedPayment = paymentService.handlePaymentCallback(transactionId, status, payerId);
            System.out.println("Payment callback received for Transaction ID: " + transactionId + ", Status: " + status);
            return ResponseEntity.ok("Callback processed. Payment status: " + updatedPayment.getStatus());
        } catch (RuntimeException e) {
            System.err.println("Error processing payment callback: " + e.getMessage());
            return new ResponseEntity<>("Error processing callback: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')") // Typically, only admins or specific roles can initiate refunds
    public ResponseEntity<String> initiateRefund(@PathVariable Long paymentId) {
        paymentService.initiateRefund(paymentId);
        return ResponseEntity.ok("Refund initiated for payment ID: " + paymentId);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER', 'ADMIN')") // Rider can see their payment, driver/admin can see others
    public ResponseEntity<Payment> getPaymentDetails(@PathVariable Long paymentId) {
        // You'd add authorization logic here: check if current user owns the booking linked to this payment
        return paymentService.getPaymentById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
