import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lambda.Handler;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class HandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(HandlerTest.class);

    @Test
    public void handler_HandleRequest_ReturnSuccess() {
        logger.info("Invoke TEST");

        HashMap<String,String> event = new HashMap<String,String>();
        event.put("s3Path", "sample.json");

        Context context = new TestContext();
        Handler handler = new Handler();

        handler.handleRequest(event, context);
    }
}
