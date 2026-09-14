<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PhotoConnect | <c:out value="${errorTitle != null ? errorTitle : 'Notice'}"/></title>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .error-viewport {
            min-height: calc(100vh - 80px);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 3rem 1.5rem;
            background: radial-gradient(circle at 50% 30%, rgba(201, 169, 110, 0.04) 0%, rgba(8, 8, 10, 0.95) 70%);
        }

        .error-card {
            background: var(--bg-card, #121217);
            border: 1px solid rgba(255, 255, 255, 0.08);
            border-radius: 12px;
            padding: 3.5rem 3rem;
            max-width: 600px;
            width: 100%;
            text-align: center;
            box-shadow: 0 24px 64px rgba(0, 0, 0, 0.6);
            backdrop-filter: blur(12px);
        }

        .error-status-badge {
            display: inline-block;
            font-family: 'Playfair Display', serif;
            font-size: 4.5rem;
            font-weight: 700;
            line-height: 1;
            letter-spacing: -0.04em;
            color: var(--accent-gold, #c9a96e);
            margin-bottom: 0.75rem;
            text-shadow: 0 4px 20px rgba(201, 169, 110, 0.2);
        }

        .error-code-pill {
            display: inline-block;
            padding: 0.35rem 0.85rem;
            background: rgba(255, 255, 255, 0.04);
            border: 1px solid rgba(255, 255, 255, 0.12);
            border-radius: 20px;
            font-size: 0.75rem;
            font-family: 'Inter', monospace;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--text-muted, #9e9ea7);
            margin-bottom: 1.5rem;
        }

        .error-title {
            font-family: 'Playfair Display', serif;
            font-size: 1.85rem;
            font-weight: 600;
            color: #ffffff;
            margin: 0 0 1rem 0;
            letter-spacing: -0.01em;
        }

        .error-description {
            font-size: 1rem;
            line-height: 1.6;
            color: var(--text-muted, #a1a1aa);
            margin: 0 auto 2.25rem auto;
            max-width: 460px;
        }

        .error-actions {
            display: flex;
            gap: 1rem;
            justify-content: center;
            flex-wrap: wrap;
        }

        .btn-gold {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 0.85rem 1.8rem;
            background: var(--accent-gold, #c9a96e);
            color: #08080a;
            font-weight: 600;
            font-size: 0.9rem;
            border-radius: 6px;
            text-decoration: none;
            transition: all 0.25s ease;
            border: 1px solid var(--accent-gold, #c9a96e);
        }

        .btn-gold:hover {
            background: #dfc89e;
            border-color: #dfc89e;
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(201, 169, 110, 0.25);
        }

        .btn-outline {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 0.85rem 1.8rem;
            background: transparent;
            color: #ffffff;
            font-weight: 500;
            font-size: 0.9rem;
            border-radius: 6px;
            text-decoration: none;
            border: 1px solid rgba(255, 255, 255, 0.2);
            transition: all 0.25s ease;
        }

        .btn-outline:hover {
            border-color: rgba(255, 255, 255, 0.5);
            background: rgba(255, 255, 255, 0.05);
            transform: translateY(-2px);
        }
    </style>
</head>
<body>

    <%@ include file="fragments/navbar.jsp" %>

    <main class="error-viewport">
        <div class="error-card">
            <div class="error-status-badge">
                <c:out value="${statusCode != null ? statusCode : 'Notice'}"/>
            </div>

            <c:if test="${not empty errorCode}">
                <div>
                    <span class="error-code-pill">
                        <c:out value="${errorCode}"/>
                    </span>
                </div>
            </c:if>

            <h1 class="error-title">
                <c:out value="${errorTitle != null ? errorTitle : 'Unexpected Request'}"/>
            </h1>

            <p class="error-description">
                <c:out value="${errorMessage != null ? errorMessage : 'The requested operation could not be completed.'}"/>
            </p>

            <div class="error-actions">
                <a href="/" class="btn-gold">Return to Home</a>
                <a href="/photographers" class="btn-outline">Explore Marketplace</a>
                <button type="button" onclick="window.history.back()" class="btn-outline" style="cursor: pointer;">Previous Page</button>
            </div>
        </div>
    </main>

</body>
</html>
