<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Subject Management - AI Smart Study Planner" scope="request"/>
<c:set var="activePage" value="subjects" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>
<jsp:include page="/WEB-INF/views/layout/navbar.jsp"/>

<div class="container py-4">
    <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1"><i class="fa-solid fa-book-bookmark text-primary me-2"></i> Subjects</h2>
            <p class="text-muted mb-0">Manage your semester courses, upcoming exam dates, and course syllabi.</p>
        </div>
        <button type="button" class="btn btn-primary shadow-sm" data-bs-toggle="modal" data-bs-target="#addSubjectModal">
            <i class="fa-solid fa-plus me-1"></i> Add Subject
        </button>
    </div>

    <!-- Subjects Cards Grid -->
    <div class="row g-4">
        <c:choose>
            <c:when test="${empty subjects}">
                <div class="col-12 text-center py-5">
                    <div class="text-muted mb-3" style="font-size: 3rem;"><i class="fa-solid fa-folder-open"></i></div>
                    <h5>No subjects added yet</h5>
                    <p class="text-muted">Click "Add Subject" to begin tracking your academic subjects.</p>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach var="s" items="${subjects}">
                    <div class="col-md-6 col-lg-4">
                        <div class="card stat-card h-100 p-3 position-relative">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <div>
                                    <span class="badge mb-1 text-white" style="background-color: ${s.colorHex};">
                                        <c:out value="${s.code}"/>
                                    </span>
                                    <h5 class="fw-bold mb-1"><c:out value="${s.name}"/></h5>
                                </div>
                                <div class="dropdown">
                                    <button class="btn btn-sm btn-link text-muted" data-bs-toggle="dropdown">
                                        <i class="fa-solid fa-ellipsis-vertical"></i>
                                    </button>
                                    <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                                        <li>
                                            <a class="dropdown-item" href="${pageContext.request.contextPath}/topics?subjectId=${s.id}">
                                                <i class="fa-solid fa-list-check me-2"></i> Manage Topics
                                            </a>
                                        </li>
                                        <li>
                                            <button class="dropdown-item" data-bs-toggle="modal" data-bs-target="#editSubjectModal${s.id}">
                                                <i class="fa-solid fa-pen-to-square me-2"></i> Edit Subject
                                            </button>
                                        </li>
                                        <li><hr class="dropdown-divider"></li>
                                        <li>
                                            <form action="${pageContext.request.contextPath}/subjects/delete" method="post" onsubmit="return confirm('Delete this subject and all its topics?')">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="id" value="${s.id}">
                                                <button type="submit" class="dropdown-item text-danger">
                                                    <i class="fa-solid fa-trash me-2"></i> Delete
                                                </button>
                                            </form>
                                        </li>
                                    </ul>
                                </div>
                            </div>

                            <p class="text-muted small mb-3 flex-grow-1"><c:out value="${s.description}"/></p>

                            <div class="mb-3">
                                <div class="d-flex justify-content-between small text-muted mb-1">
                                    <span>Completion</span>
                                    <strong>${s.progressPercentage}%</strong>
                                </div>
                                <div class="progress" style="height: 7px;">
                                    <div class="progress-bar" role="progressbar" style="width: ${s.progressPercentage}%; background-color: ${s.colorHex};"></div>
                                </div>
                            </div>

                            <div class="pt-2 border-top d-flex justify-content-between align-items-center">
                                <div class="small">
                                    <i class="fa-solid fa-calendar-day text-muted me-1"></i>
                                    Exam: <span class="fw-semibold">${s.examDate}</span>
                                </div>
                                <div>
                                    <c:set var="days" value="${s.getDaysUntilExam(today)}"/>
                                    <c:choose>
                                        <c:when test="${days <= 7}">
                                            <span class="badge bg-danger">${days} days left</span>
                                        </c:when>
                                        <c:when test="${days <= 14}">
                                            <span class="badge bg-warning text-dark">${days} days left</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">${days} days</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <!-- Edit Modal -->
                        <div class="modal fade" id="editSubjectModal${s.id}" tabindex="-1">
                            <div class="modal-dialog">
                                <form action="${pageContext.request.contextPath}/subjects/update" method="post">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${s.id}">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h6 class="modal-title fw-bold">Edit Subject</h6>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="mb-3">
                                                <label class="form-label">Subject Name</label>
                                                <input type="text" name="name" class="form-control" value="<c:out value='${s.name}'/>" required>
                                            </div>
                                            <div class="row g-2 mb-3">
                                                <div class="col-8">
                                                    <label class="form-label">Course Code</label>
                                                    <input type="text" name="code" class="form-control" value="<c:out value='${s.code}'/>">
                                                </div>
                                                <div class="col-4">
                                                    <label class="form-label">Color</label>
                                                    <input type="color" name="colorHex" class="form-control form-control-color w-100" value="${s.colorHex}">
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">Examination Date</label>
                                                <input type="date" name="examDate" class="form-control" value="${s.examDate}" required>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">Description</label>
                                                <textarea name="description" class="form-control" rows="2"><c:out value="${s.description}"/></textarea>
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
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Add Subject Modal -->
<div class="modal fade" id="addSubjectModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/subjects/create" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <div class="modal-content">
                <div class="modal-header">
                    <h6 class="modal-title fw-bold">Add Academic Subject</h6>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Subject Name <span class="text-danger">*</span></label>
                        <input type="text" name="name" class="form-control" placeholder="e.g. Operating Systems" required>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-8">
                            <label class="form-label">Course Code</label>
                            <input type="text" name="code" class="form-control" placeholder="e.g. CS204">
                        </div>
                        <div class="col-4">
                            <label class="form-label">Color Theme</label>
                            <input type="color" name="colorHex" class="form-control form-control-color w-100" value="#4361ee">
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Examination Date <span class="text-danger">*</span></label>
                        <input type="date" name="examDate" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Description (Optional)</label>
                        <textarea name="description" class="form-control" rows="2" placeholder="Key topics covered..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Add Subject</button>
                </div>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

