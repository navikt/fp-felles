package no.nav.vedtak.felles.testutilities.cdi;
import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CdiAwareExtension.class)
public class CdiExtensionTest {

    @Inject
    private MyBean myBean;

    @Test
    void gotMyBean() throws Exception {
        assertThat(myBean).isNotNull();
    }
}


@ApplicationScoped
class MyBean {

    @Inject
    public MyBean() {

    }
}