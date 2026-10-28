<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Study Availability - AI Smart Study Planner" scope="request"/>
<c:set var="activePage" value="availability" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>
<jsp:include page="/WEB-INF/views/layout/navbar.jsp"/>

<div class="container py-4">
    <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

    <div class="row align-items-center mb-4">
        <div class="col-md-7">
            <h2 class="fw-bold mb-1">
                <i class="fa-solid fa-clock text-primary me-2"></i> Study Availability Windows
            </h2>
            <p class="text-muted mb-0">Specify when you are free to study, preferred session lengths, and break times.</p>
        </div>
        <div class="col-md-5 text-md-end mt-3 mt-md-0">
            <button type="button" class="btn btn-outline-primary me-2 shadow-sm" data-bs-toggle="modal" data-bs-target="#quickWeekModal">
                <i class="fa-solid fa-calendar-week me-1"></i> Quick 7-Day Setup
            </button>
            <button type="button" class="btn btn-primary shadow-sm" data-bs-toggle="modal" data-bs-target="#addAvailModal">
                <i class="fa-solid fa-plus me-1"></i> Add Custom Window
            </button>
        </div>
    </div>

    <!-- Availability Cards Table -->
    <div class="card stat-card shadow-sm border-0 overflow-hidden">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th style="width: 25%;">Available Date</th>
                        <th style="width: 25%;">Time Window</th>
                        <th style="width: 20%;">Session & Break</th>
                        <th style="width: 15%;">Total Capacity</th>
                        <th style="width: 15%;" class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty availabilities}">
                            <tr>
                                <td colspan="5" class="text-center py-5 text-muted">
                                    <i class="fa-solid fa-calendar-xmark mb-2" style="font-size: 2rem;"></i>
                                    <div>No study availability windows configured yet.</div>
                                    <p class="small text-muted mb-0">Use "Quick 7-Day Setup" to automatically populate this week's hours.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="a" items="${availabilities}">
                                <tr>
                                    <td>
                                        <div class="fw-bold">${a.availableDate}</div>
                                        <c:choose>
                                            <c:when test="${a.availableDate == today}">
                                                <span class="badge bg-primary-subtle text-primary small">Today</span>
                                            </c:when>
                                            <c:when test="${a.availableDate < today}">
                                                <span class="badge bg-secondary-subtle text-secondary small">Past</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-info-subtle text-info small">Upcoming</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <i class="fa-regular fa-clock text-muted me-1"></i>
                                        <span class="fw-semibold">${a.startTime}</span> to <span class="fw-semibold">${a.endTime}</span>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border">
                                            ${a.slotDurationMinutes}m study + ${a.breakDurationMinutes}m break
                                        </span>
                                    </td>
                                    <td>
                                        <strong>${a.totalSpanMinutes}</strong> total minutes
                                    </td>
                                    <td class="text-end">
                                        <form action="${pageContext.request.contextPath}/availability/delete" method="post" class="d-inline" onsubmit="return confirm('Remove this study window?')">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${a.id}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger">
                                                <i class="fa-solid fa-trash"></i>
                                            </button>
                                        </form>
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

<!-- Add Custom Window Modal -->
<div class="modal fade" id="addAvailModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/availability/add" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <div class="modal-content">
                <div class="modal-header">
                    <h6 class="modal-title fw-bold">Add Availability Window</h6>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Date <span class="text-danger">*</span></label>
                        <input type="date" name="availableDate" class="form-control" value="${today}" min="${today}" required>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label">Start Time <span class="text-danger">*</span></label>
                            <input type="time" name="startTime" class="form-control" value="18:00" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label">End Time <span class="text-danger">*</span></label>
                            <input type="time" name="endTime" class="form-control" value="22:00" required>
                        </div>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label">Slot Duration (Mins)</label>
                            <input type="number" name="slotMinutes" class="form-control" value="45" min="15" max="180" step="5" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label">Break Duration (Mins)</label>
                            <input type="number" name="breakMinutes" class="form-control" value="15" min="0" max="60" step="5" required>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Save Window</button>
                </div>
            </div>
        </form>
    </div>
</div>

<!-- Quick 7-Day Modal -->
<div class="modal fade" id="quickWeekModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/availability/quick-week" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <div class="modal-content">
                <div class="modal-header">
                    <h6 class="modal-title fw-bold">Quick 7-Day Evening Schedule Generator</h6>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p class="text-muted small">
                        This helper automatically creates study windows for each of the next 7 days using your preferred hours.
                    </p>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label">Daily Start Time</label>
                            <input type="time" name="startTime" class="form-control" value="18:00" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label">Daily End Time</label>
                            <input type="time" name="endTime" class="form-control" value="21:30" required>
                        </div>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label">Session Duration</label>
                            <input type="number" name="slotMinutes" class="form-control" value="45" min="15" max="120" step="5" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label">Break Time</label>
                            <input type="number" name="breakMinutes" class="form-control" value="15" min="0" max="60" step="5" required>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Generate 7 Days</button>
                </div>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

