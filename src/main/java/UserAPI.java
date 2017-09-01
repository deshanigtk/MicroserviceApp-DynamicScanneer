import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@EnableAutoConfiguration
@RequestMapping("dynamicScanner/runScan")
public class UserAPI {

    @RequestMapping(value = "zap", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam("scriptPath") String scriptPath, @RequestParam("zapHost") String zapHost, @RequestParam("host") String host, @RequestParam("sessionPath") String sessionPath) throws Exception {
        MainController.runZapScript(new String[]{scriptPath, zapHost, host, sessionPath, Constant.LOGIN_SESSION, Constant.AUTHENTICATED_CONTEXT});
    }

}