package com.pb.crm.accounts.domain;

import com.pb.crm.accounts.domain.company.Cnpj;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnpjTest {

    @Test
    void acceptsNumericCnpjWithOrWithoutMask() {
        Cnpj masked = Cnpj.of("11.222.333/0001-81");
        Cnpj plain = Cnpj.of("11222333000181");

        assertThat(masked).isEqualTo(plain);
        assertThat(masked.value()).isEqualTo("11222333000181");
        assertThat(masked.formatted()).isEqualTo("11.222.333/0001-81");
    }

    @Test
    void acceptsAlphanumericCnpj() {
        Cnpj cnpj = Cnpj.of("12.abc.345/01de-35");

        assertThat(cnpj.value()).isEqualTo("12ABC34501DE35");
    }

    @Test
    void rejectsWrongCheckDigitsRepeatedCharactersAndBadFormat() {
        assertThatThrownBy(() -> Cnpj.of("11.222.333/0001-82")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cnpj.of("00000000000000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cnpj.of("1122233300018")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cnpj.of(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromBaseComputesCheckDigits() {
        assertThat(Cnpj.fromBase("112223330001").value()).isEqualTo("11222333000181");
        assertThat(Cnpj.fromBase("12ABC34501DE").value()).isEqualTo("12ABC34501DE35");
    }
}
