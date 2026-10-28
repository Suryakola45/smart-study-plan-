<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Study Schedule - AI Smart Study Planner" scope="request"/>
<c:set var="activePage" value="schedule" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>
<jsp:include page="/WEB-INF/views/layout/navbar.jsp"/>

<div class="container py-4">
    <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

    <!-- Header & Planner Control Center -->
    <div class="card stat-card p-4 mb-4 shadow-sm border-0">
        <div class="row align-items-center">
            <div class="col-lg-6 mb-3 mb-lg-0">
                <h3 class="fw-bold mb-1">
                    <i class="fa-solid fa-calendar-days text-primary me-2"></i> Study Schedule Generator
                </h3>
                <p class="text-muted small mb-0">
                    Schedules pending topics into your available time slots using PriorityQueue, Collections, and AI Rules.
                </p>
            </div>
            <div class="col-lg-6 text-lg-end">
                <form action="${pageContext.request.contextPath}/schedule/generate" method="post" class="d-inline-flex flex-wrap gap-2 justify-content-lg-end align-items-center">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">

                    <div class="input-group input-group-sm" style="width: auto;">
                        <span class="input-group-text bg-light"><i class="fa-solid fa-sliders"></i></span>
                        <select name="strategy" class="form-select form-select-sm" title="Select Planning Strategy">
                            <option value="PRIORITY_BASED" ${currentStrategy == 'PRIORITY_BASED' ? 'selected' : ''}>
                                AI Priority-Based (0.35D+0.30U+0.20W+0.15P)
                            </option>
                            <option value="DEADLINE_BASED" ${currentStrategy == 'DEADLINE_BASED' ? 'selected' : ''}>
                                Deadline-First (Closest Exam Date)
                            </option>
                        </select>
                    </div>

                    <div class="btn-group btn-group-sm">
                        <input type="hidden" name="startDate" value="${targetDate}">
                        <button type="submit" name="rangeType" value="week" class="btn btn-primary">
                            <i class="fa-solid fa-wand-magic-sparkles me-1"></i> Generate 7-Day Plan
                        </button>
                        <button type="submit" name="rangeType" value="day" class="btn btn-outline-primary">
                            Daily
                        </button>
                    </div>
                </form>

                <form action="${pageContext.request.contextPath}/schedule/reschedule" method="post" class="d-inline ms-1">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <button type="submit" class="btn btn-sm btn-outline-warning shadow-sm" onclick="return confirm('Reschedule reorganizes missed/pending sessions while strictly preserving your completed study history. Continue?')">
                        <i class="fa-solid fa-arrows-rotate me-1"></i> AI Reschedule
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- View Navigation Bar (Week / Day selector) -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="btn-group btn-group-sm">
            <a href="${pageContext.request.contextPath}/schedule?view=week&date=${startDate}"
               class="btn ${viewMode == 'week' ? 'btn-primary' : 'btn-outline-secondary'}">
                <i class="fa-solid fa-calendar-week me-1"></i> Week View
            </a>
            <a href="${pageContext.request.contextPath}/schedule?view=day&date=${targetDate}"
               class="btn ${viewMode == 'day' ? 'btn-primary' : 'btn-outline-secondary'}">
                <i class="fa-solid fa-calendar-day me-1"></i> Day View
            </a>
        </div>
        <div class="text-muted small">
            Displaying: <strong>${startDate}</strong> to <strong>${endDate}</strong> (${sessions.size()} sessions planned)
        </div>
    </div>

    <!-- Scheduled Sessions Calendar by Day -->
    <c:choose>
        <c:when test="${empty sessions}">
            <div class="card stat-card p-5 text-center shadow-sm">
                <div class="text-muted mb-3" style="font-size: 3rem;"><i class="fa-solid fa-calendar-xmark"></i></div>
                <h5 class="fw-bold">No sessions scheduled for this period</h5>
                <p class="text-muted mb-3">Make sure you have added your study availability windows, then click "Generate 7-Day Plan".</p>
                <div>
                    <a href="${pageContext.request.contextPath}/availability" class="btn btn-outline-primary btn-sm me-2">
                        <i class="fa-solid fa-clock me-1"></i> Manage Availability
                    </a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <c:forEach var="entry" items="${sessionsByDate}">
                    <c:set var="dayDate" value="${entry.key}"/>
                    <c:set var="daySessions" value="${entry.value}"/>

                    <div class="col-12">
                        <div class="card stat-card p-3 shadow-sm border-0">
                            <div class="d-flex justify-content-between align-items-center border-bottom pb-2 mb-3">
                                <h6 class="fw-bold mb-0 text-primary">
                                    <i class="fa-regular fa-calendar me-2"></i> ${dayDate}
                                    <c:if test="${dayDate == today}">
                                        <span class="badge bg-primary ms-2">Today</span>
                                    </c:if>
                                </h6>
                                <span class="badge bg-light text-muted border">${daySessions.size()} session(s)</span>
                            </div>

                            <c:choose>
                                <c:when test="${empty daySessions}">
                                    <p class="text-muted small mb-0 py-2">No study sessions allocated for this date.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="row g-3">
                                        <c:forEach var="s" items="${daySessions}">
                                            <div class="col-md-6 col-lg-4">
                                                <div class="card h-100 p-3 shadow-sm session-timeline-card ${s.status == 'COMPLETED' ? 'session-completed' : (s.status == 'SKIPPED' ? 'session-skipped' : (s.status == 'PARTIALLY_COMPLETED' ? 'session-partial' : ''))}"
                                                     style="border-left-color: ${s.subjectColorHex} !important;">

                                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                                        <span class="badge text-white" style="background-color: ${s.subjectColorHex};">
                                                            <c:out value="${s.subjectCode}"/>
                                                        </span>
                                                        <c:choose>
                                                            <c:when test="${s.status == 'COMPLETED'}">
                                                                <span class="badge bg-success small"><i class="fa-solid fa-check"></i> Completed</span>
                                                            </c:when>
                                                            <c:when test="${s.status == 'SKIPPED'}">
                                                                <span class="badge bg-secondary small"><i class="fa-solid fa-forward"></i> Skipped</span>
                                                            </c:when>
                                                            <c:when test="${s.status == 'PARTIALLY_COMPLETED'}">
                                                                <span class="badge bg-warning text-dark small"><i class="fa-solid fa-adjust"></i> Partial</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-primary-subtle text-primary small">Scheduled</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    <h6 class="fw-bold mb-1"><c:out value="${s.topicTitle}"/></h6>
                                                    <div class="small text-muted mb-2">Course: <c:out value="${s.subjectName}"/></div>

                                                    <div class="small text-secondary mb-3">
                                                        <i class="fa-regular fa-clock me-1"></i> ${s.startTime} - ${s.endTime} (${s.durationMinutes} mins)
                                                        <c:if test="${s.actualMinutesStudied > 0}">
                                                            <div class="text-success fw-semibold mt-1">
                                                                <i class="fa-solid fa-check-circle me-1"></i> Recorded: ${s.actualMinutesStudied} mins studied
                                                            </div>
                                                        </c:if>
                                                        <c:if test="${not empty s.notes}">
                                                            <div class="fst-italic text-muted mt-1 small">
                                                                &ldquo;<c:out value="${s.notes}"/>&rdquo;
                                                            </div>
                                                        </c:if>
                                                    </div>

                                                    <c:if test="${s.status == 'SCHEDULED'}">
                                                        <div class="mt-auto pt-2 border-top d-flex justify-content-end gap-1">
                                                            <button type="button" class="btn btn-sm btn-success" data-bs-toggle="modal" data-bs-target="#completeSessionModal"
                                                                    data-session-id="${s.id}" data-topic-title="<c:out value='${s.topicTitle}'/>" data-duration="${s.durationMinutes}" title="Complete">
                                                                <i class="fa-solid fa-check me-1"></i> Done
                                                            </button>
                                                            <button type="button" class="btn btn-sm btn-outline-warning" data-bs-toggle="modal" data-bs-target="#partialSessionModal"
                                                                    data-session-id="${s.id}" data-topic-title="<c:out value='${s.topicTitle}'/>" data-duration="${s.durationMinutes}" title="Partial">
                                                                <i class="fa-solid fa-hourglass-start me-1"></i> Partial
                                                            </button>
                                                            <form action="${pageContext.request.contextPath}/session/action" method="post" class="d-inline" onsubmit="return confirm('Skip this session?')">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="id" value="${s.id}">
                                                                <input type="hidden" name="action" value="skip">
                                                                <input type="hidden" name="returnUrl" value="/schedule">
                                                                <button type="submit" class="btn btn-sm btn-outline-secondary" title="Skip">
                                                                    <i class="fa-solid fa-forward"></i>
                                                                </button>
                                                            </form>
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <!-- Top-Level Complete Session Modal -->
    <div class="modal fade" id="completeSessionModal" tabindex="-1" aria-labelledby="completeSessionModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <form action="${pageContext.request.contextPath}/session/action" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="id" id="completeSessionId" value="">
                <input type="hidden" name="action" value="complete">
                <input type="hidden" name="returnUrl" value="/schedule">
                <div class="modal-content shadow">
                    <div class="modal-header bg-success text-white">
                        <h6 class="modal-title fw-bold" id="completeSessionModalLabel">
                            <i class="fa-solid fa-circle-check me-2"></i> Complete Study Session
                        </h6>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p class="mb-3">Topic: <strong id="completeTopicTitle" class="text-primary"></strong></p>
                        <div class="mb-3">
                            <label for="completeActualMinutes" class="form-label fw-semibold">Actual Minutes Studied</label>
                            <input type="number" name="actualMinutes" id="completeActualMinutes" class="form-control" value="45" min="1" required>
                            <div class="form-text">Minutes recorded towards completing this topic.</div>
                        </div>
                        <div class="mb-3">
                            <label for="completeNotes" class="form-label fw-semibold">Study Notes</label>
                            <textarea name="notes" id="completeNotes" class="form-control" rows="3" placeholder="Summary of what you learned..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer bg-light">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-success btn-sm px-3">
                            <i class="fa-solid fa-check me-1"></i> Confirm Completed
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Top-Level Partial Session Modal -->
    <div class="modal fade" id="partialSessionModal" tabindex="-1" aria-labelledby="partialSessionModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <form action="${pageContext.request.contextPath}/session/action" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="id" id="partialSessionId" value="">
                <input type="hidden" name="action" value="partial">
                <input type="hidden" name="returnUrl" value="/schedule">
                <div class="modal-content shadow">
                    <div class="modal-header bg-warning text-dark">
                        <h6 class="modal-title fw-bold" id="partialSessionModalLabel">
                            <i class="fa-solid fa-hourglass-half me-2"></i> Partial Study Duration
                        </h6>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p class="mb-3">Topic: <strong id="partialTopicTitle" class="text-primary"></strong></p>
                        <div class="mb-3">
                            <label for="partialActualMinutes" class="form-label fw-semibold">Actual Minutes Studied</label>
                            <input type="number" name="actualMinutes" id="partialActualMinutes" class="form-control" value="30" min="1" required>
                            <div class="form-text">Recorded minutes will count towards total topic progress.</div>
                        </div>
                        <div class="mb-3">
                            <label for="partialNotes" class="form-label fw-semibold">Notes</label>
                            <textarea name="notes" id="partialNotes" class="form-control" rows="3" placeholder="Notes on partial progress..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer bg-light">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning btn-sm px-3">
                            <i class="fa-solid fa-save me-1"></i> Save Partial
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

