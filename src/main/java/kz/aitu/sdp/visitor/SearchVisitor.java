package kz.aitu.sdp.visitor;

import kz.aitu.sdp.element.DirectoryNode;
import kz.aitu.sdp.element.Element;
import kz.aitu.sdp.element.FileNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class SearchVisitor implements FileSystemVisitor {
    private final String nameQuery;
    private final long minSizeThreshold;
    private final List<String> matchedPaths = new ArrayList<>();
    private final Deque<String> pathSegments = new ArrayDeque<>();

    public SearchVisitor(String nameQuery, long minSizeThreshold) {
        if (minSizeThreshold < 0) {
            throw new IllegalArgumentException("Minimum size threshold must be non-negative");
        }
        this.nameQuery = (nameQuery == null) ? "" : nameQuery;
        this.minSizeThreshold = minSizeThreshold;
    }

    public void reset() {
        matchedPaths.clear();
        pathSegments.clear();
    }

    @Override
    public void visit(FileNode file) {
        boolean matchesName = nameQuery.isEmpty() || file.getName().contains(nameQuery);
        boolean matchesSize = file.getSize() >= minSizeThreshold;

        if (matchesName && matchesSize) {
            matchedPaths.add(buildCurrentPath(file.getName()));
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        pathSegments.addLast(directory.getName());
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
        pathSegments.removeLast();
    }

    private String buildCurrentPath(String fileName) {
        if (pathSegments.isEmpty()) {
            return fileName;
        }
        return String.join("/", pathSegments) + "/" + fileName;
    }

    public List<String> getMatchedPaths() {
        List<String> sorted = new ArrayList<>(matchedPaths);
        Collections.sort(sorted);
        return Collections.unmodifiableList(sorted);
    }
}
