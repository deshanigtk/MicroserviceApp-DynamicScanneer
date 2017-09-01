import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;


@Controller
@EnableAutoConfiguration
@RequestMapping("dynamicScanner/runScan")
public class UserAPI {

    @RequestMapping(value = "zap", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam("scriptPath") String scriptPath, @RequestParam("zapHost") String zapHost, @RequestParam("host") String host, @RequestParam("sessionPath") String sessionPath){
        MainController.runShellScript(new String[]{scriptPath, zapHost, host, sessionPath, Constant.LOGIN_SESSION, Constant.AUTHENTICATED_CONTEXT});
    }

    @RequestMapping(value = "uploadProductZipFile", method = RequestMethod.GET)
    @ResponseBody
    public String uploadProductZipFile(@RequestParam("zipFile") String zipFile, @RequestParam("productPath") String productPath, @RequestParam("replaceExisting") boolean replaceExisting) throws IOException {
        return MainController.extractZipFileAndReturnServerFile(zipFile, productPath, replaceExisting);
    }

}