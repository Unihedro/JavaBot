package com.gmail.inverseconduit.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class PrintUtilTest {

    @Test
    public void tagParsing() {
        final String testString = "testing [tag:tags] for correct splitting";
        List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("[tag:tags]", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void metaTagParsing() {
        final String testString = "testing [meta-tag:meta-tags] for correct splitting";

        List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("[meta-tag:meta-tags]", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void strikethrough() {
        final String testString = "testing ---strikethrough--- for correct splitting";

        List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("---strikethrough---", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void strikethroughMultipleWords() {
        final String testString = "testing ---multiple struck words--- for correct splitting";

        List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("---multiple struck words---", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void simpleWords() {
        final String testString = "testing simple words for correct splitting.";

        final List<String> parts = PrintUtils.splitUsefully(testString);
        final List<String> expected = Arrays.asList(testString.split(" "));

        assertEquals(expected, parts);
    }

    @Test
    public void codeTokens() {
        final String testTString = "testing `code` for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testTString);

        assertEquals("`code`", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void multiWordCodeTokens() {
        final String testString = "testing `multiple words of code` for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("`multiple words of code`", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void simpleLink() {
        final String testString = "testing [something](http://example.com) for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals(5, parts.size());
        assertEquals("[something](http://example.com)", parts.get(1));
    }

    @Test
    public void sophisticatedLink() {
        final String testString = "testing [a very sophisticated link](https://chat.meta.stackexchange.com/rooms/89 \"with a title text\") for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("[a very sophisticated link](https://chat.meta.stackexchange.com/rooms/89 \"with a title text\")", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void overlyStrangeLink() {
        final String testString = "testing [an \\[overly strange\\] link](https://chat.stackexchange.com) for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("[an \\[overly strange\\] link](https://chat.stackexchange.com)", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void italicMarkdown() {
        final String testString = "testing *italic markdown* for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("*italic markdown*", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void boldMarkdown() {
        final String testString = "testing **bolded markdown** for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("**bolded markdown**", parts.get(1));
        assertEquals(5, parts.size());
    }

    @Test
    public void boldItalicMarkdown() {
        final String testString = "testing ***bolded italic markdown*** for correct splitting";

        final List<String> parts = PrintUtils.splitUsefully(testString);

        assertEquals("***bolded italic markdown***", parts.get(1));
        assertEquals(5, parts.size());

    }
}
