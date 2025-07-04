package pd.santos.asseteventmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AssetEventMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetEventMonitorApplication.class, args);
    }

}
