<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Dashboard - AI Smart Study Planner" scope="request"/>
<c:set var="activePage" value="dashboard" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>
<jsp:include page="/WEB-INF/views/layout/navbar.jsp"/>

<div class="container py-4">
    <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

    <!-- Welcome & Quick Actions Bar -->
    <div class="row align-items-center mb-4">
        <div class="col-md-7">
            <h2 class="fw-bold mb-1">
                Hello, <c:out value="${sessionScope.currentUser.fullName}"/>! 👋
            </h2>
            <p class="text-muted mb-0">
                Today is <span class="fw-semibold text-dark"><c:out value="${todayDate}"/></span> &bull; Strategy: 
                <span class="badge bg-primary-subtle text-primary border border-primary-subtle">${sessionScope.currentUser.preferredStrategy}</span>
            </p>
        </div>
        <div class="col-md-5 text-md-end mt-3 mt-md-0">
            <a href="${pageContext.request.contextPath}/schedule" class="btn btn-primary shadow-sm me-2">
                <i class="fa-solid fa-calendar-check me-1"></i> View Schedule
            </a>
            <form action="${pageContext.request.contextPath}/schedule/reschedule" method="post" class="d-inline">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <button type="submit" class="btn btn-outline-warning shadow-sm" onclick="return confirm('Run AI Rescheduler on missed & pending tasks?')">
                    <i class="fa-solid fa-arrows-rotate me-1"></i> AI Reschedule
                </button>
            </form>
        </div>
    </div>

    <!-- AI Study Insights / Recommendations Box -->
    <div class="ai-recommendation-box p-3 mb-4 shadow-sm">
        <div class="d-flex align-items-center mb-2">
            <span class="badge bg-primary text-white p-2 me-2"><i class="fa-solid fa-brain"></i> AI Insights</span>
            <span class="text-muted small">Generated via Priority Rule Engine: <code>0.35D + 0.30U + 0.20W + 0.15P</code></span>
        </div>
        <ul class="mb-0 ps-3 small text-secondary">
            <c:forEach var="rec" items="${summary.aiRecommendations}">
                <li class="mb-1"><c:out value="${rec}"/></li>
            </c:forEach>
        </ul>
    </div>

    <!-- Four Top KPI Metric Cards -->
    <div class="row g-3 mb-4">
        <div class="col-sm-6 col-lg-3">
            <div class="card stat-card p-3 h-100">
                <div class="d-flex align-items-center">
                    <div class="stat-icon bg-primary-subtle text-primary me-3">
                        <i class="fa-solid fa-chart-line"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-medium">Syllabus Completed</div>
                        <h3 class="fw-bold mb-0">${summary.overallCompletionPercentage}%</h3>
                        <small class="text-muted">${summary.completedTopics}/${summary.totalTopics} topics done</small>
                    </div>
                </div>
                <div class="progress mt-3" style="height: 6px;">
                    <div class="progress-bar bg-primary" style="width: ${summary.overallCompletionPercentage}%;"></div>
                </div>
            </div>
        </div>

        <div class="col-sm-6 col-lg-3">
            <div class="card stat-card p-3 h-100">
                <div class="d-flex align-items-center">
                    <div class="stat-icon bg-success-subtle text-success me-3">
                        <i class="fa-solid fa-stopwatch"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-medium">Time Studied</div>
                        <h3 class="fw-bold mb-0">
                            <fmt:formatNumber value="${summary.actualMinutesStudied / 60}" maxFractionDigits="1"/>h
                        </h3>
                        <small class="text-muted">${summary.actualMinutesStudied} actual mins recorded</small>
                    </div>
                </div>
                <div class="mt-3 small text-muted">
                    <i class="fa-solid fa-check-double text-success me-1"></i> ${summary.completedSessions} sessions finished
                </div>
            </div>
        </div>

        <div class="col-sm-6 col-lg-3">
            <div class="card stat-card p-3 h-100">
                <div class="d-flex align-items-center">
                    <div class="stat-icon bg-warning-subtle text-warning me-3">
                        <i class="fa-solid fa-hourglass-half"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-medium">Remaining Study</div>
                        <h3 class="fw-bold mb-0">
                            <fmt:formatNumber value="${summary.remainingMinutes / 60}" maxFractionDigits="1"/>h
                        </h3>
                        <small class="text-muted">${summary.remainingMinutes} pending mins</small>
                    </div>
                </div>
                <div class="mt-3 small text-muted">
                    <i class="fa-solid fa-list text-warning me-1"></i> ${summary.pendingTopics} pending topics
                </div>
            </div>
        </div>

        <div class="col-sm-6 col-lg-3">
            <div class="card stat-card p-3 h-100">
                <div class="d-flex align-items-center">
                    <div class="stat-icon bg-danger-subtle text-danger me-3">
                        <i class="fa-solid fa-calendar-day"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-medium">Subjects Tracked</div>
                        <h3 class="fw-bold mb-0">${summary.totalSubjects}</h3>
                        <small class="text-muted">Active semester courses</small>
                    </div>
                </div>
                <div class="mt-3 small text-muted">
                    <i class="fa-solid fa-clipboard-list text-danger me-1"></i> ${summary.scheduledSessions} upcoming slots
                </div>
            </div>
        </div>
    </div>

    <!-- Main Content Grid -->
    <div class="row g-4">
        <!-- Left Column: Today's Scheduled Sessions -->
        <div class="col-lg-7">
            <div class="card stat-card p-4 h-100">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0">
                        <i class="fa-solid fa-clock text-primary me-2"></i> Today's Study Sessions
                    </h5>
                    <a href="${pageContext.request.contextPath}/schedule" class="btn btn-sm btn-outline-primary">
                        Full Schedule <i class="fa-solid fa-arrow-right ms-1"></i>
                    </a>
                </div>

                <c:choose>
                    <c:when test="${empty todaySessions}">
                        <div class="text-center py-5">
                            <div class="text-muted mb-3" style="font-size: 2.5rem;"><i class="fa-solid fa-calendar-plus"></i></div>
                            <h6>No study sessions scheduled for today</h6>
                            <p class="text-muted small mb-3">Configure your availability and let the AI generate today's study plan.</p>
                            <a href="${pageContext.request.contextPath}/schedule" class="btn btn-primary btn-sm">
                                <i class="fa-solid fa-wand-magic-sparkles me-1"></i> Generate Today's Plan
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="d-flex flex-column gap-3">
                            <c:forEach var="s" items="${todaySessions}">
                                <div class="card border-0 shadow-sm session-timeline-card p-3 ${s.status == 'COMPLETED' ? 'session-completed' : (s.status == 'SKIPPED' ? 'session-skipped' : (s.status == 'PARTIALLY_COMPLETED' ? 'session-partial' : ''))}"
                                     style="border-left-color: ${s.subjectColorHex} !important;">
                                    <div class="d-flex justify-content-between align-items-start">
                                        <div>
                                            <span class="badge mb-1 text-white" style="background-color: ${s.subjectColorHex};">
                                                <c:out value="${s.subjectCode}"/> - <c:out value="${s.subjectName}"/>
                                            </span>
                                            <h6 class="fw-bold mb-1"><c:out value="${s.topicTitle}"/></h6>
                                            <small class="text-muted">
                                                <i class="fa-regular fa-clock me-1"></i> ${s.startTime} - ${s.endTime} (${s.durationMinutes} mins)
                                                <c:if test="${s.actualMinutesStudied > 0}">
                                                    &bull; <strong class="text-success">${s.actualMinutesStudied}m studied</strong>
                                                </c:if>
                                            </small>
                                        </div>
                                        <div class="text-end">
                                            <c:choose>
                                                <c:when test="${s.status == 'COMPLETED'}">
                                                    <span class="badge bg-success"><i class="fa-solid fa-check me-1"></i> Completed</span>
                                                </c:when>
                                                <c:when test="${s.status == 'SKIPPED'}">
                                                    <span class="badge bg-secondary"><i class="fa-solid fa-forward me-1"></i> Skipped</span>
                                                </c:when>
                                                <c:when test="${s.status == 'PARTIALLY_COMPLETED'}">
                                                    <span class="badge bg-warning text-dark"><i class="fa-solid fa-adjust me-1"></i> Partial</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="btn-group btn-group-sm">
                                                        <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#completeModal${s.id}" title="Complete">
                                                            <i class="fa-solid fa-check"></i>
                                                        </button>
                                                        <button type="button" class="btn btn-outline-warning" data-bs-toggle="modal" data-bs-target="#partialModal${s.id}" title="Partial">
                                                            <i class="fa-solid fa-hourglass-start"></i>
                                                        </button>
                                                        <form action="${pageContext.request.contextPath}/session/action" method="post" class="d-inline" onsubmit="return confirm('Skip this session?')">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="id" value="${s.id}">
                                                            <input type="hidden" name="action" value="skip">
                                                            <input type="hidden" name="returnUrl" value="/dashboard">
                                                            <button type="submit" class="btn btn-outline-secondary" title="Skip">
                                                                <i class="fa-solid fa-forward"></i>
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>

                                <!-- Complete Modal -->
                                <div class="modal fade" id="completeModal${s.id}" tabindex="-1">
                                    <div class="modal-dialog">
                                        <form action="${pageContext.request.contextPath}/session/action" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${s.id}">
                                            <input type="hidden" name="action" value="complete">
                                            <input type="hidden" name="returnUrl" value="/dashboard">
                                            <div class="modal-content">
                                                <div class="modal-header">
                                                    <h6 class="modal-title fw-bold">Complete Study Session</h6>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                </div>
                                                <div class="modal-body">
                                                    <p class="mb-2">Topic: <strong><c:out value="${s.topicTitle}"/></strong></p>
                                                    <div class="mb-3">
                                                        <label class="form-label">Actual Minutes Studied</label>
                                                        <input type="number" name="actualMinutes" class="form-control" value="${s.durationMinutes}" min="1" required>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label">Study Notes (Optional)</label>
                                                        <textarea name="notes" class="form-control" rows="2" placeholder="Key concepts grasped..."></textarea>
                                                    </div>
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                    <button type="submit" class="btn btn-success btn-sm">Confirm Completed</button>
                                                </div>
                                            </div>
                                        </form>
                                    </div>
                                </div>

                                <!-- Partial Modal -->
                                <div class="modal fade" id="partialModal${s.id}" tabindex="-1">
                                    <div class="modal-dialog">
                                        <form action="${pageContext.request.contextPath}/session/action" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${s.id}">
                                            <input type="hidden" name="action" value="partial">
                                            <input type="hidden" name="returnUrl" value="/dashboard">
                                            <div class="modal-content">
                                                <div class="modal-header">
                                                    <h6 class="modal-title fw-bold">Record Partial Study Minutes</h6>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                </div>
                                                <div class="modal-body">
                                                    <div class="mb-3">
                                                        <label class="form-label">Minutes Studied</label>
                                                        <input type="number" name="actualMinutes" class="form-control" value="30" min="1" max="${s.durationMinutes}" required>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label">Notes</label>
                                                        <textarea name="notes" class="form-control" rows="2" placeholder="Reason or partial coverage..."></textarea>
                                                    </div>
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                    <button type="submit" class="btn btn-warning btn-sm">Save Partial Progress</button>
                                                </div>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Right Column: Exam Countdowns & Weekly Trend -->
        <div class="col-lg-5">
            <!-- Upcoming Examinations -->
            <div class="card stat-card p-4 mb-4">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0">
                        <i class="fa-solid fa-graduation-cap text-danger me-2"></i> Upcoming Exams
                    </h5>
                    <a href="${pageContext.request.contextPath}/subjects" class="btn btn-sm btn-outline-secondary">Manage</a>
                </div>

                <div class="d-flex flex-column gap-2">
                    <c:forEach var="sub" items="${subjects}">
                        <div class="p-2 border rounded d-flex justify-content-between align-items-center bg-white">
                            <div class="d-flex align-items-center">
                                <span class="badge me-2" style="background-color: ${sub.colorHex};">&nbsp;</span>
                                <div>
                                    <div class="fw-semibold small"><c:out value="${sub.name}"/></div>
                                    <small class="text-muted">${sub.examDate}</small>
                                </div>
                            </div>
                            <div>
                                <c:set var="days" value="${sub.getDaysUntilExam(todayDate)}"/>
                                <c:choose>
                                    <c:when test="${days <= 7}">
                                        <span class="badge bg-danger">${days} days left</span>
                                    </c:when>
                                    <c:when test="${days <= 15}">
                                        <span class="badge bg-warning text-dark">${days} days left</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-info text-dark">${days} days</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <!-- Weekly Study Trend Chart -->
            <div class="card stat-card p-4">
                <h5 class="fw-bold mb-3">
                    <i class="fa-solid fa-chart-column text-primary me-2"></i> Weekly Study Trend (Mins)
                </h5>
                <canvas id="weeklyChart" height="180"></canvas>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const rawData = ${weeklyStatsJson};
    const labels = Object.keys(rawData);
    const dataValues = Object.values(rawData);

    const ctx = document.getElementById('weeklyChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Actual Minutes Studied',
                data: dataValues,
                backgroundColor: 'rgba(67, 97, 238, 0.75)',
                borderColor: 'rgba(67, 97, 238, 1)',
                borderWidth: 1,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 }
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
});
</script>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

