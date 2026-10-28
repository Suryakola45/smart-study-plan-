<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Study Analytics & Progress - AI Smart Study Planner" scope="request"/>
<c:set var="activePage" value="progress" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>
<jsp:include page="/WEB-INF/views/layout/navbar.jsp"/>

<div class="container py-4">
    <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

    <div class="mb-4">
        <h2 class="fw-bold mb-1">
            <i class="fa-solid fa-chart-pie text-primary me-2"></i> Study Analytics & Progress Tracking
        </h2>
        <p class="text-muted mb-0">Live metrics, topic completion percentages, and weekly study statistics (Requirement 9).</p>
    </div>

    <!-- Top KPI Stats Row -->
    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card stat-card p-3 text-center">
                <div class="text-muted small fw-medium">Overall Syllabus Progress</div>
                <h2 class="fw-bold text-primary mb-1">${summary.overallCompletionPercentage}%</h2>
                <div class="progress" style="height: 6px;">
                    <div class="progress-bar bg-primary" style="width: ${summary.overallCompletionPercentage}%;"></div>
                </div>
                <small class="text-muted mt-2 d-block">${summary.completedTopics} of ${summary.totalTopics} topics completed</small>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card stat-card p-3 text-center">
                <div class="text-muted small fw-medium">Actual Time Invested</div>
                <h2 class="fw-bold text-success mb-1">
                    <fmt:formatNumber value="${summary.actualMinutesStudied / 60}" maxFractionDigits="1"/>h
                </h2>
                <small class="text-muted d-block mt-2">${summary.actualMinutesStudied} actual recorded minutes</small>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card stat-card p-3 text-center">
                <div class="text-muted small fw-medium">Estimated Work Remaining</div>
                <h2 class="fw-bold text-warning mb-1">
                    <fmt:formatNumber value="${summary.remainingMinutes / 60}" maxFractionDigits="1"/>h
                </h2>
                <small class="text-muted d-block mt-2">${summary.remainingMinutes} pending minutes</small>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card stat-card p-3 text-center">
                <div class="text-muted small fw-medium">Session Success Ratio</div>
                <h2 class="fw-bold text-info mb-1">
                    <c:choose>
                        <c:when test="${summary.completedSessions + summary.skippedSessions > 0}">
                            <fmt:formatNumber value="${(summary.completedSessions / (summary.completedSessions + summary.skippedSessions)) * 100}" maxFractionDigits="0"/>%
                        </c:when>
                        <c:otherwise>100%</c:otherwise>
                    </c:choose>
                </h2>
                <small class="text-muted d-block mt-2">${summary.completedSessions} completed &bull; ${summary.skippedSessions} skipped</small>
            </div>
        </div>
    </div>

    <!-- Charts Row -->
    <div class="row g-4 mb-4">
        <!-- 7-Day Study Trend Chart -->
        <div class="col-lg-7">
            <div class="card stat-card p-4 h-100">
                <h5 class="fw-bold mb-3">
                    <i class="fa-solid fa-chart-column text-primary me-2"></i> Weekly Study Investment (Minutes per Day)
                </h5>
                <canvas id="progressWeeklyChart" height="200"></canvas>
            </div>
        </div>

        <!-- Subject Topic Distribution Donut Chart -->
        <div class="col-lg-5">
            <div class="card stat-card p-4 h-100">
                <h5 class="fw-bold mb-3">
                    <i class="fa-solid fa-chart-pie text-primary me-2"></i> Subject Topics Distribution
                </h5>
                <canvas id="subjectDonutChart" height="200"></canvas>
            </div>
        </div>
    </div>

    <!-- Subject-wise Progress Breakdown Table -->
    <div class="card stat-card p-4 shadow-sm border-0">
        <h5 class="fw-bold mb-3">
            <i class="fa-solid fa-bars-progress text-primary me-2"></i> Subject-Wise Syllabus Breakdown
        </h5>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th style="width: 25%;">Subject</th>
                        <th style="width: 15%;">Exam Date</th>
                        <th style="width: 15%;">Days Remaining</th>
                        <th style="width: 15%;">Topics Completed</th>
                        <th style="width: 15%;">Time Studied / Total</th>
                        <th style="width: 15%;">Completion %</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty summary.subjectProgressList}">
                            <tr>
                                <td colspan="6" class="text-center py-4 text-muted">No subject data found.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="item" items="${summary.subjectProgressList}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <span class="badge me-2" style="background-color: ${item.colorHex};">&nbsp;</span>
                                            <div>
                                                <div class="fw-bold"><c:out value="${item.subjectName}"/></div>
                                                <small class="text-muted"><c:out value="${item.subjectCode}"/></small>
                                            </div>
                                        </div>
                                    </td>
                                    <td>${item.examDate}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.daysUntilExam <= 7}">
                                                <span class="badge bg-danger">${item.daysUntilExam} days</span>
                                            </c:when>
                                            <c:when test="${item.daysUntilExam <= 14}">
                                                <span class="badge bg-warning text-dark">${item.daysUntilExam} days</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">${item.daysUntilExam} days</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <strong>${item.completedTopics}</strong> / ${item.totalTopics}
                                    </td>
                                    <td>
                                        ${item.completedMinutes}m / ${item.estimatedMinutes}m
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <div class="progress flex-grow-1" style="height: 6px;">
                                                <div class="progress-bar" style="width: ${item.completionPercentage}%; background-color: ${item.colorHex};"></div>
                                            </div>
                                            <span class="fw-semibold small">${item.completionPercentage}%</span>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    // 1. Weekly Bar Chart
    const weeklyData = ${weeklyStatsJson};
    const weekCtx = document.getElementById('progressWeeklyChart').getContext('2d');
    new Chart(weekCtx, {
        type: 'bar',
        data: {
            labels: Object.keys(weeklyData),
            datasets: [{
                label: 'Study Minutes',
                data: Object.values(weeklyData),
                backgroundColor: 'rgba(67, 97, 238, 0.8)',
                borderColor: 'rgba(67, 97, 238, 1)',
                borderWidth: 1,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true, ticks: { precision: 0 } }
            },
            plugins: { legend: { display: false } }
        }
    });

    // 2. Subject Distribution Donut Chart
    const subjects = ${subjectProgressJson};
    if (subjects && subjects.length > 0) {
        const subLabels = subjects.map(s => s.subjectName);
        const subTopics = subjects.map(s => s.totalTopics);
        const subColors = subjects.map(s => s.colorHex || '#4361ee');

        const donutCtx = document.getElementById('subjectDonutChart').getContext('2d');
        new Chart(donutCtx, {
            type: 'doughnut',
            data: {
                labels: subLabels,
                datasets: [{
                    data: subTopics,
                    backgroundColor: subColors,
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }
});
</script>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

