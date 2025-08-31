package info.kgeorgiy.ja.petrasiuk.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {
    private static final Comparator<Student> BY_NAME = Comparator
            .comparing(Student::firstName)
            .thenComparing(Student::lastName)
            .thenComparingInt(Student::id)
            .thenComparing(Student::groupName);

    private static final Comparator<Map.Entry<GroupName, List<Student>>> BY_SIZE_WHEN_NAME = Comparator
            .comparingInt(
                    (Map.Entry<GroupName, List<Student>> entry) -> entry.getValue().size()
            )
            .thenComparing(Map.Entry::getKey);

    private final Comparator<Map.Entry<GroupName, List<Student>>> BY_DISTINCT_NAMES_SIZE_THEN_REVERSE_NAME = Comparator
            .comparingInt(
                    (Map.Entry<GroupName, List<Student>> entry) -> Set.copyOf(getFirstNames(entry.getValue())).size()
            )
            .thenComparing(Map.Entry::getKey, Comparator.reverseOrder());

    private Map<GroupName, List<Student>> processGroups(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(Student::groupName));
    }

    private Stream<Group> sortedGroups(Collection<Student> students, Function<Collection<Student>, List<Student>> sorter) {
        return processGroups(students).entrySet().stream()
                .map(
                        entry -> new Group(
                                entry.getKey(),
                                sorter.apply(entry.getValue())
                        )
                )
                .sorted(Comparator.comparing(Group::name));
    }

    private GroupName maxGroup(Collection<Student> students, Comparator<Map.Entry<GroupName, List<Student>>> comparator) {
        return processGroups(students).entrySet().stream()
                .max(comparator)
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return sortedGroups(students, this::sortStudentsByName).toList();
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return sortedGroups(students, this::sortStudentsById).toList();
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return maxGroup(students, BY_SIZE_WHEN_NAME);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return maxGroup(students, BY_DISTINCT_NAMES_SIZE_THEN_REVERSE_NAME);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return students.stream().map(Student::firstName).toList();
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return students.stream().map(Student::lastName).toList();
    }

    @Override
    public List<GroupName> getGroupNames(List<Student> students) {
        return students.stream().map(Student::groupName).toList();
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return students.stream()
                .map(student -> student.firstName() + " " + student.lastName())
                .toList();
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.comparingInt(Student::id))
                .map(Student::firstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream().sorted(Comparator.comparingInt(Student::id)).toList();
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream().sorted(BY_NAME).toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return sortStudentsByName(
                students.stream()
                        .filter(student -> student.firstName().equals(name))
                        .toList()
        );
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return students.stream()
                .filter(student -> student.lastName().equals(name))
                .toList();
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return sortStudentsByName(
                students.stream()
                        .filter(student -> student.groupName().equals(group))
                        .toList()
        );
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group)
                .stream()
                .collect(
                        Collectors.toMap(
                                Student::lastName, Student::firstName,
                                (firstName1, firstName2) ->
                                        firstName1.compareTo(firstName2) <= 0 ? firstName1 : firstName2
                        )
                );
    }
}
