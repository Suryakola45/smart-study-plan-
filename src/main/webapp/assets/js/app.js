/**
 * AI Smart Study Planner Frontend JavaScript
 */
document.addEventListener('DOMContentLoaded', function () {
    // Enable Bootstrap tooltips everywhere
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Auto-dismiss alerts after 6 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 6000);
    });

    // Dynamic Complete Session Modal population
    const completeModal = document.getElementById('completeSessionModal');
    if (completeModal) {
        completeModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            if (!button) return;
            const sessionId = button.getAttribute('data-session-id') || '';
            const topicTitle = button.getAttribute('data-topic-title') || '';
            const duration = button.getAttribute('data-duration') || '45';

            const idInput = document.getElementById('completeSessionId');
            const topicSpan = document.getElementById('completeTopicTitle');
            const minutesInput = document.getElementById('completeActualMinutes');

            if (idInput) idInput.value = sessionId;
            if (topicSpan) topicSpan.textContent = topicTitle;
            if (minutesInput) minutesInput.value = duration;
        });
    }

    // Dynamic Partial Session Modal population
    const partialModal = document.getElementById('partialSessionModal');
    if (partialModal) {
        partialModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            if (!button) return;
            const sessionId = button.getAttribute('data-session-id') || '';
            const topicTitle = button.getAttribute('data-topic-title') || '';
            const duration = button.getAttribute('data-duration') || '45';

            const idInput = document.getElementById('partialSessionId');
            const topicSpan = document.getElementById('partialTopicTitle');
            const minutesInput = document.getElementById('partialActualMinutes');

            if (idInput) idInput.value = sessionId;
            if (topicSpan) topicSpan.textContent = topicTitle;
            if (minutesInput) {
                const defaultPartial = Math.max(1, Math.min(30, parseInt(duration, 10)));
                minutesInput.value = defaultPartial;
                minutesInput.max = duration;
            }
        });
    }
});

// Give users feedback while a valid form submission is being processed and
// prevent multiple accidental double clicks without breaking browser submit dispatch.
document.addEventListener('submit', function (event) {
    if (event.defaultPrevented || !(event.target instanceof HTMLFormElement)) {
        return;
    }

    const form = event.target;
    const submitter = event.submitter;

    // Use asynchronous setTimeout to allow the browser to serialize the form and
    // dispatch the HTTP request before disabling the submit button.
    setTimeout(function () {
        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(function (button) {
            button.dataset.wasDisabled = button.disabled ? 'true' : 'false';
            button.disabled = true;
        });

        if (submitter instanceof HTMLButtonElement) {
            submitter.dataset.originalContent = submitter.innerHTML;
            submitter.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Submitting...';
        } else if (submitter) {
            submitter.dataset.originalValue = submitter.value;
            submitter.value = 'Submitting...';
        }

        form.setAttribute('aria-busy', 'true');
    }, 20);
});

// A page restored from the browser's back/forward cache must have usable forms.
window.addEventListener('pageshow', function () {
    document.querySelectorAll('form[aria-busy="true"]').forEach(function (form) {
        form.removeAttribute('aria-busy');
        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(function (button) {
            button.disabled = button.dataset.wasDisabled === 'true';
            delete button.dataset.wasDisabled;
            if (button.dataset.originalContent !== undefined) {
                button.innerHTML = button.dataset.originalContent;
                delete button.dataset.originalContent;
            }
            if (button.dataset.originalValue !== undefined) {
                button.value = button.dataset.originalValue;
                delete button.dataset.originalValue;
            }
        });
    });
});



