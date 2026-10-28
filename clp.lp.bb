;;h1 Clojure Literate Programming
;;h2 Literwhat Programming?
;;p Literate programming is the process of writing code as a human readable document first. Source code is defined in this document, and a program can be run on the document to produce both the source code and a readable document. This document could output in whatever format you like, be it Markdown, PDF or, HTML. The obvious benefit is when writing a blog about code (not unlike this one). Interweaving the source code and prose in the same file reduces friction, and allows you to verify all the code by running it.
;;p Today, we'll implement a very basic form of literate programming in Babashka. The document you are reading is actually bootstrapped from the code we're going to be writing!
;;h2 Design
;;;h3 Prose first vs Code First
;;p First lets zoom out and think how we're going to seperate code and prose.
;;p At the time of initially writing this, I've decided to simply write all code in a Babashka file. When we want to write prose, we simply comment and then signal which HTML tag we want. For example, in the source code, the paragraph you are currently reading simply starts with ";;p". This could be deemed a code first approach. We don't have to modify the way we write code much, and our IDEs will simply work as usual. However, it might be more comfortable to write prose in something like a Markdown file. Then all the code would have to go in code blocks.
;;p For the purpose of this article, we are just going to say that code must appear in the order that it would in a normal source file. This is restricting, and there are ways around it that I will talk about later.
;;h3 Source File Descovrability
;;p Initially, I'm going to write the program to simply take in any Babashka file, and output HTML from this. The Babashka file can just be used as is.
;;p One thing I have been thinking about, is automatic file discovery. Do accomodate this, I've decided to name this file "clp.lp.bb". Eventually we can use the ".lp.bb", or ".lp.clj" file suffixes to mean that the Clojure code is literate.
;;h2 MVP 
;;p First lets start by getting the basics working