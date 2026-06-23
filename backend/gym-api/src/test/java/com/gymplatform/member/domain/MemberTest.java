package com.gymplatform.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void computesBmiFromHeightAndWeight() {
        Member member = Member.builder()
                .heightCm(BigDecimal.valueOf(180))
                .weightKg(BigDecimal.valueOf(81))
                .build();

        assertThat(member.computeBmi()).isEqualByComparingTo("25.00");
    }

    @Test
    void returnsNullWhenHeightMissing() {
        Member member = Member.builder()
                .weightKg(BigDecimal.valueOf(70))
                .build();

        assertThat(member.computeBmi()).isNull();
    }

    @Test
    void returnsNullWhenWeightMissing() {
        Member member = Member.builder()
                .heightCm(BigDecimal.valueOf(170))
                .build();

        assertThat(member.computeBmi()).isNull();
    }

    @Test
    void returnsNullWhenHeightIsZeroOrNegative() {
        Member member = Member.builder()
                .heightCm(BigDecimal.ZERO)
                .weightKg(BigDecimal.valueOf(70))
                .build();

        assertThat(member.computeBmi()).isNull();
    }

    @Test
    void roundsToTwoDecimalPlaces() {
        Member member = Member.builder()
                .heightCm(BigDecimal.valueOf(165))
                .weightKg(BigDecimal.valueOf(60))
                .build();

        // 60 / (1.65^2) = 22.0385... -> rounds to 22.04
        assertThat(member.computeBmi()).isEqualByComparingTo("22.04");
    }
}
