import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.dynamicscanner.scanners.ZapScanner;

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


    @RequestMapping(value = "login-to-product", method = RequestMethod.GET)
    @ResponseBody
    public void login(@RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam("port") int port) throws Exception {

    }

    @RequestMapping(value = "zap", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam("zapHost") String zapHost,
                           @RequestParam("zapPort") int zapPort,
                           @RequestParam("productHost") String productHost,
                           @RequestParam("productPort") int productPort,
                           @RequestParam("productLoginUrl") String productLoginUrl,
                           @RequestParam("productLogoutUrl") String productLogoutUrl,
                           @RequestParam("urlListPath") String urlListPath,
                           @RequestParam("reportFilePath") String reportFilePath) throws Exception {
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "admin");
        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, productHost, productPort, productLoginUrl,productLogoutUrl,
                credentials, urlListPath, reportFilePath);

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

    @RequestMapping(value = "uploadProductZipFile", method = RequestMethod.GET)
    @ResponseBody
    public void uploadProductZipFile(@RequestParam("zipFile") String zipFile,
                                     @RequestParam("productPath") String productPath,
                                     @RequestParam("replaceExisting") boolean replaceExisting) throws IOException {

//        String wso2ServerAbsolutePath = MainController.extractZipFileAndReturnServerFile(zipFile, productPath, replaceExisting);
//        Runtime.getRuntime().exec(new String[]{"chmod", "777", wso2ServerAbsolutePath});
//        MainController.runShellScript(new String[]{wso2ServerAbsolutePath});
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public void login() throws Exception {

    }


}