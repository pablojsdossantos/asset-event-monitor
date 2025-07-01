package pd.santos.asseteventmonitor;

import org.springframework.boot.SpringApplication;

public class TestAssetEventMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.from(AssetEventMonitorApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
