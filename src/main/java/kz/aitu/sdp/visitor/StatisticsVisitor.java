package kz.aitu.sdp.visitor;

import kz.aitu.sdp.element.DirectoryNode;
import kz.aitu.sdp.element.Element;
import kz.aitu.sdp.element.FileNode;

public class StatisticsVisitor implements FileSystemVisitor {
    private int fileCount = 0;
    private int directoryCount = 0;
    private int maxDepth = 0;
    private int currentDepth = 0;

    public void reset() {
        fileCount = 0;
        directoryCount = 0;
        maxDepth = 0;
        currentDepth = 0;
    }

    @Override
    public void visit(FileNode file) {
        fileCount++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        directoryCount++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }

        currentDepth++;
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
        currentDepth--;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
