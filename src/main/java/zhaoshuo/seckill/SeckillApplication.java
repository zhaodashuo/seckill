package zhaoshuo.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class SeckillApplication {

    public static void main(String[] args) {

        SpringApplication.run(SeckillApplication.class, args);
    }

}
