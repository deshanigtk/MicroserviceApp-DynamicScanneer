import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.dynamicscanner.observerables.Wso2ServerHandler;
import org.wso2.dynamicscanner.observerables.ZapScanner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


@Controller
@EnableAutoConfiguration
@RequestMapping("dynamicScanner")
public class UserAPI {

    @RequestMapping(value = "runZap", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam String zapHost,
                           @RequestParam int zapPort,
                           @RequestParam String sessionName,
                           @RequestParam String productHostRelativeToZap,
                           @RequestParam String productHostRelativeToThis,
                           @RequestParam int productPort,
                           @RequestParam String productLoginUrl,
                           @RequestParam String productLogoutUrl,
                           @RequestParam String keyUsername,
                           @RequestParam String valueUserName,
                           @RequestParam String keyPassword,
                           @RequestParam String valuePassword,
                           @RequestParam String urlListPath,
                           @RequestParam String reportFilePath,
                           @RequestParam boolean isAuthenticatedScan) throws Exception {


        Map<String, Object> credentials = new HashMap<>();
        credentials.put(keyUsername, valueUserName);
        credentials.put(keyPassword, valuePassword);
        
        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, sessionName, productHostRelativeToZap, productHostRelativeToThis,
                productPort, productLoginUrl, productLogoutUrl, credentials, urlListPath, reportFilePath, isAuthenticatedScan);

        Observer zapObserver = new Observer() {
            String message;

            @Override
            public void update(Observable o, Object arg) {
                if (new File(reportFilePath).exists()) {
                    message = "success";
                } else {
                    message = "scan failed";
                }
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