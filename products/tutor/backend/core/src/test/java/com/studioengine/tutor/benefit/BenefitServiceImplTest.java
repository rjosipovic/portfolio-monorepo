package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.StudentBenefit;
import com.studioengine.tutor.dataaccess.enums.BenefitType;
import com.studioengine.tutor.dataaccess.repositories.StudentBenefitRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BenefitServiceImplTest {

    @Mock
    private StudentBenefitRepository studentBenefitRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private BenefitServiceMapper benefitServiceMapper;

    @InjectMocks
    private BenefitServiceImpl benefitService;

    @Nested
    class AssignTests {

        @ParameterizedTest
        @MethodSource("benefits")
        void shouldAssign(BenefitType benefitType, BigDecimal value) {
            // given
            var studentId = UUID.randomUUID();
            var note = "Zbog otkazivanja termina";
            var command = AssignBenefitCommand.builder()
                    .studentId(studentId)
                    .type(benefitType)
                    .value(value)
                    .note(note)
                    .build();
            var student = mock(Student.class);
            var benefit = mock(StudentBenefit.class);
            var assignedBenefit = mock(AssignedBenefit.class);

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentBenefitRepository.save(any(StudentBenefit.class))).thenReturn(benefit);
            when(benefitServiceMapper.toAssignedBenefit(benefit)).thenReturn(assignedBenefit);

            // when
            var result = benefitService.assign(command);

            // then
            verify(studentRepository).findById(studentId);
            verify(studentBenefitRepository).save(any(StudentBenefit.class));
            verify(benefitServiceMapper).toAssignedBenefit(benefit);

            assertThat(result).isNotNull();
        }

        @Test
        void shouldNotAssignWhenStudentNotFound() {
            // given
            var studentId = UUID.randomUUID();
            var benefitType = BenefitType.FREE_LESSON;
            var value = BigDecimal.valueOf(10);
            var note = "Zbog otkazivanja termina";
            var command = AssignBenefitCommand.builder()
                    .studentId(studentId)
                    .type(benefitType)
                    .value(value)
                    .note(note)
                    .build();

            when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> benefitService.assign(command)).isInstanceOf(ResourceNotFoundException.class);

            // then
            verify(studentRepository).findById(studentId);
            verify(studentBenefitRepository, never()).save(any());
            verify(benefitServiceMapper, never()).toAssignedBenefit(any());
        }

        private static Stream<Arguments> benefits() {
            return Stream.of(
                    Arguments.of(BenefitType.FREE_LESSON, null),
                    Arguments.of(BenefitType.FIXED_AMOUNT_OFF, BigDecimal.valueOf(5)),
                    Arguments.of(BenefitType.PERCENTAGE_DISCOUNT, BigDecimal.valueOf(10))
            );
        }
    }

    @Nested
    class ApplyTests {

        @Test
        void shouldApply() {
            // given
            var originalPrice = BigDecimal.valueOf(25);
            var benefitId = UUID.randomUUID();
            var studentId = UUID.randomUUID();
            var benefitType = BenefitType.FREE_LESSON;
            var student = mock(Student.class);
            var benefit = mock(StudentBenefit.class);

            when(student.getId()).thenReturn(studentId);
            when(benefit.getType()).thenReturn(benefitType);
            when(benefit.getId()).thenReturn(benefitId);
            when(studentBenefitRepository.findOldestUnconsumedByStudentId(studentId)).thenReturn(Optional.of(benefit));

            // when
            var result = benefitService.apply(student, originalPrice);

            // then
            verify(studentBenefitRepository).findOldestUnconsumedByStudentId(studentId);

            assertThat(result).isNotNull();
            assertThat(result.getBenefitId()).isEqualTo(benefitId);
            assertThat(result.getType()).isEqualTo(benefitType);
            assertThat(result.getOriginalPrice()).isEqualTo(originalPrice);
            assertThat(result.getFinalPrice()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.isApplied()).isTrue();
        }

        @Test
        void shouldApplyEmptyBenefit() {
            // given
            var originalPrice = BigDecimal.valueOf(25);
            var studentId = UUID.randomUUID();
            var student = mock(Student.class);

            when(student.getId()).thenReturn(studentId);
            when(studentBenefitRepository.findOldestUnconsumedByStudentId(studentId)).thenReturn(Optional.empty());

            // when
            var result = benefitService.apply(student, originalPrice);

            // then
            verify(studentBenefitRepository).findOldestUnconsumedByStudentId(studentId);

            assertThat(result).isNotNull();
            assertThat(result.getBenefitId()).isNull();
            assertThat(result.getType()).isNull();
            assertThat(result.getOriginalPrice()).isEqualTo(originalPrice);
            assertThat(result.getFinalPrice()).isEqualTo(originalPrice);
            assertThat(result.isApplied()).isFalse();
        }
    }

    @Nested
    class ConsumeTests {

        @Test
        void shouldConsumeEmptyBenefit() {
            // given
            var benefitApplication = mock(BenefitApplication.class);
            var appointment = mock(Appointment.class);

            when(benefitApplication.isApplied()).thenReturn(false);

            // when
            benefitService.consume(benefitApplication, appointment);

            // then
            verify(studentBenefitRepository, never()).findById(any());
            verify(studentBenefitRepository, never()).save(any());
        }

        @Test
        void shouldConsumeBenefit() {
            // given
            var benefitId = UUID.randomUUID();
            var benefitApplication = mock(BenefitApplication.class);
            var appointment = mock(Appointment.class);
            var studentBenefit = mock(StudentBenefit.class);

            when(benefitApplication.getBenefitId()).thenReturn(benefitId);
            when(benefitApplication.isApplied()).thenReturn(true);
            when(studentBenefitRepository.findById(benefitId)).thenReturn(Optional.of(studentBenefit));

            // when
            benefitService.consume(benefitApplication, appointment);

            // then
            verify(studentBenefitRepository).findById(benefitId);
            verify(studentBenefit).consume(appointment);
            verify(studentBenefitRepository).save(studentBenefit);
        }

        @Test
        void shouldNotConsumeWhenBenefitNotExists() {
            // given
            var benefitId = UUID.randomUUID();
            var benefitApplication = mock(BenefitApplication.class);
            var appointment = mock(Appointment.class);

            when(benefitApplication.getBenefitId()).thenReturn(benefitId);
            when(benefitApplication.isApplied()).thenReturn(true);
            when(studentBenefitRepository.findById(benefitId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> benefitService.consume(benefitApplication, appointment)).isInstanceOf(IllegalStateException.class);

            // then
            verify(studentBenefitRepository).findById(benefitId);
            verify(studentBenefitRepository, never()).save(any());
        }
    }

    @Nested
    class GetBenefitsTests {

        @Test
        void shouldGetBenefits() {
            // given
            var studentId = UUID.randomUUID();
            var studentBenefit = mock(StudentBenefit.class);
            var benefitEntry = mock(BenefitEntry.class);

            when(studentBenefitRepository.findByStudentIdOrderByGrantedAtDesc(studentId)).thenReturn(List.of(studentBenefit));
            when(benefitServiceMapper.toBenefitEntries(List.of(studentBenefit))).thenReturn(List.of(benefitEntry));

            // when
            var benefits = benefitService.getBenefits(studentId);

            // then
            verify(studentBenefitRepository).findByStudentIdOrderByGrantedAtDesc(studentId);
            verify(benefitServiceMapper).toBenefitEntries(List.of(studentBenefit));

            assertThat(benefits).hasSize(1);
        }

        @Test
        void shouldGetEmptyBenefits() {
            // given
            var studentId = UUID.randomUUID();

            when(studentBenefitRepository.findByStudentIdOrderByGrantedAtDesc(studentId)).thenReturn(List.of());
            when(benefitServiceMapper.toBenefitEntries(List.of())).thenReturn(List.of());

            // when
            var benefits = benefitService.getBenefits(studentId);

            // then
            verify(studentBenefitRepository).findByStudentIdOrderByGrantedAtDesc(studentId);
            verify(benefitServiceMapper).toBenefitEntries(List.of());

            assertThat(benefits).hasSize(0);
        }
    }
}