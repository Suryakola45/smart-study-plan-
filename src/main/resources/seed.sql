-- ========================================================
-- Local development sample data only. Never run against production.
-- Seed Data for AI Smart Study Planner
-- Sample B.Tech 2nd Year Student Data
-- Demo Account: username="student1", password="password123"
-- ========================================================

USE smart_study_planner;

-- Clear previous test data in proper order
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE study_sessions;
TRUNCATE TABLE availability;
TRUNCATE TABLE topics;
TRUNCATE TABLE subjects;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Demo Student (Password is 'password123' hashed with BCrypt)
INSERT INTO users (id, username, email, password_hash, full_name, preferred_strategy)
VALUES (1, 'student1', 'student1@university.edu', '$2a$10$OieSrBdHo5WCzu4DyYFg9OvGQFCSD/bhIZOm2iN9fMvOQlV4uECxO', 'Alex Sharma', 'PRIORITY_BASED');

-- 2. Insert B.Tech Semester Subjects with Upcoming Exam Dates
INSERT INTO subjects (id, user_id, name, code, exam_date, color_hex, description)
VALUES 
(1, 1, 'Data Structures & Algorithms', 'CS201', DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY), '#4361ee', 'Arrays, Linked Lists, Trees, Graphs, Sorting, Dynamic Programming'),
(2, 1, 'Database Management Systems', 'CS202', DATE_ADD(CURRENT_DATE, INTERVAL 18 DAY), '#3a0ca3', 'Relational Model, Normalization, SQL, Transactions & ACID properties'),
(3, 1, 'Computer Networks', 'CS203', DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY), '#7209b7', 'OSI model, TCP/IP, Routing Algorithms, Congestion Control, DNS'),
(4, 1, 'Discrete Mathematics', 'MA201', DATE_ADD(CURRENT_DATE, INTERVAL 32 DAY), '#f72585', 'Graph Theory, Combinatorics, Recurrence Relations, Logic & Sets');

-- 3. Insert Topics for Subjects
-- Subject 1: Data Structures & Algorithms
INSERT INTO topics (id, subject_id, title, description, difficulty, confidence_level, estimated_minutes, completed_minutes, status, calculated_priority)
VALUES
(1, 1, 'Binary Search Trees & AVL Trees', 'Insertion, deletion, rotations, balancing checks', 4, 2, 120, 0, 'PENDING', 0.81),
(2, 1, 'Graph Traversals (BFS & DFS)', 'Adjacency list representation, queue/stack traversal', 3, 3, 90, 0, 'PENDING', 0.65),
(3, 1, 'Dynamic Programming: 0/1 Knapsack', 'Memoization vs Tabulation, state transition', 5, 1, 150, 0, 'PENDING', 0.94),
(4, 1, 'Sorting Algorithms (Quick & Merge)', 'Divide and conquer, recurrence relation, stability', 2, 4, 60, 60, 'COMPLETED', 0.35);

-- Subject 2: Database Management Systems
INSERT INTO topics (id, subject_id, title, description, difficulty, confidence_level, estimated_minutes, completed_minutes, status, calculated_priority)
VALUES
(5, 2, 'Normalization (1NF, 2NF, 3NF, BCNF)', 'Functional dependencies, decomposition, lossless join', 4, 2, 120, 0, 'PENDING', 0.76),
(6, 2, 'Transaction Management & ACID', 'Serializability, 2PL locking protocol, deadlocks', 4, 3, 90, 0, 'PENDING', 0.68),
(7, 2, 'SQL Complex Joins & Subqueries', 'Inner, outer, cross joins, correlated subqueries', 2, 5, 60, 0, 'PENDING', 0.42);

-- Subject 3: Computer Networks
INSERT INTO topics (id, subject_id, title, description, difficulty, confidence_level, estimated_minutes, completed_minutes, status, calculated_priority)
VALUES
(8, 3, 'TCP 3-Way Handshake & Flow Control', 'SYN, ACK, Sliding window protocol, timeout estimation', 3, 3, 90, 0, 'PENDING', 0.58),
(9, 3, 'Routing Algorithms (Dijkstra & Bellman-Ford)', 'Link state vs Distance vector routing', 4, 2, 120, 0, 'PENDING', 0.70),
(10, 3, 'IP Addressing & Subnetting', 'Classless addressing, CIDR notation, subnet masks', 3, 4, 60, 0, 'PENDING', 0.49);

-- Subject 4: Discrete Mathematics
INSERT INTO topics (id, subject_id, title, description, difficulty, confidence_level, estimated_minutes, completed_minutes, status, calculated_priority)
VALUES
(11, 4, 'Recurrence Relations & Generating Functions', 'Homogeneous and non-homogeneous relations', 4, 2, 90, 0, 'PENDING', 0.62),
(12, 4, 'Eulerian & Hamiltonian Graphs', 'Graph coloring, planar graphs, Kuratowski theorem', 3, 3, 90, 0, 'PENDING', 0.52);

-- 4. Insert Student Availability Slots (for next 7 days)
INSERT INTO availability (user_id, available_date, start_time, end_time, slot_duration_minutes, break_duration_minutes)
VALUES
(1, CURRENT_DATE, '17:00:00', '21:00:00', 45, 15),
(1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '18:00:00', '22:00:00', 45, 15),
(1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '17:30:00', '21:30:00', 45, 15),
(1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), '16:00:00', '21:00:00', 60, 15),
(1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), '18:00:00', '22:00:00', 45, 15),
(1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), '09:00:00', '13:00:00', 60, 15),
(1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), '10:00:00', '15:00:00', 60, 15);
