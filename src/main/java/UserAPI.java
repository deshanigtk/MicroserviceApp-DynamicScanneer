import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;


@Controller
@EnableAutoConfiguration
@RequestMapping("dynamicScanner")
public class UserAPI {


    @RequestMapping(value = "login-to-product", method = RequestMethod.GET)
    @ResponseBody
    public void login(@RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam("port") int port) {

    }

    @RequestMapping(value = "zap", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam("scriptPath") String scriptPath,
                           @RequestParam("zapHost") String zapHost,
                           @RequestParam("host") String host,
                           @RequestParam("sessionPath") String sessionPath) {


    }

    @RequestMapping(value = "uploadProductZipFile", method = RequestMethod.GET)
    @ResponseBody
    public void uploadProductZipFile(@RequestParam("zipFile") String zipFile,
                                     @RequestParam("productPath") String productPath,
                                     @RequestParam("replaceExisting") boolean replaceExisting) throws IOException {

        String wso2ServerAbsolutePath = MainController.extractZipFileAndReturnServerFile(zipFile, productPath, replaceExisting);
        Runtime.getRuntime().exec(new String[]{"chmod", "777", wso2ServerAbsolutePath});
        MainController.runShellScript(new String[]{wso2ServerAbsolutePath});
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public void login() throws Exception {
        MainController.main2(new String[]{});
    }


}