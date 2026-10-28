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
 * Service managing Subject business operations and validation.
 */
public class SubjectService {

    private final SubjectDAO subjectDAO;
    private final TopicDAO topicDAO;

    public SubjectService() {
        this.subjectDAO = new SubjectDAO();
        this.topicDAO = new TopicDAO();
    }

    public SubjectService(SubjectDAO subjectDAO, TopicDAO topicDAO) {
        this.subjectDAO = subjectDAO;
        this.topicDAO = topicDAO;
    }

    public Subject createSubject(Long userId, String name, String code, LocalDate examDate,
                                 String colorHex, String description) throws InvalidStudyDataException {

        validateSubject(name, examDate);

        Subject subject = new Subject();
        subject.setUserId(userId);
        subject.setName(name.trim());
        subject.setCode(code != null ? code.trim().toUpperCase() : "");
        subject.setExamDate(examDate);
        subject.setColorHex(colorHex != null && !colorHex.isBlank() ? colorHex.trim() : "#4361ee");
        subject.setDescription(description);

        return subjectDAO.save(subject);
    }

    public boolean updateSubject(Long subjectId, Long userId, String name, String code,
                                 LocalDate examDate, String colorHex, String description)
            throws InvalidStudyDataException {

        validateSubject(name, examDate);

        Optional<Subject> existing = subjectDAO.findById(subjectId);
        if (existing.isEmpty() || !existing.get().getUserId().equals(userId)) {
            return false;
        }

        Subject subject = existing.get();
        subject.setName(name.trim());
        subject.setCode(code != null ? code.trim().toUpperCase() : "");
        subject.setExamDate(examDate);
        subject.setColorHex(colorHex != null && !colorHex.isBlank() ? colorHex.trim() : "#4361ee");
        subject.setDescription(description);

        return subjectDAO.update(subject);
    }

    public boolean deleteSubject(Long subjectId, Long userId) {
        return subjectDAO.deleteByIdAndUserId(subjectId, userId);
    }

    public Optional<Subject> getSubjectById(Long id) {
        Optional<Subject> opt = subjectDAO.findById(id);
        if (opt.isPresent()) {
            Subject s = opt.get();
            List<Topic> topics = topicDAO.findBySubjectId(s.getId());
            s.setTopics(topics);
        }
        return opt;
    }

    public List<Subject> getSubjectsWithTopics(Long userId) {
        List<Subject> subjects = subjectDAO.findByUserId(userId);
        for (Subject s : subjects) {
            List<Topic> topics = topicDAO.findBySubjectId(s.getId());
            s.setTopics(topics);
        }
        return subjects;
    }

    private void validateSubject(String name, LocalDate examDate) throws InvalidStudyDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidStudyDataException("name", "Subject name cannot be empty.");
        }
        if (examDate == null) {
            throw new InvalidStudyDataException("examDate", "Examination date is required.");
        }
        if (examDate.isBefore(LocalDate.now())) {
            throw new InvalidStudyDataException("examDate", "Examination date cannot be in the past.");
        }
    }
}

