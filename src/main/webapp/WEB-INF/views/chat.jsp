<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat - Booking #${booking.id} - PhotoConnect</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/photoconnect.css">
    <style>
        .chat-container {
            max-width: 800px;
            margin: 0 auto;
        }
        .chat-box {
            background: var(--bg-dark-secondary, #101014);
            border: 1px solid var(--border, #26262e);
            display: flex;
            flex-direction: column;
            height: 520px;
        }
        .chat-messages {
            flex: 1;
            overflow-y: auto;
            padding: 1.5rem;
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        .message-bubble {
            max-width: 75%;
            padding: 0.85rem 1.15rem;
            border-radius: 4px;
            font-size: 0.95rem;
            line-height: 1.5;
            word-wrap: break-word;
        }
        .message-mine {
            align-self: flex-end;
            background: #1c1c24;
            border: 1px solid rgba(201, 169, 110, 0.35);
            color: #f3f4f6;
        }
        .message-mine .message-sender {
            color: var(--accent-gold, #c9a96e);
        }
        .message-partner {
            align-self: flex-start;
            background: #14141a;
            border: 1px solid var(--border, #26262e);
            color: #e5e7eb;
        }
        .message-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 1rem;
            margin-bottom: 0.35rem;
            font-size: 0.75rem;
            letter-spacing: 0.03em;
        }
        .message-sender {
            font-weight: 600;
        }
        .message-time {
            color: var(--text-muted, #9ca3af);
        }
        .chat-input-bar {
            padding: 1rem 1.5rem;
            border-top: 1px solid var(--border, #26262e);
            background: var(--bg-dark, #08080a);
            display: flex;
            gap: 1rem;
            align-items: center;
        }
        .chat-input-field {
            flex: 1;
            min-width: 0;
            padding: 0.85rem 1.15rem;
            background: #101014;
            border: 1px solid var(--border, #26262e);
            color: var(--text-color, #ffffff);
            font-family: inherit;
            font-size: 0.95rem;
            border-radius: 2px;
            outline: none;
            transition: border-color 0.2s ease;
        }
        @media (max-width: 640px) {
            .chat-box { height: min(520px, 68vh); }
            .chat-messages { padding: 1rem; }
            .message-bubble { max-width: 90%; }
            .chat-input-bar { padding: 0.75rem; gap: 0.75rem; }
            .chat-input-bar button { padding-inline: 1rem !important; }
        }
        .chat-input-field:focus {
            border-color: var(--accent-gold, #c9a96e);
        }
        .connection-indicator {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.8rem;
            color: var(--text-muted, #9ca3af);
        }
        .dot-indicator {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #f59e0b;
        }
        .dot-connected {
            background: #10b981;
        }
    </style>
</head>
<body>
    <jsp:include page="fragments/navbar.jsp" />

    <main style="padding: 6rem 0; min-height: 85vh;">
        <div class="editorial-container chat-container">
            <div class="pc-stack-mobile" style="margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center; gap: 1rem;">
                <a href="${backUrl}" class="text-link" style="font-size: 0.9rem;">&larr; Back to Booking Details</a>
                <div id="connStatus" class="connection-indicator">
                    <span id="connDot" class="dot-indicator"></span>
                    <span id="connText">Connecting...</span>
                </div>
            </div>

            <div style="margin-bottom: 2rem;">
                <h1 class="editorial-title" style="font-size: 2rem; margin-bottom: 0.5rem;">Conversation with <c:out value="${partnerName}"/></h1>
                <p style="color: var(--text-muted); font-size: 0.95rem;">
                    Booking #${booking.id} &bull; 
                    <span style="color: var(--text-color); font-weight: 500;">${booking.status}</span> &bull; 
                    Role: <c:out value="${partnerRole}"/>
                </p>
            </div>

            <div class="chat-box">
                <div id="messagesContainer"
                     class="chat-messages"
                     data-booking-id="${booking.id}"
                     data-current-user-id="${currentUserId}"
                     data-context-path="${pageContext.request.contextPath}">
                    <c:choose>
                        <c:when test="${empty messages}">
                            <div id="emptyNotice" style="text-align: center; color: var(--text-muted); margin: auto; padding: 2rem;">
                                No messages yet. Send a message to begin your conversation.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="msg" items="${messages}">
                                <div class="message-bubble ${msg.senderId == currentUserId ? 'message-mine' : 'message-partner'}">
                                    <div class="message-meta">
                                        <span class="message-sender">
                                            <c:out value="${msg.senderId == currentUserId ? 'You' : msg.senderName}" />
                                        </span>
                                        <span class="message-time"><c:out value="${msg.formattedSentAt}" /></span>
                                    </div>
                                    <div><c:out value="${msg.content}" /></div>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>

                <form id="chatForm" class="chat-input-bar">
                    <input type="text"
                           id="messageInput"
                           class="chat-input-field"
                           placeholder="Type your message here..."
                           autocomplete="off"
                           maxlength="2000"
                           required />
                    <button type="submit" id="sendBtn" class="btn btn-primary" style="padding: 0.85rem 1.75rem; white-space: nowrap;">
                        Send
                    </button>
                </form>
            </div>
        </div>
    </main>

    <!-- SockJS and STOMP client scripts -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

    <script>
        (function () {
            'use strict';

            const messagesContainer = document.getElementById('messagesContainer');
            if (!messagesContainer) return;

            const bookingId = parseInt(messagesContainer.dataset.bookingId, 10);
            const currentUserId = parseInt(messagesContainer.dataset.currentUserId, 10);
            const contextPath = messagesContainer.dataset.contextPath || '';
            const messageInput = document.getElementById('messageInput');
            const chatForm = document.getElementById('chatForm');
            const connDot = document.getElementById('connDot');
            const connText = document.getElementById('connText');

            let stompClient = null;
            let isStompConnected = false;

            function scrollToBottom() {
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }
            scrollToBottom();

            function appendMessage(msg) {
                const emptyNotice = document.getElementById('emptyNotice');
                if (emptyNotice) {
                    emptyNotice.remove();
                }

                const isMine = (msg.senderId === currentUserId);
                const bubble = document.createElement('div');
                bubble.className = 'message-bubble ' + (isMine ? 'message-mine' : 'message-partner');

                const meta = document.createElement('div');
                meta.className = 'message-meta';

                const senderSpan = document.createElement('span');
                senderSpan.className = 'message-sender';
                senderSpan.textContent = isMine ? 'You' : (msg.senderName || 'User');

                const timeSpan = document.createElement('span');
                timeSpan.className = 'message-time';
                timeSpan.textContent = msg.formattedSentAt || 'Just now';

                meta.appendChild(senderSpan);
                meta.appendChild(timeSpan);

                const contentDiv = document.createElement('div');
                contentDiv.textContent = msg.content;

                bubble.appendChild(meta);
                bubble.appendChild(contentDiv);
                messagesContainer.appendChild(bubble);

                scrollToBottom();
            }

            // Initialize WebSocket connection
            function connectWebSocket() {
                try {
                    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
                        throw new Error('STOMP/SockJS libraries unavailable, using REST fallback');
                    }

                    const socket = new SockJS(contextPath + '/ws');
                    stompClient = Stomp.over(socket);
                    stompClient.debug = null; // Disable noisy console logging

                    stompClient.connect({}, function (frame) {
                        isStompConnected = true;
                        if (connDot) connDot.classList.add('dot-connected');
                        if (connText) connText.textContent = 'Live (WebSocket)';

                        stompClient.subscribe('/topic/booking/' + bookingId + '/chat', function (response) {
                            try {
                                const message = JSON.parse(response.body);
                                appendMessage(message);
                            } catch (e) {
                                console.error('Failed to parse chat message payload', e);
                            }
                        });
                    }, function (error) {
                        isStompConnected = false;
                        if (connDot) connDot.classList.remove('dot-connected');
                        if (connText) connText.textContent = 'Connected (REST Fallback)';
                        startRestPolling();
                    });
                } catch (err) {
                    isStompConnected = false;
                    if (connDot) connDot.classList.remove('dot-connected');
                    if (connText) connText.textContent = 'Connected (REST Mode)';
                    startRestPolling();
                }
            }

            let pollingInterval = null;
            function startRestPolling() {
                if (pollingInterval) return;
                pollingInterval = setInterval(function () {
                    fetch(contextPath + '/api/bookings/' + bookingId + '/messages')
                        .then(res => res.json())
                        .then(res => {
                            if (res.success && Array.isArray(res.data)) {
                                // Update messages container if new messages received
                                const currentCount = messagesContainer.querySelectorAll('.message-bubble').length;
                                if (res.data.length > currentCount) {
                                    messagesContainer.innerHTML = '';
                                    res.data.forEach(appendMessage);
                                }
                            }
                        })
                        .catch(err => console.debug('Polling error', err));
                }, 3000);
            }

            if (chatForm && messageInput) {
                chatForm.addEventListener('submit', function (e) {
                    e.preventDefault();
                    const content = messageInput.value.trim();
                    if (!content) return;

                    messageInput.disabled = true;

                    if (isStompConnected && stompClient) {
                        stompClient.send('/app/chat.send', {}, JSON.stringify({
                            bookingId: bookingId,
                            content: content
                        }));
                        messageInput.value = '';
                        messageInput.disabled = false;
                        messageInput.focus();
                    } else {
                        // Fallback to REST API
                        fetch(contextPath + '/api/bookings/' + bookingId + '/messages', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                bookingId: bookingId,
                                content: content
                            })
                        })
                        .then(res => res.json())
                        .then(res => {
                            if (res.success && res.data) {
                                appendMessage(res.data);
                            }
                            messageInput.value = '';
                        })
                        .catch(err => {
                            alert('Failed to send message: ' + err.message);
                        })
                        .finally(() => {
                            messageInput.disabled = false;
                            messageInput.focus();
                        });
                    }
                });
            }

            connectWebSocket();
        })();
    </script>
</body>
</html>
