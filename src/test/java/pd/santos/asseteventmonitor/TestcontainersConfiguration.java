package pd.santos.asseteventmonitor;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import pd.santos.asseteventmonitor.model.AssetEvent;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @Primary
    KafkaTemplate<String, AssetEvent> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }
}
