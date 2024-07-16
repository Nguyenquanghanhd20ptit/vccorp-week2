package ite.project.hbase.config.gson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;

@Configuration
public class JsonMapper {
    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) new JsonMapper().resetJsonConfig();
        return objectMapper;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        if (objectMapper == null) new JsonMapper().resetJsonConfig();
        return objectMapper;
    }

    public void resetJsonConfig() {
        objectMapper = new ObjectMapper();
        objectMapper
                .registerModule(new JavaTimeModule()) //đăng kí 1 module thời gian để hỗ trợ việc sử lý localDatime
                .setPropertyNamingStrategy(SNAKE_CASE) //nó sẽ tự động đổi tên thuộc tính sang dạng SNAKE_CASE vd camelCase -> snake_case
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false); // không ném ra ngoại lệ khi có thuộc tính json k tương thich vs object
//        objectMapper.registerModule(JSONModule());
    }
}