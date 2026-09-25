package com.photoconnect.ui;

import com.photoconnect.config.SiteMeshConfig;
import org.junit.jupiter.api.Test;
import org.sitemesh.DecoratorSelector;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ObjectFactory;
import org.sitemesh.config.xml.Xml;
import org.sitemesh.config.xml.XmlFilterConfigurator;
import org.sitemesh.webapp.WebAppContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies TASK-A03 contract compliance for SiteMesh 3 decorator integration:
 * 1. SiteMesh 3 Jakarta-compatible dependency exists in pom.xml.
 * 2. SiteMesh 3 configuration exists with required decorator mappings and exclusions.
 * 3. Decorators exist (default.jsp and admin.jsp).
 * 4. Default decorator loads Bootstrap CSS before PhotoConnect CSS, includes navbar and body write.
 * 5. Admin decorator is distinct, has admin-layout, and includes navbar and body write.
 * 6. Content JSPs (except excluded error.jsp) no longer duplicate outer HTML shell (DOCTYPE, html, body, navbar).
 * 7. Bootstrap CSS is centralized in decorators and no longer duplicated across content JSPs.
 * 8. Error page remains self-contained and is excluded from decoration.
 * 9. Critical page-specific scripts and styles are preserved (chat STOMP/SockJS, payment scripts).
 * 10. Chat page maintains a single currentUserId declaration and valid script boundaries.
 * 11. TASK-A01 Bootstrap primitives remain intact in representative pages.
 * 12. Regression test: Effective decorator path resolution prevents double-prefixing.
 */
class SiteMeshIntegrationContractTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path VIEWS = ROOT.resolve("src/main/webapp/WEB-INF/views");
    private static final Path DECORATORS = ROOT.resolve("src/main/webapp/WEB-INF/decorators");

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }

    private static class StubWebAppContext extends WebAppContext {
        private final String customPath;

        public StubWebAppContext(String customPath) {
            super("text/html", null, null, null, null, null, false);
            this.customPath = customPath;
        }

        @Override
        public String getPath() {
            return customPath;
        }
    }

    private static WebAppContext createContext(String path) {
        return new StubWebAppContext(path);
    }

    @Test
    void pomXmlContainsSiteMesh3JakartaDependency() throws IOException {
        String pom = read("pom.xml");
        assertThat(pom)
                .as("SiteMesh groupId present in pom.xml")
                .contains("<groupId>org.sitemesh</groupId>")
                .as("SiteMesh artifactId present in pom.xml")
                .contains("<artifactId>sitemesh</artifactId>")
                .as("SiteMesh Jakarta-compatible version 3.3.0-RC1 specified")
                .contains("<version>3.3.0-RC1</version>");
    }

    @Test
    void siteMeshConfigurationAndFilterExistWithRequiredRules() throws IOException {
        String sitemeshXml = read("src/main/webapp/WEB-INF/sitemesh3.xml");
        assertThat(sitemeshXml)
                .as("Defines decorator prefix")
                .contains("<decorator-prefix>/WEB-INF/decorators/</decorator-prefix>")
                .as("Excludes static assets")
                .contains("<mapping path=\"/assets/*\" exclude=\"true\"/>")
                .as("Excludes WebSocket endpoints")
                .contains("<mapping path=\"/ws/*\" exclude=\"true\"/>")
                .as("Excludes API endpoints")
                .contains("<mapping path=\"/api/*\" exclude=\"true\"/>")
                .as("Excludes error dispatch")
                .contains("<mapping path=\"/error\" exclude=\"true\"/>")
                .contains("<mapping path=\"/error/*\" exclude=\"true\"/>")
                .as("Maps admin routes to admin decorator relative to prefix")
                .contains("<mapping path=\"/admin/*\" decorator=\"admin.jsp\"/>")
                .as("Maps default routes to default decorator relative to prefix")
                .contains("<mapping path=\"/*\" decorator=\"default.jsp\"/>");

        String configJava = read("src/main/java/com/photoconnect/config/SiteMeshConfig.java");
        assertThat(configJava)
                .as("Spring Boot SiteMeshConfig registers SiteMesh filter")
                .contains("class SiteMeshConfig")
                .contains("FilterRegistrationBean<ConfigurableSiteMeshFilter>")
                .contains("DispatcherType.REQUEST")
                .contains("DispatcherType.FORWARD")
                .contains("DECORATOR_PREFIX = \"/WEB-INF/decorators/\"")
                .contains("DEFAULT_DECORATOR = \"default.jsp\"")
                .contains("ADMIN_DECORATOR = \"admin.jsp\"");
    }

    @Test
    void effectiveDecoratorResolutionPreventsDoublePrefixing() throws Exception {
        // 1. Verify Programmatic Configuration (SiteMeshConfig.configureSiteMesh)
        SiteMeshFilterBuilder progBuilder = new SiteMeshFilterBuilder();
        SiteMeshConfig.configureSiteMesh(progBuilder);
        DecoratorSelector<WebAppContext> progSelector = progBuilder.getDecoratorSelector();

        org.sitemesh.content.Content emptyContent = new org.sitemesh.content.memory.InMemoryContent();

        // Verify public/customer/photographer route resolution
        String[] progDefaultDecs = progSelector.selectDecoratorPaths(emptyContent, createContext("/"));
        assertThat(progDefaultDecs)
                .as("Programmatic config: '/' resolves to exactly /WEB-INF/decorators/default.jsp")
                .containsExactly("/WEB-INF/decorators/default.jsp")
                .doesNotContain("/WEB-INF/decorators/WEB-INF/decorators/default.jsp")
                .doesNotContain("/WEB-INF/decorators//WEB-INF/decorators/default.jsp");

        String[] progPhotoDecs = progSelector.selectDecoratorPaths(emptyContent, createContext("/photographers"));
        assertThat(progPhotoDecs)
                .as("Programmatic config: '/photographers' resolves to exactly /WEB-INF/decorators/default.jsp")
                .containsExactly("/WEB-INF/decorators/default.jsp");

        // Verify admin route resolution
        String[] progAdminDecs = progSelector.selectDecoratorPaths(emptyContent, createContext("/admin/dashboard"));
        assertThat(progAdminDecs)
                .as("Programmatic config: '/admin/dashboard' resolves to exactly /WEB-INF/decorators/admin.jsp")
                .containsExactly("/WEB-INF/decorators/admin.jsp")
                .doesNotContain("/WEB-INF/decorators/WEB-INF/decorators/admin.jsp")
                .doesNotContain("/WEB-INF/decorators//WEB-INF/decorators/admin.jsp");

        // 2. Verify XML Configuration (sitemesh3.xml parsed by XmlFilterConfigurator)
        File xmlFile = ROOT.resolve("src/main/webapp/WEB-INF/sitemesh3.xml").toFile();
        Document doc = Xml.getSecureDocumentBuilder().parse(xmlFile);
        SiteMeshFilterBuilder xmlBuilder = new SiteMeshFilterBuilder();
        XmlFilterConfigurator xmlConfigurator = new XmlFilterConfigurator(new ObjectFactory.Default(), doc.getDocumentElement());
        xmlConfigurator.configureFilter(xmlBuilder);
        DecoratorSelector<WebAppContext> xmlSelector = xmlBuilder.getDecoratorSelector();

        String[] xmlDefaultDecs = xmlSelector.selectDecoratorPaths(emptyContent, createContext("/"));
        assertThat(xmlDefaultDecs)
                .as("XML config: '/' resolves to exactly /WEB-INF/decorators/default.jsp")
                .containsExactly("/WEB-INF/decorators/default.jsp")
                .doesNotContain("/WEB-INF/decorators/WEB-INF/decorators/default.jsp")
                .doesNotContain("/WEB-INF/decorators//WEB-INF/decorators/default.jsp");

        String[] xmlAdminDecs = xmlSelector.selectDecoratorPaths(emptyContent, createContext("/admin/dashboard"));
        assertThat(xmlAdminDecs)
                .as("XML config: '/admin/dashboard' resolves to exactly /WEB-INF/decorators/admin.jsp")
                .containsExactly("/WEB-INF/decorators/admin.jsp")
                .doesNotContain("/WEB-INF/decorators/WEB-INF/decorators/admin.jsp")
                .doesNotContain("/WEB-INF/decorators//WEB-INF/decorators/admin.jsp");

        // 3. Verify that the resolved target files actually exist on disk
        for (String decPath : List.of(progDefaultDecs[0], progAdminDecs[0])) {
            Path fileOnDisk = ROOT.resolve("src/main/webapp" + decPath);
            assertThat(Files.exists(fileOnDisk))
                    .as("Resolved decorator exists on disk: " + decPath)
                    .isTrue();
        }

        // 4. Verify exclusions work as expected
        org.sitemesh.webapp.contentfilter.Selector selector = progBuilder.getSelector();
        MockHttpServletRequest assetReq = new MockHttpServletRequest("GET", "/assets/css/photoconnect.css");
        assetReq.setServletPath("/assets/css/photoconnect.css");
        assertThat(selector.excludePatternInUse(assetReq)).isEqualTo("/assets/*");

        MockHttpServletRequest wsReq = new MockHttpServletRequest("GET", "/ws/chat");
        wsReq.setServletPath("/ws/chat");
        assertThat(selector.excludePatternInUse(wsReq)).isEqualTo("/ws/*");

        MockHttpServletRequest errorReq = new MockHttpServletRequest("GET", "/error");
        errorReq.setServletPath("/error");
        assertThat(selector.excludePatternInUse(errorReq)).isEqualTo("/error");

        MockHttpServletRequest pageReq = new MockHttpServletRequest("GET", "/photographers");
        pageReq.setServletPath("/photographers");
        assertThat(selector.excludePatternInUse(pageReq)).isNull();
    }

    @Test
    void decoratorsExistAndCentralizeShellAndBootstrap() throws IOException {
        String defaultDec = read("src/main/webapp/WEB-INF/decorators/default.jsp");
        String adminDec = read("src/main/webapp/WEB-INF/decorators/admin.jsp");

        // Default decorator contracts
        assertThat(defaultDec)
                .as("Default decorator defines DOCTYPE and html")
                .contains("<!DOCTYPE html>")
                .contains("<html lang=\"en\">")
                .as("Default decorator writes title")
                .contains("<sitemesh:write property='title'>")
                .as("Default decorator loads Bootstrap 5.3.3 CSS")
                .contains("bootstrap@5.3.3/dist/css/bootstrap.min.css")
                .as("Default decorator loads PhotoConnect custom CSS")
                .contains("/assets/css/photoconnect.css")
                .as("Default decorator writes head")
                .contains("<sitemesh:write property='head'/>")
                .as("Default decorator includes shared navbar")
                .contains("/WEB-INF/views/fragments/navbar.jsp")
                .as("Default decorator writes body")
                .contains("<sitemesh:write property='body'/>");

        int defBootstrapIdx = defaultDec.indexOf("bootstrap@5.3.3/dist/css/bootstrap.min.css");
        int defCustomCssIdx = defaultDec.indexOf("/assets/css/photoconnect.css");
        int defHeadWriteIdx = defaultDec.indexOf("<sitemesh:write property='head'/>");

        assertThat(defBootstrapIdx)
                .as("Bootstrap CSS must load before PhotoConnect CSS in default decorator")
                .isLessThan(defCustomCssIdx);
        assertThat(defCustomCssIdx)
                .as("PhotoConnect CSS must load before page-specific head content in default decorator")
                .isLessThan(defHeadWriteIdx);

        // Admin decorator contracts
        assertThat(adminDec)
                .as("Admin decorator defines DOCTYPE and html")
                .contains("<!DOCTYPE html>")
                .contains("<html lang=\"en\">")
                .as("Admin decorator has admin-layout class on body")
                .contains("<body class=\"admin-layout\">")
                .as("Admin decorator loads Bootstrap 5.3.3 CSS")
                .contains("bootstrap@5.3.3/dist/css/bootstrap.min.css")
                .as("Admin decorator loads PhotoConnect custom CSS")
                .contains("/assets/css/photoconnect.css")
                .as("Admin decorator includes navbar")
                .contains("/WEB-INF/views/fragments/navbar.jsp")
                .as("Admin decorator writes body")
                .contains("<sitemesh:write property='body'/>");
    }

    @Test
    void contentJspsDoNotDuplicateHtmlShellOrBootstrapImports() throws IOException {
        try (var files = Files.walk(VIEWS)) {
            List<Path> contentPages = files
                    .filter(path -> path.toString().endsWith(".jsp"))
                    .filter(path -> !path.toString().contains("fragments"))
                    .filter(path -> !path.getFileName().toString().equals("error.jsp"))
                    .toList();

            assertThat(contentPages).hasSize(31);

            for (Path page : contentPages) {
                String jsp = Files.readString(page);

                assertThat(jsp)
                        .as("No DOCTYPE in content JSP %s", page.getFileName())
                        .doesNotContain("<!DOCTYPE html>")
                        .as("No <html> opening tag in content JSP %s", page.getFileName())
                        .doesNotContain("<html")
                        .as("No </html> closing tag in content JSP %s", page.getFileName())
                        .doesNotContain("</html>")
                        .as("No <body> opening tag in content JSP %s", page.getFileName())
                        .doesNotContain("<body")
                        .as("No </body> closing tag in content JSP %s", page.getFileName())
                        .doesNotContain("</body>")
                        .as("No duplicate Bootstrap CSS in content JSP %s", page.getFileName())
                        .doesNotContain("bootstrap@5.3.3/dist/css/bootstrap.min.css")
                        .as("No duplicate navbar include in content JSP %s", page.getFileName())
                        .doesNotContain("fragments/navbar.jsp");
            }
        }
    }

    @Test
    void errorPageRemainsSelfContained() throws IOException {
        String error = read("src/main/webapp/WEB-INF/views/error.jsp");

        assertThat(error)
                .as("Error page retains DOCTYPE")
                .contains("<!DOCTYPE html>")
                .as("Error page retains html tag")
                .contains("<html lang=\"en\">")
                .as("Error page retains Bootstrap CSS for independent error rendering")
                .contains("bootstrap@5.3.3/dist/css/bootstrap.min.css")
                .as("Error page retains PhotoConnect CSS")
                .contains("/assets/css/photoconnect.css")
                .as("Error page retains navbar")
                .contains("fragments/navbar.jsp")
                .as("Error page retains closing tags")
                .contains("</body>")
                .contains("</html>");
    }

    @Test
    void criticalPageScriptsAndStylesPreserved() throws IOException {
        String chat = read("src/main/webapp/WEB-INF/views/chat.jsp");
        String deposit = read("src/main/webapp/WEB-INF/views/deposit.jsp");
        String portfolio = read("src/main/webapp/WEB-INF/views/photographer-portfolio.jsp");

        // Chat critical scripts & styles
        assertThat(chat)
                .as("Chat retains SockJS client script")
                .contains("sockjs-client/1.6.1/sockjs.min.js")
                .as("Chat retains STOMP client script")
                .contains("stomp.js/2.3.3/stomp.min.js")
                .as("Chat retains custom chat-box styling")
                .contains(".chat-box")
                .contains(".message-bubble")
                .as("Chat retains single currentUserId extraction without duplication")
                .contains("const currentUserId = parseInt(messagesContainer.dataset.currentUserId, 10);");

        int currentUserIdCount = chat.split("const currentUserId", -1).length - 1;
        assertThat(currentUserIdCount)
                .as("Exactly one currentUserId declaration in chat.jsp")
                .isEqualTo(1);

        // Deposit payment scripts & styles
        assertThat(deposit)
                .as("Deposit retains payment.css link")
                .contains("/assets/css/payment.css")
                .as("Deposit retains demo-checkout.js script")
                .contains("/assets/js/demo-checkout.js");

        // Portfolio expandable panel
        assertThat(portfolio)
                .as("Portfolio retains upload panel styling")
                .contains(".upload-expandable-panel")
                .as("Portfolio retains upload panel toggle script")
                .contains("uploadDrawerPanel");
    }

    @Test
    void taskA01BootstrapPrimitivesRetainedAcrossMigratedPages() throws IOException {
        String login = read("src/main/webapp/WEB-INF/views/login.jsp");
        String register = read("src/main/webapp/WEB-INF/views/register.jsp");
        String photographers = read("src/main/webapp/WEB-INF/views/photographers.jsp");
        String adminDashboard = read("src/main/webapp/WEB-INF/views/admin-dashboard.jsp");
        String bookingDetail = read("src/main/webapp/WEB-INF/views/booking-detail.jsp");

        assertThat(login).contains("form-control", "form-label", "alert-danger", "btn-primary");
        assertThat(register).contains("form-control", "form-label", "form-text", "alert-danger", "alert-success");
        assertThat(photographers).contains("row g-3", "col-12 col-md-3", "form-control", "form-label", "alert-danger");
        assertThat(adminDashboard).contains("row g-4", "col-12 col-md-4", "col-12 col-md-6", "alert-warning", "alert-success");
        assertThat(bookingDetail).contains("row g-4", "col-12 col-md-6", "alert-success", "alert-danger");
    }
}
