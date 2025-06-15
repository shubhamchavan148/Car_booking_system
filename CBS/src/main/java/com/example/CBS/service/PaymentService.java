//This service demonstrates how payment initiation and webhook handling would conceptually work.
// It'll simulate interactions with a payment gateway.
package com.example.CBS.service;

import com.example.CBS.model.Booking;
import com.example.CBS.model.Payment;
import com.example.CBS.model.Payment.PaymentStatus;
import com.example.CBS.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPaymentForBooking(Booking booking) {
        // Check if a payment record already exists for this booking
        Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
        if (existingPayment.isPresent()) {
            // You might want to handle re-attempts or existing payments differently
            return existingPayment.get();
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getActualFare());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("PAYPAL_MOCK"); // Indicate it's a mock payment
        payment.setTransactionId(UUID.randomUUID().toString()); // Generate a mock transaction ID
        payment.setPaymentDate(LocalDateTime.now()); // Set initial date

        return paymentRepository.save(payment);
    }

    /**
     * Simulates initiating a payment with a payment gateway (e.g., PayPal).
     * In a real application, this would involve calling PayPal's SDK/REST API.
     * This method would return a redirect URL or a payment token to the frontend.
     */
    @Transactional
    public String initiatePaymentWithGateway(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING status.");
        }

        // --- CONCEPTUAL CALL TO PAYPAL API ---
        System.out.println("Simulating call to PayPal for Payment ID: " + paymentId + " Amount: " + payment.getAmount());
        // For a real PayPal integration:
        // 1. Create a PayPal order/payment using PayPal SDK or REST API.
        // 2. Get the approval URL from PayPal's response.
        // 3. Return this URL to the frontend for redirection.
        String mockRedirectUrl = "http://mock-paypal.com/pay?paymentId=" + paymentId + "&token=" + UUID.randomUUID().toString();
        // --- END CONCEPTUAL CALL ---

        return mockRedirectUrl; // Return the URL to redirect the user to PayPal
    }


    /**
     * Handles the webhook/callback from the payment gateway after payment processing.
     * This is where the actual payment status update happens based on the gateway's response.
     */
    @Transactional
    public Payment handlePaymentCallback(String transactionId, String status, String payerId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction ID: " + transactionId));

        // Update payment status based on callback from gateway
        if ("COMPLETED".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPayerId(payerId); // Store payer ID if provided by gateway
            payment.setPaymentDate(LocalDateTime.now());
            System.out.println("Payment " + transactionId + " successfully COMPLETED.");

            // Further actions: mark booking as paid, send confirmation, etc.
            // booking.setIsPaid(true); // Assuming Booking has an isPaid field
            // bookingRepository.save(booking);

        } else if ("FAILED".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.FAILED);
            System.err.println("Payment " + transactionId + " FAILED.");
        } else if ("REFUNDED".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.REFUNDED);
            System.out.println("Payment " + transactionId + " REFUNDED.");
        }
        // Add more status handling as per actual payment gateway documentation (e.g., PENDING, CAPTURED, VOIDED)

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment initiateRefund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot refund a payment that is not COMPLETED.");
        }

        // --- CONCEPTUAL CALL TO PAYPAL REFUND API ---
        System.out.println("Simulating refund call to PayPal for Payment ID: " + paymentId);
        // In a real app, call PayPal refund API. If successful:
        payment.setStatus(PaymentStatus.REFUNDED);
        // --- END CONCEPTUAL CALL ---

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }
}
