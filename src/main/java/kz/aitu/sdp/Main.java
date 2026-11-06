package kz.aitu.sdp;

import kz.aitu.sdp.element.DirectoryNode;
import kz.aitu.sdp.element.FileNode;
import kz.aitu.sdp.visitor.SearchVisitor;
import kz.aitu.sdp.visitor.StatisticsVisitor;
import kz.aitu.sdp.visitor.TotalSizeVisitor;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== In-Memory File System Visitor Demo ===");

        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode src = new DirectoryNode("src");
        DirectoryNode docs = new DirectoryNode("docs");

        FileNode config = new FileNode("config.yaml", 256);
        FileNode mainJava = new FileNode("Main.java", 1024);
        FileNode testJava = new FileNode("MainTest.java", 512);
        FileNode manual = new FileNode("manual.pdf", 4096);

        src.addElement(mainJava);
        src.addElement(testJava);
        docs.addElement(manual);

        root.addElement(config);
        root.addElement(src);
        root.addElement(docs);

        // 1. Total Size Visitor
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        System.out.println("Total calculated size: " + sizeVisitor.getTotalBytes() + " bytes");

        // 2. Search Visitor
        SearchVisitor searchVisitor = new SearchVisitor("Main", 500);
        root.accept(searchVisitor);
        System.out.println("Matching files (name contains 'Main', size >= 500):");
        searchVisitor.getMatchedPaths().forEach(path -> System.out.println(" - " + path));

        // 3. Statistics Visitor
        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        System.out.println("Tree Statistics:");
        System.out.println(" - Total files: " + statsVisitor.getFileCount());
        System.out.println(" - Total directories: " + statsVisitor.getDirectoryCount());
        System.out.println(" - Maximum depth: " + statsVisitor.getMaxDepth());
    }
}
