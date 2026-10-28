<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Login - AI Smart Study Planner" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="container py-5 my-auto">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="text-center mb-4">
                <div class="brand-icon mx-auto mb-2 bg-primary text-white" style="width: 50px; height: 50px; font-size: 1.5rem;">
                    <i class="fa-solid fa-brain"></i>
                </div>
                <h3 class="fw-bold">AI Smart Study Planner</h3>
                <p class="text-muted">Sign in to your intelligent study planner</p>
            </div>

            <div class="card stat-card shadow border-0 p-4">
                <jsp:include page="/WEB-INF/views/layout/alerts.jsp"/>

                <form action="${pageContext.request.contextPath}/login" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">

                    <div class="mb-3">
                        <label for="identifier" class="form-label fw-semibold">Username or Email</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="fa-solid fa-user text-muted"></i></span>
                            <input type="text" class="form-control" id="identifier" name="identifier" 
                                   value="${identifier}" required placeholder="Enter username or email" autofocus>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="password" class="form-label fw-semibold">Password</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="fa-solid fa-lock text-muted"></i></span>
                            <input type="password" class="form-control" id="password" name="password" 
                                   required placeholder="Enter your password">
                        </div>
                    </div>

                    <div class="d-grid mb-3">
                        <button type="submit" class="btn btn-primary py-2 fw-semibold">
                            <i class="fa-solid fa-arrow-right-to-bracket me-2"></i> Sign In
                        </button>
                    </div>

                    <div class="text-center border-top pt-3">
                        <p class="mb-0 text-muted">
                            Don't have an account? 
                            <a href="${pageContext.request.contextPath}/register" class="fw-semibold text-primary text-decoration-none">
                                Register here
                            </a>
                        </p>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

