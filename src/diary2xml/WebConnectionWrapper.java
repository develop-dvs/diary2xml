package diary2xml;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import java.io.IOException;

/**
 *
 * @author dcrm
 */
public class WebConnectionWrapper extends com.gargoylesoftware.htmlunit.util.WebConnectionWrapper {

    public WebConnectionWrapper(WebClient webClient) throws IllegalArgumentException {
        super(webClient);
    }

    @Override
    public WebResponse getResponse(WebRequest request) throws IOException, VerifyError {
        String requestBody = request.getRequestBody();

        WebResponse response = super.getResponse(request);
        //if (request.getUrl().toExternalForm().contains("my_url")) {
            /*String content = response.getContentAsString();

            //change content
            WebResponseData data = new WebResponseData(content.getBytes(),
                    response.getStatusCode(), response.getStatusMessage(), response.getResponseHeaders());
            
            

            response = new WebResponse(data, request, response.getLoadTime());*/
        //}
        if (response.getStatusCode()==404) {
            //Log.err(request.getUrl().toString());
            //Log.err(Integer.toString(response.getStatusCode()));
            throw new VerifyError("404 ["+request.getUrl().toString()+"]");
        }
        return response;
    }

}
