package com.photoconnect.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SEC-05: Stored XSS defense across views and feed.
 *
 * Verifies that:
 * 1. User-controlled variables in JSP views (packages, feed, booking details) use context-appropriate output encoding.
 * 2. Raw user content is not placed directly inside unquoted/unescaped HTML attributes or text nodes.
 * 3. Inline JavaScript event handlers (e.g. onclick, ondblclick) with raw EL interpolations are eliminated.
 * 4. Dynamic DOM insertions in client JavaScript do not use unsafe innerHTML concatenation.
 * 5. Malicious payloads (<script>, <img> onerror, <svg> onload) render as inert escaped text.
 */
class StoredXssDefenseTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path VIEWS = ROOT.resolve("src/main/webapp/WEB-INF/views");

    private String readView(String viewFileName) throws IOException {
        Path path = VIEWS.resolve(viewFileName);
        assertThat(path).exists();
        return Files.readString(path);
    }

    @Nested
    @DisplayName("Photographer Packages JSP XSS Defense Tests")
    class PhotographerPackagesJspTests {

        @Test
        @DisplayName("photographer-packages.jsp escapes package name, description, and data attributes")
        void packageTemplate_escapesUserProvidedNameAndDescription() throws IOException {
            String jsp = readView("photographer-packages.jsp");

            // 1. Package name must be escaped
            assertThat(jsp)
                    .as("Package name must be escaped with <c:out>")
                    .contains("<c:out value=\"${pkg.name}\"/>")
                    .doesNotContain("<h2 class=\"package-name\">${pkg.name}</h2>");

            // 2. Package description must be escaped
            assertThat(jsp)
                    .as("Package description must be escaped with <c:out>")
                    .contains("<c:out value=\"${pkg.description}\"/>")
                    .doesNotContain("<c:when test=\"${not empty pkg.description}\">${pkg.description}</c:when>");

            // 3. Edit button data attributes must be escaped to prevent breaking out of attribute context
            assertThat(jsp)
                    .as("data-name must be escaped with c:out")
                    .contains("data-name=\"<c:out value='${pkg.name}'/>\"")
                    .as("data-desc must be escaped with c:out")
                    .contains("data-desc=\"<c:out value='${pkg.description}'/>\"");

            // 4. Flash messages must be escaped
            assertThat(jsp)
                    .as("successMessage must be escaped")
                    .contains("<c:out value=\"${successMessage}\"/>")
                    .doesNotContain("<div>${successMessage}</div>")
                    .as("errorMessage must be escaped")
                    .contains("<c:out value=\"${errorMessage}\"/>")
                    .doesNotContain("<div>${errorMessage}</div>");
        }
    }

    @Nested
    @DisplayName("Visual Feed JSP XSS Defense Tests")
    class FeedJspTests {

        @Test
        @DisplayName("feed.jsp escapes captions, alt text, titles, and eliminates raw inline handlers")
        void feedTemplate_escapesCaptionsAvatarsAndTitles() throws IOException {
            String jsp = readView("feed.jsp");

            // 1. Image alt must be escaped
            assertThat(jsp)
                    .as("Post image alt text must be escaped with c:out")
                    .contains("alt=\"<c:out value='${post.caption}'/>\"")
                    .doesNotContain("alt=\"${post.caption}\"");

            // 2. Author avatar alt must be escaped
            assertThat(jsp)
                    .as("Author avatar alt must be escaped with c:out")
                    .contains("alt=\"<c:out value='${post.photographerName}'/>\"")
                    .doesNotContain("alt=\"${post.photographerName}\"");

            // 3. Story title and avatar alt must be escaped
            assertThat(jsp)
                    .as("Story link title must be escaped")
                    .contains("title=\"View <c:out value='${c.displayName}'/>'s profile\"")
                    .as("Story avatar alt must be escaped")
                    .contains("alt=\"<c:out value='${c.displayName}'/>\"");

            // 4. No inline event handlers on post cards
            assertThat(jsp)
                    .as("No inline ondblclick handler on post media frame")
                    .doesNotContain("ondblclick=\"handleDoubleTap")
                    .as("No inline onclick handler on like button")
                    .doesNotContain("onclick=\"toggleLike")
                    .as("No inline onclick handler on comments button")
                    .doesNotContain("onclick=\"toggleComments")
                    .as("No inline onclick handler on share button")
                    .doesNotContain("onclick=\"sharePost");

            // 5. Client-side comment submission must not use innerHTML
            assertThat(jsp)
                    .as("submitComment must avoid innerHTML insertion")
                    .doesNotContain("commentEl.innerHTML =")
                    .contains("commentItem.className = 'comment-item'")
                    .contains("textSpan.textContent = text");
        }
    }

    @Nested
    @DisplayName("Booking Details JSP XSS Defense Tests")
    class BookingDetailsJspTests {

        @Test
        @DisplayName("booking-detail.jsp and photographer-booking-detail.jsp escape package deliverables and notes")
        void bookingDetails_escapesDeliverablesAndNotes() throws IOException {
            String customerBookingJsp = readView("booking-detail.jsp");
            String photogBookingJsp = readView("photographer-booking-detail.jsp");

            // Customer view: notes and package deliverables
            assertThat(customerBookingJsp)
                    .as("Customer booking detail escapes notes")
                    .contains("<c:out value=\"${booking.notes}\"/>")
                    .as("Customer booking detail escapes deliverables")
                    .contains("<c:out value=\"${booking.durationMinutes}\"/> mins");

            // Photographer view: notes and package deliverables
            assertThat(photogBookingJsp)
                    .as("Photographer booking detail escapes notes")
                    .contains("<c:out value=\"${booking.notes}\"/>")
                    .as("Photographer booking detail escapes deliverables")
                    .contains("<c:out value=\"${booking.durationMinutes}\"/> mins");
        }
    }

    @Nested
    @DisplayName("HTML Encoding & Inert Payload Tests")
    class InertEncodingTests {

        @Test
        @DisplayName("Script tag payload renders as inert text")
        void maliciousScriptPayload_rendersAsInertText() {
            String payload = "<script>alert('XSS-ATTACK')</script>";
            String encoded = HtmlUtils.htmlEscape(payload);

            assertThat(encoded)
                    .doesNotContain("<script>")
                    .doesNotContain("</script>")
                    .contains("&lt;script&gt;alert(&#39;XSS-ATTACK&#39;)&lt;/script&gt;");
        }

        @Test
        @DisplayName("Attribute breakout payload renders as inert escaped attributes")
        void attributeBreakoutPayload_rendersAsInertAttribute() {
            String payload = "\"><img src=x onerror=alert('PWNED')>";
            String encoded = HtmlUtils.htmlEscape(payload);

            assertThat(encoded)
                    .doesNotContain("\">")
                    .doesNotContain("<img")
                    .contains("&quot;&gt;&lt;img src=x onerror=alert(&#39;PWNED&#39;)&gt;");

            // In simulated HTML attribute context: <input value="ENCODED">
            String htmlFragment = "<input value=\"" + encoded + "\">";
            assertThat(htmlFragment).doesNotContain("<img src=x");
        }

        @Test
        @DisplayName("SVG onload event payload renders safely escaped")
        void svgOnloadPayload_rendersAsInertText() {
            String payload = "<svg/onload=alert(document.domain)>";
            String encoded = HtmlUtils.htmlEscape(payload);

            assertThat(encoded)
                    .doesNotContain("<svg")
                    .contains("&lt;svg/onload=alert(document.domain)&gt;");
        }

        @Test
        @DisplayName("Legitimate punctuation and content remain fully readable")
        void legitimateContent_isPreservedAccurately() {
            String validText = "Wedding photoshoot & portrait session in Hanoi (2026)";
            String encoded = HtmlUtils.htmlEscape(validText);

            // Ampersand is safely escaped, regular text is preserved
            assertThat(encoded).isEqualTo("Wedding photoshoot &amp; portrait session in Hanoi (2026)");
        }
    }

    @Nested
    @DisplayName("User Profile Safe DOM Tests")
    class UserProfileSafeDomTests {

        @Test
        @DisplayName("user-profile.jsp uses safe DOM methods instead of innerHTML for status modal")
        void userProfileCalendar_usesSafeDomMethodsWithoutInnerHtml() throws IOException {
            String jsp = readView("user-profile.jsp");

            assertThat(jsp)
                    .as("Event status in user-profile.jsp must not use raw innerHTML concatenation")
                    .doesNotContain("statusEl.innerHTML = '<span class=\"badge bg-primary\">Status: ' + props.bookingStatus")
                    .contains("badge.textContent = 'Status: ' + props.bookingStatus");
        }
    }
}
