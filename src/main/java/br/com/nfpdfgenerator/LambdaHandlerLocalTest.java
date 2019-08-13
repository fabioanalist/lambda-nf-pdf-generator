package br.com.nfpdfgenerator;

import br.com.nfpdfgenerator.model.RequestClass;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class LambdaHandlerLocalTest {

    public static void main(String[] args) {

        RequestClass requestClass = new RequestClass();
        requestClass.headers.put("bu", "nome_empresa");
        requestClass.pathParameters.put("access_key", "NFeXXXXXXXXXXXXXXXX");

        LambdaLogger lambdaLogger = new LambdaLogger() {
            @Override
            public void log(String message) {
                System.out.print(message);
            }

            @Override
            public void log(byte[] message) {
                System.out.print(new String(message));
            }
        };

        LambdaHandler lambdaHandler = new LambdaHandler();
        System.out.print(lambdaHandler.handleRequest(requestClass, lambdaLogger));
    }
}
