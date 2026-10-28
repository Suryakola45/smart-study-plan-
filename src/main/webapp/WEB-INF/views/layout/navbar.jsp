<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center fw-bold" href="${pageContext.request.contextPath}/dashboard">
            <span class="brand-icon me-2"><i class="fa-solid fa-brain"></i></span>
            <span>AI StudyPlanner</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link ${activePage == 'dashboard' ? 'active fw-semibold' : ''}" href="${pageContext.request.contextPath}/dashboard">
                        <i class="fa-solid fa-gauge-high me-1"></i> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${activePage == 'subjects' ? 'active fw-semibold' : ''}" href="${pageContext.request.contextPath}/subjects">
                        <i class="fa-solid fa-book-bookmark me-1"></i> Subjects
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${activePage == 'topics' ? 'active fw-semibold' : ''}" href="${pageContext.request.contextPath}/topics">
                        <i class="fa-solid fa-list-check me-1"></i> Topics & AI Priority
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${activePage == 'availability' ? 'active fw-semibold' : ''}" href="${pageContext.request.contextPath}/availability">
                        <i class="fa-solid fa-clock me-1"></i> Availability
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${activePage == 'schedule' ? 'active fw-semibold' : ''}" href="${pageContext.request.contextPath}/schedule">
                        <i class="fa-solid fa-calendar-days me-1"></i> Study Schedule
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${activePage == 'progress' ? 'active fw-semibold' : ''}" href="${pageContext.request.contextPath}/progress">
                        <i class="fa-solid fa-chart-pie me-1"></i> Progress
                    </a>
                </li>
            </ul>

            <ul class="navbar-nav">
                <c:if test="${not empty sessionScope.currentUser}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle d-flex align-items-center text-white" href="#" id="userMenu" data-bs-toggle="dropdown">
                            <div class="user-avatar me-2"><i class="fa-solid fa-user-graduate"></i></div>
                            <span><c:out value="${sessionScope.currentUser.fullName}"/></span>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                            <li class="dropdown-header">
                                Signed in as <strong>@<c:out value="${sessionScope.currentUser.username}"/></strong>
                            </li>
                            <li><hr class="dropdown-divider"></li>
                            <li>
                                <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <button type="submit" class="dropdown-item text-danger">
                                        <i class="fa-solid fa-right-from-bracket me-2"></i> Log Out
                                    </button>
                                </form>
                            </li>
                        </ul>
                    </li>
                </c:if>
            </ul>
        </div>
    </div>
</nav>

