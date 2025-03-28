package org.jah.newsys2.backend;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentEval {
    private Map<String, Subject> subjectsMap;
    private Map<String, List<Subject>> yearSemesterMap;

    public StudentEval(String program) {
        subjectsMap = new HashMap<>();
        yearSemesterMap = new HashMap<>();
        String path;
        switch (program.toLowerCase()) {
            case "bsit":
                path = "src/main/resources/org/jah/newsys2/bsit_curriculum.xml";
                break;
            case "bsmt":
                path = "src/main/resources/org/jah/newsys2/bsmt_curriculum.xml";
                break;
            case "bsn":
                path = "src/main/resources/org/jah/newsys2/bsn_curriculum.xml";
                break;
            case "bsa":
                path = "src/main/resources/org/jah/newsys2/bsa_curriculum.xml";
                break;
            default:
                throw new IllegalArgumentException("Program does not exist: " + program);
        }
        parseXML(path);
    }

    private void parseXML(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList yearList = doc.getElementsByTagName("firstYear");
            parseYear(yearList);

            yearList = doc.getElementsByTagName("secondYear");
            parseYear(yearList);

            yearList = doc.getElementsByTagName("thirdYear");
            parseYear(yearList);

            yearList = doc.getElementsByTagName("fourthYear");
            parseYear(yearList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseYear(NodeList yearList) {
        for (int i = 0; i < yearList.getLength(); i++) {
            Node yearNode = yearList.item(i);
            if (yearNode.getNodeType() == Node.ELEMENT_NODE) {
                Element yearElement = (Element) yearNode;
                NodeList semesterList = yearElement.getChildNodes();
                for (int j = 0; j < semesterList.getLength(); j++) {
                    Node semesterNode = semesterList.item(j);
                    if (semesterNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element semesterElement = (Element) semesterNode;
                        String semesterKey = yearElement.getTagName() + "_" + semesterElement.getTagName();
                        List<Subject> subjects = new ArrayList<>();
                        NodeList subjectList = semesterElement.getElementsByTagName("subject");
                        for (int k = 0; k < subjectList.getLength(); k++) {
                            Element subjectElement = (Element) subjectList.item(k);
                            String code = subjectElement.getAttribute("subjectCode");
                            int units = Integer.parseInt(subjectElement.getAttribute("units"));
                            boolean isElective = Boolean.parseBoolean(subjectElement.getAttribute("isElective"));
                            boolean isMajor = Boolean.parseBoolean(subjectElement.getAttribute("isMajor"));
                            List<String> prerequisites = new ArrayList<>();
                            NodeList prereqList = subjectElement.getElementsByTagName("prerequisite");
                            for (int l = 0; l < prereqList.getLength(); l++) {
                                prerequisites.add(prereqList.item(l).getTextContent());
                            }
                            Subject subject = new Subject(code, units, isElective, isMajor, prerequisites);
                            subjects.add(subject);
                            subjectsMap.put(code, subject);
                        }
                        yearSemesterMap.put(semesterKey, subjects);
                    }
                }
            }
        }
    }

    public List<Subject> getAllSubjects() {
        return new ArrayList<>(subjectsMap.values());
    }

    public List<Subject> getRecommendedSubjects(Map<String, Boolean> subMap, int year, int semester) {
        List<Subject> recommendedSubs = new ArrayList<>();
        List<Subject> potentialNewSubs = new ArrayList<>();
        List<String> passedSubjects = new ArrayList<>();

        // Determine passed and failed subjects
        for (Map.Entry<String, Boolean> entry : subMap.entrySet()) {
            if (entry.getValue()) {
                passedSubjects.add(entry.getKey());
            } else {
                recommendedSubs.add(subjectsMap.get(entry.getKey()));
            }
        }

        // Determine next term
        String nextTermKey = getNextTermKey(year, semester);
        List<Subject> nextTermSubjects = yearSemesterMap.get(nextTermKey);

        if (nextTermSubjects == null) {
            throw new IllegalArgumentException("Invalid term: " + nextTermKey);
        }

        // Check prerequisites for next term subjects
        for (Subject subject : nextTermSubjects) {
            if (passedSubjects.containsAll(subject.getPrerequisites())) {
                potentialNewSubs.add(subject);
            }
        }

        // Calculate current load
        int currentLoad = recommendedSubs.stream().mapToInt(Subject::getUnits).sum();
        int maxLoad = 21; // Maximum allowed units

        // Add subjects to recommendedSubs until maxLoad is reached
        for (Subject subject : potentialNewSubs) {
            if (currentLoad + subject.getUnits() <= maxLoad) {
                recommendedSubs.add(subject);
                currentLoad += subject.getUnits();
            } else {
                break; // Stop if adding this subject would exceed maxLoad
            }
        }

        // If currentLoad is still less than maxLoad, look at term after next
        if (currentLoad < maxLoad) {
            String termAfterNextKey = getNextTermKey(year + (semester == 2 ? 1 : 0), semester == 2 ? 1 : 2);
            List<Subject> termAfterNextSubjects = yearSemesterMap.get(termAfterNextKey);

            if (termAfterNextSubjects != null) {
                for (Subject subject : termAfterNextSubjects) {
                    if (passedSubjects.containsAll(subject.getPrerequisites())) {
                        if (currentLoad + subject.getUnits() <= maxLoad) {
                            recommendedSubs.add(subject);
                            currentLoad += subject.getUnits();
                        } else {
                            break; // Stop if adding this subject would exceed maxLoad
                        }
                    }
                }
            }
        }

        return recommendedSubs;
    }

    // Overloaded method for new students (no parameters)
    public List<Subject> getRecommendedSubjects() {
        // Return all subjects in the first year, first semester
        return yearSemesterMap.get("firstYear_firstSem");
    }

    private String getNextTermKey(int year, int semester) {
        String yearKey;
        switch (year) {
            case 1: yearKey = "firstYear"; break;
            case 2: yearKey = "secondYear"; break;
            case 3: yearKey = "thirdYear"; break;
            case 4: yearKey = "fourthYear"; break;
            default: throw new IllegalArgumentException("Invalid year: " + year);
        }

        String semesterKey;
        if (semester == 1) {
            semesterKey = "secondSem";
        } else if (semester == 2) {
            semesterKey = "firstSem";
            yearKey = getNextYearKey(yearKey); // Move to the next year
        } else {
            throw new IllegalArgumentException("Invalid semester: " + semester);
        }

        return yearKey + "_" + semesterKey;
    }

    private String getNextYearKey(String currentYearKey) {
        switch (currentYearKey) {
            case "firstYear": return "secondYear";
            case "secondYear": return "thirdYear";
            case "thirdYear": return "fourthYear";
            case "fourthYear": throw new IllegalArgumentException("No next year after fourthYear");
            default: throw new IllegalArgumentException("Invalid year key: " + currentYearKey);
        }
    }
}