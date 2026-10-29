<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Register - AI Smart Study Planner" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="container py-5 my-auto">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
            <div class="text-center mb-4">
                <div class="brand-icon mx-auto mb-2 bg-primary text-white" style="width: 50px; height: 50px; font-size: 1.5rem;">
                    <i class="fa-solid fa-graduation-cap"></i>
                </div>
                <h3 class="fw-bold">Create Account</h3>
                <p class="text-muted">Start optimizing your study schedule with rule-based AI</p>
            </div>

            <div class="card stat-card shadow border-0 p-4">
                <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

                <form action="${pageContext.request.contextPath}/register" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">

                    <div class="mb-3">
                        <label for="fullName" class="form-label fw-semibold">Full Name</label>
                        <input type="text" class="form-control" id="fullName" name="fullName"
                               value="${fullName}" autocomplete="name" required placeholder="e.g. Alex Sharma">
                    </div>

                    <div class="mb-3">
                        <label for="username" class="form-label fw-semibold">Username</label>
                        <input type="text" class="form-control" id="username" name="username"
                               value="${username}" autocomplete="username" required minlength="3" placeholder="Choose a username">
                    </div>

                    <div class="mb-3">
                        <label for="email" class="form-label fw-semibold">Email Address</label>
                        <input type="email" class="form-control" id="email" name="email"
                               value="${email}" autocomplete="email" required placeholder="student@university.edu">
                    </div>

                    <div class="mb-3">
                        <label for="password" class="form-label fw-semibold">Password</label>
                        <input type="password" class="form-control" id="password" name="password"
                               autocomplete="new-password" required minlength="6" placeholder="At least 6 characters">
                    </div>

                    <div class="d-grid mb-3">
                        <button type="submit" class="btn btn-primary py-2 fw-semibold">
                            <i class="fa-solid fa-user-plus me-2"></i> Register
                        </button>
                    </div>

                    <div class="text-center border-top pt-3">
                        <p class="mb-0 text-muted">
                            Already registered? 
                            <a href="${pageContext.request.contextPath}/login" class="fw-semibold text-primary text-decoration-none">
                                Sign In
                            </a>
                        </p>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>
