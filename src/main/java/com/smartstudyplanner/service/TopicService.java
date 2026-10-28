package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.SubjectDAO;
import com.smartstudyplanner.dao.TopicDAO;
import com.smartstudyplanner.exception.InvalidStudyDataException;
import com.smartstudyplanner.model.Subject;
import com.smartstudyplanner.model.Topic;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service managing Topic business logic and AI priority recalculation.
 */
public class TopicService {

    private final TopicDAO topicDAO;
    private final SubjectDAO subjectDAO;

    public TopicService() {
        this.topicDAO = new TopicDAO();
        this.subjectDAO = new SubjectDAO();
    }

    public TopicService(TopicDAO topicDAO, SubjectDAO subjectDAO) {
        this.topicDAO = topicDAO;
        this.subjectDAO = subjectDAO;
    }

    public Topic createTopic(Long subjectId, String title, String description,
                             int difficulty, int confidenceLevel, int estimatedMinutes)
            throws InvalidStudyDataException {

        validateTopicInputs(title, difficulty, confidenceLevel, estimatedMinutes);

        Optional<Subject> subjectOpt = subjectDAO.findById(subjectId);
        if (subjectOpt.isEmpty()) {
            throw new InvalidStudyDataException("subjectId", "Associated subject does not exist.");
        }
        Subject subject = subjectOpt.get();

        Topic topic = new Topic();
        topic.setSubjectId(subjectId);
        topic.setTitle(title.trim());
        topic.setDescription(description);
        topic.setDifficulty(difficulty);
        topic.setConfidenceLevel(confidenceLevel);
        topic.setEstimatedMinutes(estimatedMinutes);
        topic.setCompletedMinutes(0);

        // Compute initial AI Priority
        double priority = PriorityCalculator.calculate(topic, subject.getExamDate(), LocalDate.now());
        topic.setCalculatedPriority(priority);

        return topicDAO.save(topic);
    }

    public boolean updateTopic(Long topicId, String title, String description,
                              int difficulty, int confidenceLevel, int estimatedMinutes,
                              int completedMinutes, String status) throws InvalidStudyDataException {

        validateTopicInputs(title, difficulty, confidenceLevel, estimatedMinutes);
        if (completedMinutes < 0) {
            throw new InvalidStudyDataException("completedMinutes", "Completed minutes cannot be negative.");
        }

        Optional<Topic> existingOpt = topicDAO.findById(topicId);
        if (existingOpt.isEmpty()) {
            return false;
        }

        Topic topic = existingOpt.get();
        topic.setTitle(title.trim());
        topic.setDescription(description);
        topic.setDifficulty(difficulty);
        topic.setConfidenceLevel(confidenceLevel);
        topic.setEstimatedMinutes(estimatedMinutes);
        topic.setCompletedMinutes(completedMinutes);
        topic.setStatus(status);

        // Recalculate AI Priority score
        double priority = PriorityCalculator.calculate(topic, topic.getExamDate(), LocalDate.now());
        topic.setCalculatedPriority(priority);

        return topicDAO.update(topic);
    }

    public boolean deleteTopic(Long topicId) {
        return topicDAO.deleteById(topicId);
    }

    public Optional<Topic> getTopicById(Long id) {
        return topicDAO.findById(id);
    }

    public List<Topic> getTopicsBySubject(Long subjectId) {
        return topicDAO.findBySubjectId(subjectId);
    }

    public List<Topic> getTopicsByUser(Long userId) {
        return topicDAO.findByUserId(userId);
    }

    public List<Topic> getPendingTopicsByUser(Long userId) {
        return topicDAO.findPendingByUserId(userId);
    }

    /**
     * Recalculates and updates AI priorities for all topics belonging to a user.
     * Called whenever exam dates, study completions, or daily schedule refreshes occur.
     */
    public void refreshAllPrioritiesForUser(Long userId) {
        List<Topic> topics = topicDAO.findByUserId(userId);
        LocalDate today = LocalDate.now();
        for (Topic t : topics) {
            double priority = PriorityCalculator.calculate(t, t.getExamDate(), today);
            t.setCalculatedPriority(priority);
            topicDAO.updateCalculatedPriority(t.getId(), priority);
        }
    }

    private void validateTopicInputs(String title, int difficulty, int confidenceLevel, int estimatedMinutes)
            throws InvalidStudyDataException {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidStudyDataException("title", "Topic title cannot be empty.");
        }
        if (difficulty < 1 || difficulty > 5) {
            throw new InvalidStudyDataException("difficulty", "Difficulty must be between 1 and 5.");
        }
        if (confidenceLevel < 1 || confidenceLevel > 5) {
            throw new InvalidStudyDataException("confidenceLevel", "Confidence level must be between 1 and 5.");
        }
        if (estimatedMinutes <= 0) {
            throw new InvalidStudyDataException("estimatedMinutes", "Estimated minutes must be greater than zero.");
        }
    }
}

