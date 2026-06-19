package com.studioengine.tutor.checkout;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentOrigin;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.ServiceCategoryRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.InvalidReservationException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.payment.PaymentCommand;
import com.studioengine.tutor.payment.PaymentService;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private static final String TRIGGERED_BY_ZERO_PRICE = "SYSTEM_ZERO_PRICE";

    private final TimeSlotRepository timeSlotRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final StudentRepository studentRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotStateMachine timeSlotStateMachine;
    private final AppointmentStateMachine appointmentStateMachine;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public Checkout checkout(CheckoutCommand command) {
        var slot = findReservedSlot(command.getReservedSlotId());
        var category = findCategory(command.getServiceCategoryId());
        var student = findOrCreateStudent(command);
        var price = category.getPrice();
        var finalPrice = determineFinalPrice(price);
        var sessionNotes = command.getSessionNotes();

        var ctx = CheckoutContext.builder()
                .slot(slot)
                .category(category)
                .student(student)
                .originalPrice(price)
                .finalPrice(finalPrice)
                .sessionNotes(sessionNotes)
                .paymentMethodChoice(command.getPaymentMethod())
                .build();

        if (isZeroPrice(finalPrice)) {
            return handleZeroPriceCheckout(ctx);
        } else {
            return handleNonZeroPriceCheckout(ctx);
        }
    }

    private TimeSlot findReservedSlot(UUID slotId) {
        var slot = timeSlotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found: " + slotId));

        if (slot.getState() != TimeSlotState.RESERVED) {
            throw new InvalidReservationException(
                    "Slot %s is in state %s, expected RESERVED".formatted(slotId, slot.getState())
            );
        }
        return slot;
    }

    private ServiceCategory findCategory(UUID serviceCategoryId) {
        return serviceCategoryRepository.findById(serviceCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory not found: " + serviceCategoryId));
    }

    private Student findOrCreateStudent(CheckoutCommand command) {
        return studentRepository.findByEmail(command.getGuestEmail())
                .orElseGet(() -> {
                    var newStudent = Student.create(command.getGuestName(), command.getGuestEmail(), command.getGuestPhone());
                    return studentRepository.save(newStudent);
                });
    }

    private Checkout handleNonZeroPriceCheckout(CheckoutContext ctx) {
        var appointment = createAndSaveAppointment(ctx, AppointmentState.RESERVED);
        var paymentChoice = ctx.getPaymentMethodChoice();
        var paymentContext = PaymentCommand.builder()
                .paymentMethod(paymentChoice)
                .appointment(appointment)
                .build();
        var paymentInitiation = paymentService.initPayment(paymentContext);
        return Checkout.builder()
                .appointmentId(paymentInitiation.getAppointmentId())
                .status(paymentInitiation.getResultingState())
                .stripeRedirectUrl(paymentInitiation.getStripeRedirectUrl())
                .message(paymentInitiation.getResultingState() == AppointmentState.PENDING_PAYMENT ? "Invoice sent to email" : null)
                .benefitApplied(ctx.getBenefitApplied())
                .build();
    }

    private Checkout handleZeroPriceCheckout(CheckoutContext ctx) {
        var slot = ctx.getSlot();
        var originalPrice = ctx.getOriginalPrice();
        var finalPrice = ctx.getFinalPrice();
        var initialState = AppointmentState.RESERVED;

        var appointment = createAndSaveAppointment(ctx, initialState);
        appointmentStateMachine.transition(appointment, AppointmentState.PAID, TRIGGERED_BY_ZERO_PRICE);
        appointmentRepository.save(appointment);

        timeSlotStateMachine.transition(slot, TimeSlotState.BOOKED, TRIGGERED_BY_ZERO_PRICE);
        timeSlotRepository.save(slot);

        return Checkout.builder()
                .appointmentId(appointment.getId())
                .status(initialState)
                .message("Benefit applied — no payment required")
                .benefitApplied(Checkout.BenefitApplied.builder()
                        .type("FREE_LESSON")
                        .originalPrice(originalPrice)
                        .finalPrice(finalPrice)
                        .build())
                .build();
    }

    private Appointment createAndSaveAppointment(CheckoutContext ctx, AppointmentState initialState) {
        var appointment = Appointment.create(
                ctx.getSlot(), ctx.getCategory(), ctx.getStudent(),
                initialState,
                ctx.getOriginalPrice(), ctx.getFinalPrice(),
                AppointmentOrigin.STOREFRONT,
                ctx.getSessionNotes()
        );
        return appointmentRepository.save(appointment);
    }

    private boolean isZeroPrice(BigDecimal price) {
        return price.compareTo(BigDecimal.ZERO) == 0;
    }

    // TODO: benefit application will be added later
    private BigDecimal determineFinalPrice(BigDecimal price) {
        return price;
    }
}