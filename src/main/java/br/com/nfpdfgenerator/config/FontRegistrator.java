package br.com.nfpdfgenerator.config;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;

public class FontRegistrator {

    private static final String[] FONT_NAMES = new String[]{
            "times-new-roman-14.ttf",
            "verdana-4.ttf"
    };

    private FontRegistrator() {
    }

    public static void register(final LambdaLogger logger) {
        Stream.of(FONT_NAMES).forEach(font -> {

            try {
                logger.log("Registrando fonte '".concat(font).concat("' para uso da aplicação.\n"));

                final InputStream resourceAsStream = FontRegistrator.class.getClassLoader().getResourceAsStream("fonts/".concat(font));

                if (Objects.isNull(resourceAsStream)) {
                    final String errorMessage = String.format("Não foi possível identificar a fonte %s para registro\n", font);
                    logger.log(errorMessage);
                    throw new RuntimeException(errorMessage);
                }

                final Font customFont = Font.createFont(Font.TRUETYPE_FONT, resourceAsStream).deriveFont(12f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);

                logger.log("Fonte '".concat(font).concat("'registrada com sucesso.\n"));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
