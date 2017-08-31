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

    private static String sessionPath = "/home/deshani/Documents/Session/";

    @RequestMapping(value = "zap", method = RequestMethod.GET)
    @ResponseBody
    public String runZapScan(@RequestParam("zapHost") String zapHost, @RequestParam("host") String host) throws Exception {
        return MainController.runZapScan(zapHost, host);
    }


}