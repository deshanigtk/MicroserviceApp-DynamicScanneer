import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.dynamicscanner.observerables.Wso2ServerHandler;
import org.wso2.dynamicscanner.observerables.ZapScanner;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;


@Controller
@EnableAutoConfiguration
@PropertySource("classpath:global.properties")
@RequestMapping("dynamicScanner")
public class UserAPI {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${report_file_path}")
    private String reportFilePath;

    @RequestMapping(value = "runZap", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam String zapHost,
                           @RequestParam int zapPort,
                           @RequestParam String sessionName,
                           @RequestParam String productHostRelativeToZap,
                           @RequestParam String productHostRelativeToThis,
                           @RequestParam int productPort,
                           @RequestParam String urlListPath,
                           @RequestParam boolean isAuthenticatedScan) throws Exception {


        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, sessionName, productHostRelativeToZap, productHostRelativeToThis,
                productPort, urlListPath, isAuthenticatedScan);

        Observer zapObserver = new Observer() {
            String message;

            @Override
            public void update(Observable o, Object arg) {
                if (new File(reportFilePath).exists()) {
                    message = "success";
                } else {
                    message = "scan failed";
                }
                System.out.println(message);
                LOGGER.info("Zap scan status: " + message);
            }
        };
        zapScanner.addObserver(zapObserver);
        new Thread(zapScanner).start();
    }

    @RequestMapping(value = "extractZipFileAndStartServer", method = RequestMethod.GET)
    @ResponseBody
    public void extractZipFileAndStartServer(@RequestParam String zipFileName,
                                             @RequestParam String productPath,
                                             @RequestParam boolean replaceExisting) throws IOException {

        Wso2ServerHandler wso2ServerHandler = new Wso2ServerHandler(zipFileName, productPath, replaceExisting);
        Observer wso2ServerObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println("startsss");
            }
        };
        wso2ServerHandler.addObserver(wso2ServerObserver);
        new Thread(wso2ServerHandler).start();
    }
}