package mil.usmc.mls2.stratis.core.processor.gcss;


import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MatsProcessorTest {

    @Test
    @DisplayName("Test to ensure null input gets flagged as 'empty'")
    void checkNullStringsAsBlankOrEmpty() {
        // Checking as this was flagged by Fortify as a potential null dereference error.
        assertThat(StringUtils.isEmpty(null)).as("Check nulls are 'empty'").isTrue();
        assertThat(StringUtils.isEmpty("")).as("Check blank string are 'empty'").isTrue();
    }
}
