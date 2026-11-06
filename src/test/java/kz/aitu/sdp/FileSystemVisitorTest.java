package kz.aitu.sdp;

import kz.aitu.sdp.element.DirectoryNode;
import kz.aitu.sdp.element.FileNode;
import kz.aitu.sdp.visitor.FileSystemVisitor;
import kz.aitu.sdp.visitor.SearchVisitor;
import kz.aitu.sdp.visitor.StatisticsVisitor;
import kz.aitu.sdp.visitor.TotalSizeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemVisitorTest {

    @Test
    @DisplayName("T1: Both element types reach correct overloaded visit methods")
    void testT1_DoubleDispatchRouting() {
        final boolean[] visitedFile = {false};
        final boolean[] visitedDir = {false};

        FileSystemVisitor testVisitor = new FileSystemVisitor() {
            @Override
            public void visit(FileNode file) {
                visitedFile[0] = true;
            }

            @Override
            public void visit(DirectoryNode directory) {
                visitedDir[0] = true;
            }
        };

        FileNode file = new FileNode("sample.txt", 100);
        DirectoryNode dir = new DirectoryNode("sampleDir");

        file.accept(testVisitor);
        assertTrue(visitedFile[0], "FileNode must dispatch to visit(FileNode)");
        assertFalse(visitedDir[0]);

        dir.accept(testVisitor);
        assertTrue(visitedDir[0], "DirectoryNode must dispatch to visit(DirectoryNode)");
    }

    @Test
    @DisplayName("T2: Size accumulation across multiple nesting levels is correct")
    void testT2_SizeAccumulation() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode sub1 = new DirectoryNode("sub1");
        DirectoryNode sub2 = new DirectoryNode("sub2");

        root.addElement(new FileNode("a.txt", 300));
        sub1.addElement(new FileNode("b.txt", 700));
        sub2.addElement(new FileNode("c.txt", 1000));

        sub1.addElement(sub2);
        root.addElement(sub1);

        TotalSizeVisitor visitor = new TotalSizeVisitor();
        root.accept(visitor);

        assertEquals(2000L, visitor.getTotalBytes());
    }

    @Test
    @DisplayName("T3: Search correctly combines both configurable filter conditions")
    void testT3_SearchCombinedCriteria() {
        DirectoryNode root = new DirectoryNode("root");
        root.addElement(new FileNode("app-log.txt", 1200));
        root.addElement(new FileNode("app-config.json", 300));
        root.addElement(new FileNode("db-dump.txt", 5000));

        SearchVisitor visitor = new SearchVisitor("app", 1000);
        root.accept(visitor);

        List<String> results = visitor.getMatchedPaths();
        assertEquals(1, results.size());
        assertEquals("root/app-log.txt", results.get(0));
    }

    @Test
    @DisplayName("T4: Search produces deterministic, lexicographically sorted root-relative paths")
    void testT4_SearchDeterministicRootRelativePaths() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode dirB = new DirectoryNode("dirB");
        DirectoryNode dirA = new DirectoryNode("dirA");

        root.addElement(dirB);
        root.addElement(dirA);

        dirB.addElement(new FileNode("z_data.csv", 50));
        dirA.addElement(new FileNode("a_data.csv", 50));
        root.addElement(new FileNode("m_data.csv", 50));

        SearchVisitor visitor = new SearchVisitor("data", 10);
        root.accept(visitor);

        List<String> expected = List.of(
                "root/dirA/a_data.csv",
                "root/dirB/z_data.csv",
                "root/m_data.csv"
        );
        assertEquals(expected, visitor.getMatchedPaths());
    }

    @Test
    @DisplayName("T5: Statistics visitor counts files, directories, and max depth correctly")
    void testT5_StatisticsVisitorVerification() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode lvl1 = new DirectoryNode("lvl1");
        DirectoryNode lvl2 = new DirectoryNode("lvl2");
        FileNode deepFile = new FileNode("deep.txt", 10);

        lvl2.addElement(deepFile);
        lvl1.addElement(lvl2);
        root.addElement(lvl1);
        root.addElement(new FileNode("root.txt", 10));

        StatisticsVisitor stats = new StatisticsVisitor();
        root.accept(stats);

        assertEquals(2, stats.getFileCount());
        assertEquals(3, stats.getDirectoryCount());
        assertEquals(3, stats.getMaxDepth());
    }

    @Test
    @DisplayName("T6: Empty structures and no-match search cases handled safely")
    void testT6_EmptyStructuresAndNoMatch() {
        DirectoryNode emptyRoot = new DirectoryNode("empty");

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        emptyRoot.accept(sizeVisitor);
        assertEquals(0L, sizeVisitor.getTotalBytes());

        SearchVisitor searchVisitor = new SearchVisitor("nonexistent", 100);
        emptyRoot.accept(searchVisitor);
        assertTrue(searchVisitor.getMatchedPaths().isEmpty());

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        emptyRoot.accept(statsVisitor);
        assertEquals(0, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getDirectoryCount());
        assertEquals(0, statsVisitor.getMaxDepth());
    }

    @Test
    @DisplayName("T7: Repeat executions do not leak or multiply state")
    void testT7_RepeatExecutionsIdempotence() {
        DirectoryNode root = new DirectoryNode("root");
        root.addElement(new FileNode("test.dat", 400));

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        assertEquals(400L, sizeVisitor.getTotalBytes());

        sizeVisitor.reset();
        root.accept(sizeVisitor);
        assertEquals(400L, sizeVisitor.getTotalBytes(), "Second run with reset must not duplicate count");

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        statsVisitor.reset();
        root.accept(statsVisitor);
        assertEquals(1, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getDirectoryCount());
    }

    @Test
    @DisplayName("T8: Invalid tree mutations are rejected (negatives, duplicates, cycles, multi-parent)")
    void testT8_TreeIntegrityConstraints() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("invalid.txt", -5));

        DirectoryNode dir = new DirectoryNode("parent");
        dir.addElement(new FileNode("duplicate.txt", 100));
        assertThrows(IllegalArgumentException.class, () -> dir.addElement(new FileNode("duplicate.txt", 200)));

        DirectoryNode anotherDir = new DirectoryNode("other");
        FileNode sharedFile = new FileNode("shared.txt", 100);
        dir.addElement(sharedFile);
        assertThrows(IllegalStateException.class, () -> anotherDir.addElement(sharedFile));

        DirectoryNode nodeA = new DirectoryNode("A");
        DirectoryNode nodeB = new DirectoryNode("B");
        nodeA.addElement(nodeB);
        assertThrows(IllegalArgumentException.class, () -> nodeB.addElement(nodeA));
    }
}
