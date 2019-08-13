package br.com.nfpdfgenerator;

import br.com.nfpdfgenerator.config.FontRegistrator;
import br.com.nfpdfgenerator.model.RequestClass;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.hibernate.service.ServiceRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LambdaHandler implements RequestHandler<RequestClass, String> {

    private static final String BUCKET_NAME = System.getenv("LOGO_BUCKET_NAME");

    private static final String DATASOURCE_URL = System.getenv("DATASOURCE_URL");
    private static final String DATASOURCE_USERNAME = System.getenv("DATASOURCE_USERNAME");
    private static final String DATASOURCE_PASSWORD = System.getenv("DATASOURCE_PASSWORD");
    private static final String QUERY = "select nfe_xml from nf.invoice where access_key = :access_key";

    private static final String LOGO_IMAGE_NAME_FORMAT = "%s.jpg";
    private static final String DANFE_JRXML_PATH = "danfe-jasper/danfe1.jrxml";

    @Override
    public String handleRequest(final RequestClass input,
                                final Context context) {

        final LambdaLogger logger = context.getLogger();
        return handleRequest(input, logger);
    }

    public String handleRequest(final RequestClass input,
                                final LambdaLogger logger) {

        final String invoiceAccessKey = input.pathParameters.get("access_key");
        logger.log("Buscando XML correspondente à nota '" + invoiceAccessKey + "'.\n");
        final String invoiceXml = findInvoiceXmlOnDatabase(logger, invoiceAccessKey);

        if (Objects.isNull(invoiceXml) || invoiceXml.isEmpty()) {
            logger.log("Informação do XML é vazia, abortando processo de conversão.\n");
            throw new RuntimeException("Informação do XML é vazia.");
        }

        logger.log("Inicializando registro de fontes necessárias para emissão do PDF.\n");
        FontRegistrator.register(logger);

        final String businessUnit = input.headers.get("bu");
        logger.log("Buscando logo correspondente da bu '" + businessUnit + "'.\n");
        final String base64BusinessUnitLogo = getBase64LogoFromBucket(logger, businessUnit);

        logger.log("Inicializando conversão do xml para PDF.\n");
        final byte[] nfPdf = convertNFeXMLToPDF(logger, invoiceXml, base64BusinessUnitLogo);

        logger.log("Retornando base64 do PDF.\n");
        return Base64.getEncoder().encodeToString(nfPdf);
    }

    private String getBase64LogoFromBucket(final LambdaLogger logger,
                                           final String businessUnit) {

        try (final S3Object businessUnitLogo = getLogoFromBucket(businessUnit);
             final S3ObjectInputStream objectInputStream = businessUnitLogo.getObjectContent()) {

            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(objectInputStream));

        } catch (IOException e) {
            final String error = "Erro ao obter imagem / ler conteúdo e transformar em Base64: Message '" + e.getMessage() + "' - Cause '" + e.getCause() + "'.\n";
            logger.log(error);
            throw new RuntimeException(error);
        }
    }

    private byte[] convertNFeXMLToPDF(final LambdaLogger logger,
                                      final String nfeXml,
                                      final String base64BusinessUnitLogo) {

        try (final InputStream fis = new ByteArrayInputStream(nfeXml.getBytes(Charset.forName("UTF-8")))) {
            return convertDanfeXmlToPdf(logger, fis, base64BusinessUnitLogo);

        } catch (Exception e) {
            final String error = "Erro ao converter nf - Message: '" + e.getMessage() + "' - Cause: '" + e.getCause() + ".\n";
            logger.log(error);
            throw new RuntimeException(error);
        }
    }

    private S3Object getLogoFromBucket(final String businessUnit) {
        final String imageName = String.format(LOGO_IMAGE_NAME_FORMAT, businessUnit);
        final AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();
        return amazonS3.getObject(BUCKET_NAME, imageName);
    }

    private byte[] convertDanfeXmlToPdf(final LambdaLogger logger,
                                        final InputStream danfeXml,
                                        final String base64BusinessUnitLogo) throws JRException {

        final JRXmlDataSource xml = new JRXmlDataSource(danfeXml, "/nfeProc/NFe/infNFe/det");

        final Map<String, Object> params = new HashMap<>();
        params.put("Logo", base64BusinessUnitLogo);

        final JasperReport compiledJasperReport = compileJasperReport(logger);

        final JasperPrint jp = JasperFillManager.fillReport(compiledJasperReport, params, xml);
        return JasperExportManager.exportReportToPdf(jp);
    }

    private JasperReport compileJasperReport(final LambdaLogger logger) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final InputStream danfeTemplateStream = classLoader.getResourceAsStream(DANFE_JRXML_PATH);
            return JasperCompileManager.compileReport(danfeTemplateStream);

        } catch (Exception e) {
            final String error = "Erro ao compilar jasper report - Message: '" + e.getMessage() + "' - Cause: '" + e.getCause() + ".\n";
            logger.log(error);
            throw new RuntimeException(error);
        }
    }

    private String findInvoiceXmlOnDatabase(final LambdaLogger logger,
                                            final String invoiceAccessKey) {

        final Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.url", DATASOURCE_URL);
        configuration.setProperty("hibernate.connection.username", DATASOURCE_USERNAME);
        configuration.setProperty("hibernate.connection.password", DATASOURCE_PASSWORD);

        final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

        try (SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
             Session session = sessionFactory.openSession()) {

            final NativeQuery invoiceByAccessKey =
                    session.createNativeQuery(QUERY)
                            .setParameter("access_key", invoiceAccessKey);

            final List list = invoiceByAccessKey.list();
            if (list.isEmpty()) {
                logger.log("Não foi encontrado nenhuma invoice com access_key '" + invoiceAccessKey + "'.\n");
                return null;
            }

            return list.get(0).toString();

        } catch (HibernateException e) {
            logger.log("Erro na inicialização da SessionFactory devido '"+  e.getMessage() + "'.\n");
            throw new RuntimeException("Erro na inicialização da sessão com banco.", e);

        } catch (Exception e) {
            logger.log("Erro ao executar comando '" + QUERY + "', devido '" + e.getMessage() + "'.\n");
            throw new RuntimeException("Erro ao executar consulta no banco.", e);
        }
    }
}
