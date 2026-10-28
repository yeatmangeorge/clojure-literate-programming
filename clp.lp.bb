;;h1 Clojure Literate Programming
;;h2 Literwhat Programming?
;;p Literate programming is the process of writing code as a human-readable document first. Source code is defined in this document, and a program can be run on the document to produce both the source code and a readable document. This document could output in whatever format you like, be it Markdown, PDF or, HTML. The obvious benefit is when writing a blog about code (not unlike this one). Interweaving the source code and prose in the same file reduces friction, and allows you to verify all the code by running it.
;;p Today, we'll implement a very basic form of literate programming in Babashka. The document you are reading is actually bootstrapped from the code we're going to be writing!
;;h2 Design
;;h3 Prose first vs Code First
;;p First lets zoom out and think how we're going to separate code and prose.
;;p At the time of initially writing this, I've decided to simply write all code in a Babashka file. When we want to write prose, we simply comment and then signal which HTML tag we want. For example, in the source code, the paragraph you are currently reading simply starts with ";;p". This could be deemed a code first approach. We don't have to modify the way we write code much, and our IDEs will simply work as usual. However, it might be more comfortable to write prose in something like a Markdown file. Then all the code would have to go in code blocks.
;;p For the purpose of this article, we are just going to say that code must appear in the order that it would in a normal source file. This is restricting, and there are ways around it that I will talk about later.
;;h3 Source File Discoverability
;;p Initially, I'm going to write the program to simply take in any Babashka file, and output HTML from this. The Babashka file can just be used as is.
;;p One thing I have been thinking about, is automatic file discovery. Do accommodate this, I've decided to name this file "clp.lp.bb". Eventually we can use the ".lp.bb", or ".lp.clj" file suffixes to mean that the Clojure code is literate.
;;h2 MVP 
;;h3 HTML Gen
;;p First lets start by getting the basics working. Let's start by writing some functions to generate basic HTML so we have something to use when we start to generate the output. We could use Hiccup for this, but lets keep the project dependency free for now.
(ns clp.lp)

(defn def-tag-gen
  "We use this to generate tag generators. This is for writing output, not deciding what to generate."
  [tag]
  (fn [content] (str "<" tag ">" content "</" tag ">")))

(def h1 (def-tag-gen "h1"))
(def h2 (def-tag-gen "h2"))
(def h3 (def-tag-gen "h3"))
(def p (def-tag-gen "p"))

;;p Using my REPL, I can test that (p "Hello, World!") produces "<p>Hello, World!</p>".
;;p Cool! Now let's generate an HTML file of all the literate comments so far.
;;h3 Read File and Output HTML Comments
;;p First we shall slurp this file and go through line by line. When we detect that it starts with a comment tag, such as ";;p", let's remove the tag, wrap the rest of the line in an HTML tag, and spit it to the output HTML file.
;;p Let's write a convenience function to detect convert each line into a tagged string:
(require '[clojure.string :as str])

(defn lp-line->HTML [line]
  (let [tag-case (fn [prefix tag-gen]
                   (when (str/starts-with? line prefix)
                     (tag-gen (subs line (count prefix)))))]
    (or (tag-case ";;p " p)
        (tag-case ";;h1 " h1)
        (tag-case ";;h2 " h2)
        (tag-case ";;h3 " h3)
        line)))

;;p Nice! This this works as follows: (lp-line->HTML ";;p Abc, 123") => "<p>Abc, 123</p>"
;;p Now let's try this on the article you are currently reading. Clojure has a cool ability to detect the current file using "*file*".
(require '[babashka.fs :as fs])

(fs/create-dirs ".output")
(spit ".output/index.html"
      (str/join "\n" (map lp-line->HTML (str/split-lines (slurp *file*)))))

;;p I can open up the generated index.html see it works! But, well, you can't. Let's fix that! 