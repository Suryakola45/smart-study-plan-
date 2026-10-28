<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Topic Management & AI Priority - AI Smart Study Planner" scope="request"/>
<c:set var="activePage" value="topics" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>
<jsp:include page="/WEB-INF/views/layout/navbar.jsp"/>

<div class="container py-4">
    <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

    <!-- Header & Action Row -->
    <div class="row align-items-center mb-4">
        <div class="col-md-6">
            <h2 class="fw-bold mb-1">
                <i class="fa-solid fa-list-check text-primary me-2"></i> Topics & AI Priority
            </h2>
            <p class="text-muted mb-0">Prioritized using formula: <code>0.35D + 0.30U + 0.20W + 0.15P</code></p>
        </div>
        <div class="col-md-6 text-md-end mt-3 mt-md-0">
            <form action="${pageContext.request.contextPath}/topics/prioritize" method="post" class="d-inline me-2">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <button type="submit" class="btn btn-outline-primary shadow-sm" title="Re-evaluate all AI priorities based on days left to exams">
                    <i class="fa-solid fa-brain me-1"></i> Refresh AI Priorities
                </button>
            </form>
            <button type="button" class="btn btn-primary shadow-sm" data-bs-toggle="modal" data-bs-target="#addTopicModal">
                <i class="fa-solid fa-plus me-1"></i> Add Topic
            </button>
        </div>
    </div>

    <!-- Filter by Subject Bar -->
    <div class="card stat-card p-3 mb-4">
        <form method="get" action="${pageContext.request.contextPath}/topics" class="row g-2 align-items-center">
            <div class="col-auto">
                <label class="fw-semibold text-muted small"><i class="fa-solid fa-filter me-1"></i> Filter Course:</label>
            </div>
            <div class="col-md-4">
                <select name="subjectId" class="form-select form-select-sm" onchange="this.form.submit()">
                    <option value="">All Subjects (${subjects.size()})</option>
                    <c:forEach var="sub" items="${subjects}">
                        <option value="${sub.id}" ${selectedSubjectId == sub.id ? 'selected' : ''}>
                            <c:out value="${sub.name}"/> (<c:out value="${sub.code}"/>)
                        </option>
                    </c:forEach>
                </select>
            </div>
            <c:if test="${not empty selectedSubjectId}">
                <div class="col-auto">
                    <a href="${pageContext.request.contextPath}/topics" class="btn btn-sm btn-outline-secondary">Clear Filter</a>
                </div>
            </c:if>
        </form>
    </div>

    <!-- Topics Table -->
    <div class="card stat-card shadow-sm border-0 overflow-hidden">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th style="width: 20%;">Subject</th>
                        <th style="width: 25%;">Topic Title</th>
                        <th style="width: 12%;">Difficulty</th>
                        <th style="width: 12%;">Confidence</th>
                        <th style="width: 12%;">Progress</th>
                        <th style="width: 10%;">AI Priority</th>
                        <th style="width: 9%;" class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty topics}">
                            <tr>
                                <td colspan="7" class="text-center py-5 text-muted">
                                    <i class="fa-solid fa-clipboard-list mb-2" style="font-size: 2rem;"></i>
                                    <div>No topics found. Add your first topic to enable AI scheduling.</div>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="t" items="${topics}">
                                <tr>
                                    <td>
                                        <span class="badge text-white" style="background-color: ${t.subjectColorHex};">
                                            <c:out value="${t.subjectCode}"/>
                                        </span>
                                        <div class="small fw-semibold text-truncate" style="max-width: 160px;">
                                            <c:out value="${t.subjectName}"/>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="fw-bold"><c:out value="${t.title}"/></div>
                                        <small class="text-muted text-truncate d-block" style="max-width: 240px;">
                                            <c:out value="${t.description}"/>
                                        </small>
                                    </td>
                                    <td>
                                        <span class="text-warning">
                                            <c:forEach begin="1" end="${t.difficulty}">★</c:forEach>
                                        </span>
                                        <small class="text-muted">(${t.difficulty}/5)</small>
                                    </td>
                                    <td>
                                        <span class="text-info">
                                            <c:forEach begin="1" end="${t.confidenceLevel}">★</c:forEach>
                                        </span>
                                        <small class="text-muted">(${t.confidenceLevel}/5)</small>
                                    </td>
                                    <td>
                                        <div class="small fw-semibold">${t.completedMinutes} / ${t.estimatedMinutes}m</div>
                                        <div class="progress" style="height: 5px;">
                                            <div class="progress-bar ${t.status == 'COMPLETED' ? 'bg-success' : 'bg-primary'}" 
                                                 style="width: ${t.completionPercentage}%;"></div>
                                        </div>
                                        <c:choose>
                                            <c:when test="${t.status == 'COMPLETED'}">
                                                <span class="badge bg-success-subtle text-success small mt-1">Done</span>
                                            </c:when>
                                            <c:when test="${t.status == 'IN_PROGRESS'}">
                                                <span class="badge bg-warning-subtle text-warning small mt-1">In Progress</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary-subtle text-secondary small mt-1">Pending</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${t.calculatedPriority >= 0.75}">
                                                <span class="badge badge-priority-high p-2" 
                                                      data-bs-toggle="tooltip" 
                                                      title="High Study Priority: 0.35D + 0.30U + 0.20W + 0.15P">
                                                    🔥 <fmt:formatNumber value="${t.calculatedPriority}" minFractionDigits="2" maxFractionDigits="2"/>
                                                </span>
                                            </c:when>
                                            <c:when test="${t.calculatedPriority >= 0.50}">
                                                <span class="badge badge-priority-med p-2" 
                                                      data-bs-toggle="tooltip" 
                                                      title="Moderate Priority: 0.35D + 0.30U + 0.20W + 0.15P">
                                                    ⚡ <fmt:formatNumber value="${t.calculatedPriority}" minFractionDigits="2" maxFractionDigits="2"/>
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-priority-low p-2" 
                                                      data-bs-toggle="tooltip" 
                                                      title="Normal Priority: 0.35D + 0.30U + 0.20W + 0.15P">
                                                    ✓ <fmt:formatNumber value="${t.calculatedPriority}" minFractionDigits="2" maxFractionDigits="2"/>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <div class="btn-group btn-group-sm">
                                            <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#editTopicModal${t.id}">
                                                <i class="fa-solid fa-pen-to-square"></i>
                                            </button>
                                            <form action="${pageContext.request.contextPath}/topics/delete" method="post" class="d-inline" onsubmit="return confirm('Delete this topic?')">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="id" value="${t.id}">
                                                <button type="submit" class="btn btn-outline-danger">
                                                    <i class="fa-solid fa-trash"></i>
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Edit Topic Modal -->
                                <div class="modal fade" id="editTopicModal${t.id}" tabindex="-1">
                                    <div class="modal-dialog">
                                        <form action="${pageContext.request.contextPath}/topics/update" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${t.id}">
                                            <div class="modal-content">
                                                <div class="modal-header">
                                                    <h6 class="modal-title fw-bold">Edit Topic: <c:out value="${t.title}"/></h6>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                </div>
                                                <div class="modal-body">
                                                    <div class="mb-3">
                                                        <label class="form-label">Topic Title</label>
                                                        <input type="text" name="title" class="form-control" value="<c:out value='${t.title}'/>" required>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label">Description</label>
                                                        <textarea name="description" class="form-control" rows="2"><c:out value="${t.description}"/></textarea>
                                                    </div>
                                                    <div class="row g-2 mb-3">
                                                        <div class="col-6">
                                                            <label class="form-label">Difficulty (1 to 5)</label>
                                                            <select name="difficulty" class="form-select">
                                                                <c:forEach begin="1" end="5" var="d">
                                                                    <option value="${d}" ${t.difficulty == d ? 'selected' : ''}>${d} - ${d == 1 ? 'Very Easy' : (d == 5 ? 'Very Hard' : (d == 3 ? 'Medium' : ''))}</option>
                                                                </c:forEach>
                                                            </select>
                                                        </div>
                                                        <div class="col-6">
                                                            <label class="form-label">Confidence (1 to 5)</label>
                                                            <select name="confidenceLevel" class="form-select">
                                                                <c:forEach begin="1" end="5" var="c">
                                                                    <option value="${c}" ${t.confidenceLevel == c ? 'selected' : ''}>${c} - ${c == 1 ? 'Low' : (c == 5 ? 'Mastered' : (c == 3 ? 'Average' : ''))}</option>
                                                                </c:forEach>
                                                            </select>
                                                        </div>
                                                    </div>
                                                    <div class="row g-2 mb-3">
                                                        <div class="col-6">
                                                            <label class="form-label">Estimated Minutes</label>
                                                            <input type="number" name="estimatedMinutes" class="form-control" value="${t.estimatedMinutes}" min="15" required>
                                                        </div>
                                                        <div class="col-6">
                                                            <label class="form-label">Completed Minutes</label>
                                                            <input type="number" name="completedMinutes" class="form-control" value="${t.completedMinutes}" min="0" required>
                                                        </div>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label">Status</label>
                                                        <select name="status" class="form-select">
                                                            <option value="PENDING" ${t.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                                            <option value="IN_PROGRESS" ${t.status == 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
                                                            <option value="COMPLETED" ${t.status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                                                        </select>
                                                    </div>
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                    <button type="submit" class="btn btn-primary btn-sm">Save Changes</button>
                                                </div>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Add Topic Modal -->
<div class="modal fade" id="addTopicModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/topics/create" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <div class="modal-content">
                <div class="modal-header">
                    <h6 class="modal-title fw-bold">Add Subject Topic</h6>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Select Subject <span class="text-danger">*</span></label>
                        <select name="subjectId" class="form-select" required>
                            <c:forEach var="sub" items="${subjects}">
                                <option value="${sub.id}" ${selectedSubjectId == sub.id ? 'selected' : ''}>
                                    <c:out value="${sub.name}"/> (<c:out value="${sub.code}"/>)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Topic Title <span class="text-danger">*</span></label>
                        <input type="text" name="title" class="form-control" placeholder="e.g. Dynamic Programming (0/1 Knapsack)" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Description (Optional)</label>
                        <textarea name="description" class="form-control" rows="2" placeholder="Sub-topics, theorems, problems to solve..."></textarea>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label">Difficulty (1 - 5) <span class="text-danger">*</span></label>
                            <select name="difficulty" class="form-select" required>
                                <option value="1">1 - Very Easy</option>
                                <option value="2">2 - Easy</option>
                                <option value="3" selected>3 - Medium</option>
                                <option value="4">4 - Hard</option>
                                <option value="5">5 - Very Hard</option>
                            </select>
                        </div>
                        <div class="col-6">
                            <label class="form-label">Confidence (1 - 5) <span class="text-danger">*</span></label>
                            <select name="confidenceLevel" class="form-select" required>
                                <option value="1">1 - Very Low (Needs Attention)</option>
                                <option value="2">2 - Low</option>
                                <option value="3" selected>3 - Moderate</option>
                                <option value="4">4 - Good</option>
                                <option value="5">5 - Mastered</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Estimated Study Duration (Minutes) <span class="text-danger">*</span></label>
                        <input type="number" name="estimatedMinutes" class="form-control" value="90" min="15" step="15" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Create Topic</button>
                </div>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

